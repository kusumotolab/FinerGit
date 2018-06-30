package jp.kusumotolab.finergit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

public class FinerRepoBuilder {

  public final Path srcPath;
  public final Path desPath;
  private GitRepo srcRepo;
  private FinerRepo desRepo;
  private final BranchName branchID;
  private final Map<RevCommit, RevCommit> commitMap;
  private final Map<RevCommit, Integer> branchMap;

  public FinerRepoBuilder(final Path srcPath, final Path desPath) {
    this.srcPath = srcPath;
    this.desPath = desPath;
    this.srcRepo = null;
    this.desRepo = null;
    this.branchID = new BranchName();
    this.commitMap = new HashMap<>();
    this.branchMap = new HashMap<>();
  }

  /**
   * start to generate a finer git repository
   */
  public void exec() {

    this.srcRepo = new GitRepo(this.srcPath);
    this.desRepo = new FinerRepo(this.desPath);

    try {

      // initialize finer repository
      this.srcRepo.initialize();
      this.desRepo.initialize();

      // retrieve HEAD information
      final RevCommit headCommit = this.srcRepo.getHeadCommit();

      // caching checked commits
      final Set<RevCommit> checkedCommits = new HashSet<>();

      exec(headCommit, this.branchID.newID(), checkedCommits);

    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  private RevCommit exec(final RevCommit targetCommit, final int branchID,
      final Set<RevCommit> checkedCommits) {

    if (checkedCommits.contains(targetCommit)) {
      final RevCommit cachedNewCommit = this.commitMap.get(targetCommit);
      return cachedNewCommit;
    }

    // processing parent commits if exist
    final List<RevCommit> srcParents = this.srcRepo.getParentCommits(targetCommit);
    final RevCommit[] desParents = new RevCommit[2];
    for (int index = 0; index < srcParents.size(); index++) {
      switch (index) {
        case 0: {
          desParents[0] = exec(srcParents.get(index), branchID, checkedCommits);
          break;
        }
        default: {
          desParents[1] = exec(srcParents.get(index), this.branchID.newID(), checkedCommits);
          break;
        }
      }
    }

    // processing for a normal commit
    RevCommit newCommit = null;
    if (0 == srcParents.size()) {

      // 対象コミットのファイルの一覧を取得
      final Map<String, byte[]> dataInCommit = this.srcRepo.getFiles(targetCommit);

      // targetCommitに含まれる各ファイルを新しいリポジトリに追加
      final Set<String> addedFiles = dataInCommit.keySet();
      final Set<String> modifiedFiles = dataInCommit.keySet();
      this.addFiles(dataInCommit, addedFiles, modifiedFiles);

      // git commitコマンドの実行
      final PersonIdent authorIdent = targetCommit.getAuthorIdent();
      final String message = targetCommit.getFullMessage();
      newCommit = this.desRepo.doCommitCommand(authorIdent, message);
    }

    else if (1 == srcParents.size()) {

      // 親コミットのブランチIDと今のブランチIDを比較．異なれば，ブランチを作成
      final int parentBranchID = this.branchMap.get(desParents[0]);
      if (branchID != parentBranchID) {
        this.checkout(branchID, true, desParents[0]);
      }

      // 対象コミットの変更一覧を取得
      final List<DiffEntry> diffEntries = srcRepo.getDiff(targetCommit);
      final Set<String> addedFiles = this.getAddedFiles(diffEntries);
      final Set<String> modifiedFiles = this.getModifiedFiles(diffEntries);
      final Set<String> deletedFiles = this.getDeletedFiles(diffEntries);

      // 対象コミットのファイルの一覧を取得
      final Map<String, byte[]> dataInCommit = this.srcRepo.getFiles(targetCommit);

      // targetCommitに含まれる各ファイルを新しいリポジトリに追加
      this.addFiles(dataInCommit, addedFiles, modifiedFiles);

      // ワーキングディレクトリには含まれるがtargetCommitには含まれないファイルを新しいリポジトリから削除
      this.removeFiles(deletedFiles);

      // git commitコマンドの実行
      final PersonIdent authorIdent = targetCommit.getAuthorIdent();
      final String message = targetCommit.getFullMessage();
      newCommit = this.desRepo.doCommitCommand(authorIdent, message);
    }

    // processing for a merge commit
    else if (2 == srcParents.size()) {

      // 1つ目の親のブランチにスイッチ
      this.checkout(branchID, false, null);

      // 2つ目の親を対象にしてマージ
      final MergeStatus mergeStatus = this.desRepo.doMergeCommand(desParents[1]);

      // 対象コミットのファイルの一覧を取得
      final Map<String, byte[]> dataInCommit = this.srcRepo.getFiles(targetCommit);

      // targetCommitに含まれる各ファイルを新しいリポジトリに追加
      final Set<String> filesInCommit = dataInCommit.keySet();
      this.addFiles(dataInCommit, filesInCommit, filesInCommit);

      final Set<String> deletedFiles = this.getWorkingFiles(this.desPath);
      deletedFiles.removeAll(filesInCommit);
      if (!deletedFiles.isEmpty()) {
        this.removeFiles(deletedFiles);
      }

      // targetCommitの内容でコミット
      final String message = targetCommit.getFullMessage();
      final PersonIdent authorIdent = targetCommit.getAuthorIdent();
      newCommit = this.desRepo.doCommitCommand(authorIdent, message);

      System.out.println(targetCommit.abbreviate(7).name() + "("
          + srcParents.get(0).abbreviate(7).name() + ", " + srcParents.get(1).abbreviate(7).name()
          + ")" + " : " + newCommit.abbreviate(7).name() + "(" + desParents[0].abbreviate(7).name()
          + ", " + desParents[1].abbreviate(7).name() + ")" + " : " + branchID + "--"
          + this.branchMap.get(desParents[1]) + " : "
          + new Date(targetCommit.getCommitTime() * 1000L) + " : " + mergeStatus);
    }

    // オリジナルリポジトリと細粒度リポジトリのコミットのマップをとる
    this.commitMap.put(targetCommit, newCommit);

    // 細粒度リポジトリとブランチとのマップをとる
    this.branchMap.put(newCommit, branchID);

    checkedCommits.add(targetCommit);

    return newCommit;
  }

  private void checkout(final int branchID, final boolean create, final RevCommit startPoint) {
    final String branchName = BranchName.getLabel(branchID);
    this.desRepo.doCheckoutCommand(branchName, create, startPoint);
  }

  private void addFiles(final Map<String, byte[]> dataInCommit, final Set<String> addedFiles,
      final Set<String> modifiedFiles) {

    final Set<String> updatedPaths = new HashSet<>();

    // 各ファイルを新しいリポジトリに保存
    for (final Entry<String, byte[]> entry : dataInCommit.entrySet()) {

      final String path = entry.getKey();

      if (!addedFiles.contains(path) && !modifiedFiles.contains(path)) {
        continue;
      }

      // ファイルの絶対パスを取得
      final Path absolutePath = this.desRepo.path.resolve(path);

      // ファイルの親ディレクトリがなければ作成
      final Path parent = absolutePath.getParent();
      if (Files.notExists(parent)) {
        try {
          Files.createDirectories(parent);
        } catch (final IOException e) {
          System.err.println("failed to create a new directory: " + parent.toString());
          e.printStackTrace();
          continue;
        }
      }

      // ファイル書き込み
      try {
        final byte[] data = entry.getValue();
        Files.write(absolutePath, data);
      } catch (final IOException e) {
        System.err.println("failed to write a file: " + absolutePath.toString());
        e.printStackTrace();
        continue;
      }

      updatedPaths.add(path);
    }

    // addコマンドの実行
    this.desRepo.doAddCommand(updatedPaths);
  }

  private void removeFiles(final Set<String> paths) {
    // rmコマンドの実行
    this.desRepo.doRmCommand(paths);
  }

  private Set<String> getWorkingFiles(final Path repoPath) {
    return FileUtils.listFiles(repoPath.toFile(), null, true).stream()
        .map(f -> Paths.get(f.getAbsolutePath()))
        .filter(ap -> !ap.startsWith(repoPath.resolve(".git"))).map(ap -> repoPath.relativize(ap))
        .map(lp -> lp.toString()).collect(Collectors.toSet());
  }

  private Set<String> getAddedFiles(final List<DiffEntry> diffEntries) {
    return diffEntries.stream().filter(d -> ChangeType.ADD == d.getChangeType())
        .map(d -> d.getNewPath()).collect(Collectors.toSet());
  }

  private Set<String> getModifiedFiles(final List<DiffEntry> diffEntries) {
    return diffEntries.stream().filter(d -> ChangeType.MODIFY == d.getChangeType())
        .map(d -> d.getNewPath()).collect(Collectors.toSet());
  }

  private Set<String> getDeletedFiles(final List<DiffEntry> diffEntries) {
    return diffEntries.stream().filter(d -> ChangeType.DELETE == d.getChangeType())
        .map(d -> d.getOldPath()).collect(Collectors.toSet());
  }
}
