Bối cảnh dự án RealTalk

Tôi đang xây dựng Backend cho dự án RealTalk, một hệ thống chat real-time tương tự Facebook Messenger.

Công nghệ hiện tại
Backend: Java 21
Framework: Spring Boot 4.1.0
Build tool: Maven
Database: MySQL 8.0 chạy bằng Docker
Database name: realTalk_db
MySQL host port: 3307
MySQL container port: 3306
Frontend dự kiến: ReactJS
Realtime dự kiến: WebSocket/STOMP
Cache/Presence dự kiến: Redis
Lưu file dự kiến: MinIO

Package gốc hiện tại:

com.chatBox.realTalk

Dự án được định hướng theo Modular Monolith vì quy mô mục tiêu khoảng 1.000 người dùng. Các module dự kiến gồm Identity/Auth, User, Conversation, Messaging, Presence, Attachment, Search, Notification và Admin/Audit.

Mục tiêu ban đầu đã thống nhất

Không tạo toàn bộ hệ thống ngay từ đầu. Trước tiên chỉ khởi tạo các thành phần nền tảng dùng chung:

config/
common/
response/
exception/
constant/
util/
system/

Các class nền tảng dự kiến:

JpaAuditingConfig
ActiveProfileLogger
BaseEntity
ApiResponse
ApiErrorResponse
PageResponse
ErrorCode
AppException
GlobalExceptionHandler
ApiConstants
PaginationUtils
SystemController

Sau khi nền tảng hoạt động mới làm theo thứ tự:

1. User
2. Authentication
3. Conversation
4. Messaging REST
5. WebSocket
6. Read receipt
7. Presence
8. Attachment
9. Reply/Recall/Delete-for-me
10. Search và Notification
Cấu trúc tài nguyên dự kiến
src/
├── main/
│   ├── java/com/chatBox/realTalk/
│   │   ├── RealTalkApplication.java
│   │   ├── config/
│   │   ├── common/
│   │   ├── response/
│   │   ├── exception/
│   │   ├── constant/
│   │   ├── util/
│   │   └── system/
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-local.yml
│       └── db/
│           └── migration/
│               └── V1__create_flyway_test.sql
│
└── test/
    ├── java/
    └── resources/
        └── application-test.yml
Ý nghĩa ba file cấu hình
application.yml

Chứa cấu hình chung cho tất cả môi trường:

Tên ứng dụng
Profile mặc định
JPA chung
Jackson chung
Server port
Logging
Actuator
Cấu hình lỗi mặc định
application-local.yml

Chỉ dùng khi chạy trên máy cá nhân:

MySQL datasource
Flyway
Redis local sau này
Logging SQL chi tiết
Secret local thông qua biến môi trường
application-test.yml

Dùng khi chạy test:

Profile test
Datasource Testcontainers
Flyway cho database test
Logging ít hơn
Cấu hình riêng cho integration test

Khi chạy local, Spring ghép:

application.yml + application-local.yml
Cấu hình application.yml hiện tại

Cấu hình ban đầu của tôi có một lỗi Jackson bị lồng sai:

spring:
  jackson:
    default-property-inclusion: non_null
    spring:
      jackson:
        datatype:
          datetime:
            write-dates-as-timestamps: false

Phần spring.jackson bên trong spring.jackson là sai và cần xóa.

Cấu hình đề xuất đã sửa:

spring:
  application:
    name: realTalk

  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}

  jackson:
    default-property-inclusion: non_null

  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
        jdbc:
          time_zone: UTC

server:
  port: ${SERVER_PORT:8080}

  error:
    include-message: never
    include-stacktrace: never
    include-binding-errors: never

management:
  endpoints:
    web:
      exposure:
        include: health,info

  endpoint:
    health:
      show-details: always

logging:
  level:
    root: INFO
    com.chatBox.realTalk: DEBUG

  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"

Lưu ý: trước đó Jackson của Spring Boot 4 báo lỗi liên quan đến:

tools.jackson.databind.SerializationFeature

và không chấp nhận:

write-dates-as-timestamps: false

nên hiện tại đã tạm bỏ cấu hình đó.

Cấu hình application-local.yml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3307}/${DB_NAME:realTalk_db}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:123456}
    driver-class-name: com.mysql.cj.jdbc.Driver

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: false
    validate-on-migrate: true

logging:
  level:
    org.flywaydb: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.orm.jdbc.bind: TRACE

Các biến như:

${DB_HOST:localhost}

có nghĩa là:

Nếu có biến môi trường DB_HOST thì dùng biến đó.
Nếu không có thì dùng localhost.
Profile local đã hoạt động

Tôi đã tạo ActiveProfileLogger và log xác nhận:

2026-08-07 07:00:08.004 [restartedMain] INFO  c.c.r.config.ActiveProfileLogger - Active profiles: [local]
2026-08-07 07:00:08.005 [restartedMain] INFO  c.c.r.config.ActiveProfileLogger - Default profiles: [default]

Như vậy:

application.yml đã được đọc
application-local.yml đã được kích hoạt
Profile local không phải nguyên nhân Flyway không chạy
MySQL và JPA đã kết nối thành công

Log hiện tại:

Database JDBC URL [jdbc:mysql://localhost:3307/realTalk_db?...]
Database driver: MySQL Connector/J
Database dialect: MySQLDialect
Database version: 8.0.46
Default catalog/schema: realTalk_db/undefined
Isolation level: REPEATABLE_READ

Ngoài ra còn có:

Initialized JPA EntityManagerFactory for persistence unit 'default'
Tomcat started on port 8080
Started RealTalkApplication

Điều này xác nhận:

MySQL đang chạy
Port 3307 đúng
Database realTalk_db tồn tại
Username/password datasource đúng
Spring Data JPA hoạt động
Hibernate kết nối được database
Ứng dụng chạy trên port 8080

Log:

HHH000489: No JTA platform available

không phải lỗi. Với một datasource MySQL thông thường không cần JTA.

Flyway là gì?

Flyway là công cụ quản lý phiên bản cấu trúc database bằng migration SQL.

Ví dụ:

V1__create_users.sql
V2__create_user_profiles.sql
V3__create_conversations.sql
V4__create_messages.sql

Khi ứng dụng khởi động:

Flyway đọc db/migration
→ kiểm tra migration nào chưa chạy
→ chạy SQL
→ tạo bảng nghiệp vụ
→ ghi lịch sử vào flyway_schema_history

Flyway quản lý bảng trong database nhưng database realTalk_db phải tồn tại trước.

Bảng:

flyway_schema_history

phải do Flyway tự tạo. Không nên tự tạo bằng SQL.

Vấn đề hiện tại

Mặc dù:

Profile local hoạt động
MySQL kết nối được
JPA hoạt động
spring.flyway.enabled=true

nhưng:

Không có bất kỳ log Flyway nào
Không có bảng flyway_schema_history
Migration không chạy

Do ứng dụng vẫn khởi động thành công mà hoàn toàn không log Flyway, nguyên nhân khả năng cao là:

Dependency Flyway chưa được Maven resolve hoặc Flyway chưa có trong classpath
Phiên bản Spring Boot thực tế

Trong pom.xml:

<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.1.0</version>
    <relativePath/>
</parent>

Java:

<properties>
    <java.version>21</java.version>
</properties>
pom.xml hiện tại

Các dependency chính:

spring-boot-starter-data-elasticsearch
spring-boot-starter-data-jpa
spring-boot-starter-security
spring-boot-starter-security-oauth2-client
spring-boot-starter-validation
spring-boot-starter-webmvc
mysql-connector-j
spring-boot-devtools
lombok

Các dependency test được Spring Initializr tạo theo Spring Boot 4:

spring-boot-starter-data-elasticsearch-test
spring-boot-starter-data-jpa-test
spring-boot-starter-security-oauth2-client-test
spring-boot-starter-security-test
spring-boot-starter-validation-test
spring-boot-starter-webmvc-test

Hiện tại mysql-connector-j bị khai báo hai lần, cần xóa một dependency trùng.

Dependency Flyway đã thử

Đã thử:

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-flyway</artifactId>
</dependency>

<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>

Nhưng Maven/IDE báo:

Dependency 'org.flywaydb:flyway-mysql:' not found

Trước đó cũng đã thử:

<groupId>com.redgate.flyway</groupId>
<artifactId>flyway-mysql</artifactId>

và cũng không dùng được.

com.redgate.flyway không phải lựa chọn cần dùng cho bản open-source thông thường. GroupId dự kiến đúng là:

org.flywaydb
Giả thuyết gần nhất về lỗi Flyway

Dấu lỗi:

org.flywaydb:flyway-mysql:

có dấu : ở cuối nhưng không có version, cho thấy Maven không xác định được version của dependency.

Giải pháp đang được đề xuất thử là:

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-flyway</artifactId>
</dependency>

<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
    <version>${flyway.version}</version>
</dependency>

Hoặc kiểm tra giá trị version Spring Boot quản lý:

mvn help:evaluate "-Dexpression=flyway.version" -q -DforceStdout

Tuy nhiên, cần xác minh chính xác Spring Boot 4.1.0 đang quản lý artifact và version Flyway nào, vì đây là phiên bản mới.

Các lệnh cần chạy tiếp theo
1. Kiểm tra Maven có dependency Flyway hay không
mvn dependency:tree | Select-String "flyway"

Nếu không trả ra dòng nào thì Flyway chưa có trong classpath.

2. Ép Maven cập nhật dependency
mvn -U clean compile
3. Kiểm tra property version của Flyway
mvn help:evaluate "-Dexpression=flyway.version" -q -DforceStdout
4. Kiểm tra dependency trực tiếp

Sau khi biết version, ví dụ <VERSION>:

mvn dependency:get "-Dartifact=org.flywaydb:flyway-mysql:<VERSION>" -U
5. Kiểm tra Maven settings
mvn help:effective-settings

Cần kiểm tra:

Maven có đang Work offline không
Có mirror nào chặn Maven Central không
Có lỗi mạng/chứng chỉ không
6. Nếu Maven từng cache dependency lỗi
Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository\org\flywaydb\flyway-mysql"

Sau đó:

mvn -U clean compile
Migration thử nghiệm cần tạo

Đường dẫn:

src/main/resources/db/migration/V1__create_flyway_test.sql

Nội dung:

CREATE TABLE flyway_test (
    id BIGINT NOT NULL AUTO_INCREMENT,
    content VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_flyway_test PRIMARY KEY (id)
);

Kiểm tra file có trong source:

Get-ChildItem -Recurse .\src\main\resources\db\migration

Sau khi build, kiểm tra file đã được đưa vào classpath:

Get-ChildItem -Recurse .\target\classes\db\migration
Kết quả mong đợi khi Flyway hoạt động

Log phải có các dòng tương tự:

Database: jdbc:mysql://localhost:3307/realTalk_db
Successfully validated 1 migration
Creating Schema History table
Migrating schema to version "1 - create flyway test"
Successfully applied 1 migration

Database phải có:

flyway_schema_history
flyway_test

Kiểm tra:

USE realTalk_db;

SHOW TABLES;

Kiểm tra lịch sử:

SELECT *
FROM flyway_schema_history;
API kiểm tra hệ thống

Sau khi Flyway hoạt động, cần thêm Spring Boot Actuator:

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

Gọi:

GET http://localhost:8080/actuator/health

Kết quả dự kiến:

{
  "status": "UP"
}

Có thể tạo thêm endpoint riêng:

GET /api/v1/system/health

để kiểm tra format ApiResponse.

Kế hoạch sau khi sửa Flyway

Khi hoàn thành Flyway:

1. Tạo migration users
2. Tạo UserRole và UserStatus
3. Tạo User Entity
4. Tạo UserProfile Entity
5. Tạo UserRepository
6. Xây dựng POST /api/v1/auth/register
7. Validation đăng ký
8. BCrypt password
9. Xây dựng login
10. JWT access token
11. Refresh token/session
12. GET /api/v1/users/me

Không làm WebSocket trước Authentication và Conversation.