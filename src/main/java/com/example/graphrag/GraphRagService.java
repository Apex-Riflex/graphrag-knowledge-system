package com.example.graphrag;

import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GraphRagService {

    private final Neo4jClient neo4jClient;

    @Value("${openai.api-key}")
    private String openAiApiKey;


    public String ask(String question){

        // 質問からキーワードを抽出する
        List<String> keywords = extractKeywords(question);

        // キーワードに関連するグラフ情報だけを抽出する
        String graphContext = keywords.isEmpty() ? fetchAllContext() : fetchContextByKeywords(keywords);

        // 2. LLMに質問＋抽出したグラフ情報を渡す
        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName("gpt-4o-mini")
                .build();

        String prompt = """
                あなたは社内情報に詳しいアシスタントです。
                以下のナレッジグラフの情報をもとに質問に答えてください。
    
                ルール：
                - グラフ情報に含まれている内容だけを使って答えてください
                - 情報が見つからない場合は「情報が見つかりませんでした」と答えてください
                - 回答は日本語で簡潔にまとめてください
    
                【グラフ情報】
                %s
    
                【質問】
                %s
                """.formatted(graphContext, question);

        return model.generate(prompt);

    }

    public String fetchContextByKeywords(List<String> keywords){
        StringBuilder sb = new StringBuilder();

        for (String keyword : keywords){
            Collection<Map<String, Object>> results = neo4jClient.query(
                    """
                            MATCH (n {name: $keyword})
                            OPTIONAL MATCH(n)-[r1]->(m)
                            OPTIONAL MATCH(m)-[r2]->(l)
                            RETURN n.name AS 起点,
                                type(r1) AS 関係1,
                                m.name AS 中間,
                                m.budget AS 予算,
                                type(r2) AS 関係2,
                                l.name AS 終点
                            """
            ).bind(keyword).to("keyword")
                    .fetch().all();

            for (Map<String, Object> row : results){
                if (row.get("起点") != null) {
                    sb.append(row.get("起点"));
                }
                if (row.get("関係1") != null) {
                    sb.append(" -[").append(row.get("関係1")).append("]-> ");
                }
                if (row.get("中間") != null) {
                    sb.append(row.get("中間"));
                }
                if (row.get("予算") != null) {
                    sb.append("（予算:").append(row.get("予算")).append("万円）");
                }
                if (row.get("関係2") != null) {
                    sb.append(" -[").append(row.get("関係2")).append("]-> ");
                }
                if (row.get("終点") != null) {
                    sb.append(row.get("終点"));
                }
                sb.append("\n");
                }

        }

        return sb.toString();





    }

    public String fetchAllContext(){

        // 社員の所属・上司・スキル情報を全部取得
        Collection<Map<String, Object>> results = neo4jClient.query(
                """
                        MATCH (e:Employee)
                        OPTIONAL MATCH (e)-[:BELONGS_TO]->(d:Department)
                        OPTIONAL MATCH (e)-[:REPORTS_TO]->(m:Employee)
                        OPTIONAL MATCH (e)-[:KNOWS]->(s:Skill)
                        RETURN e.name AS 社員名,
                               e.role AS 役職,
                               d.name AS 部門,
                               d.budget AS 部門予算,
                               m.name AS 上司,
                               collect(s.name) AS スキル
                        """
        )
                .fetch().all();

        // グラフ情報を文字列に変換
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> row: results) {
            sb.append("社員名: ").append(row.get("社員名")).append("\n");
            sb.append("役職: ").append(row.get("役職")).append("\n");
            sb.append("部門: ").append(row.get("部門")).append("\n");
            sb.append("部門予算: ").append(row.get("部門予算")).append("万円\n");
            sb.append("上司: ").append(row.get("上司")).append("\n");
            sb.append("スキル: ").append(row.get("スキル")).append("\n\n");
        }
        return sb.toString();
    }

    public List<String> extractKeywords(String question){
        // 名前が入っているノードを一旦すべて取り出す。
        Collection<Map<String, Object>> names = neo4jClient.query(
                """
                        MATCH (n)
                        WHERE n.name IS NOT NULL
                        RETURN n.name AS name
                        """
        ).fetch().all();

        List<String> keywords = new ArrayList<>();

        // ノードの名前が質問文に含まれているかを確認。
        for (Map<String, Object> row: names){
            String name = (String)row.get("name");
            if (question.contains(name)){
                keywords.add(name);
            }
        }
        return keywords;
    }

}
