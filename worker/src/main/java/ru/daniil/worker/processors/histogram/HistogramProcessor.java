package ru.daniil.worker.processors.histogram;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.daniil.worker.processors.Processor;
import ru.daniil.worker.processors.ProcessorParams;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

@Component
@ConditionalOnProperty(prefix = "filter", name = "type", havingValue = "HISTOGRAM")
public class HistogramProcessor implements Processor {
    private final int size = 256;

    @Override
    public BufferedImage doProcess(BufferedImage bufferedImage, ProcessorParams params) {
        int red;
        int green;
        int blue;
        int alpha;
        int newPixel;
        var histLut = histogramEqualizationLut(bufferedImage);
        var histogramEq = new BufferedImage(bufferedImage.getWidth(),
                bufferedImage.getHeight(),
                bufferedImage.getType());

        for (var i = 0; i < bufferedImage.getWidth(); i++) {
            for (var j = 0; j < bufferedImage.getHeight(); j++) {
                alpha = new Color(bufferedImage.getRGB(i, j)).getAlpha();
                red = new Color(bufferedImage.getRGB(i, j)).getRed();
                green = new Color(bufferedImage.getRGB(i, j)).getGreen();
                blue = new Color(bufferedImage.getRGB(i, j)).getBlue();

                red = histLut.get(0)[red];
                green = histLut.get(1)[green];
                blue = histLut.get(2)[blue];

                newPixel = colorToRgb(alpha, red, green, blue);

                histogramEq.setRGB(i, j, newPixel);
            }
        }

        return histogramEq;
    }

    private ArrayList<int[]> histogramEqualizationLut(BufferedImage bufferedImage) {
        var imageHist = imageHistogram(bufferedImage);
        var imageLut = new ArrayList<int[]>();

        var redHistogram = new int[size];
        var greenHistogram = new int[size];
        var blueHistogram = new int[size];

        long sumR = 0;
        long sumG = 0;
        long sumB = 0;
        var size2 = 255;

        var scaleFactor = (float) (255.0 / (bufferedImage.getWidth() * bufferedImage.getHeight()));

        for (var i = 0; i < redHistogram.length; i++) {
            sumR += imageHist.get(0)[i];
            redHistogram[i] = Math.min((int) (sumR * scaleFactor), size2);

            sumG += imageHist.get(1)[i];
            greenHistogram[i] = Math.min((int) (sumG * scaleFactor), size2);

            sumB += imageHist.get(2)[i];
            blueHistogram[i] = Math.min((int) (sumB * scaleFactor), size2);
        }

        imageLut.add(redHistogram);
        imageLut.add(greenHistogram);
        imageLut.add(blueHistogram);

        return imageLut;
    }

    private ArrayList<int[]> imageHistogram(BufferedImage bufferedImage) {
        var redHistogram = new int[size];
        var greenHistogram = new int[size];
        var blueHistogram = new int[size];

        int red;
        int green;
        int blue;

        for (var i = 0; i < bufferedImage.getWidth(); i++) {
            for (var j = 0; j < bufferedImage.getHeight(); j++) {
                red = new Color(bufferedImage.getRGB(i, j)).getRed();
                green = new Color(bufferedImage.getRGB(i, j)).getGreen();
                blue = new Color(bufferedImage.getRGB(i, j)).getBlue();

                redHistogram[red]++;
                greenHistogram[green]++;
                blueHistogram[blue]++;

            }
        }

        var hist = new ArrayList<int[]>();
        hist.add(redHistogram);
        hist.add(greenHistogram);
        hist.add(blueHistogram);
        return hist;
    }

    private int colorToRgb(int alpha, int red, int green, int blue) {
        var newPixel = 0;
        newPixel += alpha;
        newPixel = newPixel << 8;
        newPixel += red;
        newPixel = newPixel << 8;
        newPixel += green;
        newPixel = newPixel << 8;
        newPixel += blue;
        return newPixel;
    }
}
