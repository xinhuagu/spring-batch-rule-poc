# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3 application demonstrating Spring Batch integration with a planned rule engine. The POC uses Java 17, Maven, and H2 in-memory database for batch processing operations.

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
```

## Architecture Overview

### Package Structure
- `com.accenture.poc1` - Root package
  - `Application.java` - Spring Boot main class
  - `config/BatchConfig.java` - Spring Batch configuration with job definitions

### Key Components

**BatchConfig.java** - Central batch configuration containing:
- `sampleJob` - Main batch job definition using JobBuilder
- `sampleStep` - Step implementation using StepBuilder  
- `sampleTasklet` - Simple tasklet that logs execution context information
- Uses constructor injection with JobRepository and PlatformTransactionManager

### Spring Batch Setup
- Uses `@EnableBatchProcessing` annotation
- Configured with H2 database for metadata storage
- Job auto-execution is disabled (`spring.batch.job.enabled=false`)
- Batch metadata tables initialized automatically
- Uses RunIdIncrementer for job parameter management

### Database Configuration
- H2 in-memory database (`jdbc:h2:mem:testdb`)
- H2 console enabled at `/h2-console` for development
- Connection details: username=`sa`, password=(empty)
- Hibernate with `create-drop` DDL strategy
- SQL logging enabled for debugging

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

### Extending the Application
When adding new Jobs:
1. Create Job beans in BatchConfig following the existing pattern
2. Define Steps using StepBuilder with appropriate transaction manager
3. Implement Tasklets or use ItemReader/ItemProcessor/ItemWriter pattern
4. Add corresponding unit tests following BatchConfigTest structure

The application is structured to accommodate future rule engine integration with the existing batch processing framework.