package com.example.lab5.runner;

import com.example.lab5.entity.OperationLog;
import com.example.lab5.entity.Product;
import com.example.lab5.repository.OperationLogRepository;
import com.example.lab5.repository.ProductRepository;
import com.example.lab5.service.ExperimentService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Запускает все три эксперимента при старте приложения
 * и выводит результаты в консоль.
 */
@Component
public class ExperimentRunner implements ApplicationRunner {

    private final ExperimentService experimentService;
    private final ProductRepository productRepository;
    private final OperationLogRepository logRepository;

    public ExperimentRunner(ExperimentService experimentService,
                            ProductRepository productRepository,
                            OperationLogRepository logRepository) {
        this.experimentService = experimentService;
        this.productRepository = productRepository;
        this.logRepository = logRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("  ЛАБОРАТОРНАЯ РАБОТА 5 — ЭКСПЕРИМЕНТЫ С ТРАНЗАКЦИЯМИ");
        System.out.println("=".repeat(80));

        // Очистка данных перед экспериментами
        productRepository.deleteAll();
        logRepository.deleteAll();

        // ===== Эксперимент 1 =====
        System.out.println("\n" + "-".repeat(80));
        System.out.println("  ЭКСПЕРИМЕНТ 1: Оба метода в рамках одной транзакции (оба успешны)");
        System.out.println("-".repeat(80));
        try {
            String result = experimentService.experiment1_BothInTransaction();
            System.out.println("  Результат: " + result);
        } catch (Exception e) {
            System.out.println("  Ошибка: " + e.getMessage());
        }
        printCurrentState();

        // ===== Эксперимент 2 =====
        System.out.println("\n" + "-".repeat(80));
        System.out.println("  ЭКСПЕРИМЕНТ 2: Оба метода в транзакции, второй падает → ОТКАТ");
        System.out.println("-".repeat(80));
        try {
            String result = experimentService.experiment2_SecondFailsInTransaction();
            System.out.println("  Результат: " + result);
        } catch (Exception e) {
            System.out.println("  Транзакция откатилась: " + e.getMessage());
        }
        printCurrentState();

        // ===== Эксперимент 3 =====
        System.out.println("\n" + "-".repeat(80));
        System.out.println("  ЭКСПЕРИМЕНТ 3: Оба метода вне контекста транзакции (оба успешны)");
        System.out.println("-".repeat(80));
        try {
            String result = experimentService.experiment3_BothWithoutTransaction();
            System.out.println("  Результат: " + result);
        } catch (Exception e) {
            System.out.println("  Ошибка: " + e.getMessage());
        }
        printCurrentState();

        // ===== Итоговый отчёт =====
        System.out.println("\n" + "=".repeat(80));
        System.out.println("  ИТОГОВЫЙ ОТЧЁТ — ЛОГИ ОПЕРАЦИЙ");
        System.out.println("=".repeat(80));
        List<OperationLog> allLogs = logRepository.findAll();
        for (OperationLog log : allLogs) {
            System.out.printf("  [%s] %s | %s | %s | %s%n",
                    log.getExecutedAt(),
                    log.getExperimentName(),
                    log.getMethodName(),
                    log.getStatus(),
                    log.getErrorMessage());
        }
        System.out.println("=".repeat(80));
        System.out.println("  Эксперименты завершены. REST API доступен на http://localhost:8080");
        System.out.println("=".repeat(80) + "\n");
    }

    private void printCurrentState() {
        List<Product> products = productRepository.findAll();
        System.out.println("  Текущее состояние таблицы products:");
        if (products.isEmpty()) {
            System.out.println("    (пусто)");
        } else {
            for (Product p : products) {
                System.out.printf("    id=%d, name='%s', price=%.2f, quantity=%d%n",
                        p.getId(), p.getName(), p.getPrice(), p.getQuantity());
            }
        }
    }
}
