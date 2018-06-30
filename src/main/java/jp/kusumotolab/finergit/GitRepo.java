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
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;

public class GitRepo {

  public final Path path;
  private FileRepository jgitFileRepository;
  private ObjectReader jgitObjectReader;
  private RevWalk jgitRevWalk;
  private TreeWalk jgitTreeWalk;

  public GitRepo(final Path path) {
    this.path = path;
    this.jgitFileRepository = null;
    this.jgitObjectReader = null;
    this.jgitRevWalk = null;
    this.jgitTreeWalk = null;
  }

  public boolean initialize() {

    final Path configPath = this.path.resolve(".git");
    if (Files.notExists(configPath)) {
      System.err.println("given repositoy is incorrect: " + this.path.toString());
      return false;
    }

    try {
      this.jgitFileRepository = new FileRepository(configPath.toFile());
      this.jgitObjectReader = this.jgitFileRepository.newObjectReader();
      this.jgitRevWalk = new RevWalk(this.jgitObjectReader);
      this.jgitTreeWalk = new TreeWalk(this.jgitObjectReader);
      return true;
    } catch (final IOException e) {
      System.err
          .println("an error happened in reading a given repository: " + this.path.toString());
      e.printStackTrace();
      return false;
    }
  }


  public void dispose() {
    this.jgitTreeWalk.close();
    this.jgitRevWalk.close();
    this.jgitObjectReader.close();
    this.jgitFileRepository.close();
  }

  public RevCommit getHeadCommit() {
    try {
      final ObjectId headId = this.jgitFileRepository.resolve(Constants.HEAD);
      final RevCommit headCommit = this.jgitRevWalk.parseCommit(headId);
      return headCommit;
    } catch (final Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public List<RevCommit> getParentCommits(final RevCommit commit) {

    final List<RevCommit> parents = new ArrayList<>();
    for (final RevCommit parent : commit.getParents()) {
      try {
        final RevCommit parentCommit = this.jgitRevWalk.parseCommit(parent);
        parents.add(parentCommit);
      } catch (final Exception e) {
        e.printStackTrace();
      }
    }
    return parents;
  }

  public Map<String, byte[]> getFiles(final RevCommit commit) {

    final Map<String, byte[]> files = new HashMap<>();

    try {
      final RevTree tree = commit.getTree();
      this.jgitTreeWalk.addTree(tree);
      this.jgitTreeWalk.setRecursive(true);

      while (this.jgitTreeWalk.next()) {

        final String path = this.jgitTreeWalk.getPathString();

        // ファイルの中身を取得
        final TreeWalk nodeWalk = TreeWalk.forPath(this.jgitObjectReader, path, tree);
        final byte[] data = this.jgitObjectReader.open(nodeWalk.getObjectId(0)).getBytes();

        files.put(path, data);
      }
    } catch (final Exception e) {
      System.err.println("an error happaned in reading a commit object.");
      e.printStackTrace();
    }

    return files;
  }

  public List<DiffEntry> getDiff(final RevCommit commit) {

    try (final Git git = new Git(this.jgitFileRepository)) {

      final ObjectId parentObjectId = this.jgitFileRepository.resolve(commit.name() + "^");
      final RevCommit parentCommit = this.jgitRevWalk.parseCommit(parentObjectId);

      final CanonicalTreeParser oldParser = new CanonicalTreeParser();
      oldParser.reset(this.jgitObjectReader, parentCommit.getTree());

      final CanonicalTreeParser newParser = new CanonicalTreeParser();
      newParser.reset(this.jgitObjectReader, commit.getTree());

      final DiffCommand diffCommand = git.diff();
      final List<DiffEntry> diffEntries = diffCommand.setOldTree(oldParser).setNewTree(newParser)
          .setShowNameAndStatusOnly(true).call();
      return diffEntries;

    } catch (final Exception e) {
      System.err.println("an error happend in executing git-diff command.");
      e.printStackTrace();
      return Collections.emptyList();
    }
  }
}
