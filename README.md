# algo-trader
A Java app to screen potential trades and take them.

## Setup

### Authentication Setup

1. Create a `secret.txt` file in the project root directory containing your Upstox API secret key

### HTTPS Local Server Setup

1. Generate a keystore file for HTTPS:
   ```bash
   keytool -genkeypair -alias serverkey -keyalg RSA -keysize 2048 -keystore keystore.jks -validity 365
   ```
   - When prompted, create a secure password
   - For local development, you can use any values for the certificate information prompts
   - Store the generated `keystore.jks` file in the project root directory

2. Create a `keystore_password.txt` file:
   - Create a new file named `keystore_password.txt` in the project root directory
   - Add the password you used when generating the keystore
   - Make sure this file contains only the password with no extra spaces or newlines

Note: Both `keystore.jks` and `keystore_password.txt` contain sensitive information and should never be committed to the repository.
