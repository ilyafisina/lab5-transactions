package com.example.lab5.controller;

import com.example.lab5.entity.OperationLog;
import com.example.lab5.entity.Product;
import com.example.lab5.repository.OperationLogRepository;
import com.example.lab5.repository.ProductRepository;
import com.example.lab5.service.ExperimentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/experiments")
public class ExperimentController {

    private final ExperimentService experimentService;
    private final ProductRepository productRepository;
    private final OperationLogRepository logRepository;

    public ExperimentController(ExperimentService experimentService,
                                ProductRepository productRepository,
                                OperationLogRepository logRepository) {
        this.experimentService = experimentService;
        this.productRepository = productRepository;
        this.logRepository = logRepository;
    }

    /**
     * Эксперимент 1: оба метода в рамках одной транзакции — оба успешны.
     */
    @PostMapping("/experiment1")
    public ResponseEntity<Map<String, Object>> runExperiment1() {
        Map<String, Object> result = new HashMap<>();
        try {
            String msg = experimentService.experiment1_BothInTransaction();
            result.put("status", "SUCCESS");
            result.put("message", msg);
        } catch (Exception e) {
            result.put("status", "FAILURE");
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Эксперимент 2: второй метод падает → вся транзакция откатывается.
     */
    @PostMapping("/experiment2")
    public ResponseEntity<Map<String, Object>> runExperiment2() {
        Map<String, Object> result = new HashMap<>();
        try {
            String msg = experimentService.experiment2_SecondFailsInTransaction();
            result.put("status", "SUCCESS");
            result.put("message", msg);
        } catch (Exception e) {
            result.put("status", "ROLLED_BACK");
            result.put("message", "Транзакция откатилась: " + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Эксперимент 3: оба метода вне контекста транзакции — оба успешны.
     */
    @PostMapping("/experiment3")
    public ResponseEntity<Map<String, Object>> runExperiment3() {
        Map<String, Object> result = new HashMap<>();
        try {
            String msg = experimentService.experiment3_BothWithoutTransaction();
            result.put("status", "SUCCESS");
            result.put("message", msg);
        } catch (Exception e) {
            result.put("status", "FAILURE");
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Запуск всех трёх экспериментов последовательно.
     */
    @PostMapping("/run-all")
    public ResponseEntity<Map<String, Object>> runAll() {
        Map<String, Object> results = new HashMap<>();

        // Эксперимент 1
        try {
            results.put("experiment1", experimentService.experiment1_BothInTransaction());
        } catch (Exception e) {
            results.put("experiment1", "FAILURE: " + e.getMessage());
        }

        // Эксперимент 2
        try {
            results.put("experiment2", experimentService.experiment2_SecondFailsInTransaction());
        } catch (Exception e) {
            results.put("experiment2", "ROLLED_BACK: " + e.getMessage());
        }

        // Эксперимент 3
        try {
            results.put("experiment3", experimentService.experiment3_BothWithoutTransaction());
        } catch (Exception e) {
            results.put("experiment3", "FAILURE: " + e.getMessage());
        }

        return ResponseEntity.ok(results);
    }

    /**
     * Получить все записи из таблицы products.
     */
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    /**
     * Получить все логи операций.
     */
    @GetMapping("/logs")
    public ResponseEntity<List<OperationLog>> getLogs() {
        return ResponseEntity.ok(logRepository.findAll());
    }

    /**
     * Очистить данные для повторного запуска экспериментов.
     */
    @PostMapping("/reset")
    public ResponseEntity<String> reset() {
        productRepository.deleteAll();
        logRepository.deleteAll();
        return ResponseEntity.ok("Данные очищены");
    }
}
