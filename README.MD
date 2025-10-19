# StackSpot API Integration Java Client

A robust Java client implementation for interacting with the StackSpot AI API, providing seamless integration for AI-powered conversations and code generation capabilities.

## 🚀 Features

- Token-based authentication with automatic renewal
- AI chat interactions with streaming response support
- Execution management for AI operations
- Conversation tracking and management
- Environment configuration management

## 📋 Prerequisites

- Java 17 or higher
- Maven
- StackSpot API credentials (Realm, Client ID, and Client Secret)

## ⚙️ Configuration

1. Create a `.env` file in the project root based on the provided `.env.example`:

```env
STACKSPOT_REALM=your_realm
STACKSPOT_CLIENT_ID=your_client_id
STACKSPOT_CLIENT_SECRET=your_client_secret
AGENT_ID=your_agent_id
```

## 🛠️ Project Structure

```
src/
├── main/
│   └── java/
│       └── org/
│           └── stackspotapi/
│               ├── Main.java                      # Application entry point
│               ├── dto/                           # Data Transfer Objects
│               │   ├── ChatRequestDto.java        # Chat request structure
│               │   ├── CreateExecutionRequestDto.java
│               │   ├── CreateExecutionResponseDto.java
│               │   ├── EnsureDto.java            # Token management
│               │   └── GetConversationResponseDto.java
│               └── service/
│                   ├── AiChatService.java        # AI chat operations
│                   ├── EnsureTokenService.java   # Token management
│                   └── ExecutionService.java      # Execution handling
```

## 🔧 Services

### 1. AiChatService

- Manages AI chat interactions
- Handles streaming responses
- Coordinates the complete flow from authentication to response

### 2. EnsureTokenService

- Handles authentication token management
- Automatic token renewal
- Secure credential handling

### 3. ExecutionService

- Creates and manages AI executions
- Handles conversation ID retrieval
- Manages execution state and callbacks

## 📝 DTOs (Data Transfer Objects)

### ChatRequestDto

- Structures chat requests with context
- Manages conversation metadata

### CreateExecutionRequestDto/ResponseDto

- Handles execution creation requests
- Manages execution responses and IDs

### EnsureDto

- Manages authentication tokens
- Handles token expiration

### GetConversationResponseDto

- Structures conversation responses
- Manages conversation state

## 🚀 Usage

1. Run the application:

```bash
mvn clean install
mvn exec:java -Dexec.mainClass="org.stackspotapi.Main"
```

2. Use the interactive menu to:
   - Send questions to the AI
   - Get AI-powered responses
   - Exit the application

Example interaction:

```java
String response = AiChatService.ask("Your question here");
```

## 🔒 Security

- Environment variables for sensitive credentials
- Token-based authentication
- Automatic token renewal
- Secure HTTP client implementation

## 🛡️ Dependencies

```xml
<dependencies>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.20.0</version>
    </dependency>
    <dependency>
        <groupId>io.github.cdimascio</groupId>
        <artifactId>dotenv-java</artifactId>
        <version>3.2.0</version>
    </dependency>
</dependencies>
```

## 🔍 Error Handling

The application implements comprehensive error handling:

- Network communication errors
- Authentication failures
- API response validation
- Token expiration handling
- Invalid input validation

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## 📄 License

This project is currently unlicensed. Please contact the repository owner for usage permissions.

## ✨ Best Practices Implemented

- Clean Code architecture
- Dependency Injection ready
- Proper error handling
- Comprehensive logging
- Security best practices
- HTTP client pooling
- Stream processing for large responses

## 📞 Contact

Create an issue in the repository for bugs, features, or questions.
