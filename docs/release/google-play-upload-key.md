# Google Play Upload Key Setup

This document covers the local setup needed before building the first signed release AAB.

## 1. Create the upload key

Use the JDK `keytool` command on your machine.

```powershell
keytool -genkeypair `
  -v `
  -keystore release-upload-key.jks `
  -alias karamemo-upload `
  -keyalg RSA `
  -keysize 4096 `
  -validity 10000
```

- Store the generated `release-upload-key.jks` outside the repository if possible.
- Keep the keystore password and key password in a password manager.
- Do not commit the keystore file or real passwords.

## 2. Add local signing settings

Copy `keystore.properties.example` to `keystore.properties` at the repository root, then fill in your local values.

```properties
storeFile=C:/secure-path/release-upload-key.jks
storePassword=your-store-password
keyAlias=karamemo-upload
keyPassword=your-key-password
```

`keystore.properties` is gitignored and is required for release tasks.

## 3. Add AdMob IDs for the ads-enabled release

Set these values in your local environment or in `~/.gradle/gradle.properties`.

```properties
KARAMEMO_RELEASE_ADS_ENABLED=true
KARAMEMO_ADMOB_APP_ID=ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy
KARAMEMO_BANNER_AD_UNIT_ID=ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy
KARAMEMO_INTERSTITIAL_AD_UNIT_ID=ca-app-pub-xxxxxxxxxxxxxxxx/zzzzzzzzzz
```

- Release builds will fail if ads are enabled and any of these values are missing.
- Do not ship the Google sample ad IDs in release.

## 4. Build the signed AAB

Run this after the keystore and AdMob IDs are set.

```powershell
./gradlew.bat bundleRelease
```

Expected output:

- `app/build/outputs/bundle/release/app-release.aab`

## 5. Play Console side

- Enable Play App Signing on first upload.
- Use the generated upload key for the local signed AAB.
- Keep the keystore so future updates can be signed with the same upload key.
