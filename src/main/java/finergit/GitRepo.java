package finergit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
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
import finergit.sv.MinimumRenameScore;
import finergit.util.RevCommitUtil;

public class GitRepo {

  private static final Logger log = LoggerFactory.getLogger(GitRepo.class);

  public final Path path;
  private FileRepository repository;

  public GitRepo(final Path path) {
    log.trace("enter GitRepo(Path=\"{}\")", path.toString());

    this.path = path;
    this.repository = null;
  }

  public boolean initialize() {
    log.trace("enter initialize()");

    final Path configPath = this.path.resolve(".git");
    if (Files.notExists(configPath)) {
      log.error("repository \"" + configPath.toString() + "\" does not exist");
      return false;
    }

    try {
      this.repository = new FileRepository(configPath.toFile());
    } catch (final IOException e) {
      log.error("repository \"" + configPath.toString()
          + "\" appears to already exist but cannot be accessed");
      log.error(e.getMessage());
      return false;
    }

    return true;
  }


  public void dispose() {
    log.trace("enter dispose()");
    this.repository.close();
  }

  public RevCommit getHeadCommit() {
    log.trace("enter getHeadCommit()");
    return this.getCommit(Constants.HEAD);
  }

  /**
   * 引数で与えられたコミットIDを持つRevCommitを返す．引数で与えられたコミットがない場合にはnullを返す．
   * 
   * @param commit
   * @return
   */
  public RevCommit getCommit(final String commit) {
    log.trace("enter getCommit(String)");
    final ObjectId commitId = this.getObjectId(commit);
    final RevCommit revCommit = this.getRevCommit(commitId);
    return revCommit;
  }

  public List<RevCommit> getParentCommits(final RevCommit commit) {
    log.trace("enter getParentCommits(RevCommit=\"{}\")", RevCommitUtil.getAbbreviatedID(commit));
    final List<RevCommit> parents = new ArrayList<>();
    for (final RevCommit parent : commit.getParents()) {
      final RevCommit parentCommit = this.getRevCommit(parent);
      parents.add(parentCommit);
    }
    return parents;
  }

  public Map<String, byte[]> getFiles(final RevCommit commit, final Collection<String> paths) {
    log.trace("enter getFiles(RevCommit=\"{}\", Collection<String>=\"{}\")",
        RevCommitUtil.getAbbreviatedID(commit), paths.size());

    final Map<String, byte[]> files = new HashMap<>();
    final ObjectReader reader = this.repository.newObjectReader();
    final RevTree tree = commit.getTree();

    for (final String path : paths) {
      final byte[] data = loadFile(reader, path, tree, commit);
      if (null != data) {
        files.put(path, data);
      }
    }

    return files;
  }

  private byte[] loadFile(final ObjectReader reader, final String path, final RevTree tree,
      final RevCommit commit) {
    log.trace("enter loadFile(ObjectReader, String=\"{}\", RevTree, RevCommit=\"\"", path,
        RevCommitUtil.getAbbreviatedID(commit));

    try {
      final TreeWalk nodeWalk = TreeWalk.forPath(reader, path, tree);
      if (null == nodeWalk) {
        log.error("failed to access \"{}\", unusual characters may be included in the path", path);
        return null;
      }
      final ObjectId fileID = nodeWalk.getObjectId(0);
      final ObjectLoader fileLoader = reader.open(fileID);
      final byte[] data = fileLoader.getBytes();
      return data;
    } catch (final MissingObjectException e) {
      final String commitID = RevCommitUtil.getAbbreviatedID(commit);
      log.error("missing object for \"" + path + "\" in commit<" + commitID + ">");
      log.error(e.getMessage());
      return null;
    } catch (final IOException e) {
      log.error("failed to access \"{}\"", path);
      log.error(e.getMessage());
      return null;
    }
  }

  public Map<String, byte[]> getFiles(final RevCommit commit) {
    log.trace("enter getFiles(RevCommit=\"{}\")", RevCommitUtil.getAbbreviatedID(commit));

    final ObjectReader reader = this.repository.newObjectReader();
    final CanonicalTreeParser parser = getCanonicalTreeParser(commit, reader);
    final RevTree tree = commit.getTree();
    final Map<String, byte[]> files = new HashMap<>();

    collectFiles(reader, commit, tree, parser, files);

    reader.close();
    return files;
  }

  private void collectFiles(final ObjectReader reader, final RevCommit commit, final RevTree tree,
      final CanonicalTreeParser parser, final Map<String, byte[]> files) {
    log.trace(
        "enter collectgetFiles(ObjectReader, RevCommit=\"{}\", RevTree, CanonicalTreeParser, Map<String, byte[]>)",
        RevCommitUtil.getAbbreviatedID(commit));

    for (CanonicalTreeParser currentParser = parser; !currentParser.eof(); currentParser =
        currentParser.next()) {

      final String path = currentParser.getEntryPathString();
      final FileMode mode = currentParser.getEntryFileMode();

      if (mode.equals(FileMode.REGULAR_FILE) || mode.equals(FileMode.EXECUTABLE_FILE)) {
        final byte[] data = this.loadFile(reader, path, tree, commit);
        if (null != data) {
          files.put(path, data);
        }
      }

      else if (mode.equals(FileMode.TREE)) {
        try {
          final CanonicalTreeParser subParser = currentParser.createSubtreeIterator(reader);
          collectFiles(reader, commit, tree, subParser, files);
        } catch (final IOException e) {
          final String commitID = RevCommitUtil.getAbbreviatedID(commit);
          log.error("failed to creat a parser for \"{}\" on commit \"{}\"", path, commitID);
          log.error(e.getMessage());
        }
      }

      else if (mode.equals(FileMode.GITLINK)) {
        log.warn("submodule \"{}\" is out of conversion", path);
      }

      else {
        log.warn("unknown type file \"{}\" is ignored", path);
      }
    }
  }

  public List<DiffEntry> getDiff(final RevCommit commit) {
    log.trace("enter getDiff(RevCommit=\"{}\")", RevCommitUtil.getAbbreviatedID(commit));

    final Git git = new Git(this.repository);
    final RevCommit parentCommit = this.getRevCommit(commit.getParent(0));
    if (null == parentCommit) {
      git.close();
      return Collections.emptyList();
    }

    final ObjectReader objectReader = this.repository.newObjectReader();

    final CanonicalTreeParser oldParser = this.getCanonicalTreeParser(parentCommit, objectReader);
    if (null == oldParser) {
      objectReader.close();
      git.close();
      return Collections.emptyList();
    }

    final CanonicalTreeParser newParser = this.getCanonicalTreeParser(commit, objectReader);
    if (null == newParser) {
      objectReader.close();
      git.close();
      return Collections.emptyList();
    }

    final DiffCommand diffCommand = git.diff();
    try {
      final List<DiffEntry> diffEntries = diffCommand.setShowNameAndStatusOnly(true)
          .setOldTree(oldParser)
          .setNewTree(newParser)
          .call();
      return diffEntries;
    } catch (final GitAPIException e) {
      log.error("failed to execute git-diff");
      git.close();
      return Collections.emptyList();
    } finally {
      objectReader.close();
      git.close();
    }
  }

  private ObjectId getObjectId(final String name) {
    log.trace("enter getObjectId(String=\"{}\")", name);

    if (null == name) {
      return null;
    }

    try {
      final ObjectId objectId = this.repository.resolve(name);
      return objectId;
    } catch (final RevisionSyntaxException e) {
      log.error("FileRepository#resolve is invoked with an incorrect formatted argument");
      log.error(e.getMessage());
    } catch (final AmbiguousObjectException e) {
      log.error("FileRepository#resolve is invoked with an an ambiguous object ID");
      log.error(e.getMessage());
    } catch (final IncorrectObjectTypeException e) {
      log.error("FileRepository#resolve is invoked with an ID of inappropriate object type");
      log.error(e.getMessage());
    } catch (final IOException e) {
      log.error("cannot access to repository \"" + this.repository.getWorkTree()
          .toString());
    }
    return null;
  }

  public RevCommit getRevCommit(final AnyObjectId commitId) {
    log.trace("enter getRevCommit(AnyObjectId=\"{}\")", RevCommitUtil.getAbbreviatedID(commitId));

    if (null == commitId) {
      return null;
    }

    try (final RevWalk revWalk = new RevWalk(this.repository)) {
      final RevCommit commit = revWalk.parseCommit(commitId);
      return commit;
    } catch (IOException e) {
      log.error("cannot parse commit \"{}\"", RevCommitUtil.getAbbreviatedID(commitId));
      log.error(e.getMessage());
      return null;
    }
  }

  private CanonicalTreeParser getCanonicalTreeParser(final RevCommit commit,
      final ObjectReader reader) {
    log.trace("enter getCanonicalTreeParser(RevCommit=\"{}\")",
        RevCommitUtil.getAbbreviatedID(commit));

    final CanonicalTreeParser parser = new CanonicalTreeParser();
    try {
      parser.reset(reader, commit.getTree());
      return parser;
    } catch (final IOException e) {
      log.error("cannot parse commit \"{}\"", RevCommitUtil.getAbbreviatedID(commit));
      log.error(e.getMessage());
      return null;
    }
  }

  public Iterable<RevCommit> getLog(final String path, final AnyObjectId startCommit) {
    try (final Git git = new Git(this.repository)) {
      return git.log()
          .addPath(path)
          .add(startCommit)
          .call();
    } catch (final Exception e) {
      log.error("failed to execute git-log command for \"{}\"", path);
      log.error(e.getMessage());
    }
    return Collections.emptyList();
  }

  public String getPathBeforeRename(final String path, final RevCommit commit,
      final MinimumRenameScore minimumRenameScore) {

    log.trace("enter getPathBeforeName(String=\"{}\", RevCommit=\"{}\", MinimumRenameScore=\"{}\")",
        path, RevCommitUtil.getAbbreviatedID(commit), minimumRenameScore.getValue());

    if (null == commit) {
      log.debug("return because the target commit doesn't exist");
      return null;
    }

    if (0 == commit.getParentCount()) {
      log.debug("return because parent commit doesn\'t exist");
      return null;
    }

    try (final TreeWalk treeWalk = new TreeWalk(this.repository)) {
      treeWalk.setRecursive(true);
      final RevCommit parentCommit = this.getRevCommit(commit.getParent(0));
      treeWalk.addTree(parentCommit.getTree());
      treeWalk.addTree(commit.getTree());

      final RenameDetector renameDetector = new RenameDetector(this.repository);
      if (!minimumRenameScore.isRepositoryDefault()) {
        renameDetector.setRenameScore(minimumRenameScore.getValue());
      }
      renameDetector.addAll(DiffEntry.scan(treeWalk));
      final List<DiffEntry> files = renameDetector.compute();
      for (final DiffEntry file : files) {
        if ((file.getChangeType() == DiffEntry.ChangeType.RENAME
            || file.getChangeType() == DiffEntry.ChangeType.COPY) && file.getNewPath()
                .contains(path)) {
          final String oldPath = file.getOldPath();
          log.debug("an old path was found \"{}\"", oldPath);
          return oldPath;
        }
      }
    } catch (final IOException e) {
      log.error("failed to find path before rename for \"{}\"", path);
      log.error(e.getMessage());
      return null;
    }

    log.debug("old path not found");
    return null;
  }
}
