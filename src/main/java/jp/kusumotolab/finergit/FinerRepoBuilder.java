package jp.kusumotolab.finergit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jp.kusumotolab.finergit.ast.FinerJavaFileBuilder;
import jp.kusumotolab.finergit.ast.FinerJavaModule;
import jp.kusumotolab.finergit.util.RevCommitUtil;

public class FinerRepoBuilder {

  private static final Logger log = LoggerFactory.getLogger(FinerRepoBuilder.class);

  private final FinerGitConfig config;
  private final GitRepo srcRepo;
  private final FinerRepo desRepo;
  private final BranchName branchID;
  private final Map<RevCommit, RevCommit> commitMap;
  private final Map<RevCommit, Integer> branchMap;
  private int numberOfTrackedCommits;
  private int numberOfRebuiltCommits;

  public FinerRepoBuilder(final FinerGitConfig config) {
    log.trace("enter FinerRepoBuilder(FinerGitConfig)");
    this.config = config;
    this.srcRepo = new GitRepo(this.config.getSrcPath());
    this.desRepo = new FinerRepo(this.config.getDesPath());
    this.branchID = new BranchName();
    this.commitMap = new HashMap<>();
    this.branchMap = new HashMap<>();
    this.numberOfTrackedCommits = 0;
    this.numberOfRebuiltCommits = 0;
  }

  /**
   * FinerGitのリポジトリを生成する
   */
  public void exec() {
    log.trace("enter exec()");
    try {

      // initialize finer repository
      this.srcRepo.initialize();
      this.desRepo.initialize();

      // retrieve HEAD information
      final String headCommitId = this.config.getHeadCommitId();
      final RevCommit headCommit = null != headCommitId ? this.srcRepo.getCommit(headCommitId)
          : this.srcRepo.getHeadCommit();
      if (null == headCommit) {
        log.error("\"{}\" is an invalid commit ID for option \"--head\"", headCommitId);
        System.exit(0);
      }

      exec(headCommit, this.branchID.newID(), new HashSet<RevCommit>());

    } catch (final Exception e) {
      e.printStackTrace();
    }
    log.trace("exit exec()");
  }

  // 第一引数で与えたれたコミットに対して，そこに含まれるJavaファイルの細粒度版からなるコミットを生成する．
  // 第二引数で与えられたブランチは，細粒度版Javaファイルをコミットするブランチ．
  // 第三引数で与えれたコミット群は，すでにチェックしたコミット群．
  private RevCommit exec(final RevCommit targetCommit, final int branchID,
      final Set<RevCommit> checkedCommits) {
    log.trace("enter exec(RevCommit=\"{}\", int=\"{}\", Set<RevCommit>=\"{}\")",
        RevCommitUtil.getAbbreviatedID(targetCommit), branchID, checkedCommits.size());

    // すでに処理済みのコミットの場合は，それに対応する細粒度リポジトリのコミットを取得し，メソッドを抜ける
    if (checkedCommits.contains(targetCommit)) {
      final RevCommit cachedNewCommit = this.commitMap.get(targetCommit);
      return cachedNewCommit;
    }

    this.numberOfTrackedCommits++;

    // 親が存在した場合には，親をたどる
    final List<RevCommit> srcParents = this.srcRepo.getParentCommits(targetCommit);
    final RevCommit[] desParents = new RevCommit[2];
    for (int index = 0; index < srcParents.size(); index++) {
      switch (index) {
        case 0: {
          desParents[0] = exec(srcParents.get(index), branchID, checkedCommits);
          break;
        }
        case 1: {
          desParents[1] = exec(srcParents.get(index), this.branchID.newID(), checkedCommits);
          break;
        }
        default: {
          log.error("unexpected parent commit was found.");
          break;
        }
      }
    }

    this.numberOfRebuiltCommits++;
    final String currentBranchName = this.desRepo.getCurrentBranch();
    RevCommit newCommit = null;

    // 親がないとき（initial commit）の処理
    if (0 == srcParents.size()) {

      log.info("{}/{} rebuilding root commit \"{}\" ({}) on {}", this.numberOfRebuiltCommits,
          this.numberOfTrackedCommits, RevCommitUtil.getAbbreviatedID(targetCommit),
          RevCommitUtil.getDate(targetCommit, RevCommitUtil.DATE_FORMAT), currentBranchName);

      // master上のinitial commitでない場合は，orphanブランチを作る
      if (!BranchName.isMasterBranch(branchID)) {
        this.checkout(branchID, true, null, true, targetCommit);
        final Set<String> filesInWorkingDir = this.getWorkingFiles(this.desRepo.path);
        this.removeFiles(filesInWorkingDir);
      }

      // 対象コミットのファイル群を取得
      final Map<String, byte[]> dataInCommit = this.srcRepo.getFiles(targetCommit);
      final Map<String, byte[]> javaDataInCommit =
          this.filterMap(dataInCommit, path -> path.endsWith(".java"));
      final Map<String, byte[]> otherDataInCommit =
          this.filterMap(dataInCommit, path -> !path.endsWith(".java"));

      // 対象ファイルのファイル群をリポジトリに追加
      if (this.config.isOriginalJavaIncluded()) {
        this.addFiles(javaDataInCommit);
      }
      if (this.config.isOtherFilesIncluded()) {
        this.addFiles(otherDataInCommit);
      }

      // 対象コミットのファイル群から，細粒度Javaファイルを作成し，git-add コマンドを実行
      final Map<String, byte[]> finerJavaData = this.generateFinerJavaModules(javaDataInCommit);
      this.addFiles(finerJavaData);

      // git-commitコマンドの実行
      final PersonIdent authorIdent = targetCommit.getAuthorIdent();
      final String id = RevCommitUtil.getAbbreviatedID(targetCommit);
      final String message = targetCommit.getFullMessage();
      newCommit = this.desRepo.doCommitCommand(authorIdent, id, message);
    }

    // 親が1つのとき（normal commit）の処理
    else if (1 == srcParents.size()) {

      log.info("{}/{} rebuilding normal commit \"{}\" ({}) on {}", this.numberOfRebuiltCommits,
          this.numberOfTrackedCommits, RevCommitUtil.getAbbreviatedID(targetCommit),
          RevCommitUtil.getDate(targetCommit, RevCommitUtil.DATE_FORMAT), currentBranchName);

      // 親コミットのブランチIDと今のブランチIDを比較．異なれば，ブランチを作成
      final int parentBranchID = this.branchMap.get(desParents[0]);
      if (branchID != parentBranchID) {
        this.checkout(branchID, true, desParents[0], false, targetCommit);
      }

      // 対象コミットの変更一覧を取得
      final List<DiffEntry> diffEntries = this.srcRepo.getDiff(targetCommit);
      final Set<String> addedFiles = this.getAddedFiles(diffEntries);
      final Set<String> modifiedFiles = this.getModifiedFiles(diffEntries);
      final Set<String> deletedFiles = this.getDeletedFiles(diffEntries);
      final Set<String> deletedJavaFiles = this.filterSet(deletedFiles, f -> f.endsWith(".java"));
      final Set<String> deletedOtherFiles = this.filterSet(deletedFiles, f -> !f.endsWith(".java"));

      // 対象コミットにおいて変更されたファイルの内容を取得
      final Map<String, byte[]> addedDataInCommit = this.srcRepo.getFiles(targetCommit, addedFiles);
      final Map<String, byte[]> modifiedDataInCommit =
          this.srcRepo.getFiles(targetCommit, modifiedFiles);
      final Map<String, byte[]> addedJavaDataInCommit =
          this.filterMap(addedDataInCommit, path -> path.endsWith(".java"));
      final Map<String, byte[]> addedOtherDataInCommit =
          this.filterMap(addedDataInCommit, path -> !path.endsWith(".java"));
      final Map<String, byte[]> modifiedJavaDataInCommit =
          this.filterMap(modifiedDataInCommit, path -> path.endsWith(".java"));
      final Map<String, byte[]> modifiedOtherDataInCommit =
          this.filterMap(modifiedDataInCommit, path -> !path.endsWith(".java"));

      // 対象コミットに含まれるファイルのうち，
      // 追加されたファイルおよび修正されたファイルに対して，git-add コマンドを実行
      // 削除されたファイルに対して，git-rm コマンドを実行
      if (this.config.isOriginalJavaIncluded()) {
        this.addFiles(addedJavaDataInCommit);
        this.addFiles(modifiedJavaDataInCommit);
        this.removeFiles(deletedJavaFiles);
      }
      if (this.config.isOtherFilesIncluded()) {
        this.addFiles(addedOtherDataInCommit);
        this.addFiles(modifiedOtherDataInCommit);
        this.removeFiles(deletedOtherFiles);
      }

      // 追加されたファイルから細粒度ファイルを生成し，git-add コマンドを実行
      final Map<String, byte[]> finerJavaFilesInAddedFiles =
          this.generateFinerJavaModules(addedDataInCommit);
      this.addFiles(finerJavaFilesInAddedFiles);

      // 修正されたファイルから細粒度ファイルを生成し，git-add コマンドを実行
      final Map<String, byte[]> finerJavaFilesInModifiedFiles =
          this.generateFinerJavaModules(modifiedDataInCommit);
      this.addFiles(finerJavaFilesInModifiedFiles);

      // 修正されたファイルから以前に生成された細粒度ファイルのうち，
      // 修正されたファイルから今回生成された細粒度ファイルに含まれないファイルに対して git-rm コマンドを実行
      final Set<String> finerJavaFilesInWorkingDir =
          this.getWorkingFiles(this.config.getDesPath(), "fjava", "mjava");
      final Set<String> modifiedJavaFilePrefixes = this.removePrefixes(modifiedFiles);
      final Set<String> finerJavaFilesToDelete1 =
          this.getFilesHavingPrefix(finerJavaFilesInWorkingDir, modifiedJavaFilePrefixes);
      finerJavaFilesToDelete1.removeAll(finerJavaFilesInModifiedFiles.keySet());
      this.removeFiles(finerJavaFilesToDelete1);

      // 削除されたファイルから以前に生成された細粒度ファイルに対して，git-rm コマンドを実行
      final Set<String> deletedJavaFilePrefixes = this.removePrefixes(deletedFiles);
      final Set<String> finerJavaFilesToDelete2 =
          this.getFilesHavingPrefix(finerJavaFilesInWorkingDir, deletedJavaFilePrefixes);
      this.removeFiles(finerJavaFilesToDelete2);

      // git-commitコマンドの実行
      final PersonIdent authorIdent = targetCommit.getAuthorIdent();
      final String id = RevCommitUtil.getAbbreviatedID(targetCommit);
      final String message = targetCommit.getFullMessage();
      newCommit = this.desRepo.doCommitCommand(authorIdent, id, message);
    }

    // 親が2つのとき（merge commit）の処理
    else if (2 == srcParents.size()) {

      log.info("{}/{} rebuilding merge commit \"{}\" ({}) on {}", this.numberOfRebuiltCommits,
          this.numberOfTrackedCommits, RevCommitUtil.getAbbreviatedID(targetCommit),
          RevCommitUtil.getDate(targetCommit, RevCommitUtil.DATE_FORMAT), currentBranchName);

      // ワーキングディレクトリに余計なファイルが有れば削除
      // コマンドラインのgitならこの処理なしでも動くが，jGitだとなぜかこの処理が必要
      final Status status = this.desRepo.doStatusCommand();
      final Set<String> nonIndexedFiles = status.getIgnoredNotInIndex();
      this.deleteFiles(nonIndexedFiles);

      // 1つ目の親のブランチにスイッチ
      final int parentBranchID = this.branchMap.get(desParents[0]);
      if (!currentBranchName.equals(BranchName.getLabel(parentBranchID))) {
        this.checkout(parentBranchID, false, null, false, targetCommit);
      }

      // もし1つ目の親のブランチが，コミットすべきブランチではない場合は新しいブランチを作成
      if (parentBranchID != branchID) {
        this.checkout(branchID, true, desParents[0], false, targetCommit);
      }

      // 2つ目の親を対象にしてマージ
      final MergeStatus mergeStatus = this.desRepo.doMergeCommand(desParents[1]);
      if (null == mergeStatus) {
        log.error("  building a finer repository aborted due to a merge error at commit \"{}\"",
            RevCommitUtil.getAbbreviatedID(targetCommit));
        log.error("  failed on branch \"{}\" and merge target branch is \"{}\"",
            BranchName.getLabel(branchID), RevCommitUtil.getAbbreviatedID(desParents[1]));
        System.exit(0);
      }

      // マージが失敗したときには，古いリポジトリからファイルを持ってくる
      // git-subtree の場合も，古いリポジトリからファイルを持ってくる
      if (!mergeStatus.isSuccessful() || RevCommitUtil.isSubtreeCommit(targetCommit)) {

        // 対象コミットのファイルの一覧を取得し，新しいリポジトリに追加
        final Map<String, byte[]> dataInCommit = this.srcRepo.getFiles(targetCommit);
        final Map<String, byte[]> javaDataInCommit =
            this.filterMap(dataInCommit, path -> path.endsWith(".java"));
        final Map<String, byte[]> otherDataInCommit =
            this.filterMap(dataInCommit, path -> !path.endsWith(".java"));

        if (this.config.isOriginalJavaIncluded()) {
          this.addFiles(javaDataInCommit);
        }
        if (this.config.isOtherFilesIncluded()) {
          this.addFiles(otherDataInCommit);
        }

        // 対象コミットのファイル群から，細粒度Javaファイルを作成し，新しいリポジトリに追加
        final Map<String, byte[]> finerJavaData = this.generateFinerJavaModules(dataInCommit);
        this.addFiles(finerJavaData);

        // ワーキングファイルに含まれているファイルのうち，dataInCommit と finerJavaData のどちらにも
        // 含まれていないファイルに対して，git-rm コマンドを実行
        final Set<String> filesToDelete = this.getWorkingFiles(this.config.getDesPath());
        filesToDelete.removeAll(dataInCommit.keySet());
        filesToDelete.removeAll(finerJavaData.keySet());
        this.removeFiles(filesToDelete);
      }

      // targetCommitの内容でコミット
      final PersonIdent authorIdent = targetCommit.getAuthorIdent();
      final String id = RevCommitUtil.getAbbreviatedID(targetCommit);
      final String message = targetCommit.getFullMessage();
      newCommit = this.desRepo.doCommitCommand(authorIdent, id, message);
    }

    final Status status = this.desRepo.doStatusCommand();
    if (!status.isClean()) {
      log.warn("  status after rebuilding commit \"{}\" is not clean",
          RevCommitUtil.getAbbreviatedID(targetCommit));
      this.getDirtyFiles(status)
          .forEach((f, s) -> log.warn(s + f));
      System.exit(0);
    }

    final Set<String> nonIndexedFiles = status.getIgnoredNotInIndex();
    this.deleteFiles(nonIndexedFiles);

    // オリジナルリポジトリと細粒度リポジトリのコミットのマップをとる
    this.commitMap.put(targetCommit, newCommit);

    // 細粒度リポジトリとブランチとのマップをとる
    this.branchMap.put(newCommit, branchID);

    checkedCommits.add(targetCommit);

    log.trace("exit exec(RevCommit=\"{}\", int=\"{}\", Set<RevCommit>=\"{}\")",
        RevCommitUtil.getAbbreviatedID(targetCommit), branchID, checkedCommits.size());

    return newCommit;
  }

  // 第一引数のブランチに対して git-checkout する．
  private void checkout(final int branchID, final boolean create, final RevCommit startPoint,
      final boolean orphan, final RevCommit targetCommit) {
    log.trace("enter checkout(int=\"{}\", boolean=\"{}\", RevCommit=\"{}\", boolean=\"{}\")",
        branchID, create, RevCommitUtil.getAbbreviatedID(startPoint), orphan);

    final String branchName = BranchName.getLabel(branchID);
    final boolean success = this.desRepo.doCheckoutCommand(branchName, create, startPoint, orphan);

    // checkout が成功し，create が true のとき
    if (success && create) {
      log.debug("  created new branch \"{}\" and switched to it", BranchName.getLabel(branchID));
      return;
    }

    // checkout が成功し，create が false のとき
    else if (success && !create) {
      log.debug("  switched to branch \"{}\"", BranchName.getLabel(branchID));
      return;
    }

    // 以下，エラー処理
    if (create) {
      log.error("  failed to create new branch \"{}\"", BranchName.getLabel(branchID));
    } else {
      log.error("  failed to switch to branch \"{}\"", BranchName.getLabel(branchID));
    }

    log.error(
        "  rebuilding aborted due to a fatal problem, a commit \"{}\" (\"{}\") and its later commits have not been rebuilded",
        RevCommitUtil.getAbbreviatedID(targetCommit),
        RevCommitUtil.getDate(targetCommit, RevCommitUtil.DATE_FORMAT));
    System.exit(0);
  }

  // 引数で与えたれたファイル群のうち，Javaファイルに対して細粒度Javaファイルを作成する
  private Map<String, byte[]> generateFinerJavaModules(final Map<String, byte[]> files) {
    log.trace("enter generateFinerJavaModules(Map<String, byte[]>=\"{}\")", files.size());
    final Map<String, byte[]> finerJavaData = new HashMap<>();

    files.forEach((path, data) -> {

      if (!path.endsWith(".java")) {
        return;
      }

      final FinerJavaFileBuilder builder = new FinerJavaFileBuilder(this.config);
      final String text = new String(data, StandardCharsets.UTF_8);
      final List<FinerJavaModule> finerJavaModules = builder.constructAST(path, text);

      for (final FinerJavaModule module : finerJavaModules) {
        final Path finerPath = module.getPath();
        final String finerText = String.join(System.lineSeparator(), module.getLines());
        finerJavaData.put(finerPath.toString(), finerText.getBytes(StandardCharsets.UTF_8));
      }
    });

    return finerJavaData;
  }

  // 第一引数で与えられたファイルのうち，第二引数で与えられたいずれかの接頭辞をもつものを抽出
  private Set<String> getFilesHavingPrefix(final Set<String> files, final Set<String> prefixes) {
    log.trace("enter getFilesHavingPrefix(Set<String>=\"{}\", Set<String>=\"{}\")", files.size(),
        prefixes.size());
    return files.stream()
        .filter(f -> prefixes.stream()
            .anyMatch(p -> f.startsWith(p)))
        .collect(Collectors.toSet());
  }

  // 引数で与えられたファイルをgit-addする．
  private void addFiles(final Map<String, byte[]> files) {
    log.trace("enter addFiles(Map<String, byte[]>=\"{}\")", files.size());

    // 各ファイルを新しいリポジトリに保存
    files.forEach((path, data) -> {

      // ファイルの絶対パスを取得
      final Path absolutePath = this.desRepo.path.resolve(path);

      // ファイルの親ディレクトリがなければ作成
      final Path parent = absolutePath.getParent();
      if (Files.notExists(parent)) {
        try {
          Files.createDirectories(parent);
        } catch (final IOException e) {
          log.error("  failed to create a new directory \"{}\"", parent.toString());
          Stream.of(e.getStackTrace())
              .forEach(s -> log.error(s.toString()));
          e.printStackTrace();
          return;
        }
      }

      // ファイル書き込み
      try {
        Files.write(absolutePath, data);
      } catch (final IOException e) {
        log.error("  failed to write file \"{}\"", absolutePath.toString());
        log.error(e.getMessage());
        Stream.of(e.getStackTrace())
            .forEach(s -> log.error(s.toString()));
        return;
      }
    });

    // addコマンドの実行
    this.desRepo.doAddCommand(files.keySet());
  }

  // 引数で与えられたファイルをgit-rmする
  private void removeFiles(final Set<String> paths) {
    log.trace("enter removeFiles(Set<String>=\"{}\")", paths.size());
    if (!paths.isEmpty()) {
      this.desRepo.doRmCommand(paths);
    }
  }

  // 引数で指定されたディレクトリ以下にある全ファイルを取得する．ただし，".git"以下は除く．
  private Set<String> getWorkingFiles(final Path repoPath, final String... extensions) {
    log.trace("enter getWorkingFiles(Path=\"{}\", String...=\"{}\")", repoPath.toFile(),
        extensions);
    return FileUtils
        .listFiles(repoPath.toFile(), (0 == extensions.length ? null : extensions), true)
        .stream()
        .map(f -> Paths.get(f.getAbsolutePath()))
        .filter(ap -> !this.isRepositoryFile(ap))
        .map(ap -> repoPath.relativize(ap))
        .map(lp -> lp.toString())
        .collect(Collectors.toSet());
  }

  private boolean isRepositoryFile(final Path path) {
    Path currentPath = path.toAbsolutePath();
    do {
      if (currentPath.endsWith(".git") && Files.isDirectory(currentPath)) {
        return true;
      }
      currentPath = currentPath.getParent();
    } while (null != currentPath);
    return false;
  }

  // 引数で与えられたDiffEntryのうち，ChangeTypeがADDなもののパスを取得する
  private Set<String> getAddedFiles(final List<DiffEntry> diffEntries) {
    log.trace("enter getAddedFiles(List<DiffEntry>=\"{}\")", diffEntries.size());
    return diffEntries.stream()
        .filter(d -> ChangeType.ADD == d.getChangeType())
        .map(d -> d.getNewPath()) // new path なので注意！！
        .collect(Collectors.toSet());
  }

  // 引数で与えられたDiffEntryのうち，ChangeTypeがMODIFYなもののパスを取得する
  private Set<String> getModifiedFiles(final List<DiffEntry> diffEntries) {
    log.trace("enter getModifiedFiles(List<DiffEntry>=\"{}\")", diffEntries.size());
    return diffEntries.stream()
        .filter(d -> ChangeType.MODIFY == d.getChangeType())
        .map(d -> d.getNewPath()) // new path でも old path でもどちらでも良い
        .collect(Collectors.toSet());
  }

  // 引数で与えられたDiffEntryのうち，ChangeTypeがDELETEなもののパスを取得する
  private Set<String> getDeletedFiles(final List<DiffEntry> diffEntries) {
    log.trace("enter getDeletedFiles(List<DiffEntry>=\"{}\")", diffEntries.size());
    return diffEntries.stream()
        .filter(d -> ChangeType.DELETE == d.getChangeType())
        .map(d -> d.getOldPath()) // old path なので注意！！
        .collect(Collectors.toSet());
  }

  // 第一引数で与えられたSetのうち，第二引数の条件に合うものを抽出
  private Set<String> filterSet(final Set<String> set, final Predicate<String> p) {
    log.trace("enter filterSet(Set<String>=\"{}\", Predicate<String>)", set.size());
    return set.stream()
        .filter(p)
        .collect(Collectors.toSet());
  }

  // 第一引数で与えられたMapのうち，第二引数の条件に合うものを抽出
  private Map<String, byte[]> filterMap(final Map<String, byte[]> map, final Predicate<String> p) {
    log.trace("enter filterMap(Map<String, byte[]>=\"{}\", Predicate<String>)", map.size());
    return map.keySet()
        .stream()
        .filter(p)
        .collect(Collectors.toMap(k -> k, k -> map.get(k)));
  }

  // 引数で与えられたファイルパスの集合から，java ファイルのみを取り出し拡張子を取り除く
  private Set<String> removePrefixes(final Set<String> files) {
    log.trace("enter removePrefixes(Set<String>=\"{}\")", files.size());
    return files.stream()
        .filter(file -> file.endsWith(".java"))
        .map(file -> file.substring(0, file.lastIndexOf('.')))
        .collect(Collectors.toSet());
  }

  // 引数で与えられたファイルパスの集合に対して削除を行う
  private void deleteFiles(final Set<String> filesToDelete) {
    log.trace("enter deleteFiles(Set<String>=\"{}\")", filesToDelete.size());
    final Path finerRepoPath = this.desRepo.path;
    for (final String file : filesToDelete) {
      final Path absoluteFilePath = finerRepoPath.resolve(file);
      try {
        if (Files.exists(absoluteFilePath)) {
          FileUtils.forceDelete(absoluteFilePath.toFile());
        }
      } catch (final IOException e) {
        log.error("  failed to delete a file \"{}\"", absoluteFilePath.toString());
        log.error(e.getMessage());
      }
    }
  }

  private Map<String, String> getDirtyFiles(final Status status) {
    final Map<String, String> files = new HashMap<>();
    files.putAll(convertToMap(status.getAdded(), "added: "));
    files.putAll(convertToMap(status.getChanged(), "changed: "));
    files.putAll(convertToMap(status.getConflicting(), "conflicted: "));
    files.putAll(convertToMap(status.getMissing(), "missing: "));
    files.putAll(convertToMap(status.getModified(), "modified: "));
    files.putAll(convertToMap(status.getRemoved(), "removed: "));
    files.putAll(convertToMap(status.getIgnoredNotInIndex(), "ignored or not-indexed: "));
    files.putAll(convertToMap(status.getUncommittedChanges(), "uncommitted change: "));
    files.putAll(convertToMap(status.getUntracked(), "untracked: "));
    files.putAll(convertToMap(status.getUntrackedFolders(), "untracked folder: "));
    return files;
  }

  private Map<String, String> convertToMap(final Collection<String> files, final String prefix) {
    return files.stream()
        .collect(Collectors.toMap(f -> f, f -> prefix));
  }
}
