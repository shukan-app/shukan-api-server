テストを作成する対象は以下の通り

- Entity の各メソッド
- Value Object の各メソッド
- Mapper の各メソッド
- Service の各メソッド (Testcontainersで立ち上げたテスト用のDBを用いる)
- ArchUnit のテスト

コードレビューと保守性の観点から，テストは代表的なケースのみを実装する