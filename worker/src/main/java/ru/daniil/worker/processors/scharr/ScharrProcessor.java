package ru.daniil.worker.processors.scharr;

import ij.ImagePlus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.daniil.worker.processors.Processor;
import ru.daniil.worker.processors.ProcessorParams;

import java.awt.image.BufferedImage;

@Component
@ConditionalOnProperty(prefix = "filter", name = "type", havingValue = "SCHARR")
public class ScharrProcessor implements Processor {
    private final String title = "Image";

    @Override
    public BufferedImage doProcess(BufferedImage bufferedImage, ProcessorParams processorParams) {
        var scharrParams = (ScharrParams) processorParams;
        var imageProcessor = new ImagePlus(title, bufferedImage).getProcessor();
        int[] kernel;
        if (scharrParams.type().equals("Horizontal")) {
            kernel = new int[]{-3, -10, -3,
                    0, 0, 0,
                    3, 10, 3};
        } else {
            kernel = new int[]{-3, 0, 3,
                    -10, 0, 10,
                    -3, 0, 3};
        }
        imageProcessor.convolve3x3(kernel);
        return imageProcessor.getBufferedImage();
    }
}
