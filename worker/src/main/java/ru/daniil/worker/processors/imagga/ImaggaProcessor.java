package ru.daniil.worker.processors.imagga;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import ru.daniil.worker.dto.imagga.tag.TagsDto;
import ru.daniil.worker.processors.Processor;
import ru.daniil.worker.processors.ProcessorParams;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

@Component
@ConditionalOnProperty(prefix = "filter", name = "type", havingValue = "IMAGGA")
@RequiredArgsConstructor
public class ImaggaProcessor implements Processor {
    private final ImaggaController imaggaController;

    @Override
    public BufferedImage doProcess(BufferedImage bufferedImage, ProcessorParams params) {
        var outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "jpg", outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var imageResource = new ByteArrayResource(outputStream.toByteArray()) {
            @Override
            public String getFilename() {
                return "image.jpg";
            }
        };
        var uploadId = imaggaController.postImage(imageResource).result().uploadId();
        var tagsResponseDto = imaggaController.getTags(uploadId).result().tags();
        addTagsToImage(tagsResponseDto, bufferedImage);
        return bufferedImage;
    }

    private void addTagsToImage(List<TagsDto> tags, BufferedImage bufferedImage) {
        var fontSize = 40;
        var baseFont = new Font("Arial", Font.BOLD, fontSize);
        var graphics = bufferedImage.getGraphics();
        graphics.setColor(Color.BLACK);
        var startX = 15;
        for (var i = 0; i < tags.size(); i++) {
            var tag = tags.get(i).tag().en();
            graphics.setFont(getRelevantFont(graphics, baseFont, bufferedImage, tag));
            graphics.drawString(tag, startX, fontSize + (i * fontSize));
        }
    }

    private Font getRelevantFont(Graphics graphics, Font baseFont, BufferedImage bufferedImage, String text) {
        var ruler = graphics.getFontMetrics(baseFont);
        var outline = baseFont.createGlyphVector(ruler.getFontRenderContext(), text).getOutline(0, 0);
        var expectedWidth = outline.getBounds().getWidth();

        if (bufferedImage.getWidth() >= expectedWidth) {
            return baseFont;
        }
        var widthBasedFontSize = (baseFont.getSize2D() * bufferedImage.getWidth()) / expectedWidth;
        return baseFont.deriveFont(baseFont.getStyle(), (float) widthBasedFontSize);
    }
}
