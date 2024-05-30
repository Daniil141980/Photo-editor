package ru.daniil.worker.domains;


import lombok.Getter;
import ru.daniil.worker.processors.ProcessorParams;
import ru.daniil.worker.processors.kuwahara.KuwaharaParams;
import ru.daniil.worker.processors.scharr.ScharrParams;
import ru.daniil.worker.processors.seamcarver.SeamCarverParams;

@Getter
public enum FilterType {
    HISTOGRAM("Improve image contrast", null),
    KUWAHARA("Reduces noise", KuwaharaParams.class),
    SCHARR("Edge detection", ScharrParams.class),
    SEAM_CARVER("Content-aware image resizing", SeamCarverParams.class),
    IMAGGA("Add tag to image", null);

    private final String description;
    private final Class<? extends ProcessorParams> paramsDto;

    FilterType(String description, Class<? extends ProcessorParams> paramsDto) {
        this.description = description;
        this.paramsDto = paramsDto;
    }
}
