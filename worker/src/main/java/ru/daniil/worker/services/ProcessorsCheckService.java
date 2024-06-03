package ru.daniil.worker.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.daniil.worker.domains.ProcessorsCheckEntity;
import ru.daniil.worker.repositories.processorscheck.ProcessorsCheckRepository;

@Service
@RequiredArgsConstructor
public class ProcessorsCheckService {
    private final ProcessorsCheckRepository processorsCheckRepository;

    @Transactional
    public void save(ProcessorsCheckEntity processorsCheckEntity) {
        processorsCheckRepository.save(processorsCheckEntity);
    }

    @Transactional
    public Boolean exist(ProcessorsCheckEntity processorsCheckEntity) {
        return processorsCheckRepository.exist(processorsCheckEntity);
    }
}
