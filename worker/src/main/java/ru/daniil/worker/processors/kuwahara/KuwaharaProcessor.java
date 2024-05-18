package ru.daniil.worker.processors.kuwahara;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.daniil.worker.processors.Processor;
import ru.daniil.worker.processors.ProcessorParams;

import java.awt.image.BufferedImage;

@Component
@ConditionalOnProperty(prefix = "filter", name = "type", havingValue = "KUWAHARA")
public class KuwaharaProcessor implements Processor {
    private final String title = "Image";

    @Override
    public BufferedImage doProcess(BufferedImage bufferedImage, ProcessorParams processorParams) {
        var kuwaharaParams = (KuwaharaParams) processorParams;
        var colorProcessor = (ColorProcessor) new ImagePlus(title, bufferedImage).getProcessor();
        var width = colorProcessor.getWidth();
        var height = colorProcessor.getHeight();
        var size = width * height;
        var r = new byte[size];
        var g = new byte[size];
        var b = new byte[size];

        colorProcessor.getRGB(r, g, b);
        var red = new ByteProcessor(width, height, r, null);
        filter(red, kuwaharaParams.size());

        var green = new ByteProcessor(width, height, g, null);
        filter(green, kuwaharaParams.size());

        var blue = new ByteProcessor(width, height, b, null);
        filter(blue, kuwaharaParams.size());

        colorProcessor.setRGB((byte[]) red.getPixels(), (byte[]) green.getPixels(), (byte[]) blue.getPixels());
        return colorProcessor.getBufferedImage();
    }

    private void filter(ImageProcessor imageProcessor, Integer size) {
        var roi = imageProcessor.getRoi();
        var width = roi.width;
        var height = roi.height;
        var size2 = (size + 1) / 2;
        var offset = (size - 1) / 2;
        var width2 = imageProcessor.getWidth() + offset;
        var height2 = imageProcessor.getHeight() + offset;
        var mean = new float[width2][height2];
        var variance = new float[width2][height2];
        var x1Start = roi.x;
        var y1Start = roi.y;
        double sum;
        double sum2;
        int n;
        int v;
        int xBase;
        int yBase;
        for (var y1 = y1Start - offset; y1 < y1Start + height; y1++) {
            for (var x1 = x1Start - offset; x1 < x1Start + width; x1++) {
                sum = 0;
                sum2 = 0;
                n = 0;
                for (int x2 = x1; x2 < x1 + size2; x2++) {
                    for (int y2 = y1; y2 < y1 + size2; y2++) {
                        v = imageProcessor.getPixel(x2, y2);
                        sum += v;
                        sum2 += v * v;
                        n++;
                    }
                }
                mean[x1 + offset][y1 + offset] = (float) (sum / n);
                variance[x1 + offset][y1 + offset] = (float) (sum2 - sum * sum / n);
            }
        }
        int xBase2 = 0;
        int yBase2 = 0;
        float var;
        float min;
        for (var y1 = y1Start; y1 < y1Start + height; y1++) {
            for (var x1 = x1Start; x1 < x1Start + width; x1++) {
                min = Float.MAX_VALUE;
                xBase = x1;
                yBase = y1;
                var = variance[xBase][yBase];
                if (var < min) {
                    min = var;
                    xBase2 = xBase;
                    yBase2 = yBase;
                }
                xBase = x1 + offset;
                var = variance[xBase][yBase];
                if (var < min) {
                    min = var;
                    xBase2 = xBase;
                    yBase2 = yBase;
                }
                yBase = y1 + offset;
                var = variance[xBase][yBase];
                if (var < min) {
                    min = var;
                    xBase2 = xBase;
                    yBase2 = yBase;
                }
                xBase = x1;
                var = variance[xBase][yBase];
                if (var < min) {
                    xBase2 = xBase;
                    yBase2 = yBase;
                }
                imageProcessor.putPixel(x1, y1, (int) (mean[xBase2][yBase2] + 0.5));
            }
        }
    }
}
