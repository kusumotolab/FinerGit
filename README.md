# FinerGit
[Click here to Japanese README](#user-content-finergit-日本語)

FinerGit is a command-line tool for obtaining change histories of Java methods by using Git mechanisms.
FinerGit takes a Git repository as input and generates another Git repository whose Java source files are rewritten into finer-grained files.

By default, the generated repository has the following features.

- Each Java method or constructor is extracted as a single `.mjava` file.
- Each line of an extracted Java method file contains only one token.

Optional settings can also keep original Java files, keep non-Java files, and generate class, field, or peripheral token files.


## Quick Start

### Check Your Environment

FinerGit is built with the Java 25 toolchain.
Please make sure that JDK 25+ is installed in your environment.

```shell-session
$ java -version
java version "25" ...
```

FinerGit can parse target source files from Java 1.4 through Java 25.
Use `-j` or `--java-version` to select the Java version of the input source files.
If this option is omitted, FinerGit uses Java 25.

### Build FinerGit

Build `FinerGit-all.jar` with the following commands.

```shell-session
$ git clone https://github.com/kusumotolab/FinerGit.git
$ cd FinerGit
$ ./gradlew shadowJar
```

It is fine if you have `FinerGit-all.jar` in the `FinerGit/build/libs` directory.
If you specify `jar` or `build` instead of `shadowJar` as an argument for `gradlew`, you will also get `FinerGit.jar` in the directory.
`FinerGit.jar` is not a single executable jar file.

### Run FinerGit

A basic command to convert a Git repository to a FinerGit repository is as follows.

```shell-session
$ java -jar build/libs/FinerGit-all.jar create --src /path/to/repoA --des /path/to/repoB
```

Herein, `/path/to/repoA` is an existing Git repository, and `/path/to/repoB` is a path to output a new FinerGit repository.

FinerGit has several options for converting repositories.
The options are printed with the following command.

```shell-session
$ java -jar build/libs/FinerGit-all.jar create
```

### Main Options

Required options:

- `-s`, `--src <path>`: path to the input repository.
- `-d`, `--des <path>`: path to the output repository.

Input and repository options:

- `-j`, `--java-version <version>`: Java version of target source files. Supported values are `1.4` to `1.8`, and `9` to `25`. The default is `25`.
- `-o`, `--original-javafiles <true|false>`: include original Java files. The default is `false`.
- `-p`, `--otherfiles <true|false>`: include non-Java files. The default is `false`.
- `--nthreads <num>`: number of threads used for repository rewriting. The default is one less than the number of available processors, or `1` if only one processor is available.
- `-l`, `--log-level <level>`: log level. Supported values are `trace`, `debug`, `info`, `warn`, and `error`.

Generated file options:

- `--method-file-generated <true|false>`: generate method files. The default is `true`.
- `--class-file-generated <true|false>`: generate class files. The default is `false`.
- `--field-file-generated <true|false>`: generate field files. The default is `false`.
- `--peripheral-file-generated <true|false>`: generate files for peripheral tokens outside generated class, method, and field files. The default is `false`.

Token and file-name options:

- `-t`, `--tokenize <true|false>`: tokenize generated Java method and field files. The default is `true`.
- `--access-modifier-included <true|false>`: include access modifiers in generated method and field file names. The default is `true`.
- `--method-type-erasure-included <true|false>`: include method type parameters in generated method file names. The default is `true`.
- `--return-type-included <true|false>`: include return types in generated method file names. The default is `true`.
- `--token-type-included <true|false>`: append token type names to tokenized lines. The default is `false`.
- `--method-token-included <true|false>`: include method boundary tokens in generated method files. The default is `true`.
- `--max-file-name-length <num>`: maximum generated file name length. The value must be between `13` and `255`. The default is `255`.
- `--hash-length <num>`: length of the hash value attached to shortened file names. The value must be between `7` and `40`. The default is `7`.

### See Change Histories of Java Methods in a FinerGit Repository

By default, FinerGit repositories contain `.mjava` files.
Depending on generation options, they can also contain `.cjava`, `.fjava`, and `.pjava` files.

- Extension `.mjava` means that the file represents a Java method or constructor.
- Extension `.cjava` means that the file represents Java class-level tokens. Method bodies are replaced with method tokens, and method files are generated when `--method-file-generated` is `true`.
- Extension `.fjava` means that the file represents a Java field declaration.
- Extension `.pjava` means that the file represents peripheral tokens from the original Java file.

By default, method file names include the class name, access modifier, return type, method name, and parameter types.
For example, a method may be generated as `Foo#public_void_bar(int).mjava`.
Field file names use a similar format, such as `Foo#private_String_name.fjava`.
If generated file names are too long, FinerGit shortens them and appends a hash value.

If you want to see the change history of `Foo#public_void_bar(int).mjava`, type the following command.

```shell-session
$ git log "Foo#public_void_bar(int).mjava"
```

You will get all commits where method `bar(int)` was changed.

The `--follow` option is useful since it enables Git to track files even if their names got changed.

```shell-session
$ git log --follow "Foo#public_void_bar(int).mjava"
```

## Use in Your Research

If you are using FinerGit in your research, please cite the following paper:

Yoshiki Higo, Shinpei Hayashi, and Shinji Kusumoto, "On Tracking Java Methods with Git Mechanisms," Journal of Systems and Software, Vol.165, 2020. [[available online](https://doi.org/10.1016/j.jss.2020.110571)]
```
@article{10.1016/j.jss.2020.110571,
author = {Higo, Yoshiki and Hayashi, Shinpei and Kusumoto, Shinji},
title = {On Tracking Java Methods with Git Mechanisms},
year = {2020},
issue_date = {July 2020},
publisher = {Elsevier Science Inc.},
address = {USA},
volume = {165},
issn = {0164-1212},
url = {https://doi.org/10.1016/j.jss.2020.110571},
doi = {10.1016/j.jss.2020.110571},
journal = {Journal of Systems and Software},
month = July,
numpages = {13},
keywords = {Mining software repositories, Source code analysis, Tracking Java methods}
}
```

## At the End

FinerGit is still under development. We mainly use macOS + JDK 25 + IntelliJ IDEA in our FinerGit development.
We rarely test FinerGit on Windows environment.

[cregit](https://github.com/cregit/cregit) and [git-stein](https://github.com/sh5i/git-stein) are other tools that convert or rewrite Git repositories.
FinerGit internally uses git-stein.

-----
-----

# <a name="Japanese"></a>FinerGit （日本語）

FinerGit は Java メソッドの変更履歴を容易に取得することを目的として開発されているコマンドラインツールです．
Git のメカニズムを利用して Java メソッドの変更履歴を取得します．
FinerGit の入力は Java ソースコードを含む Git リポジトリです．
FinerGit はそのリポジトリを，より細粒度なファイル構成をもつ別の Git リポジトリに変換します．

デフォルトでは，FinerGit の出力リポジトリは以下の特徴をもちます．

- 各 Java メソッドまたはコンストラクタが1つの `.mjava` ファイルとして抽出されている．
- 抽出された各 Java メソッドファイルの各行は1つの字句のみを含む．

オプションにより，元の Java ファイルや Java 以外のファイルを残したり，クラス，フィールド，周辺トークンのファイルを生成したりできます．


## 使い方

### 環境の確認

FinerGit は Java 25 の toolchain でビルドされます．
実行環境に JDK 25 以降がインストールされていることを確認してください．

```shell-session
$ java -version
java version "25" ...
```

変換対象の Java ソースコードのバージョンは，Java 1.4 から Java 25 まで指定できます．
入力ソースコードの Java バージョンは `-j` または `--java-version` で指定してください．
このオプションを省略した場合は Java 25 として解析します．

### FinerGit のビルド

以下のコマンドを順に実行し，`FinerGit-all.jar` をビルドしてください．

```shell-session
$ git clone https://github.com/kusumotolab/FinerGit.git
$ cd FinerGit
$ ./gradlew shadowJar
```

`FinerGit/build/libs` ディレクトリに `FinerGit-all.jar` ができていればOKです．
`gradlew` の引数として `shadowJar` ではなく `jar` や `build` を指定した場合も jar ファイルは作成されますが，
その場合の名前は `FinerGit.jar` になり，単体で実行可能な jar ファイルではありません．


### FinerGit の実行

Git リポジトリを変換するための基本コマンドは以下の通りです．

```shell-session
$ java -jar build/libs/FinerGit-all.jar create --src /path/to/repoA --des /path/to/repoB
```

ここで，`/path/to/repoA` は既存の Git リポジトリのパス，`/path/to/repoB` は生成する FinerGit リポジトリのパスを表しています．

FinerGit は変換のオプションをいくつか備えています．
オプション一覧は以下のコマンドにより確認できます．

```shell-session
$ java -jar build/libs/FinerGit-all.jar create
```

### 主なオプション

必須オプション:

- `-s`, `--src <path>`: 入力リポジトリのパス．
- `-d`, `--des <path>`: 出力リポジトリのパス．

入力とリポジトリ変換に関するオプション:

- `-j`, `--java-version <version>`: 変換対象ソースコードの Java バージョン．`1.4` から `1.8`，および `9` から `25` を指定できます．デフォルトは `25` です．
- `-o`, `--original-javafiles <true|false>`: 元の Java ファイルを出力リポジトリに含めるかどうか．デフォルトは `false` です．
- `-p`, `--otherfiles <true|false>`: Java 以外のファイルを出力リポジトリに含めるかどうか．デフォルトは `false` です．
- `--nthreads <num>`: リポジトリ書き換えに利用するスレッド数．デフォルトは利用可能なプロセッサ数から1を引いた値です．プロセッサ数が1の場合は `1` です．
- `-l`, `--log-level <level>`: ログレベル．`trace`，`debug`，`info`，`warn`，`error` を指定できます．

生成ファイルに関するオプション:

- `--method-file-generated <true|false>`: メソッドファイルを生成するかどうか．デフォルトは `true` です．
- `--class-file-generated <true|false>`: クラスファイルを生成するかどうか．デフォルトは `false` です．
- `--field-file-generated <true|false>`: フィールドファイルを生成するかどうか．デフォルトは `false` です．
- `--peripheral-file-generated <true|false>`: 生成されたクラス，メソッド，フィールドファイルの外側にある周辺トークンのファイルを生成するかどうか．デフォルトは `false` です．

字句化とファイル名に関するオプション:

- `-t`, `--tokenize <true|false>`: 生成される Java メソッドファイルおよびフィールドファイルを字句化するかどうか．デフォルトは `true` です．
- `--access-modifier-included <true|false>`: 生成されるメソッドファイル名およびフィールドファイル名にアクセス修飾子を含めるかどうか．デフォルトは `true` です．
- `--method-type-erasure-included <true|false>`: 生成されるメソッドファイル名にメソッド型パラメータを含めるかどうか．デフォルトは `true` です．
- `--return-type-included <true|false>`: 生成されるメソッドファイル名に戻り値型を含めるかどうか．デフォルトは `true` です．
- `--token-type-included <true|false>`: 字句化された各行に字句の種類を付加するかどうか．デフォルトは `false` です．
- `--method-token-included <true|false>`: 生成されるメソッドファイルにメソッド境界トークンを含めるかどうか．デフォルトは `true` です．
- `--max-file-name-length <num>`: 生成されるファイル名の最大長．`13` から `255` の範囲で指定できます．デフォルトは `255` です．
- `--hash-length <num>`: 短縮されたファイル名に付与するハッシュ値の長さ．`7` から `40` の範囲で指定できます．デフォルトは `7` です．

### FinerGit リポジトリを使って Java メソッドの変更履歴を確認する

デフォルトでは，FinerGit リポジトリには `.mjava` ファイルが含まれます．
生成オプションによっては，`.cjava`，`.fjava`，`.pjava` ファイルも含まれます．

- 拡張子が `.mjava` のファイルは，Java のメソッドまたはコンストラクタを表します．
- 拡張子が `.cjava` のファイルは，Java クラスレベルのトークンを表します．メソッド本体はメソッドトークンに置き換えられ，`--method-file-generated` が `true` の場合はメソッドファイルも生成されます．
- 拡張子が `.fjava` のファイルは，Java のフィールド宣言を表します．
- 拡張子が `.pjava` のファイルは，元の Java ファイルに含まれる周辺トークンを表します．

デフォルトでは，メソッドファイル名にはクラス名，アクセス修飾子，戻り値型，メソッド名，引数型が含まれます．
例えば `Foo#public_void_bar(int).mjava` のようなファイル名になります．
フィールドファイル名も同様に，例えば `Foo#private_String_name.fjava` のような形式になります．
生成されるファイル名が長すぎる場合，FinerGit はファイル名を短縮し，ハッシュ値を付与します．

例えば，

```shell-session
$ git log "Foo#public_void_bar(int).mjava"
```

というコマンドを入力すると，`bar(int)` メソッドに変更を加えたコミットの一覧を得ることができます．

```shell-session
$ git log --follow "Foo#public_void_bar(int).mjava"
```

というように，`--follow` オプションを利用すれば，メソッド名やそれを含むクラス名が変わっていた場合でも追跡して，コミット一覧を表示します．


## 研究での利用

研究で FinerGit を利用した場合には，以下の論文を引用してください．

Yoshiki Higo, Shinpei Hayashi, and Shinji Kusumoto, "On Tracking Java Methods with Git Mechanisms," Journal of Systems and Software, Vol.165, 2020. [[available online](https://doi.org/10.1016/j.jss.2020.110571)]
```
@article{10.1016/j.jss.2020.110571,
author = {Higo, Yoshiki and Hayashi, Shinpei and Kusumoto, Shinji},
title = {On Tracking Java Methods with Git Mechanisms},
year = {2020},
issue_date = {July 2020},
publisher = {Elsevier Science Inc.},
address = {USA},
volume = {165},
issn = {0164-1212},
url = {https://doi.org/10.1016/j.jss.2020.110571},
doi = {10.1016/j.jss.2020.110571},
journal = {Journal of Systems and Software},
month = July,
numpages = {13},
keywords = {Mining software repositories, Source code analysis, Tracking Java methods}
}
```


## 最後に

FinerGit は主に，macOS + JDK 25 + IntelliJ IDEA を用いて開発されています．
Windows 環境ではほとんど動作確認を行っていません．

Git リポジトリの変換／書換ツールとしては，他に [cregit](https://github.com/cregit/cregit) や [git-stein](https://github.com/sh5i/git-stein) があります．
FinerGit では内部で git-stein を利用しています．
