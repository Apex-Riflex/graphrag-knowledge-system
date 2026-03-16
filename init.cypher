// ノードの作成
CREATE (:Department {name: '営業部', budget: 5000})
CREATE (:Department {name: '開発部', budget: 8000})

CREATE (:Employee {name: '田中太郎', age: 35, role: 'マネージャー'})
CREATE (:Employee {name: '佐藤花子', age: 28, role: 'エンジニア'})
CREATE (:Employee {name: '鈴木一郎', age: 42, role: '部長'})
CREATE (:Employee {name: '山田美咲', age: 31, role: 'エンジニア'})

CREATE (:Skill {name: 'Java'})
CREATE (:Skill {name: 'PostgreSQL'})
CREATE (:Skill {name: 'Neo4j'})
CREATE (:Skill {name: 'Python'})

// 所属
MATCH (e:Employee {name: '田中太郎'})
MATCH (d:Department {name: '営業部'})
CREATE (e)-[:BELONGS_TO]->(d)

MATCH (e:Employee {name: '佐藤花子'})
MATCH (d:Department {name: '開発部'})
CREATE (e)-[:BELONGS_TO]->(d)

MATCH (e:Employee {name: '鈴木一郎'})
MATCH (d:Department {name: '開発部'})
CREATE (e)-[:BELONGS_TO]->(d)

MATCH (e:Employee {name: '山田美咲'})
MATCH (d:Department {name: '開発部'})
CREATE (e)-[:BELONGS_TO]->(d)

// 上司・部下
MATCH (e:Employee {name: '佐藤花子'})
MATCH (m:Employee {name: '鈴木一郎'})
CREATE (e)-[:REPORTS_TO]->(m)

MATCH (e:Employee {name: '山田美咲'})
MATCH (m:Employee {name: '鈴木一郎'})
CREATE (e)-[:REPORTS_TO]->(m)

// スキル
MATCH (e:Employee {name: '佐藤花子'})
MATCH (s:Skill {name: 'Java'})
CREATE (e)-[:KNOWS]->(s)

MATCH (e:Employee {name: '佐藤花子'})
MATCH (s:Skill {name: 'Neo4j'})
CREATE (e)-[:KNOWS]->(s)

MATCH (e:Employee {name: '山田美咲'})
MATCH (s:Skill {name: 'Python'})
CREATE (e)-[:KNOWS]->(s)

MATCH (e:Employee {name: '山田美咲'})
MATCH (s:Skill {name: 'PostgreSQL'})
CREATE (e)-[:KNOWS]->(s)

MATCH (e:Employee {name: '田中太郎'})
MATCH (s:Skill {name: 'Java'})
CREATE (e)-[:KNOWS]->(s)