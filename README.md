# Blogtrack -- はてなブログ内の引用可視化ツール

Blogtrackを使うことで、はてなブログ内の記事の引用関係を可視化できます。

## Components

Blogtrackは3(4)つのコンポーネントで構成されています。

### Server

プロジェクトルートがServerです。Serverは後述する**Widget**コンポーネントからのAPIリクエストを受信し、記事の引用情報を返却します。このためにServerはNeo4jサーバと通信します。また、Serverは後述する**Notifier**からの通知を受信して、新たな引用情報の更新を行います。

ServerはScalaで記述されています。

### Widget

`widget/`以下がWidgetコンポーネントです。WidgetはJavascriptとしてブログ内にタグとともに埋め込まれ、Serverコンポーネントから受信した引用情報をDOM上にレンダリングします。

WidgetはScala.jsで記述され、UIライブラリとしてLaminarを利用しています。CSSフレームワークとしてFomanticUIを利用します。

### Notifier

(WIP) Notifierは定期的にブログ内の新規エントリをチェックし、更新を検知するとServerコンポーネントに通報することで情報更新を促します。

### Protocol

`protocol/` 以下がProtocolです。ProtocolはServer-Widget-Notifier間のインターフェイスを定義するためのSmithyによる記述層です。コンポーネント間インターフェイスはSmithyにより定義され、これをSmithy4sがScalaコードに展開します。
