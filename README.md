# Spring Batch Rule POC 1

## 项目概述
这是一个基于Spring Boot 3和Spring Batch的POC项目，用于演示批处理和规则引擎的集成。

## 技术栈
- **Java**: 17
- **Spring Boot**: 3.2.5
- **Spring Batch**: 5.x
- **数据库**: H2 (内存数据库)
- **构建工具**: Maven
- **其他**: Lombok, Spring Data JPA

## 项目结构
```
spring-batch-rule-poc-1/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/accenture/poc1/
│   │   │       ├── Application.java           # Spring Boot主类
│   │   │       └── config/
│   │   │           └── BatchConfig.java       # Spring Batch配置
│   │   └── resources/
│   │       └── application.yml                # 应用配置文件
│   └── test/
│       ├── java/
│       │   └── com/accenture/poc1/
│       │       ├── ApplicationTests.java      # 应用测试类
│       │       └── config/
│       │           └── BatchConfigTest.java   # Batch配置测试
│       └── resources/
│           └── application-test.yml           # 测试环境配置
├── pom.xml                                     # Maven配置文件
└── README.md                                   # 项目文档
```

## 快速开始

### 前置条件
- JDK 17 或更高版本
- Maven 3.6 或更高版本

### 构建项目
```bash
cd spring-batch-rule-poc-1
mvn clean install
```

### 运行应用
```bash
# 方式1: 使用Maven Spring Boot插件
mvn spring-boot:run

# 方式2: 运行打包后的JAR文件
java -jar target/spring-batch-rule-poc-1-1.0.0-SNAPSHOT.jar
```

### 运行测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=BatchConfigTest

# 跳过测试构建
mvn clean install -DskipTests
```

## 功能特性

### 当前实现
- ✅ Spring Batch基础框架配置
- ✅ H2内存数据库集成
- ✅ 示例Job和Step实现
- ✅ Tasklet示例
- ✅ 单元测试框架

### 主要组件说明

#### Application.java
Spring Boot应用入口点，使用`@SpringBootApplication`注解启动应用。

#### BatchConfig.java
Spring Batch配置类，包含：
- `sampleJob`: 示例批处理作业
- `sampleStep`: 示例步骤
- `sampleTasklet`: 示例任务单元，输出执行信息

#### application.yml
应用配置文件，包含：
- H2数据库配置
- Spring Batch配置
- JPA配置
- 日志配置
- 服务器端口配置

## 访问管理端点

### H2控制台
应用启动后，可通过以下URL访问H2数据库控制台：
```
http://localhost:8080/h2-console
```
连接参数：
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (留空)

### 健康检查端点
```
http://localhost:8080/actuator/health
http://localhost:8080/actuator/info
http://localhost:8080/actuator/metrics
```

## 执行批处理作业

### 通过REST API执行（需要额外配置Controller）
```bash
# 示例：触发sampleJob
curl -X POST http://localhost:8080/jobs/sampleJob
```

### 通过JobLauncher编程执行
```java
@Autowired
private JobLauncher jobLauncher;

@Autowired
private Job sampleJob;

public void runJob() throws Exception {
    JobParameters params = new JobParametersBuilder()
        .addLong("time", System.currentTimeMillis())
        .toJobParameters();
    
    JobExecution execution = jobLauncher.run(sampleJob, params);
    System.out.println("Job Status: " + execution.getStatus());
}
```

## 扩展开发指南

### 添加新的Job
1. 在`BatchConfig`类中创建新的Job Bean
2. 定义相关的Step
3. 实现ItemReader、ItemProcessor、ItemWriter或Tasklet

### 添加规则引擎
1. 添加规则引擎依赖（如Drools）
2. 创建规则配置类
3. 在Step中集成规则处理

### 数据源配置
如需使用其他数据库，修改`application.yml`中的datasource配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/batchdb
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: password
```

## 常见问题

### Q: 如何禁用Job自动执行？
A: 在`application.yml`中设置`spring.batch.job.enabled=false`

### Q: 如何查看批处理执行历史？
A: 查询Spring Batch元数据表：
- BATCH_JOB_INSTANCE
- BATCH_JOB_EXECUTION
- BATCH_STEP_EXECUTION

### Q: 如何配置并行处理？
A: 在Step配置中使用`taskExecutor`和`throttleLimit`：
```java
.tasklet(tasklet())
.taskExecutor(taskExecutor())
.throttleLimit(10)
```

## 项目维护
- **作者**: Accenture POC Team
- **版本**: 1.0.0-SNAPSHOT
- **许可**: 内部使用

## 后续计划
- [ ] 添加REST控制器用于Job管理
- [ ] 集成规则引擎（Drools/Easy Rules）
- [ ] 添加更多批处理模式示例
- [ ] 实现错误处理和重试机制
- [ ] 添加监控和报告功能