# Лабораторная работа №5 — Транзакции

## Описание

Проект демонстрирует работу с **транзакциями** в Spring Boot с использованием **Java 17** и **MySQL**.

В рамках сервиса `ProductService` реализованы 2 метода:
- **insertProduct** — вставка нового продукта в таблицу `products`
- **updateProductPrice** — обновление цены существующего продукта

Результаты выполнения каждого метода логируются в **отдельную таблицу** `operation_logs`.

## Технологии

- Java 17
- Spring Boot 3.2.4
- Spring Data JPA
- MySQL
- Maven

## Структура проекта

```
src/main/java/com/example/lab5/
├── Lab5TransactionsApplication.java    # Главный класс
├── entity/
│   ├── Product.java                    # Сущность «Продукт»
│   └── OperationLog.java              # Сущность «Лог операции»
├── repository/
│   ├── ProductRepository.java          # Репозиторий продуктов
│   └── OperationLogRepository.java     # Репозиторий логов
├── service/
│   ├── ProductService.java             # Сервис с 2 методами (insert, update)
│   ├── LogService.java                 # Сервис логирования (REQUIRES_NEW)
│   └── ExperimentService.java          # Сервис запуска экспериментов
├── controller/
│   └── ExperimentController.java       # REST API контроллер
└── runner/
    └── ExperimentRunner.java           # Запуск экспериментов при старте
```

## Запуск

### Предварительные требования

1. Java 17
2. MySQL (запущен на `localhost:3306`)
3. Maven

### Настройка БД

```sql
CREATE DATABASE IF NOT EXISTS lab5_transactions;
```

### Запуск приложения

```bash
mvn spring-boot:run
```

При запуске автоматически выполняются все 3 эксперимента, результаты выводятся в консоль.

### REST API

| Метод | URL | Описание |
|-------|-----|----------|
| POST | `/api/experiments/experiment1` | Эксперимент 1 |
| POST | `/api/experiments/experiment2` | Эксперимент 2 |
| POST | `/api/experiments/experiment3` | Эксперимент 3 |
| POST | `/api/experiments/run-all` | Запуск всех экспериментов |
| GET  | `/api/experiments/products` | Просмотр продуктов |
| GET  | `/api/experiments/logs` | Просмотр логов |
| POST | `/api/experiments/reset` | Очистка данных |

## Эксперименты

Подробный анализ результатов — см. [REPORT.md](REPORT.md).
