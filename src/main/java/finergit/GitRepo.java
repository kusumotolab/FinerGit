package finergit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import org.eclipse.jgit.api.CleanCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefDatabase;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  public FileRepository getRepository() {
    return repository;
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

  public void setIgnoreCase(final boolean ignore) {
    log.trace("enter setIgnoreCase(boolean={})", ignore);

    final StoredConfig config = repository.getConfig();
    config.setBoolean("core", null, "ignorecase", ignore);

    try {
      config.save();
    } catch (final IOException e) {
      log.error("failed to change the git repository configuration");
      log.error(e.getMessage());
    }
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
    } catch (final IOException e) {
      log.error("cannot parse commit \"{}\"", RevCommitUtil.getAbbreviatedID(commitId));
      log.error(e.getMessage());
      return null;
    }
  }

  /**
   * `git reset --hard (HEAD)` を適用する．
   */
  public boolean resetHard() {
    log.trace("enter resetHard()");
    try (final Git git = new Git(this.repository)) {
      final ResetCommand cmd = git.reset();
      cmd.setMode(ResetType.HARD);
      cmd.call();
      return true;
    } catch (final GitAPIException | RuntimeException e) {
      // 作業コピーの整理に失敗してもリポジトリ（オブジェクトと参照）の書き換えは完了しているので，例外は伝播させない．
      // 例えば，生成したファイル名に実行環境のロケールで表現できない文字が含まれると，jgit は
      // InvalidPathException（RuntimeException）を投げる（#114）．
      log.warn("git reset --hard failed in \"{}\": {}", this.path, e.toString());
      return false;
    }
  }

  /**
   * `git clean -fd` を適用する．
   */
  public boolean clean() {
    log.trace("enter clean()");
    try (final Git git = new Git(this.repository)) {
      final CleanCommand cmd = git.clean();
      cmd.setForce(true);
      cmd.setCleanDirectories(true);
      cmd.call();
      return true;
    } catch (final GitAPIException | RuntimeException e) {
      // resetHard() と同じく，作業コピーの整理の失敗は伝播させない
      log.warn("git clean -fd failed in \"{}\": {}", this.path, e.toString());
      return false;
    }
  }

  /**
   * リポジトリに含まれるブランチ（refs/heads/以下の参照）の名前と，そのブランチが指すオブジェクトIDの
   * 対応を返す．
   *
   * @return ブランチ名とオブジェクトIDの対応
   * @throws IOException 参照を読み込めなかった場合
   */
  public Map<String, ObjectId> getBranches() throws IOException {
    log.trace("enter getBranches()");

    final RefDatabase refDatabase = this.repository.getRefDatabase();
    // 書き換えられた後の参照を読み込むために，jgitが保持しているキャッシュを更新する
    refDatabase.refresh();

    final Map<String, ObjectId> branches = new TreeMap<>();
    for (final Ref ref : refDatabase.getRefsByPrefix(Constants.R_HEADS)) {
      branches.put(ref.getName(), ref.getObjectId());
    }
    return branches;
  }
}
