# KaraokeMemo Android再実装 ドキュメント一覧

設計書は機能ごとに `docs/design/` 配下へ分割した。

## 参照順

1. [全体概要](./design/00-overview.md)
2. [ドメインモデルとデータ設計](./design/01-domain-and-data.md)
3. [曲機能設計](./design/02-song-feature.md)
4. [アーティスト機能設計](./design/03-artist-feature.md)
5. [プレイリスト機能設計](./design/04-playlist-feature.md)
6. [設定・テーマ・広告設計](./design/05-settings-theme-ads.md)
7. [状態管理とアーキテクチャ設計](./design/06-state-and-architecture.md)
8. [非機能要件とテスト方針](./design/07-nonfunctional-and-test.md)

## ドキュメント分割方針

- 全体像と導線は `00-overview.md`
- 永続化対象とモデルは `01-domain-and-data.md`
- ユーザー操作に直結する機能は `02` から `05`
- 実装基盤は `06-state-and-architecture.md`
- 品質基準と互換注意点は `07-nonfunctional-and-test.md`

## 利用目的

この分割構成は、以下の用途を想定している。

- Android 実装前の合意形成
- 画面単位の実装着手
- ViewModel / Repository / Room 設計の切り出し
- テスト観点の早期整理

