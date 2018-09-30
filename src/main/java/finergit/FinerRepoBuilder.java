package finergit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import finergit.ast.FinerJavaFileBuilder;
import finergit.ast.FinerJavaModule;
import finergit.util.RevCommitUtil;

/**
 * Gitリポジトリから細粒度リポジトリの生成処理を行うクラス
 * 
 * @author higo
 *
 */
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
   * 細粒度リポジトリの構築を開始するメソッド．対象Gitリポジトリをたどり， ルートコミットから順に細粒度リポジトリのコミットを生成して，コミットしていく．
   */
  public FinerRepo exec() {
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
    return this.desRepo;
  }

  /**
   * 第一引数で与えたれたコミットに対して，そこに含まれるJavaファイルの細粒度版からなるコミットを生成する． 第二引数で与えられたブランチは，細粒度版Javaファイルをコミットするブランチ．
   * 第三引数で与えれたコミット群は，すでに構築したコミット群．
   * 
   * @param targetCommit 構築対象コミット
   * @param branchID 細粒度なファイルをコミットするためのブランチ
   * @param checkedCommits すでに構築したコミット群
   * @return 構築対象コミット（第一引数）から生成した細粒度版Javaファイルからなるコミット
   */
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
        this.removeAllFiles(this.desRepo.path);
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

      final Set<String> filesInPreviousCommit = this.desRepo.listFiles(desParents[0]);
      newCommit = this.buildCommit(targetCommit, filesInPreviousCommit);
    }

    // 親が2つのとき（merge commit）の処理
    else if (2 == srcParents.size()) {

      log.info("{}/{} rebuilding merge commit \"{}\" ({}) on {}", this.numberOfRebuiltCommits,
          this.numberOfTrackedCommits, RevCommitUtil.getAbbreviatedID(targetCommit),
          RevCommitUtil.getDate(targetCommit, RevCommitUtil.DATE_FORMAT), currentBranchName);

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

      // マージの成否にかかわらず，マージの結果は使わない．手動マージが行われていた場合に自動では絶対に再現できないため．
      final Set<String> filesInPreviousCommit = this.desRepo.listFiles(desParents[0]);
      newCommit = this.buildCommit(targetCommit, filesInPreviousCommit);
    }

    if (this.config.isCheckCommit()) {
      final Status status = this.desRepo.doStatusCommand();
      if (!status.isClean()) {
        log.error("  status after rebuilding commit \"{}\" is not clean",
            RevCommitUtil.getAbbreviatedID(targetCommit));
        this.getDirtyFiles(status)
            .forEach((f, s) -> log.error(s + f));
      }
    }

    // オリジナルリポジトリと細粒度リポジトリのコミットのマップをとる
    this.commitMap.put(targetCommit, newCommit);

    // 細粒度リポジトリとブランチとのマップをとる
    this.branchMap.put(newCommit, branchID);

    checkedCommits.add(targetCommit);

    log.trace("exit exec(RevCommit=\"{}\", int=\"{}\", Set<RevCommit>=\"{}\")",
        RevCommitUtil.getAbbreviatedID(targetCommit), branchID, checkedCommits.size());

    return newCommit;
  }

  /**
   * 第一引数で与えられたコミットを再構築する．再構築したコミットを返す．
   * 
   * @param targetCommit 構築対象コミット
   * @param filesInPreviousCommit 構築する細粒度版Javaファイルからなるコミットの親コミットに含まれるファイル群
   * @return 構築した細粒度版Javaファイルからなるコミット
   */
  private RevCommit buildCommit(final RevCommit targetCommit,
      final Set<String> filesInPreviousCommit) {
    log.trace("enter buildCommit(RevCommit=\"{}\", Set<String>=\"{}\"",
        RevCommitUtil.getAbbreviatedID(targetCommit), filesInPreviousCommit.size());

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

    // 追加されたファイルおよび修正されたファイルから細粒度ファイルを生成
    final Map<String, byte[]> finerJavaFilesInAddedFiles =
        this.generateFinerJavaModules(addedDataInCommit);
    final Map<String, byte[]> finerJavaFilesInModifiedFiles =
        this.generateFinerJavaModules(modifiedDataInCommit);

    // IMPORTANT! 必ず削除してから追加の処理をすること．逆順でした場合，
    // case insensitive なファイルシステム上で，case sensitive なファイル名の変更を追跡できなくなる

    // 修正されたファイルから以前に生成された細粒度ファイルのうち，
    // 修正されたファイルから今回生成された細粒度ファイルに含まれないファイルに対して git-rm コマンドを実行
    final Set<String> finerJavaFilesInPreviousCommit =
        this.filterSet(filesInPreviousCommit, p -> p.endsWith(".cjava") || p.endsWith(".mjava"));
    final Set<String> modifiedJavaFilePrefixes = this.removeExtension(modifiedFiles);
    final Set<String> finerJavaFilesToDelete1 =
        this.getFilesHavingPrefix(finerJavaFilesInPreviousCommit, modifiedJavaFilePrefixes);
    finerJavaFilesToDelete1.removeAll(finerJavaFilesInModifiedFiles.keySet());
    this.removeFiles(finerJavaFilesToDelete1);

    // 削除されたファイルから以前に生成された細粒度ファイルに対して，git-rm コマンドを実行
    final Set<String> deletedJavaFilePrefixes = this.removeExtension(deletedFiles);
    final Set<String> finerJavaFilesToDelete2 =
        this.getFilesHavingPrefix(finerJavaFilesInPreviousCommit, deletedJavaFilePrefixes);
    this.removeFiles(finerJavaFilesToDelete2);

    // 対象コミットに含まれるファイルのうち，
    // 追加されたファイルおよび修正されたファイルに対して，git-add コマンドを実行
    // 削除されたファイルに対して，git-rm コマンドを実行
    if (this.config.isOriginalJavaIncluded()) {
      this.addFiles(modifiedJavaDataInCommit);
      this.addFiles(addedJavaDataInCommit);
      this.removeFiles(deletedJavaFiles);
    }
    if (this.config.isOtherFilesIncluded()) {
      this.addFiles(modifiedOtherDataInCommit);
      this.addFiles(addedOtherDataInCommit);
      this.removeFiles(deletedOtherFiles);
    }

    // 追加および修正されたファイルから生成した細粒度ファイルに対して，git-add コマンドを実行
    this.addFiles(finerJavaFilesInAddedFiles);
    this.addFiles(finerJavaFilesInModifiedFiles);

    // git-commitコマンドの実行
    final PersonIdent authorIdent = targetCommit.getAuthorIdent();
    final String id = RevCommitUtil.getAbbreviatedID(targetCommit);
    final String message = targetCommit.getFullMessage();

    log.trace("exit buildCommit(RevCommit=\"{}\", Set<String>=\"{}\"",
        RevCommitUtil.getAbbreviatedID(targetCommit), filesInPreviousCommit.size());

    return this.desRepo.doCommitCommand(authorIdent, id, message);
  }

  // 第一引数のブランチに対して git-checkout する．
  /**
   * 第一引数のブランチに対して，git-checkout を実行する
   * 
   * @param branchID git-checkout の対象ブランチ
   * @param create ブランチを新しく作るかどうか
   * @param startPoint ブランチを新しく作る場合に起点となるブランチ
   * @param orphan ブランチを新しく作る場合に，親コミットが存在しないブランチにするかどうか
   * @param targetCommit 現在構築対象になっているコミット
   */
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

  //
  /**
   * 引数で与えたれたファイル群のうち，Javaファイルに対して細粒度Javaファイルを作成する
   * 
   * @param files 細粒度Javaファイルの生成対象ファイル群
   * @return 生成された細粒度Javaファイル
   */
  private Map<String, byte[]> generateFinerJavaModules(final Map<String, byte[]> files) {
    log.trace("enter generateFinerJavaModules(Map<String, byte[]>=\"{}\")", files.size());
    final ConcurrentMap<String, byte[]> finerJavaData = new ConcurrentHashMap<>();

    files.entrySet()
        .parallelStream()
        .forEach(entry -> {
          final String path = entry.getKey();
          final byte[] data = entry.getValue();

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

  /**
   * 第一引数で与えられたファイルのうち，第二引数で与えられたいずれかの接頭辞をもつものを抽出
   * 
   * @param files 抽出対象ファイル群
   * @param prefixes 接頭辞
   * @return 与えられた接頭辞を持つファイル群
   */
  private Set<String> getFilesHavingPrefix(final Set<String> files, final Set<String> prefixes) {
    log.trace("enter getFilesHavingPrefix(Set<String>=\"{}\", Set<String>=\"{}\")", files.size(),
        prefixes.size());
    return files.parallelStream()
        .filter(f -> prefixes.stream()
            .anyMatch(p -> f.startsWith(p)))
        .collect(Collectors.toSet());
  }

  /**
   * 引数で与えられたファイルデータに対して，ファイルシステムへの書き込みと git-add を実行する
   * 
   * @param files git-add の対象ファイル群
   */
  private void addFiles(final Map<String, byte[]> files) {
    log.trace("enter addFiles(Map<String, byte[]>=\"{}\")", files.size());

    // 各ファイルを新しいリポジトリに保存
    // 現在マルチスレッドで行っているかもしかしたら，正しく動作しない可能性あり．
    // 正常に動かない場合は，シングルスレッドに戻すべき．
    files.entrySet()
        .parallelStream()
        .forEach(entry -> {
          final String path = entry.getKey();
          final byte[] data = entry.getValue();

          // ファイルの絶対パスを取得
          final Path absolutePath = this.desRepo.path.resolve(path);

          // ファイルの親ディレクトリがなければ作成
          final Path parent = absolutePath.getParent();
          if (Files.notExists(parent)) {
            synchronized (files) {
              try {
                Files.createDirectories(parent);
              } catch (final IOException e) {
                log.error("  failed to create a new directory \"{}\"", parent.toString());
                log.error(e.getMessage());
                return;
              }
            }
          }

          // ファイル書き込み
          try {
            Files.write(absolutePath, data);
          } catch (final IOException e) {
            log.error("  failed to write file \"{}\"", absolutePath.toString());
            log.error(e.getMessage());
            return;
          }
        });

    // addコマンドの実行
    this.desRepo.doAddCommand(files.keySet());
  }

  /**
   * 引数で与えられたファイル群に対して，git-rm を実行する
   * 
   * @param paths git-rm の対象ファイル群
   */
  private void removeFiles(final Set<String> paths) {
    log.trace("enter removeFiles(Set<String>=\"{}\")", paths.size());
    if (!paths.isEmpty()) {
      this.desRepo.doRmCommand(paths);
    }
  }

  /**
   * 引数で与えられたパス以下に存在する全てのファイルを削除する．ただし，".git"以下のファイルは削除の対象外．
   * 
   * @param repoPath 削除対象ファイル群のルートディレクトリ．
   */
  private void removeAllFiles(final Path repoPath) {
    log.trace("enter removeAllFiles()");
    try {
      final Set<String> files = Files.walk(repoPath)
          .parallel()
          .filter(path -> !isRepositoryFile(path) && Files.isRegularFile(path))
          .map(path -> repoPath.relativize(path))
          .map(path -> path.toString())
          .collect(Collectors.toSet());
      this.removeFiles(files);
    } catch (final IOException e) {
      log.error("failed to access \"{}\"", repoPath);
      log.error(e.getMessage());
    }
  }

  /**
   * 引数で与えられたパス（ディレクトリ）が，".git"かどうかを判定する．
   * 
   * @param path 判定対象パス
   * @return ".git"の場合は true，そうでない場合は false
   */
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

  //
  /**
   * 引数で与えられたDiffEntryのうち，ChangeTypeがADDなもののパスを抽出する
   * 
   * @param diffEntries 抽出対象のパスを含む差分情報
   * @return ChangeTypeがADDなもののパス
   */
  private Set<String> getAddedFiles(final List<DiffEntry> diffEntries) {
    log.trace("enter getAddedFiles(List<DiffEntry>=\"{}\")", diffEntries.size());
    return diffEntries.parallelStream()
        .filter(d -> ChangeType.ADD == d.getChangeType())
        .map(d -> d.getNewPath()) // new path なので注意！！
        .collect(Collectors.toSet());
  }

  /**
   * 引数で与えられたDiffEntryのうち，ChangeTypeがMODIFYなもののパスを抽出する
   * 
   * @param diffEntries 抽出対象のパスを含む差分情報
   * @return ChangeTypeがMODIFYなもののパス
   */
  private Set<String> getModifiedFiles(final List<DiffEntry> diffEntries) {
    log.trace("enter getModifiedFiles(List<DiffEntry>=\"{}\")", diffEntries.size());
    return diffEntries.parallelStream()
        .filter(d -> ChangeType.MODIFY == d.getChangeType())
        .map(d -> d.getNewPath()) // new path でも old path でもどちらでも良い
        .collect(Collectors.toSet());
  }

  /**
   * 引数で与えられたDiffEntryのうち，ChangeTypeがDELETEなもののパスを抽出する
   * 
   * @param diffEntries 抽出対象のパスを含む差分情報
   * @return ChangeTypeがDELETEなもののパス
   */
  private Set<String> getDeletedFiles(final List<DiffEntry> diffEntries) {
    log.trace("enter getDeletedFiles(List<DiffEntry>=\"{}\")", diffEntries.size());
    return diffEntries.parallelStream()
        .filter(d -> ChangeType.DELETE == d.getChangeType())
        .map(d -> d.getOldPath()) // old path なので注意！！
        .collect(Collectors.toSet());
  }

  /**
   * 第一引数で与えられた文字列（パス）のうち，第二引数の条件に合うものを抽出
   * 
   * @param set 文字列（パス）の集合
   * @param p 抽出条件
   * @return
   */
  private Set<String> filterSet(final Set<String> set, final Predicate<String> p) {
    log.trace("enter filterSet(Set<String>=\"{}\", Predicate<String>)", set.size());
    return set.parallelStream()
        .filter(p)
        .collect(Collectors.toSet());
  }

  /**
   * 第一引数で与えられたMapのうち，第二引数の条件に合うものを抽出
   * 
   * @param map 文字列（パス）をキーとするマップ
   * @param p 抽出条件
   * @return
   */
  private Map<String, byte[]> filterMap(final Map<String, byte[]> map, final Predicate<String> p) {
    log.trace("enter filterMap(Map<String, byte[]>=\"{}\", Predicate<String>)", map.size());
    return map.keySet()
        .parallelStream()
        .filter(p)
        .collect(Collectors.toMap(k -> k, k -> map.get(k)));
  }

  /**
   * 引数で与えられたファイルパスの集合から，Java ファイルのみを取り出し拡張子を取り除く
   * 
   * @param files 対象ファイルパス集合
   * @return 拡張子が取り除かれたJavaファイルのパスの集合
   */
  private Set<String> removeExtension(final Set<String> files) {
    log.trace("enter removePrefixes(Set<String>=\"{}\")", files.size());
    return files.parallelStream()
        .filter(file -> file.endsWith(".java"))
        .map(file -> file.substring(0, file.lastIndexOf('.')))
        .collect(Collectors.toSet());
  }

  /**
   * 引数で与えられた git-status の結果に含まれるさまざまな状態のファイルの情報を返す．
   * 
   * @param status git-status の結果
   * @return git-status の結果に含まれるさまざまな状態のファイルの情報
   */
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

  /**
   * 第一引数で与えれたファイル群に対して，第二引数との関連付けを行う
   * 
   * @param files 対象ファイル群
   * @param attribute 関連付ける項目
   * @return
   */
  private Map<String, String> convertToMap(final Collection<String> files, final String attribute) {
    return files.stream()
        .collect(Collectors.toMap(f -> f, f -> attribute));
  }
}
