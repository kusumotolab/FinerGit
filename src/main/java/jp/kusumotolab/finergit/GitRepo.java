package jp.kusumotolab.finergit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.LargeObjectException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jp.kusumotolab.finergit.util.RevCommitUtil;

public class GitRepo {

  private static final Logger log = LoggerFactory.getLogger(GitRepo.class);

  public final Path path;
  private FileRepository fileRepository;
  private ObjectReader objectReader;
  private RevWalk revWalk;
  private TreeWalk treeWalk;

  public GitRepo(final Path path) {
    this.path = path;
    this.fileRepository = null;
    this.objectReader = null;
    this.revWalk = null;
    this.treeWalk = null;
  }

  public boolean initialize() {

    final Path configPath = this.path.resolve(".git");
    if (Files.notExists(configPath)) {
      log.error("repository \"" + configPath.toString() + "\" does not exist");
      return false;
    }

    try {
      this.fileRepository = new FileRepository(configPath.toFile());
    } catch (final IOException e) {
      log.error("repository \"" + configPath.toString()
          + "\" appears to already exist but cannot be accessed");
      return false;
    }

    this.objectReader = this.fileRepository.newObjectReader();
    this.revWalk = new RevWalk(this.objectReader);
    this.treeWalk = new TreeWalk(this.objectReader);

    return true;
  }


  public void dispose() {
    this.treeWalk.close();
    this.revWalk.close();
    this.objectReader.close();
    this.fileRepository.close();
  }

  public RevCommit getHeadCommit() {
    final ObjectId headId = this.getObjectId(Constants.HEAD, "getHeadCommit()");
    final RevCommit headCommit = this.getRevCommit(headId);
    return headCommit;
  }

  public List<RevCommit> getParentCommits(final RevCommit commit) {
    final List<RevCommit> parents = new ArrayList<>();
    for (final RevCommit parent : commit.getParents()) {
      final RevCommit parentCommit = this.getRevCommit(parent);
      parents.add(parentCommit);
    }
    return parents;
  }

  public Map<String, byte[]> getFiles(final RevCommit commit) {

    final Map<String, byte[]> files = new HashMap<>();

    final RevTree tree = commit.getTree();
    this.treeWalk.setRecursive(true);

    try {
      this.treeWalk.addTree(tree);
    } catch (final IOException e) {
      log.error("cannot access commit<" + RevCommitUtil.getAbbreviatedID(commit) + ">");
      return files;
    }

    try {
      while (this.treeWalk.next()) {

        final String path = this.treeWalk.getPathString();

        // ファイルの中身を取得
        try {
          final TreeWalk nodeWalk = TreeWalk.forPath(this.objectReader, path, tree);
          final ObjectId fileObject = nodeWalk.getObjectId(0);
          final ObjectLoader fileObjectLoader = this.objectReader.open(fileObject);
          final byte[] data = fileObjectLoader.getBytes();
          files.put(path, data);
        } catch (final MissingObjectException e) {
          final String commitID = RevCommitUtil.getAbbreviatedID(commit);
          log.error("missing object for \"" + path + "\" in commit<" + commitID + ">");
        }
      }

    } catch (final LargeObjectException | IOException e) {
      log.error("cannot access commit<" + RevCommitUtil.getAbbreviatedID(commit) + ">");
    }

    return files;
  }

  public List<DiffEntry> getDiff(final RevCommit commit) {

    final Git git = new Git(this.fileRepository);

    final ObjectId parentObjectId = this.getObjectId(commit.name() + "^", "getDiff(RevCommit)");
    final RevCommit parentCommit = this.getRevCommit(parentObjectId);
    if (null == parentCommit) {
      git.close();
      return Collections.emptyList();
    }

    final CanonicalTreeParser oldParser = this.getCanonicalTreeParser(parentCommit);
    if (null == oldParser) {
      git.close();
      return Collections.emptyList();
    }

    final CanonicalTreeParser newParser = this.getCanonicalTreeParser(commit);
    if (null == newParser) {
      git.close();
      return Collections.emptyList();
    }

    final DiffCommand diffCommand = git.diff();
    try {
      final List<DiffEntry> diffEntries = diffCommand.setShowNameAndStatusOnly(true)
          .setOldTree(oldParser)
          .setNewTree(newParser)
          .call();
      git.close();
      return diffEntries;
    } catch (final GitAPIException e) {
      log.error("failed to execute git-diff");
      git.close();
      return Collections.emptyList();
    }
  }

  public List<DiffEntry> getDiff2(final RevCommit commit) {

    try (final Git git = new Git(this.fileRepository)) {

      final ObjectId parentObjectId = this.fileRepository.resolve(commit.name() + "^");
      final RevCommit parentCommit = this.revWalk.parseCommit(parentObjectId);

      final CanonicalTreeParser oldParser = new CanonicalTreeParser();
      oldParser.reset(this.objectReader, parentCommit.getTree());

      final CanonicalTreeParser newParser = new CanonicalTreeParser();
      newParser.reset(this.objectReader, commit.getTree());

      final DiffCommand diffCommand = git.diff();
      final List<DiffEntry> diffEntries = diffCommand.setOldTree(oldParser)
          .setNewTree(newParser)
          .setShowNameAndStatusOnly(true)
          .call();
      return diffEntries;

    } catch (final Exception e) {
      System.err.println("an error happend in executing git-diff command.");
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

  private ObjectId getObjectId(final String name, final String command) {

    if (null == name) {
      return null;
    }

    try {
      final ObjectId objectId = this.fileRepository.resolve(name);
      return objectId;
    } catch (final RevisionSyntaxException e) {
      log.error(
          "FileRepository#resolve is invoked with an incorrect formatted argument in " + command);
    } catch (final AmbiguousObjectException e) {
      log.error("FileRepository#resolve is invoked with an an ambiguous object ID in " + command);
    } catch (final IncorrectObjectTypeException e) {
      log.error("FileRepository#resolve is invoked with an ID of inappropriate object type in "
          + command);
    } catch (final IOException e) {
      log.error("cannot access to repository \"" + this.fileRepository.getWorkTree()
          .toString() + "\" in " + command);
    }
    return null;
  }

  private RevCommit getRevCommit(final AnyObjectId commitId) {

    if (null == commitId) {
      return null;
    }

    try {
      final RevCommit headCommit = this.revWalk.parseCommit(commitId);
      return headCommit;
    } catch (IOException e) {
      log.error("cannot parse commit<" + RevCommitUtil.getAbbreviatedID(commitId) + ">");
      return null;
    }
  }

  private CanonicalTreeParser getCanonicalTreeParser(final RevCommit commit) {
    final CanonicalTreeParser parser = new CanonicalTreeParser();
    try {
      parser.reset(this.objectReader, commit.getTree());
      return parser;
    } catch (final IOException e) {
      log.error("cannot read commit<" + RevCommitUtil.getAbbreviatedID(commit) + ">");
      return null;
    }
  }
}
