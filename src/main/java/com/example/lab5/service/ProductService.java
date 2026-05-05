package com.example.lab5.service;

import com.example.lab5.entity.Product;
import com.example.lab5.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для работы с продуктами.
 * Содержит два метода: insertProduct (вставка) и updateProductPrice (обновление).
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final LogService logService;

    public ProductService(ProductRepository productRepository, LogService logService) {
        this.productRepository = productRepository;
        this.logService = logService;
    }

    /**
     * Метод 1: Вставка нового продукта в БД.
     */
    public Product insertProduct(String experimentName, String name, Double price, Integer quantity) {
        try {
            Product product = new Product(name, price, quantity);
            Product saved = productRepository.save(product);
            logService.log(experimentName, "insertProduct", "SUCCESS",
                    "Добавлен продукт: " + saved);
            return saved;
        } catch (Exception e) {
            logService.log(experimentName, "insertProduct", "FAILURE", e.getMessage());
            throw e;
        }
    }

    /**
     * Метод 2: Обновление цены продукта.
     * Если simulateError=true, выбрасывает RuntimeException после обновления,
     * чтобы продемонстрировать откат транзакции.
     */
    public Product updateProductPrice(String experimentName, Long productId,
                                      Double newPrice, boolean simulateError) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException(
                            "Продукт с id=" + productId + " не найден"));
            product.setPrice(newPrice);
            Product saved = productRepository.save(product);

            if (simulateError) {
                throw new RuntimeException(
                        "Имитация ошибки при обновлении продукта id=" + productId);
            }

            logService.log(experimentName, "updateProductPrice", "SUCCESS",
                    "Обновлён продукт: " + saved);
            return saved;
        } catch (Exception e) {
            logService.log(experimentName, "updateProductPrice", "FAILURE", e.getMessage());
            throw e;
        }
    }
}
