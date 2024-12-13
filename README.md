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

### Protocol Buffer Setup

1. Install Protocol Buffer compiler (protoc):
   ```bash
   # For macOS using Homebrew
   brew install protobuf

   # For Ubuntu/Debian
   sudo apt-get install protobuf-compiler
   ```

2. Compile the Protocol Buffer definition and create JAR:
   ```bash
   # Create a temporary directory for compilation
   mkdir -p temp_compile

   # Compile proto file to Java
   protoc --java_out=temp_compile MarketDataFeed.proto

   # Compile Java files to class files
   javac -cp "$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q):." -d temp_compile temp_compile/com/upstox/marketdatafeeder/rpc/proto/*.java

   # Create JAR file
   cd temp_compile && jar cf ../libs/marketdatafeed.jar com/
   cd .. && rm -rf temp_compile
   ```

Note: The generated `marketdatafeed.jar` should not be committed to the repository. It should be generated during project setup. The `.proto` file is the source of truth and is version controlled instead.
