package ru.daniil.worker.processors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.daniil.worker.processors.histogram.HistogramProcessor;
import ru.daniil.worker.processors.kuwahara.KuwaharaParams;
import ru.daniil.worker.processors.kuwahara.KuwaharaProcessor;
import ru.daniil.worker.processors.scharr.ScharrParams;
import ru.daniil.worker.processors.scharr.ScharrProcessor;
import ru.daniil.worker.processors.seamcarver.SeamCarverParams;
import ru.daniil.worker.processors.seamcarver.SeamCarverProcessor;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ProcessorsTest {
    @Test
    @DisplayName("Test histogram")
    void histogramTest() throws IOException {
        var histogram = new HistogramProcessor();
        var bufferedImage = ImageIO.read(new File("src/test/resources/files/image.jpg"));

        var newImage = histogram.doProcess(bufferedImage, null);

        assertEquals(bufferedImage.getWidth(), newImage.getWidth());
        assertEquals(bufferedImage.getHeight(), newImage.getHeight());
        assertEquals(bufferedImage.getType(), newImage.getType());
    }

    @Test
    @DisplayName("Test kuwahara")
    void kuwaharaTest() throws IOException {
        var kuwahara = new KuwaharaProcessor();
        var bufferedImage = ImageIO.read(new File("src/test/resources/files/image.jpg"));
        var kuwaharaParams = new KuwaharaParams(15);

        var newImage = kuwahara.doProcess(bufferedImage, kuwaharaParams);

        assertEquals(bufferedImage.getWidth(), newImage.getWidth());
        assertEquals(bufferedImage.getHeight(), newImage.getHeight());
    }

    @Test
    @DisplayName("Test scharr")
    void scharrTest() throws IOException {
        var scharr = new ScharrProcessor();
        var bufferedImage = ImageIO.read(new File("src/test/resources/files/image.jpg"));
        var scharrParams = new ScharrParams("Horizontal");

        var newImage = scharr.doProcess(bufferedImage, scharrParams);

        assertEquals(bufferedImage.getWidth(), newImage.getWidth());
        assertEquals(bufferedImage.getHeight(), newImage.getHeight());
    }

    @Test
    @DisplayName("Test seam")
    void seamTest() throws IOException {
        var seam = new SeamCarverProcessor();
        var bufferedImage = ImageIO.read(new File("src/test/resources/files/image.jpg"));
        var seamCarverParams = new SeamCarverParams(150, 150);

        var newImage = seam.doProcess(bufferedImage, seamCarverParams);

        assertEquals(seamCarverParams.width(), newImage.getWidth());
        assertEquals(seamCarverParams.height(), newImage.getHeight());
    }
}
