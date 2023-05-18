package com.geonho1943.LFG.model;

import com.geonho1943.LFG.controller.HomeController;
import com.geonho1943.LFG.dto.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(locations = "/application-test.properties")
@SpringBootTest
class UserModelTest {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    private final Logger LOGGER = LoggerFactory.getLogger(UserModelTest.class);

    @BeforeEach
    public void setUp() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String createSchemaQuery = "CREATE SCHEMA IF NOT EXISTS LFGSERVICE;";
        String createTableSql = "CREATE TABLE IF NOT EXISTS LFGservice.lfg_user"
        + "(user_idx INT AUTO_INCREMENT NOT NULL, user_id VARCHAR(45) NOT NULL,"
        + "user_pw VARCHAR(45) NOT NULL, user_name VARCHAR(45) NOT NULL, user_reg TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
        + "PRIMARY KEY (user_idx), UNIQUE (user_idx))";
        jdbcTemplate.execute(createSchemaQuery);
        jdbcTemplate.execute(createTableSql);
        LOGGER.info("H2 데이터베이스의 스키마,테이블 생성이 완료 되었습니다.");
    }
    @AfterEach
    public void afterEach(){
//        UserRepository
    }
    public void roleSetUp(){
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String createSchemaQuery = "CREATE SCHEMA IF NOT EXISTS LFGSERVICE;";
        String createTableSql = "CREATE TABLE IF NOT EXISTS LFGservice.lfg_user_role"
        + " (`user_idx` int(11) NOT NULL,`user_role` int(11) NOT NULL,"
        + "PRIMARY KEY (`user_idx`), UNIQUE KEY `user_idx_UNIQUE` (`user_idx`))";
        jdbcTemplate.execute(createSchemaQuery);
        jdbcTemplate.execute(createTableSql);
        LOGGER.info("H2 데이터베이스의 스키마,테이블 생성이 완료 되었습니다.");
    }

    @Test
    void join() throws SQLException {
        //given
        User user = new User();
        user.setUser_id("test_id");
        user.setUser_pw("test_pw");
        user.setUser_name("test_name");

        //when
        User savedUser = userRepository.join(user);

        //then
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM LFGservice.lfg_user WHERE user_id = ?")) {
            pstmt.setString(1, "test_id");
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(rs.getString("user_id"), savedUser.getUser_id());
                assertEquals(rs.getString("user_name"), savedUser.getUser_name());
            }
        }
        LOGGER.info("join 검증이 완료 되었습니다.");
    }

    @Test
    void login() throws SQLException {
        // given
        User user = new User();
        user.setUser_id("test_id");
        user.setUser_pw("test_pw");
        user.setUser_name("test_name");
        userRepository.join(user);

        // when
        User loginUser = new User();
        loginUser.setUser_id("test_id");
        loginUser.setUser_pw("test_pw");
        User loggedInUser = userRepository.login(loginUser);

        // then
        assertNotNull(loggedInUser);
        assertEquals("test_id", loggedInUser.getUser_id());
        assertEquals("test_name", loggedInUser.getUser_name());

        LOGGER.info("login 검증이 완료 되었습니다.");
    }

    @Test
    void loginFail() {
        // Given
        User user = new User();
        user.setUser_id("test_id");
        user.setUser_pw("invalid_pw");

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userRepository.login(user);
        });

        //Then
        assertEquals("java.sql.SQLException: 회원정보를 다시 입력하세요", exception.getMessage());
        LOGGER.info("login 예외 검증이 완료 되었습니다.");
    }

    @Test
    void role() throws SQLException {
        // Given
        roleSetUp();
        User user = new User();
        user.setUser_idx(1);

        // When
        User assignedUser = userRepository.role(user);

        // Then
        assertEquals(user.getUser_idx(), assignedUser.getUser_idx());
        LOGGER.info("역할 할당 검증이 완료되었습니다.");
    }

    @Test
    void auth() {
        // given
        // when
        // then
    }

    @Test
    void check() {
        // given
        // when
        // then
    }

    @Test
    void modify() {
        // given
        // when
        // then
    }


    @Test
    void sleep() throws SQLException {
        // Given
        User user = new User();
        user.setUser_id("test_id");
        user.setUser_pw("test_pw");
        user.setUser_name("test_name");

        // 회원 등록
        User registeredUser = userRepository.join(user);

        // When
        userRepository.sleep(registeredUser);

        // Then
        try {
            userRepository.login(user);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("java.sql.SQLException: 회원정보를 다시 입력하세요", e.getMessage());
        }
        LOGGER.info("로그인 예외 처리 검증이 완료되었습니다.");
    }



}
