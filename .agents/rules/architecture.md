## ドメイン層

### エンティティ

- `dev.shoheiyamagiwa.shukan.domain.entity` 下に配置する

### Value Object

- `dev.shoheiyamagiwa.shukan.domain.vo` 下に配置する

## アプリケーション層

### サービス

- `dev.shoheiyamagiwa.shukan.service` 下に配置する

## インフラストラクチャ層

### リポジトリ

- `dev.shoheiyamagiwa.shukan.infra.repository` 下に配置する
- 契約のためのインターフェース (`IUserRepository` など) は **作成せず**, サービスクラスが具象クラスを直接受け取る形にする
- データの変換には，後述の `Mapper` を使用する

### DTO

- `dev.shoheiyamagiwa.shukan.infra.dto` 下に配置する

### Mapper

- `dev.shoheiyamagiwa.shukan.infra.mapper` 下に配置する
- `DB` と `エンティティ` の相互変換を担当する

## プレゼンテーション層

### Controller

- `dev.shoheiyamagiwa.shukan.presentation.controller` 下に配置する
- データの変換には，後述の `Mapper` を使用する

### DTO

- `dev.shoheiyamagiwa.shukan.presentation.dto` 下に配置する
- HTTP の `Request` / `Response` ごとにそれぞれ作成する

### Mapper

- `dev.shoheiyamagiwa.shukan.presentation.mapper` 下に配置する
- `DTO` と `エンティティ` の相互変換を担当する