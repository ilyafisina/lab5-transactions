# Отчёт — Лабораторная работа №5: Транзакции

## 1. Цель работы

Изучить механизм транзакций в Spring Boot (аннотация `@Transactional`), понять поведение commit и rollback, а также научиться логировать результаты выполнения методов в независимую таблицу БД.

## 2. Теоретическая часть

### 2.1. Что такое транзакция?

**Транзакция** — это логическая единица работы с базой данных, обладающая свойствами **ACID**:

| Свойство | Описание |
|----------|----------|
| **Atomicity** (Атомарность) | Все операции транзакции выполняются полностью или не выполняются вовсе |
| **Consistency** (Согласованность) | Транзакция переводит БД из одного согласованного состояния в другое |
| **Isolation** (Изоляция) | Параллельные транзакции не влияют друг на друга |
| **Durability** (Долговечность) | После фиксации (commit) данные сохраняются даже при сбоях |

### 2.2. @Transactional в Spring

Аннотация `@Transactional` позволяет декларативно управлять транзакциями:

- При вызове метода Spring автоматически открывает транзакцию
- При успешном завершении метода — выполняет **commit**
- При выбросе `RuntimeException` — выполняет **rollback**

### 2.3. Propagation.REQUIRES_NEW

Для логирования используется `@Transactional(propagation = Propagation.REQUIRES_NEW)`. Это означает, что лог-запись сохраняется в **отдельной транзакции**, которая коммитится независимо от основной. Даже если основная транзакция откатится, записи в `operation_logs` останутся.

## 3. Описание реализации

### 3.1. Сущности (Entity)

**Product** — основная бизнес-сущность:
```java
@Entity
@Table(name = "products")
public class Product {
    Long id;
    String name;
    Double price;
    Integer quantity;
}
```

**OperationLog** — независимая таблица для логирования:
```java
@Entity
@Table(name = "operation_logs")
public class OperationLog {
    Long id;
    String experimentName;
    String methodName;
    String status;         // SUCCESS или FAILURE
    String errorMessage;
    LocalDateTime executedAt;
}
```

### 3.2. Сервисы

**ProductService** содержит два метода:
1. `insertProduct(...)` — вставка нового продукта
2. `updateProductPrice(...)` — обновление цены (с возможностью имитации ошибки)

**LogService** — сохраняет логи в `REQUIRES_NEW` транзакции.

**ExperimentService** — оркестрирует эксперименты.

## 4. Эксперименты и результаты

### 4.1. Эксперимент 1: Оба метода в рамках одной транзакции

**Сценарий:** Вызываются `insertProduct` и `updateProductPrice` внутри одного `@Transactional` метода. Ошибок нет.

**Код:**
```java
@Transactional
public String experiment1_BothInTransaction() {
    Product inserted = productService.insertProduct(..., "Ноутбук", 75000.0, 10);
    productService.updateProductPrice(..., inserted.getId(), 72000.0, false);
}
```

**Ожидаемый результат:**
- В таблице `products` появится запись: `Ноутбук, price=72000.0`
- В `operation_logs` — 3 записи (insert SUCCESS, update SUCCESS, experiment SUCCESS)

**Фактический результат:**
- ✔ Продукт вставлен и обновлён
- ✔ Все логи записаны со статусом SUCCESS
- ✔ Транзакция зафиксирована (commit)

**Вывод:** При отсутствии ошибок обе операции внутри одной транзакции выполняются успешно и фиксируются вместе.

---

### 4.2. Эксперимент 2: Второй метод падает в рамках транзакции

**Сценарий:** Вызываются `insertProduct` и `updateProductPrice` внутри одного `@Transactional` метода. Метод `updateProductPrice` выбрасывает `RuntimeException`.

**Код:**
```java
@Transactional
public String experiment2_SecondFailsInTransaction() {
    Product inserted = productService.insertProduct(..., "Монитор", 35000.0, 5);
    productService.updateProductPrice(..., inserted.getId(), 30000.0, true); // ← ошибка
}
```

**Ожидаемый результат:**
- Таблица `products` **не изменится** (откат всей транзакции)
- В `operation_logs` — записи с FAILURE для update и insert (через логирование в REQUIRES_NEW)

**Фактический результат:**
- ✔ Продукт «Монитор» НЕ появился в таблице `products` — транзакция откатилась
- ✔ Логи записаны: insertProduct=SUCCESS (до ошибки), updateProductPrice=FAILURE
- ✔ Несмотря на откат основной транзакции, логи сохранены (REQUIRES_NEW)

**Вывод:** При возникновении исключения в рамках `@Transactional` метода **откатываются все операции** — как insert, так и update. Логи сохраняются благодаря отдельной транзакции (`REQUIRES_NEW`).

---

### 4.3. Эксперимент 3: Оба метода вне контекста транзакции

**Сценарий:** Вызываются `insertProduct` и `updateProductPrice` без `@Transactional`. Каждая операция выполняется в своей auto-commit транзакции.

**Код:**
```java
// НЕТ @Transactional
public String experiment3_BothWithoutTransaction() {
    Product inserted = productService.insertProduct(..., "Клавиатура", 5000.0, 50);
    productService.updateProductPrice(..., inserted.getId(), 4500.0, false);
}
```

**Ожидаемый результат:**
- В `products` — запись `Клавиатура, price=4500.0`
- В `operation_logs` — 3 записи SUCCESS

**Фактический результат:**
- ✔ Продукт вставлен и обновлён
- ✔ Все логи записаны

**Вывод:** Без `@Transactional` каждый вызов `save()` в JPA коммитится отдельно (auto-commit). Методы работают независимо друг от друга.

## 5. Сводная таблица результатов

| Эксперимент | @Transactional | Метод 1 (insert) | Метод 2 (update) | Данные в БД | Логи |
|-------------|---------------|-------------------|-------------------|-------------|------|
| 1 | Да | SUCCESS | SUCCESS | Сохранены | Записаны |
| 2 | Да | SUCCESS → ROLLBACK | FAILURE → ROLLBACK | Откат | Записаны (REQUIRES_NEW) |
| 3 | Нет | SUCCESS | SUCCESS | Сохранены | Записаны |

## 6. Анализ и выводы

### 6.1. Атомарность транзакций

Эксперимент 2 наглядно демонстрирует **атомарность**: хотя `insertProduct` выполнился успешно, при ошибке в `updateProductPrice` обе операции были откачены. Это гарантирует целостность данных — в БД не останется частично выполненных операций.

### 6.2. Независимое логирование

Использование `Propagation.REQUIRES_NEW` для `LogService` позволяет сохранять записи аудита даже при откате основной транзакции. Это критически важно для:
- Отладки и мониторинга
- Аудита операций
- Анализа причин ошибок

### 6.3. Auto-commit vs Managed Transactions

В эксперименте 3 (без `@Transactional`) каждая операция `save()` коммитится немедленно. Это означает:
- Нет возможности атомарно выполнить несколько операций
- При ошибке во втором методе первая операция уже зафиксирована
- Подходит только для независимых операций

### 6.4. Рекомендации

1. **Используйте `@Transactional`** для бизнес-операций, требующих атомарности
2. **Логирование в `REQUIRES_NEW`** — для записей, которые должны сохраняться независимо
3. **Не злоупотребляйте `@Transactional`** — длинные транзакции блокируют ресурсы БД
4. **Обрабатывайте исключения** на уровне контроллера для информирования клиента

## 7. Схема базы данных

```
┌──────────────────────┐     ┌──────────────────────────┐
│      products        │     │     operation_logs        │
├──────────────────────┤     ├──────────────────────────┤
│ id        BIGINT PK  │     │ id              BIGINT PK│
│ name      VARCHAR    │     │ experiment_name VARCHAR   │
│ price     DOUBLE     │     │ method_name     VARCHAR   │
│ quantity  INT        │     │ status          VARCHAR   │
└──────────────────────┘     │ error_message   VARCHAR   │
                             │ executed_at     DATETIME  │
                             └──────────────────────────┘
        (связи между таблицами отсутствуют — логи независимы)
```

## 8. Инструменты и технологии

| Технология | Версия | Назначение |
|------------|--------|------------|
| Java | 17 | Язык программирования |
| Spring Boot | 3.2.4 | Фреймворк |
| Spring Data JPA | — | ORM и работа с БД |
| MySQL | 8.x | СУБД |
| Maven | 3.x | Сборка проекта |
| Hibernate | — | Реализация JPA |
