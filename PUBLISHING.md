# Publishing to Google Play Store

This guide provides detailed instructions for publishing the TODO - Task & Tick app to the Google Play Store.

## Prerequisites

1. A Google Play Developer account (requires a one-time $25 registration fee)
2. The signed AAB or APK file (AAB is preferred)
3. App graphics and marketing materials
4. A privacy policy

## Step 1: Create a Google Play Developer Account

1. Visit the [Google Play Console](https://play.google.com/console/signup)
2. Pay the one-time $25 registration fee
3. Complete the account details and verification process

## Step 2: Prepare Store Listing Assets

Prepare the following assets for your app listing:

### Required Assets
- **App title**: "TODO - Task & Tick"
- **Short description** (up to 80 characters):
  ```
  Modern task management with priorities, categories, and reminders
  ```
- **Full description** (up to 4000 characters):
  ```
  TODO - Task & Tick is a modern, feature-rich task management app to help you stay organized and productive. 
  
  Features include:
  • Create, edit, and delete tasks with ease
  • Organize tasks by categories (Work, Personal, Urgent, Shopping, Other)
  • Assign High, Medium, or Low priority to tasks
  • Set and track task deadlines with due dates
  • Filter tasks by category, priority, or completion status
  • Search through tasks instantly
  • Select multiple tasks for bulk operations
  • Full support for light and dark themes
  • Available in English and Bengali
  • Get reminders for overdue tasks
  • Back up and restore your tasks with import/export
  
  TODO - Task & Tick is designed to be simple yet powerful, helping you manage your day efficiently without clutter or complexity.
  ```
- **App icon** (512x512 PNG)
  - Located in `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png`
- **Feature graphic** (1024x500 JPG or PNG)
  - Create this using the screenshots and app logo
- **Phone screenshots** (minimum 2, maximum 8)
  - Use the screenshots from the `screenshots` folder
- **Privacy policy URL**
  - Create a privacy policy and host it on your website, or use a service like [App Privacy Policy Generator](https://app-privacy-policy-generator.firebaseapp.com/)

## Step 3: Create a New App

1. Log in to the [Google Play Console](https://play.google.com/console)
2. Click "Create app"
3. Enter app details:
   - App name: "TODO - Task & Tick"
   - Default language: English (or your preferred default)
   - App or Game: App
   - Free or Paid: Free
4. Confirm developer program policies and US export laws

## Step 4: Complete the Store Listing

1. Navigate to "Store presence" > "Store listing"
2. Fill in all required information using the assets prepared in Step 2
3. Add all screenshots
4. Save the draft

## Step 5: Set Up App Content Rating

1. Go to "Content rating" section
2. Complete the questionnaire about your app's content
   - App category: Utility/Productivity
   - Email address: your contact email
   - Contains ads: No (unless you've added ads)
   - Content rating questionnaire: Answer honestly about app content
3. Submit for rating

## Step 6: Set Up Pricing and Distribution

1. Go to "Pricing & distribution"
2. Select app availability (countries)
   - Typically select "All countries"
3. Select Free (or Paid if you're charging for the app)
4. Answer the distribution questions:
   - Contains ads: No (unless your app has ads)
   - App contains content for children: No
   - Target audience age range: Select appropriate ranges (typically 18+)
5. Save the draft

## Step 7: Upload the Release AAB/APK

1. Go to "Production" > "Create new release"
2. Upload the signed AAB (`private_release/TODO-Task-and-Tick-v1.0.0.aab`) or APK
3. Add release notes:
   ```
   Initial release of TODO - Task & Tick.
   
   Features:
   - Task management with categories and priorities
   - Due date scheduling and reminders
   - Multi-language support (English and Bengali)
   - Dark mode support
   - Task import/export
   - And more!
   ```
4. Save and review the release

## Step 8: Complete App Compliance

1. Navigate to "Policy" > "App content"
2. Complete the Data safety section:
   - Declare all data your app collects, shares, and how it's used
   - For this app, you typically would report:
     - No data collection from users
     - All data stored locally on device
3. Complete the App access section (if required for your app's functionality)

## Step 9: Rollout to Production

1. Review all sections to ensure everything is complete
2. Click "Start rollout to production"
3. Google Play will review your app (typically takes a few days)
4. Once approved, your app will be available in the Google Play Store

## Step 10: Monitor and Update

1. Monitor your app analytics in the Play Console
2. Address any user feedback or crashes
3. Release updates as needed following a similar process

## Important Notes

- **Keep your keystore file safe** - You must use the same signing key for all future updates
- **Follow Google Play policies** - Violating policies can result in app removal
- **Test thoroughly** - Test your app on various devices before submission
- **Setup Alpha/Beta testing** - Consider using closed or open testing before a full release 