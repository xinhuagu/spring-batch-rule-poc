# Spring Batch Rule POC

## Project Overview
A Spring Boot 3 application demonstrating Spring Batch integration with a comprehensive rule-based data transformation system. This POC showcases database-to-CSV export with a flexible rule engine supporting categorization and transformation rules loaded from configuration files.

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
│   │   │       ├── rule/
│   │   │       │   ├── RuleEngine.java           # Rule engine implementation
│   │   │       │   └── TransformationRule.java   # Rule definition model
│   │   │       └── runner/JobRunner.java         # Command line job runner
│   │   └── resources/
│   │       ├── application.yml                   # Main configuration
│   │       ├── application-test.yml              # Test configuration
│   │       └── rules.txt                         # Rule definitions file
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
Create a PostgreSQL database named `poc` with user `xinhua.gu` (no password required for local development).

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
- ✅ Rule-based data transformation system with external configuration
- ✅ Client-to-CSV export batch job with rule processing
- ✅ Command line job execution with arguments support
- ✅ Comprehensive test suite including unit and integration tests
- ✅ Flexible rule engine supporting CATEGORIZE and TRANSFORM rules
- ✅ Dynamic rule loading from configuration files

### Key Components

#### BatchConfig.java
Spring Batch configuration containing:
- `clientToCsvJob`: PostgreSQL to CSV export job
- `clientToCsvStep`: Step with ItemReader/ItemProcessor/ItemWriter pattern
- Database transaction management

#### RuleEngine.java
Rule engine implementation for data transformations:
- Loads rules from `rules.txt` configuration file
- Supports CATEGORIZE rules for numeric value categorization
- Supports TRANSFORM rules for text transformations
- Name formatting (uppercase/lowercase/titlecase)
- Age categorization (Young/Adult/Senior based on ranges)
- Extensible rule system for adding new rule types

#### ClientRuleProcessor.java
ItemProcessor implementation that applies business rules to Client entities during batch processing.

#### JobRunner.java
Command line runner for programmatic job execution with parameter support.

## Database Configuration

### PostgreSQL (Production)
- Database: `poc`
- Host: `localhost:5432`
- Username: `xinhua.gu`
- Password: (empty)
- Connection pool: HikariCP (max 10 connections, 30s timeout)

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

**Input**: PostgreSQL `client` table
**Processing**: Apply transformation rules from `rules.txt`
- Name formatting (converts to uppercase)
- Age categorization (Young: ≤25, Adult: 26-40, Senior: >40)
**Output**: CSV file (`clients_export.csv`) with columns: id, name, age, ageCategory

**Execution**:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="clientToCsv"
```

## Rule System

### Rule Definition Format
Rules are defined in `src/main/resources/rules.txt` with the format:
```
FIELD_NAME:RULE_TYPE:CONDITION:ACTION
```

### Current Rules
- `age:CATEGORIZE:<=25:Young` - Ages 25 and below
- `age:CATEGORIZE:26-40:Adult` - Ages 26 to 40
- `age:CATEGORIZE:>40:Senior` - Ages over 40
- `name:TRANSFORM:*:UPPERCASE` - Convert all names to uppercase

### Rule Types
- **CATEGORIZE**: Convert numeric values to categories
  - Supports conditions: `<=`, `>`, and ranges (e.g., `26-40`)
- **TRANSFORM**: Apply text transformations
  - Actions: `UPPERCASE`, `LOWERCASE`, `TITLECASE`

## Development Guide

### Adding New Jobs
1. Create Job beans in BatchConfig following the `clientToCsvJob` pattern
2. Define Steps using StepBuilder with ItemReader/ItemProcessor/ItemWriter
3. Add job execution logic to JobRunner
4. Configure appropriate ItemReaders for data sources
5. Add unit tests following BatchConfigTest structure

### Extending Rule Engine
1. Add new rule type constants to RuleEngine (e.g., "VALIDATE", "MASK")
2. Implement rule logic in RuleEngine.applyRule() method
3. Update rules.txt with new rule definitions
4. Add unit tests in RuleEngineTest
5. Update processors to handle new transformed fields

### Data Processing Patterns
- **Database to File**: JdbcCursorItemReader + ItemProcessor + FlatFileItemWriter
- **File to Database**: FlatFileItemReader + ItemProcessor + JdbcBatchItemWriter
- **Rule-Based Transformation**: ItemProcessor with RuleEngine integration
- **Multi-Field Processing**: Single processor handling multiple field transformations

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
A: Update the `rules.txt` file with new rule definitions following the format:
```
FIELD_NAME:RULE_TYPE:CONDITION:ACTION
```
For new rule types, extend RuleEngine.applyRule() method.

## Project Information
- **Author**: Accenture POC Team
- **Version**: 1.0.0-SNAPSHOT
- **Repository**: https://github.com/xinhuagu/spring-batch-rule-poc

## Roadmap
- [ ] Add REST controllers for job management
- [ ] Implement error handling and retry mechanisms
- [ ] Add monitoring and reporting features
- [ ] Support dynamic rule reloading without restart
- [ ] Add more rule types (VALIDATE, MASK, ENCRYPT)
- [ ] Implement rule versioning and audit trail
- [ ] Add support for complex conditional rules
- [ ] Extend to support JSON and XML exports
- [ ] Add parallel processing for large datasets