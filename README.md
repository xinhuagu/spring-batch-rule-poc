# Spring Batch Rule POC

## Project Overview
A Spring Boot 3 application demonstrating Spring Batch integration with an enhanced JSON-based rule engine for comprehensive data transformation. This POC showcases database-to-CSV export with a priority-based rule system supporting validation, masking, formatting, and transformation rules.

## Technology Stack
- **Java**: 17
- **Spring Boot**: 3.2.5
- **Spring Batch**: 5.x
- **Database**: PostgreSQL (H2 for tests)
- **Build Tool**: Maven
- **Libraries**: Lombok, Jackson, Spring Data JPA, HikariCP

## Project Structure
```
spring-batch-rule-poc/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/accenture/poc1/
│   │   │       ├── Application.java                    # Spring Boot main class
│   │   │       ├── config/BatchConfig.java             # Spring Batch configuration
│   │   │       ├── model/Client.java                   # Client entity model
│   │   │       ├── processor/ClientRuleProcessor.java  # Rule-based processor
│   │   │       ├── rule/
│   │   │       │   ├── EnhancedRuleEngine.java        # Enhanced rule engine
│   │   │       │   └── model/
│   │   │       │       ├── Rule.java                  # Rule model with conditions
│   │   │       │       └── RuleConfiguration.java     # JSON configuration model
│   │   │       └── runner/JobRunner.java              # Command line job runner
│   │   └── resources/
│   │       ├── application.yml                        # Main configuration
│   │       ├── application-test.yml                   # Test configuration
│   │       └── rules.json                            # Rule definitions (JSON)
│   └── test/
│       └── java/com/accenture/poc1/
│           ├── config/BatchConfigTest.java            # Batch job tests
│           └── rule/EnhancedRuleEngineTest.java      # Rule engine tests
├── pom.xml                                            # Maven configuration
├── CLAUDE.md                                          # AI assistance guide
└── README.md                                          # This file
```

## Quick Start

### Prerequisites
- JDK 17 or higher
- Maven 3.6 or higher
- PostgreSQL database running on localhost:5432

### Database Setup
```bash
# Create database and user
createdb poc
createuser xinhua.gu

# Create client table
psql -U xinhua.gu -d poc -f create_client_table.sql

# Load sample data
psql -U xinhua.gu -d poc -f insert_test_data.sql
```

### Build Project
```bash
mvn clean install
```

### Run Application
```bash
# Run with default configuration
mvn spring-boot:run

# Execute specific batch job
mvn spring-boot:run -Dspring-boot.run.arguments="clientToCsv"

# Run from packaged JAR
java -jar target/spring-batch-rule-poc-1-1.0.0-SNAPSHOT.jar clientToCsv
```

### Run Tests
```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=EnhancedRuleEngineTest

# Skip tests during build
mvn clean install -DskipTests
```

## Enhanced Rule Engine Features

### Rule Types (Priority Order)
1. **VALIDATE** (priority 1-5): Data validation with configurable actions
   - Reject invalid data (returns null)
   - Apply default values
   - Skip validation

2. **MASK** (priority 5-20): Privacy protection through data masking
   - Full masking: Replace entire value
   - Partial masking: Show first/last N characters
   - Format-preserving: Maintain structure (e.g., XXX-XX-1234 for SSN)

3. **REPLACE** (priority 30): Pattern-based text replacement
   - Regex pattern matching
   - Case-sensitive/insensitive options
   - Profanity filtering

4. **FORMAT** (priority 50): Data formatting
   - Phone numbers: (555) 123-4567
   - Dates: MM/dd/yyyy, yyyy-MM-dd
   - Numbers: Decimal, currency formatting

5. **CATEGORIZE** (priority 100): Numeric-to-category conversion
   - Age groups: Young (≤25), Adult (26-40), Senior (>40)
   - Range-based categorization

6. **TRANSFORM** (priority 200): Text transformations
   - UPPERCASE, lowercase, TitleCase
   - Trim, Reverse

### Rule Configuration (rules.json)
```json
{
  "rules": [
    {
      "id": "mask-chen-names",
      "fieldName": "name",
      "type": "MASK",
      "priority": 10,
      "enabled": true,
      "description": "Mask names containing CHEN",
      "condition": {
        "operator": "CONTAINS",
        "value": "CHEN",
        "caseSensitive": false
      },
      "action": {
        "maskType": "FULL",
        "maskChar": "*",
        "length": 9
      }
    }
  ]
}
```

### Condition Operators
- **EQUALS**: Exact match (case-sensitive option)
- **CONTAINS**: Substring match
- **STARTS_WITH** / **ENDS_WITH**: Prefix/suffix matching
- **REGEX**: Regular expression patterns
- **BETWEEN**: Numeric range (min/max)
- **Comparison**: >, >=, <, <=
- **NOT_NULL**: Non-empty validation

## Key Components

### EnhancedRuleEngine
- Loads rules from JSON configuration
- Priority-based rule execution
- Short-circuit processing for MASK/VALIDATE rules
- Performance metrics tracking
- Hot reload capability via `reloadRules()`
- Field-indexed rule lookup for efficiency

### ClientRuleProcessor
- Spring Batch ItemProcessor implementation
- Applies EnhancedRuleEngine rules to Client entities
- Transforms name and age fields
- Adds computed ageCategory field

### BatchConfig
- Defines `clientToCsvJob` for database-to-CSV export
- Chunk-oriented processing (size: 10)
- JdbcCursorItemReader for database reading
- FlatFileItemWriter for CSV generation

## Current Rules Configuration

The system includes 12 pre-configured rules:

### Privacy Protection
- Mask names containing "CHEN" with 9 asterisks
- Mask exact name "DAVID THOMAS" with 7 asterisks
- SSN partial masking (XXX-XX-1234 format)

### Data Validation
- Email format validation with rejection on invalid
- Phone number validation with default value fallback

### Data Formatting
- Phone number formatting to (XXX) XXX-XXXX
- Date formatting from yyyy-MM-dd to MM/dd/yyyy

### Text Processing
- Profanity replacement with asterisks
- Name transformation to UPPERCASE

### Categorization
- Age groups: Young (≤25), Adult (26-40), Senior (>40)

## Performance & Monitoring

### Rule Metrics
```java
// Get execution metrics
Map<String, RuleMetrics> metrics = ruleEngine.getRuleMetrics();
// Provides: execution count, total time, average time per rule
```

### Batch Job Monitoring
- Spring Batch metadata tables track job execution
- Actuator endpoints for health and metrics
- Detailed logging with custom patterns

## Database Configuration

### PostgreSQL (Production)
```yaml
url: jdbc:postgresql://localhost:5432/poc
username: xinhua.gu
password: (empty)
pool-size: 10
timeout: 30s
```

### H2 (Testing)
- In-memory database
- Auto-schema generation
- Test data isolation

## Management Endpoints

```
http://localhost:8080/actuator/health   # Health status
http://localhost:8080/actuator/info     # Application info
http://localhost:8080/actuator/metrics  # Performance metrics
http://localhost:8080/actuator/beans    # Bean information
```

## Testing Strategy

### Unit Tests
- Individual rule type validation
- Condition operator testing
- Edge case handling

### Integration Tests
- Full batch job execution
- Database interaction
- CSV output validation

### Test Coverage
- 16 comprehensive test scenarios
- All rule types covered
- Error handling verification

## Development Guide

### Adding New Rule Types
1. Add case in `EnhancedRuleEngine.applyRule()` switch
2. Implement `apply[RuleType]Rule()` method
3. Define rules in `rules.json`
4. Add test cases in `EnhancedRuleEngineTest`

### Creating New Batch Jobs
1. Define Job bean in `BatchConfig`
2. Configure ItemReader, ItemProcessor, ItemWriter
3. Add execution logic to `JobRunner`
4. Create integration tests

### Extending Rule Conditions
1. Add operator in `Rule.RuleCondition.evaluate()`
2. Implement comparison logic
3. Update rules.json with examples
4. Document in README

## Troubleshooting

### Common Issues

**Q: Rules not applying?**
- Check rule priority order
- Verify field name matches exactly
- Ensure rule is enabled in JSON
- Check condition evaluation

**Q: Batch job not executing?**
- Verify `spring.batch.job.enabled=false` in application.yml
- Check command line arguments
- Review Spring Batch metadata tables

**Q: Database connection issues?**
- Confirm PostgreSQL is running
- Verify credentials in application.yml
- Check connection pool settings

## Project Roadmap

### Completed ✅
- JSON-based rule configuration
- Priority-based rule execution
- VALIDATE, MASK, REPLACE, FORMAT rule types
- Performance metrics tracking
- Hot reload capability
- Comprehensive test suite

### Planned Features
- [ ] REST API for rule management
- [ ] Rule versioning and audit trail
- [ ] Complex conditional rules (AND/OR logic)
- [ ] Rule conflict detection
- [ ] GraphQL API for rule queries
- [ ] Real-time rule updates via WebSocket
- [ ] Rule testing sandbox
- [ ] Export to multiple formats (JSON, XML, Parquet)

## Repository Information
- **Repository**: https://github.com/xinhuagu/spring-batch-rule-poc
- **Version**: 1.0.0-SNAPSHOT
- **License**: Internal Use Only

## Contributors
- Accenture POC Team
- Enhanced by Claude AI Assistant