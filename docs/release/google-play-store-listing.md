# Google Play Store Listing Draft

This file collects the store text and asset planning for issue `#3`.

## App Identity

- Product name (EN): `KaraokeMemo`
- Product name (JA): `カラオケメモ`
- Package name: `com.taigatkd.karamemo`

## App Summary

- Core positioning:
  - A local-first karaoke notebook for managing songs, playlists, keys, scores, and practice notes.
- Main features:
  - Save songs with artist, title, key, memo, favorite flag, and score
  - Organize songs by playlists
  - Browse by artist, song, and playlist tabs
  - Search, sort, and pick random songs
  - Save DAM / JOYSOUND karaoke machine settings
  - Free plan up to 75 songs
  - One-time Pro upgrade for unlimited songs and an ad-free experience

## Store Copy

### English

- App name:
  - `KaraokeMemo`
- Short description:
  - `Track karaoke songs, keys, scores, notes, and playlists in one local app.`
- Full description:
  - `KaraokeMemo is a local-first notebook for people who want to keep their karaoke library organized.`
  - `Save each song with the artist name, song title, key, memo, favorite flag, and score so your practice notes stay in one place.`
  - `Organize your library with playlists, browse by artist or song, search quickly, sort the list your way, and use random picks when you want help choosing the next song.`
  - `You can also keep separate karaoke machine settings for DAM and JOYSOUND, which makes it easier to remember the balance you like for BGM, mic, echo, and music.`
  - `Free plan:`
  - `- Save up to 75 songs`
  - `- Ads may be shown`
  - `Pro plan:`
  - `- One-time purchase`
  - `- Unlimited songs`
  - `- Ad-free experience`
  - `KaraokeMemo is designed primarily for on-device storage. Your karaoke library stays on your device as part of normal app use.`

### Japanese

- App name:
  - `カラオケメモ`
- Short description:
  - `曲・キー・点数・メモ・プレイリストをまとめて管理できるカラオケ用メモアプリ`
- Full description:
  - `カラオケメモは、持ち曲や練習メモを端末内でまとめて管理できるローカル保存中心のアプリです。`
  - `曲ごとにアーティスト名、曲名、キー、メモ、お気に入り、採点結果を保存できるので、練習内容や次に歌いたい曲を一か所で整理できます。`
  - `アーティスト一覧、曲一覧、プレイリスト一覧で切り替えて見られるほか、検索、並び替え、ランダム選曲にも対応しています。`
  - `さらに、DAM と JOYSOUND のカラオケ機種設定を分けて保存できるため、BGM、マイク、エコー、Music の好みの値をメモしておけます。`
  - `無料プラン:`
  - `- 75 曲まで保存可能`
  - `- 広告が表示される場合があります`
  - `Pro プラン:`
  - `- 買い切り課金`
  - `- 曲数無制限`
  - `- 広告なし`
  - `通常利用において、登録した持ち曲データは開発者のサーバーへ送信されず、主に端末内へ保存されます。`

## Release Notes Draft

### English

- `Initial release of KaraokeMemo`
- `- Manage songs, playlists, keys, notes, favorites, and scores`
- `- Search, sort, and random pick support`
- `- DAM / JOYSOUND setting memo support`
- `- One-time Pro upgrade for unlimited songs and an ad-free experience`

### Japanese

- `KaraokeMemo 初回リリース`
- `- 曲、プレイリスト、キー、メモ、お気に入り、採点結果を管理`
- `- 検索、並び替え、ランダム選曲に対応`
- `- DAM / JOYSOUND の設定メモに対応`
- `- 買い切りの Pro プランで曲数無制限・広告なし`

## Asset Shot List

Use the release build for screenshots. Capture the same story in both Japanese and English if localized assets are prepared.

### Recommended screenshot set

1. Song list
   - Show a populated list with title, artist, key tags, score tags, and favorite state.
   - Keep the search and sort actions visible.
2. Song editor
   - Open the sheet with artist, song title, key, memo, score, favorite, and playlist fields visible.
3. Artist tab
   - Show pinned artists and song counts to communicate library browsing.
4. Playlist tab
   - Show multiple playlists and at least one expanded playlist with songs inside.
5. Settings / Pro page
   - Show the Free / Pro plan card and the machine settings or Pro details view.

### Recommended sample data for screenshots

- Artists:
  - `Aster`
  - `Nova June`
  - `North Harbor`
  - `Blue Harbor`
- Songs:
  - `Skyline`
  - `Paper Moon`
  - `Silver Line`
  - `Afterglow`
- Playlist examples:
  - `Favorites`
  - `Practice Set`
  - `High Notes`

## Feature Graphic Brief

- Visual goal:
  - Make the app feel like a personal karaoke performance notebook, not a generic database tool.
- Should emphasize:
  - song tracking
  - practice notes
  - playlist organization
  - a music-oriented, polished Android look
- Avoid:
  - fake device frames that do not match the real UI
  - misleading claims about cloud sync or social features
  - references to features not implemented in the app

## Metadata Still Needed Manually

- Support email address
- Optional support website URL
- Final screenshots from a real release build
- Final feature graphic image, if used
- Public URL that will host the privacy policy page
