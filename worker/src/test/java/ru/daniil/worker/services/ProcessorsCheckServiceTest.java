package ru.daniil.worker.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.daniil.worker.config.AbstractBaseTest;
import ru.daniil.worker.domains.FilterType;
import ru.daniil.worker.domains.ProcessorsCheckEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProcessorsCheckServiceTest extends AbstractBaseTest {
    @Autowired
    private ProcessorsCheckService processorsCheckService;

    @Test
    @DisplayName("Test save processor")
    void saveProcessor() {
        var imageId = UUID.fromString("16fd2706-8baf-433b-82eb-8c7fada847da");
        var requestId = UUID.randomUUID();
        var processorsCheckEntity = new ProcessorsCheckEntity(imageId, requestId, FilterType.KUWAHARA);

        processorsCheckService.save(processorsCheckEntity);

        assertEquals(true, processorsCheckService.exist(processorsCheckEntity));
    }
}
