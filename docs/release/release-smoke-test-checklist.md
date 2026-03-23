# Release Smoke Test Checklist

Use this checklist on at least one physical Android device before treating issue `#2` as complete.

## Install And Launch

- Install the signed release build from Internal testing or an equivalent release artifact.
- Confirm the app launches without crashing.
- Confirm the first screen renders with song list UI and bottom navigation.

## Core Song Flow

- Add a new song with artist, title, key, memo, favorite, and score.
- Edit the same song and confirm all updated values are saved.
- Delete the song and confirm it disappears from the list.

## Playlist And Browse Flow

- Create a playlist.
- Add at least one song to the playlist.
- Remove a song from the playlist and confirm the list updates.
- Open the artist tab and confirm artists and song counts render correctly.

## Settings And Local Data

- Open karaoke machine settings and save at least one DAM value and one JOYSOUND value.
- Reopen the settings screen and confirm the saved values persist.
- Open Privacy Policy and Terms of Use from settings and confirm both pages render.

## Monetization And Ads

- Confirm the Pro plan screen shows `JPY 380`.
- Confirm the free-plan song limit is `75`.
- Confirm a banner ad loads on a free-plan screen.
- Confirm an interstitial ad can load and show in a real release flow.

## Final Checks

- Kill and relaunch the app, then confirm previously entered local data still exists.
- Confirm there are no debug-only controls visible in release.
- Record any crash, layout break, or policy mismatch before uploading to broader testing.
