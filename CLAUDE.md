# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Important
- ALL instructions within this document MUST BE FOLLOWED, these are not optional unless explicitly stated.
- ASK FOR CLARIFICATION If you are uncertain of any of thing within the document.
- DO NOT edit more code than you have to.
- DO NOT WASTE TOKENS, be succinct and concise.

## Project Overview

Spring Boot 3 application demonstrating Spring Batch with an enhanced JSON-based rule engine for data transformation. Uses Java 17, Maven, PostgreSQL for production, and H2 for testing.

## Core Commands

### Build and Test
```bash
mvn clean compile                      # Compile only
mvn clean package                      # Build JAR and run tests
mvn clean install -DskipTests          # Build and install without tests
mvn test                              # Run all tests
mvn test -Dtest=EnhancedRuleEngineTest  # Run specific test class
mvn test -Dtest=EnhancedRuleEngineTest#testValidateRuleType  # Run specific test method
```

### Run Application
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="clientToCsv"  # Execute specific batch job
java -jar target/spring-batch-rule-poc-1-1.0.0-SNAPSHOT.jar clientToCsv  # Run from JAR
```

### Database Operations
```bash
psql -U xinhua.gu -d poc              # Connect to PostgreSQL database
psql -U xinhua.gu -d poc -f create_client_table.sql  # Execute SQL script
psql -U xinhua.gu -d poc -f insert_test_data.sql     # Load test data
```

## Architecture Overview

### Rule Engine System

The application uses **EnhancedRuleEngine** (`com.accenture.poc1.rule.EnhancedRuleEngine`) which loads rules from `src/main/resources/rules.json`. The engine supports:

- **Priority-based execution**: Rules execute in priority order (lower number = higher priority)
- **Short-circuit processing**: MASK and VALIDATE rules stop further processing when matched
- **Performance metrics**: Tracks execution count and time per rule
- **Hot reload**: `reloadRules()` method allows runtime rule updates

Rule types hierarchy:
1. **VALIDATE** (priority 1): Data validation with reject/default actions
2. **MASK** (priority 5-20): Full/partial/format-preserving masking
3. **REPLACE** (priority 30): Regex-based pattern replacement
4. **FORMAT** (priority 50): Date/phone/number formatting
5. **CATEGORIZE** (priority 100): Numeric to category conversion
6. **TRANSFORM** (priority 200): Text transformations (uppercase/lowercase/titlecase)

### Spring Batch Configuration

**BatchConfig** (`com.accenture.poc1.config.BatchConfig`) defines:
- `clientToCsvJob`: Main batch job reading from PostgreSQL `client` table
- `clientToCsvStep`: Processing step with chunk size 10
- Reader: `JdbcCursorItemReader` fetching from database
- Processor: `ClientRuleProcessor` applying `EnhancedRuleEngine` rules
- Writer: `FlatFileItemWriter` generating `clients_export.csv`

**JobRunner** (`com.accenture.poc1.runner.JobRunner`) handles programmatic job execution via CommandLineRunner interface.

### Rule Configuration Structure

Rules are defined in `rules.json` with this structure:
```json
{
  "rules": [{
    "id": "unique-id",
    "fieldName": "target-field",
    "type": "RULE_TYPE",
    "priority": 10,
    "enabled": true,
    "condition": {
      "operator": "EQUALS|CONTAINS|REGEX|BETWEEN|>|<=",
      "value": "match-value",
      "caseSensitive": false
    },
    "action": "string-or-object"
  }]
}
```

### Database Configuration

- **Production**: PostgreSQL at `localhost:5432/poc` (user: `xinhua.gu`, no password)
- **Testing**: H2 in-memory database with auto-schema generation
- **Connection Pool**: HikariCP with max 10 connections, 30s timeout
- **Hibernate**: DDL auto-update for development

## Key Implementation Details

### Rule Processing Flow

1. **EnhancedRuleEngine.applyRules()** receives field name and value
2. Rules are retrieved from pre-indexed map by field name
3. Rules execute in priority order
4. For VALIDATE/REPLACE rules, special handling bypasses condition check in main flow
5. MASK rules cause immediate return after application (short-circuit)
6. Metrics are tracked for each rule execution

### Adding New Rule Types

1. Add case in `EnhancedRuleEngine.applyRule()` switch statement
2. Implement `apply[RuleType]Rule()` method
3. Add rule definitions to `rules.json`
4. Create test cases in `EnhancedRuleEngineTest`

### Batch Job Data Flow

1. JobRunner checks command line arguments for job name
2. Launches job with timestamp parameter for uniqueness
3. JdbcCursorItemReader queries all clients from database
4. ClientRuleProcessor applies rules to each Client object:
   - Name field gets MASK/TRANSFORM rules
   - Age field gets CATEGORIZE rules
   - Creates new Client with transformed data and ageCategory
5. FlatFileItemWriter outputs CSV with headers

## Testing Strategy

- **Unit Tests**: Test individual rule types and conditions
- **Integration Tests**: `BatchConfigTest` verifies job execution
- **Rule Engine Tests**: `EnhancedRuleEngineTest` covers all 16 rule scenarios
- **Test Data**: Uses H2 with `src/test/resources/schema.sql` and `data.sql`

## Development SQL Scripts

- `create_client_table.sql`: PostgreSQL client table DDL
- `insert_test_data.sql`: Sample production data
- `add_more_clients.sql`: Additional test data with special cases
- `src/test/resources/`: H2 test database initialization

## Current Rule Configuration

The system includes 12 active rules in `rules.json`:
- Age categorization (Young/Adult/Senior)
- Name masking for privacy (CHEN names, DAVID THOMAS)
- SSN partial masking with format preservation
- Email/phone validation
- Phone/date formatting
- Profanity replacement
- Name uppercase transformation