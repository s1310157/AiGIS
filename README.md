# AiGIS

## ビルド方法

* Jarファイルのみ作成

```
make.sh
```

* ターゲット指定

```
make.sh -target mac
```

|ターゲット|概要|
|:--|:--:|
|mac|macOS向けアプリ作成<br>(10.12.1で確認)|
|win|Windows向けアプリ作成<br>(10で確認)|
|linux|Linux向けアプリ作成<br>(ubuntu16.04で確認)|
|all|全てのアプリ作成|

* ビルド後にoutフォルダ内に成果物が格納される。詳細は下記フォルダ構成を参照。


## 注釈

* eclipseのプロジェクトファイルにもなっているので、インポートして実行可能です。
* JRE,JOGL共に64bit向けになっています。 libs,templateフォルダ内を置き換える事で32bit向けにする事も可能です。
* 同様にJRE,JOGLのバージョンを変える事も可能です。


## フォルダ構成

||||概要|
|:--|:--|:--|:--:|
|libs|||ライブラリフォルダ<br>ここのファイルを入れ替えればJOGLのバージョンなどを変更可能|
||gluegen-rt.jar||GlueGen(JOGL2.4.0)|
||jogl-all.jar||JOGL(2.4.0)|
||vecmath.jar||vecmath|
||linux||Linux用JOGLライブラリ<br>(linux-amd64)|
||mac||Mac用JOGLライブラリ<br>(macosx-universal)|
||win||Win用JOGLライブラリ<br>(windows-amd64)|
|out|||出力フォルダ<br>make.shを実行すると作成される|
||jar|||
|||AiGIS.jar|アプリケーションJAR|
|||AiGIS.sh|実行用スクリプト|
|||libs|ライブラリ|
||linux|||
|||AiGIS.desktop|デスクトップアイコン<br>ダブルクリックで実行可能、初回起動でアイコン画像作成|
|||AiGIS.sh|実行スクリプト|
|||app|アプリケーション構成フォルダ|
||mac|||
|||AIGIS.app|アプリケーション実行ファイル|
||win|||
|||AiGIS.exe|アプリケーション実行ファイル|
|||app|アプリケーション構成フォルダ|
|src|||ソースフォルダ|
|template|||テンプレートフォルダ<br>ここのファイルを入れ替えればJREのバージョンなどを変更可能|
||Jar|||
|||AiGIS.sh|起動スクリプト|
||Linux|||
|||AiGIS.desktop|デスクトップアイコン|
|||AiGIS.sh|起動スクリプト|
|||icon|アイコンファイル|
|||jre|JRE-64bit-8.0|
||Mac|||
|||AIGIS.app|実行ファイルテンプレート|
|||jre|JRE-64bit-8.0|
||Win|||
|||AiGIS|VisualStudioのプロジェクト<br>VS2015で確認|
|||jre|JRE-64bit-8.0|
||meta||Jarに格納されるMETA-INFフォルダ|
|make.sh|||ビルドスクリプト|
|README.md|||説明|
