# SF\_RxJava

## Описание проекта

Кастомная реактивная библиотека на Java, реализующая базовые принципы Rx-подобного программирования: подписку, трансформации потока данных, планирование выполнения в разных потоках.

## Основные функции

* Поддержка операторов `map`, `filter`, `flatMap` для потоков данных.
* Управление потоками через `subscribeOn` и `observeOn`.
* Пользовательская реализация `Scheduler` и планировщиков: IO, computation, single.
* Простой интерфейс подписчика `Observer` и управления подпиской `Disposable`.
* Асинхронная и лениво-вычисляемая цепочка обработки событий.

## Технологии

* Java 21
* JUnit 5

## Установка и запуск

1. Склонировать репозиторий:

   ```bash
   git clone https://github.com/example-user/SF_RxJava.git
   cd SF_RxJava
   ```
2. Собрать проект:

   ```bash
   mvn clean install
   ```
3. Запустить демо:

   ```bash
   mvn exec:java -Dexec.mainClass="mephi.rxjava.Main"
   ```

## Структура проекта

```plaintext
SF_RxJava/
├── src/
│   ├── main/java/mephi/rxjava/
│   │   ├── Observable.java
│   │   ├── Observer.java
│   │   ├── Disposable.java
│   │   ├── SimpleDisposable.java
│   │   ├── Scheduler.java
│   │   ├── IOThreadScheduler.java
│   │   ├── ComputationScheduler.java
│   │   ├── SingleThreadScheduler.java
│   │   ├── Main.java
│   └── test/java/mephi/rxjava/test/
│       ├── ObservableTest.java
│       ├── MapOperatorTest.java
│       ├── FilterOperatorTest.java
│       ├── FlatMapOperatorTest.java
│       ├── SubscribeOnObserveOnTest.java
│       ├── SchedulerTest.java
│       ├── DisposableTest.java
│       ├── ObserverTest.java
│       └── ErrorHandlingTest.java
```

## Схема архитектуры

```plaintext
Observable.create()
       │
       ▼
    map(x -> ...)
       │
       ▼
   filter(x -> ...)
       │
       ▼
  subscribeOn(Scheduler)
       │
       ▼
  observeOn(Scheduler)
       │
       ▼
     Observer
```

## Тестирование

Тестирование покрывает основные аспекты библиотеки:

* **Функциональные операторы:**

  * `map`, `filter`, `flatMap`
* **Работа с потоками:**

  * `subscribeOn`, `observeOn` через `Scheduler`
* **Ошибки и завершение:**

  * Прекращение потока через `onError`, `onComplete`
  * Поведение `Disposable`
  * Обработка ошибок внутри операторов (например, `map(x -> 10 / x)`)

Запуск тестов осуществляется через `mvn test`. В тестах активно используются `AtomicBoolean`, `AtomicReference`, `CountDownLatch` и проверка потоков выполнения.

## Отчёт

### Архитектура системы

Библиотека построена вокруг модели реактивного потока:

* `Observable<T>` — создаёт поток событий и управляет цепочкой операторов.
* `Observer<T>` — подписчик, получающий `onNext`, `onError`, `onComplete`.
* `Scheduler` — абстракция для планирования выполнения задач (`execute(Runnable)`).
* Три реализации планировщика:

  * `IOThreadScheduler` — CachedThreadPool
  * `ComputationScheduler` — FixedThreadPool
  * `SingleThreadScheduler` — SingleThreadExecutor

Каждый оператор (`map`, `filter`, `flatMap`) возвращает новый `Observable` и не нарушает чистоту потока. Методы `subscribeOn` и `observeOn` обеспечивают смену потока исполнения.

### Принципы работы Schedulers

```java
public interface Scheduler {
    void execute(Runnable task);
}
```

* **IOThreadScheduler**: используется для неблокирующих операций, многопоточная очередь.
* **ComputationScheduler**: используется для CPU-bound операций.
* **SingleThreadScheduler**: линейное исполнение задач в одном потоке.

Каждый Scheduler реализует стратегию исполнения с помощью стандартного пула потоков Java.

### Процесс тестирования

Тесты реализованы с использованием JUnit 5. Проверяются ключевые сценарии:

* Правильная трансформация данных (`map`, `filter`)
* Слияние потоков (`flatMap`)
* Асинхронность и распределение по потокам (`subscribeOn`, `observeOn`)
* Обработка ошибок (`onError`)
* Завершение (`onComplete`)
* Проверка работоспособности `Disposable`
* Симуляция исключений в операторах (`ArithmeticException` в `map`)

Для синхронизации и отслеживания состояний применяются `AtomicBoolean`, `AtomicReference` и `CountDownLatch`.

### Пример использования

```java
Observable<Integer> observable = Observable.create(emitter -> {
    emitter.onNext(10);
    emitter.onNext(0); // вызовет ошибку при делении
    emitter.onNext(5);
    emitter.onComplete();
});

observable
    .map(x -> 100 / x) // может выбросить ArithmeticException
    .subscribeOn(new IOThreadScheduler())
    .observeOn(new ComputationScheduler())
    .subscribe(new Observer<Integer>() {
        @Override
        public void onNext(Integer item) {
            System.out.println("Received: " + item);
        }

        @Override
        public void onError(Throwable e) {
            System.err.println("Handled error: " + e.getMessage());
        }

        @Override
        public void onComplete() {
            System.out.println("Stream complete");
        }
    });
```

---

Проект реализован в рамках учебного задания МИФИ

Студент: Хананиев Тимур

Telegram: @tim\_khan
GitHub: [https://github.com/tkhananiev](https://github.com/tkhananiev)
