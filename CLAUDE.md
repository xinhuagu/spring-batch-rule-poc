# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3 application demonstrating Spring Batch integration with a planned rule engine. The POC uses Java 17, Maven, and PostgreSQL database for batch processing operations with CSV export capabilities.

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
  - `runner/JobRunner.java` - CommandLineRunner for executing batch jobs

### Key Components

**BatchConfig.java** - Central batch configuration containing:
- `clientToCsvJob` - PostgreSQL to CSV export batch job
- `clientToCsvStep` - Step that reads from PostgreSQL clients table and writes to CSV
- Uses ItemReader/ItemWriter pattern for data processing
- Configured with appropriate transaction management for database operations

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
- Connection details: username=`xinhua`, password=(empty)
- Hibernate with `update` DDL strategy
- HikariCP connection pooling with max 10 connections
- SQL logging enabled for debugging with formatted output
- H2 database used for tests only

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

### Current Batch Jobs
- `clientToCsvJob` - Exports client data from PostgreSQL to CSV file
  - Reads from `clients` table using JdbcCursorItemReader
  - Processes data through Client model objects
  - Writes to CSV file using FlatFileItemWriter
  - Configurable output file location and CSV format

### Extending the Application
When adding new Jobs:
1. Create Job beans in BatchConfig following the existing `clientToCsvJob` pattern
2. Define Steps using StepBuilder with ItemReader/ItemProcessor/ItemWriter pattern
3. Add job execution logic to JobRunner for command-line triggering
4. Configure appropriate ItemReaders for data sources (JDBC, JPA, File)
5. Add corresponding unit tests following BatchConfigTest structure

### Data Processing Patterns
- **Database to File**: Use JdbcCursorItemReader + FlatFileItemWriter
- **File to Database**: Use FlatFileItemReader + JdbcBatchItemWriter
- **Data Transformation**: Implement ItemProcessor for business logic

The application demonstrates PostgreSQL integration and is structured to accommodate future rule engine integration with the existing batch processing framework.