## AGENTS.md

### Language and Libraries

`./pom.xml` を参照

### Architecture

`./ARCHITECTURE.md` を参照

### Rules

`./RULES.md` を参照

### Implementation guidelines

1. `gh project list --owner shukan-app --format json` で作業中のリポジトリに紐づいているプロジェクトを取得する
2. `gh project item-list [project-number] --owner shukan-app --format json` で現在の進捗および解決すべきIssueを確認する
3. 確認したIssueをもとに、専用のブランチを作成し実装を行う
4. 実装を完了し、実装内容を報告する

ブランチ名は `GitHub Flow` をベースとし、Issueの番号を含める (`feature/#1-create-something-great` など)
テストの作成やPRの作成などは無断で行わない