package com.example.lab5.service;

import com.example.lab5.entity.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для проведения трёх экспериментов с транзакциями.
 *
 * Эксперимент 1: Оба метода в рамках одной транзакции — оба успешны.
 * Эксперимент 2: Оба метода в рамках одной транзакции — второй падает → откат всего.
 * Эксперимент 3: Оба метода вне транзакции — каждый выполняется независимо.
 */
@Service
public class ExperimentService {

    private final ProductService productService;
    private final LogService logService;

    public ExperimentService(ProductService productService, LogService logService) {
        this.productService = productService;
        this.logService = logService;
    }

    /**
     * Эксперимент 1: Вызов метода 1 (insert) и метода 2 (update) в рамках одной транзакции.
     * Оба метода должны сработать успешно.
     */
    @Transactional
    public String experiment1_BothInTransaction() {
        String expName = "Эксперимент 1: оба метода в транзакции";

        Product inserted = productService.insertProduct(
                expName, "Ноутбук", 75000.0, 10);

        productService.updateProductPrice(
                expName, inserted.getId(), 72000.0, false);

        logService.log(expName, "experiment1", "SUCCESS",
                "Оба метода отработали успешно в рамках одной транзакции");

        return "Эксперимент 1 завершён: insert и update выполнены в одной транзакции";
    }

    /**
     * Эксперимент 2: Вызов метода 1 (insert) и метода 2 (update) в рамках одной транзакции.
     * Метод 2 выбрасывает исключение → транзакция откатывается → метод 1 тоже откатывается.
     */
    @Transactional
    public String experiment2_SecondFailsInTransaction() {
        String expName = "Эксперимент 2: второй метод падает в транзакции";

        Product inserted = productService.insertProduct(
                expName, "Монитор", 35000.0, 5);

        // simulateError = true → будет RuntimeException
        productService.updateProductPrice(
                expName, inserted.getId(), 30000.0, true);

        // Эта строка не будет достигнута из-за исключения
        return "Эксперимент 2 завершён";
    }

    /**
     * Эксперимент 3: Оба метода вызываются без транзакции.
     * Каждый метод выполняется независимо — оба должны сработать.
     */
    public String experiment3_BothWithoutTransaction() {
        String expName = "Эксперимент 3: оба метода без транзакции";

        Product inserted = productService.insertProduct(
                expName, "Клавиатура", 5000.0, 50);

        productService.updateProductPrice(
                expName, inserted.getId(), 4500.0, false);

        logService.log(expName, "experiment3", "SUCCESS",
                "Оба метода отработали успешно вне контекста транзакции");

        return "Эксперимент 3 завершён: insert и update выполнены без транзакции";
    }
}
