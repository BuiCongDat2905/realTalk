//package com.chatBox.realTalk;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
//import org.springframework.test.context.ActiveProfiles;
//import org.testcontainers.containers.MySQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//@Testcontainers
//@ActiveProfiles("test")
//@SpringBootTest
//class RealTalkApplicationTests {
//
//    @Container
//    @ServiceConnection
//    static MySQLContainer<?> mysql =
//            new MySQLContainer<>("mysql:8.0")
//                    .withDatabaseName("realtalk_test")
//                    .withUsername("test")
//                    .withPassword("test");
//
//    @Test
//    void contextLoads() {
//    }
//}