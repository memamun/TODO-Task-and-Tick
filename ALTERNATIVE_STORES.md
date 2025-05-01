# Publishing to Alternative App Stores

If you don't have access to the Google Play Store or want to expand your app's distribution, here are instructions for publishing to popular alternative app stores.

## APK File

For most alternative app stores, you'll need the signed APK file located at:
`release/TODO-Task-and-Tick-v1.0.0.apk`

## Amazon Appstore

The Amazon Appstore is available on Amazon Fire devices and some Android devices.

1. Create an [Amazon Developer account](https://developer.amazon.com/)
2. Click "Add new app" in the developer console
3. Fill in app details:
   - Title: "TODO - Task & Tick"
   - Category: Productivity
   - Description: Copy from your Play Store listing
4. Upload your APK file
5. Add screenshots and marketing assets
6. Set pricing (free) and availability
7. Submit for review

**Special Requirements:**
- Amazon requires a custom privacy policy URL
- Testing on Amazon devices is recommended but not required

## Samsung Galaxy Store

The Samsung Galaxy Store is available on Samsung devices.

1. Register as a seller on [Samsung Galaxy Store Seller Portal](https://seller.samsungapps.com/)
2. Create a new application
3. Fill in app details:
   - App name: "TODO - Task & Tick"
   - Category: Productivity
   - Description and features
4. Upload your APK file
5. Add screenshots and promo images
6. Set pricing and distribution options
7. Submit for review

**Special Requirements:**
- Samsung may require testing on Samsung devices
- Provide a valid privacy policy

## Huawei AppGallery

For Huawei devices, especially in regions where Google services are not available.

1. Register at the [Huawei Developer Console](https://developer.huawei.com/consumer/en/console)
2. Create a new app
3. Upload your APK file
4. Complete app information:
   - App name: "TODO - Task & Tick"
   - App category: Productivity
   - Languages: English, Bengali
   - Description and features
5. Add screenshots and promotional materials
6. Submit for review

**Special Requirements:**
- Huawei devices don't include Google services, so ensure your app doesn't have hard dependencies on Google APIs
- You may need to integrate with Huawei Mobile Services (HMS) for certain features

## APKPure

APKPure is a popular third-party app store.

1. Create an account on [APKPure](https://developer.apkpure.com/)
2. Submit your APK
3. Provide app metadata:
   - App name: "TODO - Task & Tick"
   - Description
   - Categories: Productivity, Tools
   - Screenshots
4. Get approved and published

## F-Droid (For Open Source Apps)

F-Droid is focused on free and open-source software.

1. Make your source code available on a public Git repository (GitHub, GitLab, etc.)
2. Ensure your app meets [F-Droid's inclusion policy](https://f-droid.org/docs/Inclusion_Policy/)
3. Submit your app by creating an issue in the [F-Droid Data repository](https://gitlab.com/fdroid/fdroiddata/issues)
4. Provide details about your app and repository
5. F-Droid maintainers will review and build your app

**Special Requirements:**
- Your app must be completely open source
- No proprietary dependencies are allowed
- Build process must be reproducible

## Direct APK Distribution

You can also distribute your APK file directly through your own channels.

### Website Distribution

1. Host the APK file on your website
2. Create a download page with installation instructions
3. Provide a changelog and app details
4. Optionally create a QR code linking to the download URL

### Email Distribution

1. Attach the APK to an email (if size permits)
2. Include installation instructions
3. Send to your distribution list

### USB/Bluetooth Distribution

1. Share the APK file directly via:
   - Bluetooth transfer
   - USB drive
   - File sharing apps like SHAREit

## Installation Instructions for Users

Include these instructions with your APK distribution:

1. Download the APK file to your Android device
2. Navigate to your device's **Settings**
3. Look for **Security** or **Privacy** settings
4. Enable **Install from Unknown Sources** or **Install Unknown Apps**
5. Open the downloaded APK file
6. Follow the installation prompts
7. Launch the app after installation completes

## Updates

For app updates through alternative stores:

1. Increment the version code and version name in `app/build.gradle`
2. Build a new signed APK
3. Submit the update to each app store following their update procedures
4. For direct distribution, notify users about the update and provide the new APK

## Marketing Tips

- Create a simple landing page for your app with screenshots and features
- Add a QR code on your website that links directly to the APK download
- Use social media to promote your app
- Consider creating a short demo video showing key features 