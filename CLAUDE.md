# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3 application demonstrating Spring Batch integration with an implemented rule engine. The POC uses Java 17, Maven, and PostgreSQL database for batch processing operations with rule-based data transformation and CSV export capabilities.

## Core Commands

### Build and Package
```bash
mvn clean package        # Build and run all tests
mvn clean install       # Build, test, and install to local repository
mvn clean install -DskipTests  # Build without running tests
```

### Running Tests
```bash
mvn test                 # Run all tests
mvn test -Dtest=BatchConfigTest        # Run specific test class
mvn test -Dtest=ApplicationTests       # Run application integration tests
```

### Running the Application
```bash
mvn spring-boot:run      # Run using Maven plugin
java -jar target/spring-batch-rule-poc-1-1.0.0-SNAPSHOT.jar  # Run packaged JAR

# Run specific batch jobs
mvn spring-boot:run -Dspring-boot.run.arguments="clientToCsv"  # Run client-to-CSV job
```

## Architecture Overview

### Package Structure
- `com.accenture.poc1` - Root package
  - `Application.java` - Spring Boot main class
  - `config/BatchConfig.java` - Spring Batch configuration with job definitions
  - `model/Client.java` - Client entity model for data processing
  - `processor/ClientRuleProcessor.java` - ItemProcessor implementing rule-based transformations
  - `rule/RuleEngine.java` - Rule engine for data transformation logic
  - `rule/TransformationRule.java` - Rule definition model
  - `runner/JobRunner.java` - CommandLineRunner for executing batch jobs

### Key Components

**BatchConfig.java** - Central batch configuration containing:
- `clientToCsvJob` - PostgreSQL to CSV export batch job
- `clientToCsvStep` - Step that reads from PostgreSQL clients table, applies rules, and writes to CSV
- Uses ItemReader/ItemProcessor/ItemWriter pattern with rule-based transformations
- Configured with appropriate transaction management for database operations
- Chunk size set to 10 for efficient processing

**RuleEngine.java** - Core rule processing engine:
- Loads transformation rules from `rules.txt` file in classpath
- Supports CATEGORIZE and TRANSFORM rule types
- CATEGORIZE: converts numeric values to categories (age to Young/Adult/Senior)
- TRANSFORM: applies text transformations (uppercase, lowercase, titlecase)
- Extensible design for adding new rule types and conditions

**ClientRuleProcessor.java** - Spring Batch ItemProcessor:
- Implements rule-based transformations during batch processing
- Applies name formatting rules (converts to uppercase)
- Applies age categorization rules (adds ageCategory field)
- Integrates with RuleEngine for consistent rule application

**JobRunner.java** - Command line job execution:
- Implements CommandLineRunner for programmatic job execution
- Supports conditional job execution based on command line arguments
- Handles job parameter generation and execution logging

### Spring Batch Setup
- Uses `@EnableBatchProcessing` annotation
- Configured with PostgreSQL database for metadata storage
- Job auto-execution is disabled (`spring.batch.job.enabled=false`)
- Batch metadata tables initialized automatically with `BATCH_` prefix
- JobRunner provides programmatic job execution control

### Database Configuration
- PostgreSQL database (`jdbc:postgresql://localhost:5432/poc`)
- Connection details: username=`xinhua.gu`, password=(empty)
- Hibernate with `update` DDL strategy
- HikariCP connection pooling with max 10 connections, 30s timeout
- SQL logging enabled for debugging with formatted output
- H2 database used for tests with in-memory configuration
- Test database uses create-drop DDL strategy for clean test isolation

### Testing Setup
- Uses `@SpringBootTest` for integration tests
- `TestBatchConfiguration.java` provides test-specific batch setup
- Test profile uses separate `application-test.yml` configuration
- BatchConfigTest demonstrates job execution testing patterns

## Development Notes

### Configuration Files
- `application.yml` - Main application configuration with detailed logging setup
- `application-test.yml` - Test-specific overrides
- Logging configured for Spring Batch debugging with custom patterns

### Management Endpoints
Available actuator endpoints: `/actuator/health`, `/actuator/info`, `/actuator/metrics`, `/actuator/beans`

### Rule-Based Data Processing

**Rule Definition Format** (`src/main/resources/rules.txt`):
```
FIELD_NAME:RULE_TYPE:CONDITION:ACTION
```

**Current Rules:**
- `age:CATEGORIZE:<=25:Young` - Ages 25 and below become "Young"  
- `age:CATEGORIZE:26-40:Adult` - Ages 26-40 become "Adult"
- `age:CATEGORIZE:>40:Senior` - Ages over 40 become "Senior"
- `name:TRANSFORM:*:UPPERCASE` - All names converted to uppercase

**Rule Types:**
- **CATEGORIZE**: Converts numeric values to categories based on conditions
  - Supports `<=`, `>`, and range conditions (`26-40`)
- **TRANSFORM**: Applies text transformations to string fields
  - Supports `UPPERCASE`, `LOWERCASE`, `TITLECASE` actions

### Current Batch Jobs
- `clientToCsvJob` - Exports client data from PostgreSQL to CSV with rule-based transformations
  - Reads from `client` table using JdbcCursorItemReader
  - Processes data through ClientRuleProcessor applying transformation rules
  - Writes to CSV file (`clients_export.csv`) using FlatFileItemWriter
  - CSV includes: id, name, age, ageCategory columns
  - Header row automatically generated

### Extending the Application

**Adding New Batch Jobs:**
1. Create Job beans in BatchConfig following the existing `clientToCsvJob` pattern
2. Define Steps using StepBuilder with ItemReader/ItemProcessor/ItemWriter pattern
3. Add job execution logic to JobRunner for command-line triggering
4. Configure appropriate ItemReaders for data sources (JDBC, JPA, File)
5. Add corresponding unit tests following BatchConfigTest structure

**Adding New Rule Types:**
1. Add new rule type constants to RuleEngine (e.g., "VALIDATE", "MASK")
2. Implement rule logic in RuleEngine.applyRule() method
3. Update rules.txt with new rule definitions
4. Add unit tests in RuleEngineTest
5. Update processors to handle new transformed fields if needed

**Adding New Processors:**
1. Implement ItemProcessor<InputType, OutputType> interface
2. Inject RuleEngine dependency for rule-based transformations
3. Register as @Component for Spring dependency injection
4. Wire into BatchConfig steps as needed

### Data Processing Patterns
- **Database to File**: JdbcCursorItemReader + ItemProcessor + FlatFileItemWriter
- **File to Database**: FlatFileItemReader + ItemProcessor + JdbcBatchItemWriter  
- **Rule-Based Transformation**: ItemProcessor with RuleEngine integration
- **Multi-Field Processing**: Single processor handling multiple field transformations

### Development Files
- `create_client_table.sql` - PostgreSQL table creation script
- `insert_test_data.sql` - Sample data for testing
- `src/test/resources/data.sql` - Test data initialization
- `src/test/resources/schema.sql` - Test database schema

The application demonstrates a fully functional rule-based batch processing system with PostgreSQL integration and extensible transformation capabilities.