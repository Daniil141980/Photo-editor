package ru.daniil.worker.processors.seamcarver;

import ru.daniil.worker.processors.ProcessorParams;

public record SeamCarverParams(Integer width, Integer height) implements ProcessorParams {
}
