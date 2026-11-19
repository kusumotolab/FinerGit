# FinerGit
[![CI](https://github.com/kusumotolab/FinerGit/actions/workflows/ci.yml/badge.svg)](https://github.com/kusumotolab/FinerGit/actions/workflows/ci.yml)

[Click here to Japanese README](#user-content-finergit-日本語)

FinerGit is a command-line tool for obtaining change histories of Java methods by using Git mechanisms.
FinerGit takes a Git repository as input and generates another Git repository whose Java source files are rewritten into finer-grained files.

By default, the generated repository has the following features.

- Each Java method or constructor declared in a top-level type or in a named member type reachable from a top-level type through member type declarations is extracted as a single `.mjava` file.
- Each line of an extracted Java method file contains only one token.
- Comments (Javadoc, block, and line comments) are kept. A multi-line comment is split into one token per line. (Exception: with `--tokenize false`, the comments inside a method or field are not kept; see the option below.)

Annotation elements appear in the enclosing token file when that file is generated.

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

Run FinerGit in a UTF-8 locale (for example `LANG=en_US.UTF-8` on Linux).
Generated file names are built from Java identifiers and type names, which may contain non-ASCII characters.
If the JVM's file-name encoding cannot represent such a name, the final checkout of the working copy fails with `InvalidPathException`.
FinerGit then still creates the repository (all objects and references are rewritten) and only prints a warning; fix the locale and run `git reset --hard` and `git clean -fd` in the output repository.

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
$ java -jar build/libs/FinerGit-all.jar create --help
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
- `-h`, `--help`: print the option list and exit.

Generated file options:

- `--method-file-generated <true|false>`: generate method files. The default is `true`.
- `--member-method-file-generated <true|false>` controls whether to include methods and constructors of named member types when method file generation is enabled. The default is `true`. Set it to `false` to extract only top-level methods and constructors.
- `--method-source-hash-enabled <true|false>`: record original method text hashes in commit trailers. Setting this option to `true` requires `--method-file-generated true`. The default is `false`.
- `--class-file-generated <true|false>`: generate class files (`.cjava`) and record files (`.rjava`). The default is `false`.
- `--field-file-generated <true|false>`: generate field files. The default is `false`.
- `--peripheral-file-generated <true|false>` controls whether to generate `.pjava` files for peripheral tokens, such as imports, outside the class, record, method, and field files. The default is `false`. The member-type exception is described below.

Token and file-name options:

- `-t`, `--tokenize <true|false>`: tokenize generated Java method and field files. The default is `true`. Class, record, and peripheral files are always tokenized; their `MethodToken[...]` and `FieldToken[...]` placeholders are generated in both modes. With `false`, each method or field file contains the source text of the declaration as printed by the parser, so the comments inside the declaration are not kept (its Javadoc is); the comments directly before the declaration and on the same line after it are still kept.
- `--access-modifier-included <true|false>`: include access modifiers in generated method and field file names. The default is `true`.
- `--method-type-erasure-included <true|false>`: include method type parameters in generated method file names. The default is `true`.
- `--return-type-included <true|false>`: include return types in generated method file names. The default is `true`.
- `--token-type-included <true|false>`: append token type names to tokenized lines. The default is `false`.
- `--method-token-included <true|false>`: include method boundary tokens in generated method files. The default is `true`.
- `--max-file-name-length <num>`: maximum generated file name length in UTF-16 code units before the `@N` suffix used to resolve collisions. The value must be between `15` and `255`, and at least `--hash-length` + 8. A shortened prefix ends at a Unicode code point boundary and can be empty when the space available is too small. Shortened member-type names retain `~.mjava` and may have the form `_hash~.mjava`. The code point boundary can change generated names and commit IDs where earlier versions split a surrogate pair, even with method source hashes disabled. The default is `255`.
- `--hash-length <num>`: length of the hash value attached to shortened file names. The value must be between `7` and `40`. The default is `7`.

### Exit Status

FinerGit returns `0` when a conversion finished successfully.
If a conversion was aborted, or if an invalid command line was given, FinerGit prints the reason and returns `1`.

```shell-session
$ java -jar build/libs/FinerGit-all.jar create --src /path/to/repoA --des /path/to/repoB || echo "conversion failed"
```

FinerGit checks the paths before it starts copying.
The output path must not exist yet or must be an empty directory, and it must not be the same as or inside the input repository.
If these conditions are not met, FinerGit exits with `1` without touching the input repository or the output path.

Note that an aborted conversion leaves an incomplete copy of the input repository in the output path.
Such a repository is not a FinerGit repository, and thus it should be removed before retrying the conversion.

### See Change Histories of Java Methods in a FinerGit Repository

By default, FinerGit repositories contain `.mjava` files.
Depending on generation options, they can also contain `.cjava`, `.rjava`, `.fjava`, and `.pjava` files.

- Extension `.mjava` means that the file represents a Java method or constructor.
- Extension `.cjava` means that the file represents Java class-level tokens. Methods declared directly in the top-level class are represented by method tokens, and method files are generated when `--method-file-generated` is `true`.
- Extension `.rjava` means that the file represents Java record-level tokens. It is generated together with `.cjava` files when `--class-file-generated` is `true`.
- Extension `.fjava` means that the file represents a Java field declaration.
- Extension `.pjava` means that the file represents peripheral tokens from the original Java file.

When class, record, or peripheral files are generated, member-type method tokens also remain in the enclosing `.cjava`, `.rjava`, or `.pjava` file. A change to a member-type method can therefore appear in both files.

With member-type extraction enabled, if JDT cannot render a Javadoc comment, the method file uses the source Javadoc with the enclosing indentation removed. This applies to methods in both top-level and member types.
With `--tokenize false`, FinerGit renders the rest of the declaration separately when possible and uses the source declaration as a fallback.

By default, method file names include the class name, access modifier, return type, method name, and parameter types.
For example, a method may be generated as `Foo#public_void_bar(int).mjava`.
A method in a named member type uses its type hierarchy with `.` between the names, as in `~Foo.Nested#public_void_bar(int)~.mjava`.
The leading `~` avoids Windows device names, and the trailing `~` before `.mjava` distinguishes member-type files from top-level method files even after shortening.
Field file names use a similar format, such as `Foo#private_String_name.fjava`.
If generated file names are too long, FinerGit shortens them and appends a hash value.

Member-type extraction is enabled by default. Repeating a conversion with the same options as before this feature can change generated paths and commit IDs. Use `--member-method-file-generated false` to keep the previous extraction scope when reproducing a dataset.

If you want to see the change history of `Foo#public_void_bar(int).mjava`, type the following command.

```shell-session
$ git log -- 'Foo#public_void_bar(int).mjava'
```

This shows the commits that changed the generated file for `bar(int)`.

The `--follow` option lets Git track the method across file renames. Here is an example for a method in a named member type.

```shell-session
$ git log --follow -- '~Foo.Nested#public_void_bar(int)~.mjava'
```

### Read Original Method Text Hashes

Enable this feature with `--method-source-hash-enabled true` and method file generation enabled.
Each rewritten commit retains the `<OriginalCommitID:…>` prefix and adds a
`FinerGit-Source-Hash-Version: 1` trailer. Each `FinerGit-Method-Source` trailer contains one JSON record.
FinerGit puts these records in the Git trailer block, which may precede a trailing comment paragraph,
a scissors section, or a legacy `Conflicts:` list.
FinerGit normalizes the line ending of a CRLF scissors marker to LF so Git recognizes it across platforms.
Whitespace-only lines immediately after the added trailers become empty lines.
Adding trailers changes rewritten commit IDs. The default keeps the original FinerGit message format.

```text
FinerGit-Source-Hash-Version: 1
FinerGit-Method-Source: {"path":"src/Foo#public_void_bar(int).mjava","sha256":"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"}
FinerGit-Method-Source: {"path":"src/Foo#public_void_old().mjava","sha256":null}
```

`path` is the generated repository-relative path, including any filename shortening or collision suffix.
For version 1, FinerGit decodes the original Java file as UTF-8, takes the source range reported by
JDT's `MethodDeclaration`, and computes SHA-256 over that text encoded as UTF-8. The range includes
attached Javadoc, annotations, and the whitespace and line endings within it. Editing a comment
outside the range can change the generated `.mjava` file without changing the method hash.
Different invalid UTF-8 byte sequences can decode to the same replacement characters and produce
the same hash.

Each commit records changes to method hashes by path relative to its first parent. Root commits
record all generated method hashes. A `sha256` value of `null` removes the method hash record for
that path. The same path can still contain an ordinary file.

The CLI uses a fixed JGit object streaming threshold of 50 MiB. If a rewritten commit with trailers
reaches that size, FinerGit stops before updating refs and reports the original commit ID and size.
If an original commit already has a `FinerGit-Source-Hash-Version` or `FinerGit-Method-Source`
trailer, FinerGit stops and reports the original commit ID and the conflicting key.
FinerGit also stops if a scissors lookalike precedes a real scissors line and Git readers can
disagree about where a trailer block ends.

Formatting can change the source hash while leaving the generated file identical. Read the records
with native Git using `#` as the comment prefix and `:` as the trailer separator, as FinerGit does.
JGit's `getFooterLines()` can omit the records when another paragraph follows the trailer block.

```shell-session
$ git show -s --format=%B HEAD | git -c 'core.commentChar=#' -c 'core.commentString=#' -c 'trailer.separators=:' interpret-trailers --no-divider --parse
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

- トップレベルの型，またはトップレベルの型からメンバー型の宣言のみをたどって到達できる名前付きメンバー型で宣言された Java のメソッドとコンストラクタが，それぞれ `.mjava` ファイルとして抽出されている．
- 抽出された各 Java メソッドファイルの各行は1つの字句のみを含む．
- コメント（Javadoc，ブロックコメント，行コメント）も保持される．複数行のコメントは1行1字句に分割される．（例外：`--tokenize false` の場合，メソッドやフィールドの内部にあるコメントは保持されない．後述のオプションの説明を参照．）

アノテーション型の要素は，それを含む型または周辺トークンのファイルを生成した場合に，そのファイルに記録されます．

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

FinerGit は UTF-8 のロケール（例えば Linux では `LANG=ja_JP.UTF-8`）で実行してください．
生成されるファイル名は Java の識別子や型名から作られるため，非 ASCII 文字を含むことがあります．
JVM のファイル名のエンコーディングでそのような名前を表現できない場合，最後に行う作業コピーのチェックアウトが `InvalidPathException` で失敗します．
この場合でもリポジトリ自体（すべてのオブジェクトと参照）は生成されており，FinerGit は警告を出すだけです．ロケールを直した上で，出力先リポジトリで `git reset --hard` と `git clean -fd` を実行してください．

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
$ java -jar build/libs/FinerGit-all.jar create --help
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
- `-h`, `--help`: オプション一覧を表示して終了します．

生成ファイルに関するオプション:

- `--method-file-generated <true|false>`: メソッドファイルを生成するかどうか．デフォルトは `true` です．
- `--member-method-file-generated <true|false>` は，メソッドファイルの生成時に名前付きメンバー型のメソッドとコンストラクタも抽出するかを指定します．デフォルトは `true` です．トップレベルの型のメソッドとコンストラクタのみを抽出する場合は `false` を指定します．
- `--method-source-hash-enabled <true|false>`: 元のメソッドテキストのハッシュをコミットのトレーラーに記録します．`true` を指定するには `--method-file-generated true` が必要です．デフォルトは `false` です．
- `--class-file-generated <true|false>`: クラスファイル（`.cjava`）とレコードファイル（`.rjava`）を生成するかどうか．デフォルトは `false` です．
- `--field-file-generated <true|false>`: フィールドファイルを生成するかどうか．デフォルトは `false` です．
- `--peripheral-file-generated <true|false>` は，import 宣言など，クラス・レコード・メソッド・フィールドの各ファイルに含まれない周辺トークンを `.pjava` に出力するかを指定します．デフォルトは `false` です．メンバー型の例外は後述します．

字句化とファイル名に関するオプション:

- `-t`, `--tokenize <true|false>`: 生成される Java メソッドファイルおよびフィールドファイルを字句化するかどうか．デフォルトは `true` です．クラスファイル・レコードファイル・周辺ファイルは常に字句化され，`MethodToken[...]` と `FieldToken[...]` の placeholder はどちらのモードでも生成されます．`false` の場合，メソッドファイルとフィールドファイルにはパーサが出力する宣言のソーステキストが入るため，宣言の内部にあるコメントは保持されません（Javadoc は保持されます）．宣言の直前のコメントと，宣言の後ろの同じ行にあるコメントは保持されます．
- `--access-modifier-included <true|false>`: 生成されるメソッドファイル名およびフィールドファイル名にアクセス修飾子を含めるかどうか．デフォルトは `true` です．
- `--method-type-erasure-included <true|false>`: 生成されるメソッドファイル名にメソッド型パラメータを含めるかどうか．デフォルトは `true` です．
- `--return-type-included <true|false>`: 生成されるメソッドファイル名に戻り値型を含めるかどうか．デフォルトは `true` です．
- `--token-type-included <true|false>`: 字句化された各行に字句の種類を付加するかどうか．デフォルトは `false` です．
- `--method-token-included <true|false>`: 生成されるメソッドファイルにメソッド境界トークンを含めるかどうか．デフォルトは `true` です．
- `--max-file-name-length <num>`: ファイル名の衝突を解消する接尾辞 `@N` を付ける前の最大長（UTF-16 コードユニット数）．`15` から `255` の範囲で，かつ `--hash-length` + 8 以上の値を指定できます．短縮時は Unicode コードポイントの境界で区切ります．使える長さによっては短縮した先頭部分が空になります．メンバー型の短縮名には末尾の `~.mjava` が残り，`_ハッシュ値~.mjava` だけになる場合があります．以前のバージョンがサロゲートペアの途中で区切っていたケースでは，メソッドハッシュの記録が無効でも，生成ファイル名とコミット ID が変わる場合があります．デフォルトは `255` です．
- `--hash-length <num>`: 短縮されたファイル名に付与するハッシュ値の長さ．`7` から `40` の範囲で指定できます．デフォルトは `7` です．

### 終了コード

FinerGit は，変換が正常に終了した場合に `0` を返します．
変換が中断された場合や，コマンドラインの指定に誤りがある場合には，その理由を出力して `1` を返します．

```shell-session
$ java -jar build/libs/FinerGit-all.jar create --src /path/to/repoA --des /path/to/repoB || echo "変換に失敗しました"
```

FinerGit はコピーを始める前にパスを検証します．
出力先は存在しないパスか空のディレクトリでなければならず，入力リポジトリと同じパスや入力リポジトリの内側を指定することはできません．
これらの条件を満たさない場合，FinerGit は入力リポジトリにも出力先にも変更を加えずに `1` を返します．

変換が中断された場合，出力先には入力リポジトリの不完全な複製が残ることに注意してください．
そのようなリポジトリは FinerGit リポジトリではないため，再実行する前に削除してください．

### FinerGit リポジトリを使って Java メソッドの変更履歴を確認する

デフォルトでは，FinerGit リポジトリには `.mjava` ファイルが含まれます．
生成オプションによっては，`.cjava`，`.rjava`，`.fjava`，`.pjava` ファイルも含まれます．

- 拡張子が `.mjava` のファイルは，Java のメソッドまたはコンストラクタを表します．
- 拡張子が `.cjava` のファイルは，Java クラスレベルのトークンを表します．トップレベルのクラスで直接宣言されたメソッドはメソッドトークンで表され，`--method-file-generated` が `true` の場合はメソッドファイルも生成されます．
- 拡張子が `.rjava` のファイルは，Java レコードレベルのトークンを表します．`--class-file-generated` が `true` の場合に `.cjava` ファイルとともに生成されます．
- 拡張子が `.fjava` のファイルは，Java のフィールド宣言を表します．
- 拡張子が `.pjava` のファイルは，元の Java ファイルに含まれる周辺トークンを表します．

クラス，レコード，周辺トークンのファイルを生成する場合，メンバー型のメソッドのトークンは，その `.mjava` と，宣言を含む `.cjava`，`.rjava` または `.pjava` の両方に記録されます．そのため，1つのメソッドの変更が2つのファイルに現れる場合があります．

メンバー型の抽出を有効にしていて JDT が Javadoc を出力できない場合は，元ソースの Javadoc から外側の宣言による字下げを取り除いてメソッドファイルに出力します．トップレベル型とメンバー型のどちらのメソッドにも適用します．
`--tokenize false` の場合は，可能であれば宣言の残りを個別に出力し，それもできない場合は元ソースの宣言を使います．

デフォルトでは，メソッドファイル名にはクラス名，アクセス修飾子，戻り値型，メソッド名，引数型が含まれます．
例えば `Foo#public_void_bar(int).mjava` のようなファイル名になります．
名前付きメンバー型のメソッドでは，`~Foo.Nested#public_void_bar(int)~.mjava` のように，外側から順に型名を `.` でつなぎます．
先頭の `~` は Windows のデバイス予約名を避けるために付けます．拡張子の直前の `~` は，ファイル名を短縮した場合にもメンバー型のメソッドをトップレベル型のメソッドと区別するために残します．
フィールドファイル名も同様に，例えば `Foo#private_String_name.fjava` のような形式になります．
生成されるファイル名が長すぎる場合，FinerGit はファイル名を短縮し，ハッシュ値を付与します．

メンバー型の抽出はデフォルトで有効です．この機能が追加される前と同じコマンドで変換しても，生成されるパスやコミット ID が変わる場合があります．過去のデータセットを再現する際に従来の抽出範囲を使うには，`--member-method-file-generated false` を指定してください．

例えば，

```shell-session
$ git log -- 'Foo#public_void_bar(int).mjava'
```

というコマンドを入力すると，`bar(int)` に対応する生成ファイルを変更したコミットの一覧を得ることができます．

```shell-session
$ git log --follow -- '~Foo.Nested#public_void_bar(int)~.mjava'
```

というように，名前付きメンバー型のメソッドにも `--follow` を使うと，ファイル名が変わった前後の履歴を追跡できます．

### 元のメソッドテキストのハッシュを読む

メソッドファイルの生成を有効にしたうえで `--method-source-hash-enabled true` を指定します．
変換後のコミットには，件名の `<OriginalCommitID:…>` に加え，`FinerGit-Source-Hash-Version: 1` が付きます．
`FinerGit-Method-Source` の各行に JSON レコードを1件記録します．トレーラーの後ろに，元からあった
コメント段落，scissors 行以降のテキスト，旧形式の `Conflicts:` 一覧が続く場合もあります．
Git が各プラットフォームで同じように認識できるよう，scissors 行の改行が CRLF の場合は LF に変換します．
追加したトレーラーの直後にある空白文字だけの行は，空行にします．
トレーラーを付けると，変換後のコミット ID が変わります．デフォルトでは従来のメッセージ形式を使います．

```text
FinerGit-Source-Hash-Version: 1
FinerGit-Method-Source: {"path":"src/Foo#public_void_bar(int).mjava","sha256":"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"}
FinerGit-Method-Source: {"path":"src/Foo#public_void_old().mjava","sha256":null}
```

`path` は短縮や衝突時の接尾辞を含む，生成先リポジトリ内のパスです．
バージョン 1 では，元の Java ファイルを UTF-8 としてデコードし，JDT の `MethodDeclaration` が示す
宣言範囲のテキストを UTF-8 に再エンコードして SHA-256 を計算します．宣言に付属する Javadoc と
アノテーション，範囲内の空白と改行を含みます．範囲外のコメントを変更すると，メソッドハッシュが
同じでも，生成される `.mjava` ファイルが変わる場合があります．異なる不正な UTF-8 バイト列が
同じ置換文字列にデコードされ，同じハッシュになる場合もあります．

各コミットは，第1親と比較したパスごとのメソッドハッシュの差分を記録します．ルートコミットは
生成された全メソッドのハッシュを記録します．`sha256` の値が `null` の場合は，そのパスの
メソッドハッシュ記録を削除します．同じパスに通常ファイルが残る場合もあります．

CLI では JGit のオブジェクトのストリーミング閾値を 50 MiB に固定しています．トレーラー付きの変換後コミットが
このサイズに達すると，FinerGit は参照を更新する前に変換を中止し，元のコミット ID とサイズを報告します．
元のコミットに `FinerGit-Source-Hash-Version` または `FinerGit-Method-Source` のトレーラーがある場合も，
変換を中止し，元のコミット ID と該当するキーを報告します．
scissors に似た行が実際の scissors 行に先行し，Git によってトレーラーの終端の判定が異なる場合も変換を中止します．

整形によって元テキストのハッシュが変わり，生成ファイルが同一になる場合があります．その変更も確認するには，
FinerGit と同じく `#` をコメント接頭辞，`:` を区切り文字として，ネイティブ Git でレコードを読み取ります．
JGit の `getFooterLines()` では，トレーラーブロックの後ろに別の段落がある場合，レコードが抜け落ちることがあります．

```shell-session
$ git show -s --format=%B HEAD | git -c 'core.commentChar=#' -c 'core.commentString=#' -c 'trailer.separators=:' interpret-trailers --no-divider --parse
```

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
