# FinerGit 
[日本語の説明はこちら](#user-content-finergit-日本語)

FinerGit is a tool to easily obtain change histories of Java methods by using the Git mechanism.
FinerGit takes a Git repository as its input and generates another Git repository.
Git repositories that FinerGit generates have the following features.
- Every method in source files gets extracted as a single file.
- Every line of extracted files includes only a single token.

The first feature realizes that Java methods are able to be tracked with Git mechanism.
The second feature improves the trackability of Java methods.


## Quick start

### Check your environment
First of all, please make sure that Java 11+ is installed in your environment.
```shell-session
$ java -version
java version "11.0.6" 2020-01-14 LTS
Java(TM) SE Runtime Environment 18.9 (build 11.0.6+8-LTS)
Java HotSpot(TM) 64-Bit Server VM 18.9 (build 11.0.6+8-LTS, mixed mode)
```

### Build FinerGit
Build FinerGit.jar with the following commands.
```shell-session
$ git clone https://github.com/kusumotolab/FinerGit.git
$ cd FinerGit
$ ./gradlew build
```

### Run FinerGit
A basic command to convert a Git repository to a FinerGit repository is as follows.
```shell-session
$ java -jar FinerGit.jar create --src /path/to/repoA --des /path/to/repoB
```
Herein, `/path/to/repoA` is an existing Git repository, and `/path/to/repoB` is a path to output a new FinerGit repository.

FinerGit has several options for converting repositories.
The options are printed with the following command.
```shell-session
$ java -jar build/libs/FinerGit.jar create
```

### See change histories of Java methods in a FinerGit repository

In FinerGit repositories, there are files whose extensions are `.cjava`, `fjava`, or `.mjava`.

- Extension `.cjava` means that its file represents a Java class. But all methods included in the class get extracted as different files.
- Extension `.fjava` means that its file represents a Java field. Names of method files follow the format of `ClassName#FieldName.fjava`.
- Extension `.mjava` means that its file represents a Java method. Names of method files follow the format of `ClassName#MethodSignature.mjava`.

If you want to see the change history of `Foo#bar().mjava`, type the following command.
```shell-session
$ git log "Foo#bar().mjava"
```
You will get all commits where method `bar()` was changed.

``--follow`` option is useful since it enables Git to track files even if their names got changed.
```shell-session
$ git log --follow "Foo#bar().mjava"
```

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


## 使い方

### 環境の確認
FinerGitはコマンドラインツールであり，実行にはJavaのバージョン11以降を必要とします．
以下のコマンドにより，Javaのバージョン11以降がインストールされていることを確認してください．
```shell-session
$ java -version
java version "11.0.6" 2020-01-14 LTS
Java(TM) SE Runtime Environment 18.9 (build 11.0.6+8-LTS)
Java HotSpot(TM) 64-Bit Server VM 18.9 (build 11.0.6+8-LTS, mixed mode)
```

### FinerGit のビルド
以下のコマンドを順に実行し，FinerGitをビルドしてください．
```shell-session
$ git clone https://github.com/kusumotolab/FinerGit.git
$ cd FinerGit
$ ./gradlew build
```

### FinerGit の実行
Gitリポジトリを変換するための基本コマンドは以下の通りです．
```shell-session
$ java -jar FinerGit.jar create --src /path/to/repoA --des /path/to/repoB
```
ここで，`/path/to/repoA`は既存のGitリポジトリのパス，`/path/to/repoB`は生成するFinerGitリポジトリのパスを表しています．

FinerGitは変換のオプションをいくつか備えています．
オブション一覧は以下のコマンドにより確認できます．
```shell-session
$ java -jar build/libs/FinerGit.jar create
```

### FinerGit リポジトリを使って Java メソッドの変更履歴を確認する

FinerGit リポジトリには拡張子が `.cjava`，`.fjava`，`.mjava` なファイルが含まれています．

- 拡張子が `.cjava` なファイルは，Java のクラスを表すファイルです．ただし，その中に定義されているメソッドは別ファイルとして抽出されています．
- 拡張子が `.fjava` なファイルは，Java のフィールドを表すクラスです．なお，Java フィールドのファイル名は，`クラス名#フィール名.fjava` となっています．
- 拡張子が `.mjava` なファイルは，Java のメソッドを表すクラスです．なお，Java メソッドのファイル名は，`クラス名#メソッドシグネチャ.mjava` となっています．

例えば，
```shell-session
$ git log "Hoge#fuga().mjava"
```
というコマンドを入力すると，`fuga()` メソッドに変更を加えたコミットの一覧を得ることができます．
```shell-session
$ git log --follow "Hoge#fuga().mjava"
```
というように，``--follow`` オプションを利用すれば，メソッド名やそれを含むクラス名が変わっていた場合でも追跡して，コミット一覧を表示します．


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










