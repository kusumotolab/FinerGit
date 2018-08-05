package jp.kusumotolab.finergit;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import jp.kusumotolab.finergit.ast.FinerJavaFileBuilder;
import jp.kusumotolab.finergit.ast.FinerJavaModule;

public class FinerRepoBuilder {

  public final Path srcPath;
  public final Path desPath;
  public final boolean isJavaIncluded;
  private GitRepo srcRepo;
  private FinerRepo desRepo;
  private final BranchName branchID;
  private final Map<RevCommit, RevCommit> commitMap;
  private final Map<RevCommit, Integer> branchMap;

  public FinerRepoBuilder(final Path srcPath, final Path desPath, final boolean isJavaIncluded) {
    this.srcPath = srcPath;
    this.desPath = desPath;
    this.isJavaIncluded = isJavaIncluded;
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

  // 第一引数で与えたれたコミットに対して，そこに含まれるJavaファイルの細粒度版からなるコミットを生成する
  // 第二引数で与えられたブランチが，細粒度版Javaファイルをコミットするブランチである．
  // 第三引数で与えれたコミット群は，すでにチェックしたコミット群
  private RevCommit exec(final RevCommit targetCommit, final int branchID,
      final Set<RevCommit> checkedCommits) {

    // すでに処理済みのコミットの場合は，それに対応する細粒度リポジトリのコミットを取得し，メソッドを抜ける
    if (checkedCommits.contains(targetCommit)) {
      final RevCommit cachedNewCommit = this.commitMap.get(targetCommit);
      return cachedNewCommit;
    }

    // 親が存在した場合には，親をたどる
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

    RevCommit newCommit = null;

    // 親がないとき（initial commit）の処理
    if (0 == srcParents.size()) {

      // 対象コミットのファイル群を取得し，リポジトリに追加
      final Map<String, byte[]> dataInCommit = this.srcRepo.getFiles(targetCommit);
      if (this.isJavaIncluded) {
        this.addFiles(dataInCommit);
      }

      // 対象コミットのファイル群から，細粒度Javaファイルを作成し，git-add コマンドを実行
      final Map<String, byte[]> finerJavaData = this.generateFinerJavaModules(dataInCommit);
      this.addFiles(finerJavaData);

      // git-commitコマンドの実行
      final PersonIdent authorIdent = targetCommit.getAuthorIdent();
      final String id = targetCommit.abbreviate(7)
          .name();
      final String message = targetCommit.getFullMessage();
      newCommit = this.desRepo.doCommitCommand(authorIdent, id, message);
    }

    // 親が1つのとき（normal commit）の処理
    else if (1 == srcParents.size()) {

      // 親コミットのブランチIDと今のブランチIDを比較．異なれば，ブランチを作成
      final int parentBranchID = this.branchMap.get(desParents[0]);
      if (branchID != parentBranchID) {
        this.checkout(branchID, true, desParents[0]);
      }

      // 対象コミットの変更一覧を取得
      final List<DiffEntry> diffEntries = this.srcRepo.getDiff(targetCommit);
      final Set<String> addedFiles = this.getAddedFiles(diffEntries);
      final Set<String> modifiedFiles = this.getModifiedFiles(diffEntries);
      final Set<String> deletedFiles = this.getDeletedFiles(diffEntries);

      // 対象コミットのファイルの一覧を取得
      final Map<String, byte[]> dataInCommit = this.srcRepo.getFiles(targetCommit);
      final Map<String, byte[]> addedData = this.retainAll(dataInCommit, addedFiles);
      final Map<String, byte[]> modifiedData = this.retainAll(dataInCommit, modifiedFiles);

      // 対象コミットに含まれるファイルのうち，
      // 追加されたファイルおよび修正されたファイルに対して，git-add コマンドを実行
      // 削除されたファイルに対して，git-rm コマンドを実行
      if (this.isJavaIncluded) {
        this.addFiles(addedData);
        this.addFiles(modifiedData);
        this.removeFiles(deletedFiles);
      }

      // 追加されたファイルから細粒度ファイルを生成し，git-add コマンドを実行
      final Map<String, byte[]> finerJavaFilesInAddedFiles =
          this.generateFinerJavaModules(addedData);
      this.addFiles(finerJavaFilesInAddedFiles);

      // 修正されたファイルから細粒度ファイルを生成し，git-add コマンドを実行
      final Map<String, byte[]> finerJavaFilesInModifiedFiles =
          this.generateFinerJavaModules(modifiedData);
      this.addFiles(finerJavaFilesInModifiedFiles);

      // 修正されたファイルから以前に生成された細粒度ファイルのうち，
      // 修正されたファイルから今回生成された細粒度ファイルに含まれないファイルに対して git-rm コマンドを実行
      final Set<String> finerJavaFilesInWorkingDir =
          this.getWorkingFiles(this.desPath, "fjava", "mjava");
      final Set<String> modifiedJavaFilePrefixes = modifiedFiles.stream()
          .filter(file -> file.endsWith(".java"))
          .map(file -> file.substring(0, file.lastIndexOf('.')))
          .collect(Collectors.toSet());
      final Set<String> finerJavaFilesInWorkingDirFromModifiedFiles =
          this.getFilesHavingPrefix(finerJavaFilesInWorkingDir, modifiedJavaFilePrefixes);
      finerJavaFilesInWorkingDirFromModifiedFiles.removeAll(finerJavaFilesInModifiedFiles.keySet());
      this.removeFiles(finerJavaFilesInWorkingDirFromModifiedFiles);

      // 削除されたファイルから以前に生成された細粒度ファイルに対して，git-rm コマンドを実行
      final Set<String> deletedJavaFilePrefixes = deletedFiles.stream()
          .filter(file -> file.endsWith(".java"))
          .map(file -> file.substring(0, file.lastIndexOf('.')))
          .collect(Collectors.toSet());
      final Set<String> finerJavaFilesInWorkingDirFromDeletedFiles =
          this.getFilesHavingPrefix(finerJavaFilesInWorkingDir, deletedJavaFilePrefixes);
      this.removeFiles(finerJavaFilesInWorkingDirFromDeletedFiles);

      // git-commitコマンドの実行
      final PersonIdent authorIdent = targetCommit.getAuthorIdent();
      final String id = targetCommit.abbreviate(7)
          .name();
      final String message = targetCommit.getFullMessage();
      newCommit = this.desRepo.doCommitCommand(authorIdent, id, message);
    }

    // 親が2つのとき（merge commit）の処理
    else if (2 == srcParents.size()) {

      // 1つ目の親のブランチにスイッチ
      this.checkout(branchID, false, null);

      // 2つ目の親を対象にしてマージ
      final MergeStatus mergeStatus = this.desRepo.doMergeCommand(desParents[1]);

      // マージが失敗したときには，古いリポジトリからファイルを持ってくる
      if (!mergeStatus.isSuccessful()) {

        // 対象コミットのファイルの一覧を取得し，新しいリポジトリに追加
        final Map<String, byte[]> dataInCommit = this.srcRepo.getFiles(targetCommit);
        if (this.isJavaIncluded) {
          this.addFiles(dataInCommit);
        }

        // 対象コミットのファイル群から，細粒度Javaファイルを作成し，新しいリポジトリに追加
        final Map<String, byte[]> finerJavaData = this.generateFinerJavaModules(dataInCommit);
        this.addFiles(finerJavaData);

        // ワーキングファイルに含まれているファイルのうち，dataInCommit と finerJavaData のどちらにも
        // 含まれていないファイルに対して，git-rm コマンドを実行
        final Set<String> filesToDelete = this.getWorkingFiles(this.desPath);
        filesToDelete.removeAll(dataInCommit.keySet());
        filesToDelete.removeAll(finerJavaData.keySet());
        this.removeFiles(filesToDelete);
      }

      // targetCommitの内容でコミット
      final PersonIdent authorIdent = targetCommit.getAuthorIdent();
      final String id = targetCommit.abbreviate(7)
          .name();
      final String message = targetCommit.getFullMessage();
      newCommit = this.desRepo.doCommitCommand(authorIdent, id, message);

      System.out.println(targetCommit.abbreviate(7)
          .name() + "("
          + srcParents.get(0)
              .abbreviate(7)
              .name()
          + ", " + srcParents.get(1)
              .abbreviate(7)
              .name()
          + ")" + " : " + newCommit.abbreviate(7)
              .name()
          + "(" + desParents[0].abbreviate(7)
              .name()
          + ", " + desParents[1].abbreviate(7)
              .name()
          + ")" + " : " + branchID + "--" + this.branchMap.get(desParents[1]) + " : "
          + new Date(targetCommit.getCommitTime() * 1000L) + " : " + mergeStatus);
    }

    // オリジナルリポジトリと細粒度リポジトリのコミットのマップをとる
    this.commitMap.put(targetCommit, newCommit);

    // 細粒度リポジトリとブランチとのマップをとる
    this.branchMap.put(newCommit, branchID);

    checkedCommits.add(targetCommit);

    return newCommit;
  }

  // 第一引数のブランチに対して git-checkout する．
  private void checkout(final int branchID, final boolean create, final RevCommit startPoint) {
    final String branchName = BranchName.getLabel(branchID);
    this.desRepo.doCheckoutCommand(branchName, create, startPoint);
  }

  private Map<String, byte[]> retainAll(final Map<String, byte[]> data,
      final Set<String> elementsToRetain) {

    final Map<String, byte[]> retainedData = new HashMap<>();

    for (final Entry<String, byte[]> entry : data.entrySet()) {
      final String path = entry.getKey();
      if (!elementsToRetain.contains(path)) {
        continue;
      }
      final byte[] bytes = entry.getValue();
      retainedData.put(path, bytes);
    }

    return retainedData;
  }

  // 引数で与えたれたファイル群のうち，Javaファイルに対して細粒度Javaファイルを作成する
  private Map<String, byte[]> generateFinerJavaModules(final Map<String, byte[]> data) {

    final Map<String, byte[]> finerJavaData = new HashMap<>();

    for (final Entry<String, byte[]> entry : data.entrySet()) {

      final String path = entry.getKey();
      if (!path.endsWith(".java")) {
        continue;
      }

      try {
        final byte[] bytes = entry.getValue();
        final String text = new String(bytes, "utf-8");

        final FinerJavaFileBuilder builder = new FinerJavaFileBuilder();
        final List<FinerJavaModule> finerJavaModules = builder.constructAST(path, text);

        for (final FinerJavaModule module : finerJavaModules) {
          final Path finerPath = module.getPath();
          final byte[] finerData = String.join(System.lineSeparator(), module.getLines())
              .getBytes("utf-8");
          finerJavaData.put(finerPath.toString(), finerData);
          // System.err.println(String.join("|", module.getLines()));
        }

      } catch (final UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }

    return finerJavaData;
  }

  // 第一引数で与えられたファイルのうち，第二引数で与えられたいずれかの接頭辞をもつものを抽出
  private Set<String> getFilesHavingPrefix(final Set<String> files, final Set<String> prefixes) {
    return files.stream()
        .filter(f -> prefixes.stream()
            .anyMatch(p -> f.startsWith(p)))
        .collect(Collectors.toSet());
  }

  // 引数で与えられたファイルをgit-addする．
  private void addFiles(final Map<String, byte[]> updatedData) {

    // 各ファイルを新しいリポジトリに保存
    for (final Entry<String, byte[]> entry : updatedData.entrySet()) {

      // ファイルの絶対パスを取得
      final String path = entry.getKey();
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
    }

    // addコマンドの実行
    this.desRepo.doAddCommand(updatedData.keySet());
  }

  // 引数で与えられたファイルをgit-rmする
  private void removeFiles(final Set<String> paths) {
    if (!paths.isEmpty()) {
      this.desRepo.doRmCommand(paths);
    }
  }

  // 引数で指定されたディレクトリ以下にある全ファイルを取得する．ただし，".git"以下は除く．
  private Set<String> getWorkingFiles(final Path repoPath, final String... extensions) {
    return FileUtils
        .listFiles(repoPath.toFile(), (0 == extensions.length ? null : extensions), true)
        .stream()
        .map(f -> Paths.get(f.getAbsolutePath()))
        .filter(ap -> !ap.startsWith(repoPath.resolve(".git")))
        .map(ap -> repoPath.relativize(ap))
        .map(lp -> lp.toString())
        .collect(Collectors.toSet());
  }

  // 引数で与えられたDiffEntryのうち，ChangeTypeがADDなもののパスを取得する
  private Set<String> getAddedFiles(final List<DiffEntry> diffEntries) {
    return diffEntries.stream()
        .filter(d -> ChangeType.ADD == d.getChangeType())
        .map(d -> d.getNewPath())
        .collect(Collectors.toSet());
  }

  // 引数で与えられたDiffEntryのうち，ChangeTypeがMODIFYなもののパスを取得する
  private Set<String> getModifiedFiles(final List<DiffEntry> diffEntries) {
    return diffEntries.stream()
        .filter(d -> ChangeType.MODIFY == d.getChangeType())
        .map(d -> d.getNewPath())
        .collect(Collectors.toSet());
  }

  // 引数で与えられたDiffEntryのうち，ChangeTypeがDELETEなもののパスを取得する
  private Set<String> getDeletedFiles(final List<DiffEntry> diffEntries) {
    return diffEntries.stream()
        .filter(d -> ChangeType.DELETE == d.getChangeType())
        .map(d -> d.getOldPath())
        .collect(Collectors.toSet());
  }
}
