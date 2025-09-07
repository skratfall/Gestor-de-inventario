# JavaFX Modular Application

A modular JavaFX application with clean architecture and organized package structure.

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   ├── module-info.java
│   │   └── com/app/
│   │       ├── Main.java                    # Application entry point
│   │       ├── controller/                  # JavaFX controllers
│   │       │   ├── BaseController.java      # Base controller with common functionality
│   │       │   └── LoginController.java     # Login view controller
│   │       ├── model/                       # Entity classes
│   │       │   ├── User.java               # User entity
│   │       │   └── Session.java            # Session entity
│   │       ├── dao/                        # Data Access Objects
│   │       │   ├── UserDAO.java            # User DAO interface
│   │       │   ├── UserDAOImpl.java        # User DAO implementation
│   │       │   └── DatabaseConnection.java # Database connection utility
│   │       └── ia/                         # AI integration placeholders
│   │           ├── AIService.java          # Main AI service
│   │           └── RecommendationEngine.java # Recommendation algorithms
│   └── resources/
│       └── com/app/view/                   # FXML files
│           └── LoginView.fxml              # Login interface
├── pom.xml                                 # Maven configuration
└── README.md                               # This file
```

## Features

- **Modular Architecture**: Clean separation of concerns with dedicated packages
- **JavaFX UI**: Modern desktop application interface using FXML
- **Maven Build System**: Easy dependency management and building
- **Entity Models**: User and Session entities with proper encapsulation
- **DAO Pattern**: Database abstraction layer with interfaces and implementations
- **Controller Pattern**: MVC architecture with dedicated controllers for each view
- **AI Integration Ready**: Placeholder classes for future AI functionality

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## Running the Application

1. **Clone or download the project**

2. **Navigate to the project directory:**
   ```bash
   cd javafx-modular-app
   ```

3. **Run with Maven:**
   ```bash
   mvn clean javafx:run
   ```

   Or compile and run manually:
   ```bash
   mvn clean compile
   mvn javafx:run
   ```

## Development Notes

### Current State
- ✅ Basic project structure implemented
- ✅ Login view with form validation
- ✅ User and Session entities
- ✅ DAO interface and mock implementation
- ✅ AI service placeholders
- 🚧 Database integration (placeholder implementation)
- 🚧 AI functionality (placeholder implementation)

### TODO Items
- Implement actual database connection (H2, PostgreSQL, MySQL, etc.)
- Add user registration functionality
- Create main application view after login
- Implement password hashing and security
- Add logging framework (SLF4J/Logback)
- Implement AI services with actual ML libraries
- Add unit tests
- Add configuration management

### Architecture Benefits

1. **Separation of Concerns**: Each package has a single responsibility
2. **Testability**: Interface-based design makes testing easier
3. **Maintainability**: Modular structure simplifies updates and bug fixes
4. **Scalability**: Easy to add new features without affecting existing code
5. **Future-Proof**: Ready for database and AI integration

### Package Responsibilities

- **`model/`**: Entity classes representing business objects
- **`controller/`**: JavaFX controllers handling UI logic and user interactions
- **`view/`**: FXML files defining the user interface layout
- **`dao/`**: Data access layer abstracting database operations
- **`ia/`**: AI and machine learning services for intelligent features

## Demo Credentials

For testing the login functionality, you can use:
- Username: `admin`, Password: `admin123`
- Username: `testuser`, Password: `test123`
- Or any username/password combination (current implementation is for demo purposes)

## Contributing

1. Follow the existing package structure
2. Add proper JavaDoc comments
3. Implement TODO items in priority order
4. Add unit tests for new functionality
5. Update this README with any significant changes