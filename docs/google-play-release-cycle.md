# Google Play Release Cycle

This file is the single checklist for shipping KaraokeMemo to Google Play.

## How To Use

- Create one release branch for this cycle, for example `codex/release-google-play-initial`.
- Update this checklist in Git as work progresses.
- Keep code changes, store asset preparation, and Play Console work aligned with the same branch or PR notes.
- When an item is blocked by a Play Console review, leave the box unchecked and add a short note under the item.

## Release Goal

- The app is ready for Google Play review and submission.
- App behavior, store listing, privacy policy, Data safety, ads declaration, and billing setup are consistent.
- A signed release AAB has been uploaded to Play Console.

## Current Project-Specific Gaps

- [ ] Decide whether the first public release includes ads.
  - [`app/build.gradle.kts`](../app/build.gradle.kts) currently sets `ADS_ENABLED=false` in `release`.
  - The release `admob_app_id` still points to the Google sample ID and must not be shipped as a real ads setup.
- [ ] Publish a public privacy policy URL.
  - [`app/src/main/res/raw/privacy_policy.txt`](../app/src/main/res/raw/privacy_policy.txt) exists, but Play Console requires a public, non-PDF URL.
- [ ] Create the Play Billing one-time product `karamemo_pro`.
  - [`app/build.gradle.kts`](../app/build.gradle.kts) expects `PRO_PRODUCT_ID="karamemo_pro"`.
- [ ] Produce a signed release AAB.
  - The current local outputs only include a debug APK build.

## 1. Repo And Release Config

- [ ] Finalize the release behavior in [`app/build.gradle.kts`](../app/build.gradle.kts).
- [ ] Set real AdMob IDs for release or remove ad setup from the first release plan.
- [ ] Confirm `MOCK_BILLING_ENABLED=false` for release.
- [ ] Review app text related to Free / Pro, ads, privacy, and terms.
  - [`app/src/main/res/values/strings.xml`](../app/src/main/res/values/strings.xml)
  - [`app/src/main/res/raw/privacy_policy.txt`](../app/src/main/res/raw/privacy_policy.txt)
  - [`app/src/main/res/raw/terms_of_use.txt`](../app/src/main/res/raw/terms_of_use.txt)
- [ ] Bump `versionCode` and `versionName` in [`app/build.gradle.kts`](../app/build.gradle.kts).
- [ ] Confirm no test IDs, debug-only behavior, or secrets remain in release artifacts.
- [ ] Build a signed release AAB.
- [ ] Smoke test the release build on at least one physical device.

## 2. Public Docs And Store Assets

- [ ] Publish the privacy policy at a public URL.
- [ ] Prepare the store listing copy.
  - App name
  - Short description
  - Full description
- [ ] Prepare localized store copy if shipping both Japanese and English.
- [ ] Capture store screenshots from the release build.
- [ ] Prepare the app icon and feature graphic if Play Console requests them for the chosen surfaces.
- [ ] Prepare support contact details.
  - Support email is required.
  - Website is strongly recommended.
- [ ] Draft release notes for the first rollout.

## 3. Play Console Setup

- [ ] Create the app in Play Console with the correct package name `com.taigatkd.karamemo`.
- [ ] Enable Play App Signing.
- [ ] Fill in the main store listing.
- [ ] Add the support contact details.
- [ ] Complete `App content` declarations.
  - Privacy policy
  - Ads
  - App access
  - Target audience and content
  - Content rating
  - Data safety
- [ ] Confirm country or region availability.
- [ ] Create the in-app product `karamemo_pro` as a one-time product.
- [ ] Upload the signed AAB to `Internal testing`.

## 4. Testing And Production Readiness

- [ ] Verify first launch, song add/edit/delete, playlist, and settings flows from the release build.
- [ ] Verify local-only data behavior matches the privacy policy.
- [ ] Verify billing purchase flow on Play test accounts.
- [ ] Verify restore purchases flow on Play test accounts.
- [ ] Verify ad behavior matches the final release plan.
- [ ] Review Play pre-launch report results and fix any blocking issues.
- [ ] If this is a personal developer account created after 2023-11-13, complete closed testing requirements.
  - At least 12 testers
  - Opted in continuously for at least 14 days
  - Apply for production access after the requirement is met

## 5. Production Submission

- [ ] Create the production release in Play Console.
- [ ] Upload the signed AAB and add release notes.
- [ ] Review the `Publishing overview` page for `Changes not yet sent for review`.
- [ ] Submit all required changes for review.
- [ ] Publish with standard publishing or managed publishing after approval.

## 6. Post-Launch Follow-Up

- [ ] Install the production version from Google Play and smoke test it.
- [ ] Confirm the listing, screenshots, and privacy policy link render correctly.
- [ ] Confirm the `karamemo_pro` product is purchasable for eligible users.
- [ ] Record any launch issues and create the next follow-up task in Git.

## Suggested Git Checkpoints

- [ ] Commit or PR checkpoint 1: release config and policy text are finalized.
- [ ] Commit or PR checkpoint 2: store assets and public documents are ready.
- [ ] Commit or PR checkpoint 3: signed AAB and release smoke test are complete.
- [ ] Commit or PR checkpoint 4: Play Console submission is complete.

## Done Definition

- [ ] Release AAB uploaded to Play Console.
- [ ] All required Play Console declarations completed.
- [ ] Testing requirements completed or confirmed not applicable.
- [ ] Production release submitted or published.
