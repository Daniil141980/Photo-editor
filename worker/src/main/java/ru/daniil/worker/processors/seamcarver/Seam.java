package ru.daniil.worker.processors.seamcarver;

import lombok.Getter;

@Getter
class Seam {
    private final int[] pixels;
    private final String direction;
    private double energy;

    public Seam(int s, String dir) {
        pixels = new int[s];
        direction = dir;
    }

    void setPixels(int position, int value) {
        pixels[position] = value;
    }

    void setEnergy(double energy) {
        this.energy = energy;
    }
}
