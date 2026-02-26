#!/bin/bash
# Script to generate a release keystore for the Android app

KEYSTORE_FILE="release.keystore"
ALIAS="upload"

echo "Generating Android Release Keystore..."

keytool -genkey -v \
  -keystore $KEYSTORE_FILE \
  -alias $ALIAS \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000

echo ""
echo "================================================================"
echo "Keystore generated: $KEYSTORE_FILE"
echo "Alias used: $ALIAS"
echo "================================================================"
echo "IMPORTANT: Do NOT commit this file to Git!"
echo "Add $KEYSTORE_FILE to your .gitignore"
echo ""
echo "To use this in GitHub Actions for signing the APK, you need to"
echo "add the following Repository Secrets to your GitHub project:"
echo ""
echo "1. SIGNING_KEY: The base64 encoded keystore file."
echo "   Get it by running: base64 -w 0 $KEYSTORE_FILE"
echo "2. ALIAS: The alias used (it is set to: $ALIAS)"
echo "3. KEY_STORE_PASSWORD: The password you just entered for the keystore."
echo "4. KEY_PASSWORD: The password you just entered for the key (usually the same)."
echo "================================================================"
