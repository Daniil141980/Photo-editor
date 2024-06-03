package ru.daniil.worker.processors.seamcarver;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.daniil.worker.processors.Processor;
import ru.daniil.worker.processors.ProcessorParams;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

@Component
@ConditionalOnProperty(prefix = "filter", name = "type", havingValue = "SEAM_CARVER")
public class SeamCarverProcessor implements Processor {

    @Override
    public BufferedImage doProcess(BufferedImage bufferedImage, ProcessorParams processorParams) {
        var seamCarverParams = (SeamCarverParams) processorParams;
        var numCarveHorizontal = bufferedImage.getHeight() - seamCarverParams.height();
        var numCarveVertical = bufferedImage.getWidth() - seamCarverParams.width();
        if (numCarveHorizontal < 0 || numCarveHorizontal >= bufferedImage.getWidth()
                || numCarveVertical < 0 || numCarveVertical >= bufferedImage.getHeight()) {
            throw new IllegalArgumentException("Width and height of"
                    + " the output image should be less then or equal to the original");
        }

        var carvedImage = copyImage(bufferedImage);
        var seams = new ArrayList<Seam>();

        Seam horizontalSeam;
        Seam verticalSeam;
        while (numCarveHorizontal > 0 || numCarveVertical > 0) {
            var energyTable = calculateEnergyTable(carvedImage);

            if (numCarveHorizontal > 0 && numCarveVertical > 0) {
                horizontalSeam = getHorizontalSeam(energyTable);
                verticalSeam = getVerticalSeam(energyTable);

                if (horizontalSeam.getEnergy() < verticalSeam.getEnergy()) {
                    seams.add(horizontalSeam);
                    carvedImage = removeSeam(horizontalSeam, carvedImage);
                    numCarveHorizontal--;
                } else {
                    seams.add(verticalSeam);
                    carvedImage = removeSeam(verticalSeam, carvedImage);
                    numCarveVertical--;
                }
            } else if (numCarveHorizontal > 0) {
                horizontalSeam = getHorizontalSeam(energyTable);
                seams.add(horizontalSeam);
                carvedImage = removeSeam(horizontalSeam, carvedImage);
                numCarveHorizontal--;
            } else {
                verticalSeam = getVerticalSeam(energyTable);
                seams.add(verticalSeam);
                carvedImage = removeSeam(verticalSeam, carvedImage);
                numCarveVertical--;
            }
        }
        return carvedImage;
    }

    private double[][] calculateEnergyTable(BufferedImage bufferedImage) {
        var width = bufferedImage.getWidth();
        var height = bufferedImage.getHeight();
        var energyTable = new double[width][height];

        double xEnergy;
        double yEnergy;
        double totalEnergy;
        int xPrevRgb;
        int xNextRgb;
        int yPrevRgb;
        int yNextRgb;
        for (var i = 0; i < width; i++) {
            for (var j = 0; j < height; j++) {
                xPrevRgb = bufferedImage.getRGB((i - 1 + width) % width, j);
                xNextRgb = bufferedImage.getRGB((i + 1 + width) % width, j);
                xEnergy = getEnergy(xPrevRgb, xNextRgb);

                yPrevRgb = bufferedImage.getRGB(i, (j - 1 + height) % height);
                yNextRgb = bufferedImage.getRGB(i, (j + 1 + height) % height);
                yEnergy = getEnergy(yPrevRgb, yNextRgb);

                totalEnergy = xEnergy + yEnergy;
                energyTable[i][j] = totalEnergy;
            }
        }
        return energyTable;
    }

    private double getEnergy(int rgb1, int rgb2) {
        var b1 = (rgb1) & 0xff;
        var g1 = (rgb1 >> 8) & 0xff;
        var r1 = (rgb1 >> 16) & 0xff;

        var b2 = (rgb2) & 0xff;
        var g2 = (rgb2 >> 8) & 0xff;
        var r2 = (rgb2 >> 16) & 0xff;

        return (r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2);
    }

    private Seam getHorizontalSeam(double[][] energyTable) {
        var width = energyTable.length;
        var height = energyTable[0].length;

        var seam = new Seam(width, "horizontal");
        var horizontalDp = new double[width][height];
        var prev = new int[width][height];

        double minValue;
        for (var i = 0; i < width; i++) {
            for (var j = 0; j < height; j++) {
                if (i == 0) {
                    horizontalDp[i][j] = energyTable[i][j];
                    prev[i][j] = -1;
                    continue;
                } else if (j == 0) {
                    minValue = Math.min(horizontalDp[i - 1][j], horizontalDp[i - 1][j + 1]);
                    if (minValue == horizontalDp[i - 1][j]) {
                        prev[i][j] = j;
                    } else {
                        prev[i][j] = j + 1;
                    }
                } else if (j == height - 1) {
                    minValue = Math.min(horizontalDp[i - 1][j], horizontalDp[i - 1][j - 1]);
                    if (minValue == horizontalDp[i - 1][j]) {
                        prev[i][j] = j;
                    } else {
                        prev[i][j] = j - 1;
                    }
                } else {
                    minValue = Math.min(horizontalDp[i - 1][j],
                            Math.min(horizontalDp[i - 1][j - 1],
                                    horizontalDp[i - 1][j + 1]));
                    if (minValue == horizontalDp[i - 1][j]) {
                        prev[i][j] = j;
                    } else if (minValue == horizontalDp[i - 1][j - 1]) {
                        prev[i][j] = j - 1;
                    } else {
                        prev[i][j] = j + 1;
                    }
                }
                horizontalDp[i][j] = energyTable[i][j] + minValue;
            }
        }

        var minEnergy = horizontalDp[width - 1][0];
        var minCoord = 0;
        for (var j = 0; j < height; j++) {
            if (minEnergy > horizontalDp[width - 1][j]) {
                minEnergy = horizontalDp[width - 1][j];
                minCoord = j;
            }
        }

        seam.setEnergy(minEnergy);
        for (var i = width - 1; i >= 0; i--) {
            seam.setPixels(i, minCoord);
            minCoord = prev[i][minCoord];
        }

        return seam;
    }

    private Seam getVerticalSeam(double[][] energyTable) {
        var width = energyTable.length;
        var height = energyTable[0].length;

        var seam = new Seam(height, "vertical");
        var verticalDp = new double[width][height];
        var prev = new int[width][height];

        double minValue;
        for (var j = 0; j < height; j++) {
            for (var i = 0; i < width; i++) {
                if (j == 0) {
                    verticalDp[i][j] = energyTable[i][j];
                    prev[i][j] = -1;
                    continue;
                } else if (i == 0) {
                    minValue = Math.min(verticalDp[i][j - 1], verticalDp[i + 1][j - 1]);
                    if (minValue == verticalDp[i][j - 1]) {
                        prev[i][j] = i;
                    } else {
                        prev[i][j] = i + 1;
                    }
                } else if (i == width - 1) {
                    minValue = Math.min(verticalDp[i][j - 1], verticalDp[i - 1][j - 1]);
                    if (minValue == verticalDp[i][j - 1]) {
                        prev[i][j] = i;
                    } else {
                        prev[i][j] = i - 1;
                    }
                } else {
                    minValue = Math.min(verticalDp[i][j - 1],
                            Math.min(verticalDp[i - 1][j - 1],
                                    verticalDp[i + 1][j - 1]));
                    if (minValue == verticalDp[i][j - 1]) {
                        prev[i][j] = i;
                    } else if (minValue == verticalDp[i - 1][j - 1]) {
                        prev[i][j] = i - 1;
                    } else {
                        prev[i][j] = i + 1;
                    }
                }
                verticalDp[i][j] = energyTable[i][j] + minValue;
            }
        }

        var minEnergy = verticalDp[0][height - 1];
        var minCoord = 0;
        for (var i = 0; i < width; i++) {
            if (minEnergy > verticalDp[i][height - 1]) {
                minEnergy = verticalDp[i][height - 1];
                minCoord = i;
            }
        }

        seam.setEnergy(minEnergy);
        for (var j = height - 1; j >= 0; j--) {
            seam.setPixels(j, minCoord);
            minCoord = prev[minCoord][j];
        }

        return seam;
    }

    private BufferedImage removeSeam(Seam seam, BufferedImage carvedImage) {
        var width = carvedImage.getWidth();
        var height = carvedImage.getHeight();
        BufferedImage imageNew;
        boolean moveToNext;

        if (seam.getDirection().equals("horizontal")) {
            imageNew = new BufferedImage(width, height - 1, BufferedImage.TYPE_INT_RGB);

            for (var i = 0; i < width; i++) {
                moveToNext = false;
                for (var j = 0; j < height - 1; j++) {
                    if (seam.getPixels()[i] == j) {
                        moveToNext = true;
                    }
                    if (moveToNext) {
                        imageNew.setRGB(i, j, carvedImage.getRGB(i, j + 1));
                    } else {
                        imageNew.setRGB(i, j, carvedImage.getRGB(i, j));
                    }
                }
            }
        } else {
            imageNew = new BufferedImage(width - 1, height, BufferedImage.TYPE_INT_RGB);

            for (var j = 0; j < height; j++) {
                moveToNext = false;
                for (var i = 0; i < width - 1; i++) {
                    if (seam.getPixels()[j] == i) {
                        moveToNext = true;
                    }
                    if (moveToNext) {
                        imageNew.setRGB(i, j, carvedImage.getRGB(i + 1, j));
                    } else {
                        imageNew.setRGB(i, j, carvedImage.getRGB(i, j));
                    }
                }
            }
        }
        return imageNew;
    }

    private BufferedImage copyImage(BufferedImage bufferedImage) {
        var colorModel = bufferedImage.getColorModel();
        var isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
        var writableRaster = bufferedImage.copyData(null);

        return new BufferedImage(colorModel, writableRaster, isAlphaPremultiplied, null);
    }
}
