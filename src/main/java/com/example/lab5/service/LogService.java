package com.example.lab5.service;

import com.example.lab5.entity.OperationLog;
import com.example.lab5.repository.OperationLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Сервис логирования результатов операций.
 * Использует REQUIRES_NEW, чтобы логи сохранялись в БД
 * независимо от того, откатилась ли основная транзакция.
 */
@Service
public class LogService {

    private final OperationLogRepository logRepository;

    public LogService(OperationLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String experimentName, String methodName, String status, String errorMessage) {
        OperationLog entry = new OperationLog(
                experimentName,
                methodName,
                status,
                errorMessage,
                LocalDateTime.now()
        );
        logRepository.save(entry);
    }
}
