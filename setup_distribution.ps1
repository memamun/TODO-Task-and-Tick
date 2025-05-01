# PowerShell script to set up a directory for distributing the Todo app

# Create a distribution directory
$distDir = "TodoApp_Distribution"
New-Item -ItemType Directory -Force -Path $distDir

# Copy the release APK to the distribution directory
Copy-Item -Path "app\build\outputs\apk\release\app-release.apk" -Destination "$distDir\"

# Copy the HTML download page
Copy-Item -Path "app_download.html" -Destination "$distDir\index.html"

# Create a screenshots directory
New-Item -ItemType Directory -Force -Path "$distDir\screenshots"

# Message to remind user to add screenshots
Write-Host "Please add screenshot images to the $distDir\screenshots directory"

# Generate a QR code using an online API (the URL will need to be replaced with your actual hosting URL)
$qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=YOUR_DOWNLOAD_URL_HERE"
Invoke-WebRequest -Uri $qrCodeUrl -OutFile "$distDir\download_qr.png"
Write-Host "A placeholder QR code has been generated. You'll need to replace it with one for your actual hosting URL"

# Create a README with instructions
@"
# Todo App Distribution Package

This folder contains files needed to distribute the Todo App.

## Contents:
- app-release.apk - The signed APK file to distribute
- index.html - The download page that users will see
- download_qr.png - A QR code for easy downloading (placeholder - update with your actual URL)
- screenshots/ - Directory for app screenshots

## Publishing Instructions:
1. Upload all contents to your web hosting provider
2. Ensure your web server is configured to serve .apk files with the correct MIME type
   Add this to your .htaccess file if needed: 
   `AddType application/vnd.android.package-archive .apk`
3. After uploading, update the QR code to point to your actual hosted APK URL
4. Share the website URL with your users

## Alternative Distribution Methods:
- Email the APK directly to users
- Share through cloud storage (Dropbox, Google Drive, etc.)
- Upload to alternative app stores (see README.md for more details)
"@ | Out-File -FilePath "$distDir\DISTRIBUTION_INSTRUCTIONS.txt"

Write-Host "Distribution package ready in the $distDir directory"
Write-Host "To publish, upload the entire contents to your web hosting provider" 