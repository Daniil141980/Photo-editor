package ru.daniil.api.dto.filter;

import ru.daniil.api.dto.processorparams.KuwaharaParams;
import ru.daniil.api.validation.constraints.ValidFilterType;


public record FiltersAndParamsRequestDto(@ValidFilterType String filter, KuwaharaParams processorParams) {
}
