package ru.daniil.photoeditor.integration.rest;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.daniil.photoeditor.config.AbstractBaseTest;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractRestTest extends AbstractBaseTest {
    protected static final String HOST = "http://localhost";
    protected static final String BASE_URL = "/api/v1";
    protected TestRestTemplate restTemplate = new TestRestTemplate();
    protected HttpHeaders httpHeaders = new HttpHeaders();

    @LocalServerPort
    private int port;

    public AbstractRestTest() {
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    protected String createURLWithPort(String uri) {
        return HOST + ":" + port + BASE_URL + uri;
    }
}
