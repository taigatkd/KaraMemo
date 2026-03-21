# KaraokeMemo Android Design Docs

The design documents for this project are split under `docs/design/`.

## Index
1. [Overview](./design/00-overview.md)
2. [Domain And Data](./design/01-domain-and-data.md)
3. [Song Feature](./design/02-song-feature.md)
4. [Artist Feature](./design/03-artist-feature.md)
5. [Playlist Feature](./design/04-playlist-feature.md)
6. [Settings Theme Ads](./design/05-settings-theme-ads.md)
7. [State And Architecture](./design/06-state-and-architecture.md)
8. [Nonfunctional And Test](./design/07-nonfunctional-and-test.md)
9. [UI Visual And Localization](./design/08-ui-visual-and-localization.md)
10. [Monetization](./design/09-monetization.md)

## Reading Order
- Start with `00-overview.md` for scope and screen map.
- Read `01-domain-and-data.md` for persistent data and business rules.
- Read `02` through `05` for feature-specific behavior.
- Read `06-state-and-architecture.md` for app structure.
- Read `07-nonfunctional-and-test.md` for quality constraints.
- Read `08-ui-visual-and-localization.md` for the current visual language, localization policy, and accessibility-oriented UI rules.
- Read `09-monetization.md` for ad cadence, persistence, and natural-break rules.

## Current Focus
The latest pass adds a dedicated design document for:
- a music-inspired Compose visual language
- explicit, labeled actions instead of ambiguous icon-only buttons
- Android-native localization using `values/` and `values-ja/`
- bottom sheet behavior that opens fully without stopping halfway
- left-aligned titles for top-level screens
- a natural-break monetization policy tied to successful song additions
- a `Free / Pro` plan with a 100-song free limit and a `JPY 480` one-time upgrade
- Google Play Billing integration plus a debug-only mock purchase path
