package ru.daniil.worker.processors;

import java.awt.image.BufferedImage;

public interface Processor {
    BufferedImage doProcess(BufferedImage bufferedImage, ProcessorParams params);
}
