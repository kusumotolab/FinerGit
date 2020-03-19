# FinerGit 
[日本語の説明はこちら](#user-content-finergit-日本語)

FinerGit is a tool to easily obtain change histories of Java methods by using the Git mechanism.
FinerGit takes a Git repository as its input and generates another Git repository.
Git repositories that FinerGit generates have the following features.
- Every method in source files gets extracted as a single file.
- Every line of extracted files includes only a single token.

The first feature realizes that Java methods are able to be tracked with Git mechanism.
The second feature improves the trackability of Java methods.


## Preparation

1. Access to [FinerGit page in GitHub](https://github.com/kusumotolab/FinerGit), and clone FinerGit to your PC.
2. Copy 2 files in directory `git-subcommand` (`FinerGit.jar` and `git-fg`) to a directory which is included in your $PATH.

Execute your terminal and type the following command.
```sh
git fg
```
If you get help message, your FinerGit installation has been succeeded.


## Convert a Git repository to a FinerGit repository

You can convert with the following command.
```sh
git fg --src repoA --des repoB
```
Herein, `repoA` is an existing Git repository, and `repoB` is a new FinerGit repository.
You must specify a non-existing path for `repoB`.

You can see other options with the following command.
```sh
git fg
```

You can also run `FinerGit.jar` directly.
```sh
java -jar FinerGit.jar create --src repoA --des repoB
```

## See change histories of Java methods in FinerGit repositories

In FinerGit repositories, there are files whose extensions are `.cjava` or `.mjava`.

- Extension `.cjava` means that its file represents a Java class. But all methods included in the class get extracted as different files.
- Extension `.mjava` means that its file represents a Java method. Names of method files follow the format of `ClassName#MethodSignature.mjava`.
- Extension `.fjava` means that its file represents a Java field. Names of method files follow the format of `ClassName#FieldSignature.fjava`.

If you want to see the change history of `Foo#bar().mjava`, type the following command.
```sh
git log "Foo#bar().mjava"
```
You will get all commits where method `bar()` was changed.

``--follow`` option is useful because it enables Git to track files even if their names got changed.
```sh
git log --follow "Foo#bar().mjava"
```

<!--

## Obtain semantic versions of Java methods

In a single phrase, [semantic versioning](https://semver.org/lang/en/) is a mechanical versioning way with the following rules.

- A software version is represented with three numbers, `a.b.c`.
- if software has gotten a change that does not preserve backward compatibility (in short, *incompatible change*), `a` is incremented. `b` and `c` get back to 0.
- If software has gotten a change that preserves backward compatibility (in short, *compatible change*), `b` is incremented. `a` is not changed and `c` gets back to 0.
- If software has gotten a change that fixes a bug (in short, *bug-fix change*), `c` is incremented. `a` and `b` are not changed.

**FinerGit has the functionality that automatically calculates semantic versioning for Java methods.**
In FinerGit, incompatible, compatible, and bug-fix changes are defined as follows.
- If either of name, parameters, return type, or modifiers of a method is changed, the change is regarded as *incompatible*.
- If only the body of a method is changed, the change is regard as *compatible*.
- *Compatible changes* in commits whose messages include any terms assuming bug fix such as "bug" or "fix" are regarded as *bug-fix changes*.

Semantic versioning for Java methods helps you to understand how many times a given method's signature has been changed or how many times bug-fix changes occurred after the last functionality addition.
FinerGit has a command, `git-sv`, for calculating semantic version for a given file

For example, the following command calculates a semantic version for Java method `fuga()`.
```sh
git sv Foo#bar().mjava
```

there are several options for `git-sv` command.
You can see all options by executing `git-sv` with no arguments.


## Calculate semantic versions for multiple files efficiently

Calculating a semantic version is not lightweight processing, it occasionally takes several seconds.
`git-sv` command internally invokes Java VM, so that the overhead to launch Java VM in many times is non-negligible.
Thus, FinerGit has another command `git-msv`, which is calculating semantic versions for multiple files.
By using `git-msv` instead of `git-sv`, you can get rid of overhead to launch Java VM in many times.
`git-msv` requires a file including a list of files to calculate semantic versions.
We recommend using **absolute paths** to specify files instead of relative ones.

-->

## Use in your research

If you are using FinerGit in your research, please cite the following paper:

Yoshiki Higo, Shinpei Hayashi, and Shinji Kusumoto, "On Tracking Java Methods with Git Mechanisms," Journal of Systems and Software, Vol.xx, No.xx, pages xx-xx, 2020. [[available online](https://doi.org/10.1016/j.jss.2020.110571)]
```
@article{10.1016/j.jss.2020.110571,
author = {Higo, Yoshiki and Hayashi, Shinpei and Kusumoto, Shinji},
title = {On Tracking Java Methods with Git Mechanisms},
year = {2020},
issue_date = {xxx 2020},
publisher = {Elsevier Science Inc.},
address = {USA},
volume = {xx},
number = {xx},
issn = {0164-1212},
url = {https://doi.org/10.1016/j.jss.2020.110571},
doi = {10.1016/j.jss.2020.110571},
journal = {Journal of Systems and Software},
month = xxx,
pages = {xxx–-xxx},
numpages = {xx},
keywords = {Mining software repositories, Source code analysis, Tracking Java methods}
}
```

## At the end

FinerGit is still under development. We mainly use MacOS + JDK11 + ~~Eclipse~~ IntelliJ IDEA in our FinerGit development.
`git-subcommand/FinerGit.jar` is built with JDk11.
We rarely test FinerGit on Windows environment.

-----
-----

# <a name="Japanese"></a>FinerGit （日本語）
FinerGit は Java メソッドの変更履歴を容易に取得することを目的として開発されているツールです．
Git のメカニズムを利用して Java メソッドの変更履歴を取得します．
FinerGit の入力は Java ソースコードを含む Git リポジトリです．
FinerGit の出力は，以下の2つの特徴をもった Git リポジトリです．
- ソースコード内の各 Java メソッドが1つのファイルとして抽出されている．
- 抽出された各 Java メソッドの各行は1つの字句のみを含む．

1つ目の特徴により，Git のファイル追跡機能を使って Java メソッドを追跡することができます．
2つ目の特徴により，Java メソッドの追跡可能性を高めることができます．

## 準備

1. [GitHub の FinerGit のページ](https://github.com/kusumotolab/FinerGit)にアクセスし，ローカルストレージに FinerGit を clone する．
2. `git-subcommand` 内のファイル（`FinerGit.jar`および`git-fg`）を環境変数 PATH が通ったディレクトリ以下にコピーする．

ターミナルを起動し，
```sh
git fg
```
とタイプし，使い方が表示されればOKです．

## Git リポジトリを FinerGit リポジトリに変換する

変換は以下のコマンドで行います．
```sh
git fg --src repoA --des repoB
```

ここで，`repoA` は既存の Git リポジトリ，`repoB` は新しく作成する FinerGit リポジトリです．
`repoB` には存在しないファイルパスを指定してください．

なお，
```sh
git fg
```

と，引数無しでタイプすれば，利用可能なオプションが表示されます．

また，`FinerGit.jar`を直接実行することで細粒度リポジトリを作成することもできます．
```sh
java -jar FinerGit.jar create --src repoA --des repoB
```

## FinerGit リポジトリを使って Java メソッドの変更履歴を確認する

FinerGit リポジトリには拡張子が `.cjava` や `.mjava` なファイルが含まれています．

- 拡張子が `.cjava` なファイルは，Java のクラスを表すファイルです．ただし，その中に定義されているメソッドは別ファイルとして抽出されています．
- 拡張子が `.mjava` なファイルは，Java のメソッドを表すクラスです．なお，Java メソッドのファイル名は，`クラス名#メソッドシグネチャ.mjava` となっています．
- 拡張子が `.fjava` なファイルは，Java のフィールドを表すクラスです．なお，Java フィールドのファイル名は，`クラス名#フィールドシグネチャ.fjava` となっています．

例えば，
```sh
git log "Hoge#fuga().mjava"
```
というコマンドを入力すると，`fuga()` メソッドに変更を加えたコミットの一覧を得ることができます．
```sh
git log --follow "Hoge#fuga().mjava"
```
というように，``--follow`` オプションを利用すれば，メソッド名やそれを含むクラス名が変わっていた場合でも追跡して，コミット一覧を表示します．

<!-- 

## FinerGit リポジトリを使って Java メソッドのセマンティックバージョンを取得する

ひとことで言うと，[セマンティックバージョニング](https://semver.org/lang/ja/)とは，ソフトウェアのバージョニングを以下のルールに基づいて行うことです．
- ソフトウェアのバージョンは，`a.b.c` の3つの組で表す．
- `a` はソフトウェアに後方互換性が無い変更が行われた場合に1つ増やす．なお，このとき，`b` および `c` は0に戻す．
- `b` はソフトウェアに後方互換性の有る変更が行われた場合に1つ増やす．なお，このとき，`a` は変更せず，`c` は0に戻す．
- `c` はソフトウェアのバグ修正が行われた場合に1つ増やす．なお，このとき，`a` と `b` は変更しない．

FinerGit は，**セマンティックバージョンを Java メソッドに対して自動的に算出する機能**を持っています．
なお，FinerGit では，Java メソッドにおける後方互換性の無い変更，後方互換性の有る変更，バグ修正は以下の定義としています．
- Java メソッドの，名前，仮引数，返り値，アクセス修飾子のいずれかが変更された場合，後方互換性の無い変更とする．
- Java メソッドの，名前，仮引数，返り値，アクセス修飾子のいずれもが保たれた変更が行われた場合，後方互換性の有る変更とする．
- 後方互換性のある変更のうち，その変更のコミットメッセージに bug や fix 等のバグ修正を連想させる単語が含まれている場合，バグ修正とする．

Java メソッドに対してセマンティックバージョンを算出することで，そのメソッドがこれまでに何度シグネチャ変更されているのか，最後に機能追加されてから何度バグ修正されているのか，といったことがわかります．
FinerGit では，Java メソッドのセマンティックバージョンの算出には，`git-sv` コマンドを使います．

例えば，
```sh
git sv Hoge#fuga().mjava
```
と入力すれば，`fuga()` メソッドのセマンティックバージョンが出力されます．また，`git-sv` コマンドにはいくつかのオプションがあり，セマンティックバージョン以外にも，これまでの変更の総数等を表示することができます．`git-sv` と引数を何もつけずにコマンドを実行すると，利用可能なオプション一覧が表示されます．

## 複数のファイルに対してセマンティックバージョンを効率的に算出する

セマンティックバージョンの算出はある程度重い処理であり，場合によっては数秒程度かかることがあります．
また，このコマンドは内部で Java VM を起動しているため，何度も連続して `git-sv` を実行する場合，そのプロセス起動オーバーヘッドも無視できない時間となります．
複数ファイルに対してセマンティックバージョンを算出したい場合には，`git-sv` ではなく，`git-msv` を使うとプロセス起動オーバーヘッドを除外することができます．
`git-msv` を実行する場合は，その引数には，各行にJavaファイルへのパスを記入したリストファイルを指定してください．Javaファイルへのパスは，相対パスでも指定できますが，絶対パスで指定することをオススメします．

-->

## 研究での利用

研究でFinerGitを利用した場合には，以下の論文を引用してください．

Yoshiki Higo, Shinpei Hayashi, and Shinji Kusumoto, "On Tracking Java Methods with Git Mechanisms," Journal of Systems and Software, Vol.xx, No.xx, pages xx-xx, 2020. [[available online](https://doi.org/10.1016/j.jss.2020.110571)]
```
@article{10.1016/j.jss.2020.110571,
author = {Higo, Yoshiki and Hayashi, Shinpei and Kusumoto, Shinji},
title = {On Tracking Java Methods with Git Mechanisms},
year = {2020},
issue_date = {xxx 2020},
publisher = {Elsevier Science Inc.},
address = {USA},
volume = {xx},
number = {xx},
issn = {0164-1212},
url = {https://doi.org/10.1016/j.jss.2020.110571},
doi = {10.1016/j.jss.2020.110571},
journal = {Journal of Systems and Software},
month = xxx,
pages = {xxx–-xxx},
numpages = {xx},
keywords = {Mining software repositories, Source code analysis, Tracking Java methods}
}
```


## 最後に

FinerGit は主に，Mac + JDK11 + ~~Eclipse~~ IntelliJ IDEA を用いて開発されています．`git-subcommand/FinerGit.jar` も JDK1.11 でビルドされています．Windows 上ではほぼテストを行っていません．










