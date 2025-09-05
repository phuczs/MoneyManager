# Google Sign-In Setup Guide

This guide will help you configure Google Sign-In authentication for the MoneyManager app.

## 🔧 Prerequisites

- Firebase project already created
- Android app registered in Firebase Console
- `google-services.json` file downloaded and placed in `app/` directory

## 📋 Step-by-Step Setup

### Step 1: Enable Google Sign-In in Firebase Console

1. **Go to Firebase Console**
   - Open [Firebase Console](https://console.firebase.google.com)
   - Select your MoneyManager project

2. **Navigate to Authentication**
   - Click on "Authentication" in left sidebar
   - Go to "Sign-in method" tab

3. **Enable Google Provider**
   - Click on "Google" provider
   - Toggle "Enable" switch
   - Set project support email
   - Click "Save"

### Step 2: Configure OAuth Consent Screen

1. **Go to Google Cloud Console**
   - Open [Google Cloud Console](https://console.cloud.google.com)
   - Select your Firebase project

2. **Configure OAuth Consent**
   - Navigate to "APIs & Services" > "OAuth consent screen"
   - Choose "External" user type
   - Fill required fields:
     - App name: "MoneyManager"
     - User support email: Your email
     - Developer contact email: Your email
   - Click "Save and Continue"

### Step 3: Create OAuth 2.0 Client ID

1. **Go to Credentials**
   - Navigate to "APIs & Services" > "Credentials"
   - Click "Create Credentials" > "OAuth 2.0 Client IDs"

2. **Configure Android Client**
   - Application type: "Android"
   - Name: "MoneyManager Android"
   - Package name: `com.example.moneymanager`
   - SHA-1 certificate fingerprint: [See below how to get this]

3. **Get SHA-1 Fingerprint**
   ```bash
   # For debug builds (development)
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey
   # Password: android
   
   # For release builds (production)
   keytool -list -v -keystore your-release-key.keystore -alias your-key-alias
   ```

4. **Create Web Client (Important!)**
   - Also create a "Web application" client
   - This provides the Web Client ID needed for Android

### Step 4: Download Updated google-services.json

1. **Download New Configuration**
   - Go back to Firebase Console
   - Project Settings > General tab
   - Scroll to "Your apps" section
   - Click download icon for `google-services.json`
   - Replace the existing file in `app/` directory

### Step 5: Update String Resource

The Web Client ID should now be automatically available. Check if it's in the updated `google-services.json`:

1. **Automatic Method (Recommended)**
   - The Google Services plugin should automatically generate the `default_web_client_id`
   - Remove the placeholder from `strings.xml` if the plugin generates it

2. **Manual Method (If needed)**
   - Open your `google-services.json`
   - Find the OAuth client with `"client_type": 3`
   - Copy the `client_id` value
   - Update `strings.xml`:
   ```xml
   <string name="default_web_client_id">YOUR_ACTUAL_WEB_CLIENT_ID</string>
   ```

## ✅ Testing the Implementation

### Test Google Sign-In

1. **Build and Run the App**
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

2. **Test on Device/Emulator**
   - Open the login screen
   - Tap "Continue with Google"
   - Select Google account
   - Verify successful authentication

### Common Issues and Solutions

#### Issue 1: "12500 Error" or "Sign-in failed"
**Solution**: SHA-1 fingerprint mismatch
- Verify SHA-1 fingerprint is correct
- Make sure you're using debug keystore for development
- Regenerate OAuth client with correct SHA-1

#### Issue 2: "Web Client ID not found"
**Solution**: Missing Web Client ID
- Create Web application OAuth client in Google Cloud Console
- Update `google-services.json`
- Verify `default_web_client_id` is in resources

#### Issue 3: "DEVELOPER_ERROR"
**Solution**: Configuration mismatch
- Package name must match exactly
- OAuth client must be for the correct project
- `google-services.json` must be up to date

## 🚀 Production Setup

### For Release Builds

1. **Generate Release Keystore**
   ```bash
   keytool -genkey -v -keystore release-key.keystore -alias my-alias -keyalg RSA -keysize 2048 -validity 10000
   ```

2. **Get Release SHA-1**
   ```bash
   keytool -list -v -keystore release-key.keystore -alias my-alias
   ```

3. **Add Release SHA-1 to OAuth Client**
   - Go to Google Cloud Console > Credentials
   - Edit your OAuth 2.0 client
   - Add the release SHA-1 fingerprint

4. **Update Build Configuration**
   ```kotlin
   // app/build.gradle.kts
   android {
       signingConfigs {
           release {
               storeFile file("release-key.keystore")
               storePassword "your_store_password"
               keyAlias "my-alias"
               keyPassword "your_key_password"
           }
       }
       buildTypes {
           release {
               signingConfig signingConfigs.release
               // ... other config
           }
       }
   }
   ```

## 📱 Features Included

After setup, your app will support:

✅ **Email/Password Authentication** (existing)
✅ **Google Sign-In Authentication** (new)
✅ **Unified User Management** (same Firebase Auth)
✅ **Automatic Sign-Out** (both methods)

## 🔐 Security Considerations

- Web Client ID is safe to include in the app
- SHA-1 fingerprints protect against package spoofing
- Firebase handles OAuth flow securely
- User data is automatically protected by Firebase Auth

## 📞 Support

If you encounter issues:

1. Check Firebase Console for authentication logs
2. Verify all configuration steps
3. Test with different Google accounts
4. Check device/emulator Google Play Services

---

**Next Steps**: After completing this setup, users can sign in with both email/password and Google accounts seamlessly!