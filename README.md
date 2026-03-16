# GraphRAG社内ナレッジシステム

## 概要
Neo4jのグラフDBとOpenAI APIを組み合わせたGraphRAGシステムです。
社内の組織情報・スキル情報をナレッジグラフとして管理し、
自然言語で質問するとJava（Spring Boot）がNeo4jでグラフの関係性をたどり、
取得した情報をOpenAIに渡して自然言語にて回答を生成します。

## 技術スタック
- Java 17
- Spring Boot 3.5.1
- Neo4j 5
- LangChain4j 0.36.2
- OpenAI API（gpt-4o-mini）
- Docker

## システム構成
```
ユーザー → Spring Boot API → Neo4j（グラフDB）
                          → OpenAI API
```

## 工夫した点
- 質問からキーワードを自動抽出し、関連情報だけをNeo4jから取得し、安全性を確保
- 全件取得ではなく必要最小限の情報だけをOpenAIに渡すことで精度向上とコスト削減を実現
- OPTIONAL MATCHで2段階のグラフ探索を実装
- グラフに存在しない情報は「情報が見つかりません」と回答する設計により
  LLMのハルシネーション（嘘の回答）を防止し情報の正確性を担保

## 前提条件
- Java 17以上
- Docker Desktop
- OpenAI APIキー

## 起動方法

### 0. リポジトリをクローンする
git clone https://github.com/あなたのユーザー名/graphrag-knowledge-system.git
cd graphrag-knowledge-system

### 1. Neo4jをDockerで起動する
docker run --name neo4j-dev -p 7474:7474 -p 7687:7687 \
-e NEO4J_AUTH=neo4j/password1234 -d neo4j:5

### 2. ナレッジグラフのデータを投入する
ブラウザで http://localhost:7474 にアクセスし、
cypher/init.cypher の内容を実行してください。

### 3. 環境変数を設定する
NEO4J_PASSWORD=your_password
OPENAI_API_KEY=your_api_key

### 4. アプリを起動する
mvn spring-boot:run
（IDE上からの起動でも大丈夫です）

### 5. 動作確認
GET http://localhost:8080/graphrag/ask?question=佐藤花子のスキルを教えてください

## API仕様
GET /graphrag/ask?question=質問内容
