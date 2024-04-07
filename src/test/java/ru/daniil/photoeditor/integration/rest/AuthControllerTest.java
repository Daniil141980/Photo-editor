package ru.daniil.photoeditor.integration.rest;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthControllerTest extends AbstractRestTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        jdbcTemplate.update("DELETE FROM tokens");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    @DisplayName("Test login success")
    public void loginSuccess() throws JSONException {
        jdbcTemplate.update(
                "INSERT INTO users VALUES (default, ?, ?)",
                "correctUsername",
                passwordEncoder.encode("correctPassword")
        );

        var requestEntity = new HttpEntity<>(
                """
                        {
                            "username": "correctUsername",
                            "password": "correctPassword"
                        }
                        """, httpHeaders
        );

        var responseEntity = restTemplate.exchange(
                createURLWithPort("/auth/login"),
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("correctUsername", new JSONObject(responseEntity.getBody()).getString("username"));
        assertThat(responseEntity.getHeaders().get("Set-Cookie")).isNotNull().anyMatch(it -> it.startsWith("token="));
    }

    @Test
    @DisplayName("Test login bad request")
    public void loginBadRequest() {
        var requestEntity = new HttpEntity<>(
                """
                        {
                            "username": "",
                            "password": ""
                        }
                        """, httpHeaders
        );

        var responseEntity = restTemplate.exchange(
                createURLWithPort("/auth/login"),
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    @DisplayName("Test login failed")
    public void loginFailed() throws JSONException {
        var requestEntity = new HttpEntity<>(
                """
                        {
                            "username": "username",
                            "password": "password"
                        }
                        """, httpHeaders
        );

        var responseEntity = restTemplate.exchange(
                createURLWithPort("/auth/login"),
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        var expected = """
                {
                    "success": false,
                    "message": "Wrong username or password"
                }
                """;

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        JSONAssert.assertEquals(expected, responseEntity.getBody(), JSONCompareMode.STRICT);
    }

    @Test
    @DisplayName("Test register success")
    public void register() throws JSONException {
        var requestEntity = new HttpEntity<>(
                """
                        {
                            "username": "username",
                            "password": "password"
                        }
                        """, httpHeaders
        );

        var responseEntity = restTemplate.exchange(
                createURLWithPort("/auth/register"),
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("username", new JSONObject(responseEntity.getBody()).getString("username"));
        assertThat(responseEntity.getHeaders().get("Set-Cookie")).isNotNull().anyMatch(it -> it.startsWith("token="));
    }

    @Test
    @DisplayName("Test register failed")
    public void registerFailed() throws JSONException {
        jdbcTemplate.update(
                "INSERT INTO users VALUES (default, ?, ?)",
                "correctUsername",
                passwordEncoder.encode("correctPassword")
        );

        var requestEntity = new HttpEntity<>(
                """
                        {
                            "username": "correctUsername",
                            "password": "password"
                        }
                        """, httpHeaders
        );

        var responseEntity = restTemplate.exchange(
                createURLWithPort("/auth/register"),
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        var expected = """
                {
                    "success": false,
                    "message": "User:correctUsername already exist"
                }
                """;

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        JSONAssert.assertEquals(expected, responseEntity.getBody(), JSONCompareMode.STRICT);
    }

    @Test
    @DisplayName("Test register bad request")
    public void registerBadRequest() {
        var requestEntity = new HttpEntity<>(
                """
                        {
                            "username": "x",
                            "password": "x"
                        }
                        """, httpHeaders
        );

        var responseEntity = restTemplate.exchange(
                createURLWithPort("/auth/register"),
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }
}
