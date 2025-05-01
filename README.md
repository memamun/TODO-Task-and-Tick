# Todo App

A modern, feature-rich Todo application for Android.

## Building the App

### Prerequisites
- Android Studio Chipmunk or newer
- JDK 11 or newer
- Gradle 7.4 or newer

### Debug Build
To build a debug version of the app, run:
```
./gradlew assembleDebug
```

The debug APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`

### Release Build
To build a signed release version of the app, run:
```
./gradlew assembleRelease
```

The signed release APK will be generated at `app/build/outputs/apk/release/app-release.apk`

## Publishing to Alternative App Stores

If you don't have access to the Google Play Store, here are some popular alternatives:

### Amazon Appstore
1. Create an [Amazon Developer account](https://developer.amazon.com/)
2. Click "Add new app" in the developer console
3. Fill in app details (title, description, category)
4. Upload your APK file
5. Set pricing and availability
6. Submit for review

### Samsung Galaxy Store
1. Register as a seller on [Samsung Galaxy Store Seller Portal](https://seller.samsungapps.com/)
2. Create a new application
3. Upload your APK file
4. Fill in app details and set pricing
5. Submit for review

### Huawei AppGallery
1. Register at the [Huawei Developer Console](https://developer.huawei.com/consumer/en/console)
2. Create a new app
3. Upload your APK file
4. Complete app information
5. Submit for review

### APKPure
1. Create an account on [APKPure](https://developer.apkpure.com/)
2. Submit your APK
3. Provide app metadata (title, description, screenshots)
4. Get approved and published

### F-Droid (For Open Source Apps)
1. If your app is open source, you can submit it to [F-Droid](https://f-droid.org/docs/Inclusion_Policy/)
2. Make your source code available on a public Git repository
3. Submit your app for inclusion
4. F-Droid will build and distribute your app

### Direct APK Distribution
You can also distribute your APK file directly:
1. Host the APK file on your website or file hosting service
2. Create a QR code linking to the download URL
3. Share the link or QR code with potential users
4. Users will need to enable "Install from unknown sources" in their device settings

## Publishing to Google Play Store

### Step 1: Create a Google Play Developer Account
1. Visit the [Google Play Console](https://play.google.com/console/signup)
2. Pay the one-time $25 registration fee
3. Complete the account details

### Step 2: Prepare Store Listing
Prepare the following assets for your app listing:
- App title: "Todo App"
- Short description (up to 80 characters)
- Full description (up to 4000 characters)
- App icon (512x512 PNG)
- Feature graphic (1024x500 JPG or PNG)
- At least 2 screenshots for each supported device type (phone, tablet)
- Privacy policy URL

### Step 3: Create a New App
1. Log in to the [Google Play Console](https://play.google.com/console)
2. Click "Create app"
3. Enter app details (name, default language, app/game, free/paid)
4. Confirm developer program policies and US export laws

### Step 4: Complete the Store Listing
1. Navigate to "Store presence" > "Store listing"
2. Fill in all required information and upload screenshots, feature graphics, and app icon
3. Save the draft

### Step 5: Set Up App Content Rating
1. Go to "Content rating" section
2. Complete the questionnaire about your app's content
3. Submit for rating

### Step 6: Set Up Pricing and Distribution
1. Go to "Pricing & distribution"
2. Select app availability (countries)
3. Select Free or Paid
4. Answer the distribution questions
5. Save the draft

### Step 7: Upload the Release APK
1. Go to "Production" > "Create new release"
2. Upload the signed APK (`app/build/outputs/apk/release/app-release.apk`)
3. Add release notes
4. Save and review the release

### Step 8: Rollout to Production
1. Click "Start rollout to production"
2. Google Play will review your app (typically takes a few days)
3. Once approved, your app will be available in the Google Play Store

## App Bundle (Alternative to APK)
Google Play prefers Android App Bundles over APKs. To generate an AAB file:
```
./gradlew bundleRelease
```

The AAB file will be located at `app/build/outputs/bundle/release/app-release.aab`

## Keystore Information

The app is signed with the following keystore:
- Keystore file: `keystore/todo_app_keystore.jks`
- Keystore password: `todoapp123`
- Key alias: `todo_app_key`
- Key password: `todoapp123`

**Important**: Keep this keystore file secure. If you lose it, you won't be able to update your app on the Play Store.

## Features
- Task creation, editing, and deletion
- Priority management
- Due date scheduling
- Category filtering
- Multi-language support (English and Bengali)
- Dark mode support
- Multi-select functionality
- Task import/export
- Overdue task notifications 