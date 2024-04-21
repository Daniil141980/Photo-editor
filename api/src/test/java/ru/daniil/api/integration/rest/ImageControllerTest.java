package ru.daniil.api.integration.rest;

import jakarta.servlet.http.Cookie;
import org.apache.tomcat.util.http.Rfc6265CookieProcessor;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import ru.daniil.api.domains.UserEntity;
import ru.daniil.api.security.JwtService;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImageControllerTest extends AbstractRestTest {
    private final Rfc6265CookieProcessor cookieProcessor = new Rfc6265CookieProcessor();
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private JwtService jwtService;
    private HttpHeaders authorizedHttpHeaders;

    private HttpHeaders authorizedHttpHeadersContentTypeNull;

    private HttpHeaders anotherAuthorizedHttpHeaders;

    private UUID storedUUID;

    @BeforeAll
    public void setUp() {
        jdbcTemplate.update("DELETE FROM tokens");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM images");

        jdbcTemplate.update(
                "INSERT INTO users VALUES (default, ?, ?)",
                "username", "password"
        );

        jdbcTemplate.update(
                "INSERT INTO users VALUES (default, ?, ?)",
                "username2", "password"
        );

        authorizedHttpHeaders = createAuthorizedHttpHeaders("username");
        authorizedHttpHeaders.setContentType(MediaType.APPLICATION_JSON);
        authorizedHttpHeadersContentTypeNull = createAuthorizedHttpHeaders("username");
        anotherAuthorizedHttpHeaders = createAuthorizedHttpHeaders("username2");
    }

    private HttpHeaders createAuthorizedHttpHeaders(String username) {
        var headers = new HttpHeaders();
        headers.addAll(httpHeaders);
        var cookie = new Cookie("token", jwtService.generateAccessToken(
                new UserEntity(1L, username, "password")));
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setMaxAge(Math.toIntExact(Duration.ofMinutes(5).getSeconds()));
        headers.add(HttpHeaders.COOKIE, cookieProcessor.generateHeader(cookie, null));
        headers.setContentType(null);
        return headers;
    }

    @Test
    @Order(0)
    public void testAuthorizedHttpHeaders() {
        assertThat(authorizedHttpHeaders.get("Cookie")).isNotNull().hasSize(1);
    }

    @Test
    @Order(1)
    public void uploadImage() throws JSONException {
        var body = new LinkedMultiValueMap<>();
        body.add("file", new ClassPathResource("files/image.jpg"));
        var httpHeaders = new HttpHeaders(authorizedHttpHeaders);
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        var responseEntity = restTemplate.postForEntity(
                createURLWithPort("/image"),
                new HttpEntity<>(body, httpHeaders),
                String.class
        );

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        storedUUID = UUID.fromString(new JSONObject(responseEntity.getBody()).getString("imageId"));
    }

    @Test
    @Order(2)
    public void uploadImageWrongType() throws JSONException {
        var body = new LinkedMultiValueMap<>();
        body.add("file", new ClassPathResource("files/test.txt"));
        var httpHeaders = new HttpHeaders(authorizedHttpHeaders);
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        var responseEntity = restTemplate.postForEntity(
                createURLWithPort("/image"),
                new HttpEntity<>(body, httpHeaders),
                String.class
        );

        var expected = """
                    {
                        "success": false,
                        "message": "400 BAD_REQUEST: Not acceptable files content type"
                    }
                """.trim();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertEquals(expected, responseEntity.getBody(), JSONCompareMode.STRICT);
    }

    @Test
    @Order(3)
    public void getImage() throws IOException {
        var responseEntity = restTemplate.exchange(
                createURLWithPort("/image/" + storedUUID),
                HttpMethod.GET,
                new HttpEntity<>(authorizedHttpHeadersContentTypeNull),
                byte[].class
        );
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(
                new ClassPathResource("files/image.jpg").getInputStream().readAllBytes());
    }

    @Test
    @Order(4)
    public void getImageNotExists() throws JSONException {
        var randomUUID = UUID.randomUUID();
        var responseEntity = restTemplate.exchange(
                createURLWithPort("/image/" + randomUUID),
                HttpMethod.GET,
                new HttpEntity<>(authorizedHttpHeadersContentTypeNull),
                String.class
        );

        var expected = """
                    {
                        "success": false,
                        "message": "Image with id:%s not found"
                    }
                """.formatted(randomUUID);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertEquals(expected, responseEntity.getBody(), JSONCompareMode.STRICT);
    }

    @Test
    @Order(5)
    public void getImageUnauthorized() {
        var responseEntity = restTemplate.exchange(
                createURLWithPort("/image/" + storedUUID),
                HttpMethod.GET,
                null,
                byte[].class
        );

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(6)
    public void getImageWithOtherUser() throws JSONException {
        var responseEntity = restTemplate.exchange(
                createURLWithPort("/image/" + storedUUID),
                HttpMethod.GET,
                new HttpEntity<>(anotherAuthorizedHttpHeaders),
                String.class
        );

        var expected = """
                    {
                        "success": false,
                        "message": "Image with id:%s not found"
                    }
                """.formatted(storedUUID);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertEquals(expected, responseEntity.getBody(), JSONCompareMode.STRICT);
    }

    @Test
    @Order(7)
    public void getImages() throws JSONException {
        var responseEntity = restTemplate.exchange(
                createURLWithPort("/images"),
                HttpMethod.GET,
                new HttpEntity<>(authorizedHttpHeadersContentTypeNull),
                String.class
        );

        var expected = """
                    {
                      "images": [
                          {
                              "imageId": "%s",
                              "filename": "image.jpg",
                              "size": 26589
                          }
                      ]
                    }
                """.formatted(storedUUID);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals(expected, responseEntity.getBody(), JSONCompareMode.STRICT);
    }

    @Test
    @Order(8)
    public void removeImage() throws JSONException {
        var responseEntity = restTemplate.exchange(
                createURLWithPort("/image/" + storedUUID),
                HttpMethod.DELETE,
                new HttpEntity<>(authorizedHttpHeadersContentTypeNull),
                String.class
        );

        var expected = """
                    {
                        "success": true,
                        "message": "Image:%s has been deleted"
                    }
                """.formatted(storedUUID);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals(expected, responseEntity.getBody(), JSONCompareMode.STRICT);
    }

    @Test
    @Order(9)
    public void removeImageNotExists() throws JSONException {
        var randomUUID = UUID.randomUUID();
        var responseEntity = restTemplate.exchange(
                createURLWithPort("/image/" + randomUUID),
                HttpMethod.GET,
                new HttpEntity<>(authorizedHttpHeadersContentTypeNull),
                String.class
        );

        var expected = """
                    {
                        "success": false,
                        "message": "Image with id:%s not found"
                    }
                """.formatted(randomUUID);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertEquals(expected, responseEntity.getBody(), JSONCompareMode.STRICT);
    }

    @Test
    @Order(10)
    public void removeImageUnauthorized() {
        var responseEntity = restTemplate.exchange(
                createURLWithPort("/image/" + storedUUID),
                HttpMethod.GET,
                null,
                byte[].class
        );

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(11)
    public void removeImageWithOtherUser() throws JSONException {
        var responseEntity = restTemplate.exchange(
                createURLWithPort("/image/" + storedUUID),
                HttpMethod.GET,
                new HttpEntity<>(anotherAuthorizedHttpHeaders),
                String.class
        );

        var expected = """
                    {
                        "success": false,
                        "message": "Image with id:%s not found"
                    }
                """.formatted(storedUUID);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertEquals(expected, responseEntity.getBody(), JSONCompareMode.STRICT);
    }

    @Test
    @Order(12)
    public void getImagesAfterRemove() throws JSONException {
        var responseEntity = restTemplate.exchange(
                createURLWithPort("/images"),
                HttpMethod.GET,
                new HttpEntity<>(authorizedHttpHeadersContentTypeNull),
                String.class
        );

        var expected = """
                    {
                      "images": []
                    }
                """;
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals(expected, responseEntity.getBody(), JSONCompareMode.STRICT);
    }
}