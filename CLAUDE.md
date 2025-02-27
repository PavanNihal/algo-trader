# Project Commands & Guidelines

## Build Commands
- `mvn compile`: Compile code
- `mvn package`: Build executable JAR with dependencies
- `mvn clean package`: Clean and rebuild
- `mvn javafx:run`: Run the JavaFX application
- `mvn test`: Run all tests
- `mvn test -Dtest=TestClassName`: Run single test class

## Code Style
- **Imports**: Standard library → Third-party → Project-specific
- **Naming**: Classes/Interfaces: PascalCase, Methods/Variables: camelCase, Constants: UPPER_SNAKE_CASE
- **Formatting**: 4-space indentation, same-line braces, block-style braces
- **Error Handling**: Try-catch with specific exceptions, use custom exceptions for domain logic
- **Comments**: Minimal but clear, focus on "why" not "what"
- **Package Structure**: Organized by functionality (api, authentication, ui, model, database, util)

## Project Setup
- Requires JDK 17
- Create `keystore.jks` and `secret.txt` for API authentication
- H2 database used for local storage
- JavaFX UI with FXML for interface components