package ru.daniil.photoeditor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.daniil.photoeditor.config.StorageProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class PhotoEditorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhotoEditorApplication.class, args);
    }

}
