# Spring Batch Rule POC

## Project Overview
A Spring Boot 3 application demonstrating Spring Batch integration with a rule-based data transformation system. This POC showcases PostgreSQL to CSV export with rule engine capabilities for data processing.

## Technology Stack
- **Java**: 17
- **Spring Boot**: 3.2.5
- **Spring Batch**: 5.x
- **Database**: PostgreSQL (H2 for tests)
- **Build Tool**: Maven
- **Others**: Lombok, Spring Data JPA, HikariCP

## Project Structure
```
spring-batch-rule-poc/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/accenture/poc1/
│   │   │       ├── Application.java              # Spring Boot main class
│   │   │       ├── config/BatchConfig.java       # Spring Batch configuration
│   │   │       ├── model/Client.java             # Client entity model
│   │   │       ├── processor/ClientRuleProcessor.java # Rule-based processor
│   │   │       ├── rule/RuleEngine.java          # Rule engine implementation
│   │   │       └── runner/JobRunner.java         # Command line job runner
│   │   └── resources/
│   │       ├── application.yml                   # Main configuration
│   │       └── application-test.yml              # Test configuration
│   └── test/
│       ├── java/
│       │   └── com/accenture/poc1/
│       │       ├── ApplicationTests.java         # Application tests
│       │       ├── ClientToCsvJobTest.java       # Job integration tests
│       │       ├── ClientToCsvManualTest.java    # Manual test scenarios
│       │       ├── config/TestBatchConfiguration.java # Test batch config
│       │       ├── processor/ClientRuleProcessorTest.java # Processor tests
│       │       └── rule/RuleEngineTest.java      # Rule engine tests
├── pom.xml                                        # Maven configuration
├── CLAUDE.md                                      # Development guidance
└── README.md                                      # Project documentation
```

## Quick Start

### Prerequisites
- JDK 17 or higher
- Maven 3.6 or higher
- PostgreSQL database running on localhost:5432

### Database Setup
Create a PostgreSQL database named `poc` with user `xinhua` (no password required for local development).

### Build Project
```bash
cd spring-batch-rule-poc
mvn clean install
```

### Run Application
```bash
# Using Maven Spring Boot plugin
mvn spring-boot:run

# Run specific batch job
mvn spring-boot:run -Dspring-boot.run.arguments="clientToCsv"

# Run packaged JAR
java -jar target/spring-batch-rule-poc-1-1.0.0-SNAPSHOT.jar
```

### Run Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=BatchConfigTest

# Build without tests
mvn clean install -DskipTests
```

## Features

### Current Implementation
- ✅ Spring Batch framework with PostgreSQL integration
- ✅ Rule-based data transformation system
- ✅ Client-to-CSV export batch job
- ✅ Command line job execution
- ✅ Comprehensive test suite
- ✅ Rule engine for data processing

### Key Components

#### BatchConfig.java
Spring Batch configuration containing:
- `clientToCsvJob`: PostgreSQL to CSV export job
- `clientToCsvStep`: Step with ItemReader/ItemProcessor/ItemWriter pattern
- Database transaction management

#### RuleEngine.java
Rule engine implementation for data transformations:
- Name formatting (uppercase conversion)
- Age categorization (Young/Adult/Senior)
- Extensible rule system

#### ClientRuleProcessor.java
ItemProcessor implementation that applies business rules to Client entities during batch processing.

#### JobRunner.java
Command line runner for programmatic job execution with parameter support.

## Database Configuration

### PostgreSQL (Production)
- Database: `poc`
- Host: `localhost:5432`
- Username: `xinhua`
- Password: (empty)
- Connection pool: HikariCP (max 10 connections)

### H2 (Testing)
- In-memory database for test isolation
- Automatic schema generation

## Management Endpoints

### Actuator Endpoints
```
http://localhost:8080/actuator/health
http://localhost:8080/actuator/info
http://localhost:8080/actuator/metrics
http://localhost:8080/actuator/beans
```

## Batch Jobs

### clientToCsvJob
Exports client data from PostgreSQL to CSV with rule-based transformations:

**Input**: PostgreSQL `clients` table
**Processing**: Apply business rules (name formatting, age categorization)
**Output**: CSV file with transformed data

**Execution**:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="clientToCsv"
```

## Development Guide

### Adding New Jobs
1. Create Job beans in BatchConfig following the `clientToCsvJob` pattern
2. Define Steps using StepBuilder with ItemReader/ItemProcessor/ItemWriter
3. Add job execution logic to JobRunner
4. Configure appropriate ItemReaders for data sources

### Extending Rule Engine
1. Add new rule methods to RuleEngine class
2. Update ClientRuleProcessor to apply new rules
3. Add corresponding unit tests
4. Update CSV output format if needed

### Data Processing Patterns
- **Database to File**: JdbcCursorItemReader + FlatFileItemWriter
- **File to Database**: FlatFileItemReader + JdbcBatchItemWriter
- **Data Transformation**: ItemProcessor with rule engine integration

## Testing

### Test Categories
- **Unit Tests**: Individual component testing
- **Integration Tests**: Full job execution testing
- **Manual Tests**: Real database scenario testing

### Test Database
Tests use H2 in-memory database with automatic cleanup and test data setup.

## FAQ

### Q: How to disable automatic job execution?
A: Set `spring.batch.job.enabled=false` in `application.yml`

### Q: How to view batch execution history?
A: Query Spring Batch metadata tables:
- BATCH_JOB_INSTANCE
- BATCH_JOB_EXECUTION
- BATCH_STEP_EXECUTION

### Q: How to configure parallel processing?
A: Use `taskExecutor` and `throttleLimit` in Step configuration:
```java
.tasklet(tasklet())
.taskExecutor(taskExecutor())
.throttleLimit(10)
```

### Q: How to add new transformation rules?
A: Add methods to RuleEngine class and update ClientRuleProcessor to apply them.

## Project Information
- **Author**: Accenture POC Team
- **Version**: 1.0.0-SNAPSHOT
- **Repository**: https://github.com/xinhuagu/spring-batch-rule-poc

## Roadmap
- [ ] Add REST controllers for job management
- [ ] Implement error handling and retry mechanisms
- [ ] Add monitoring and reporting features
- [ ] Extend rule engine with external rule definitions
- [ ] Add more batch processing patterns