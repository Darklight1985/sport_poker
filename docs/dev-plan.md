# 🚀 Технический план реализации: Карточная механика и SSE

## 1. Обзор изменений
Добавляем полноценную карточную механику в игру.
- **Привязка:** Упражнения рандомно связываются с мастями карт.
- **Карты:** Раздача, очки (числовые карты, фигуры, Джокеры).
- **Джокеры:** Красный/Черный джокер требует выполнения **двух** упражнений (по 15 очков за каждое), чтобы открыть 30 очков.
- **SSE:** Трансляция состояния (текущая карта, счет, таймер, маппинг) игрокам в реальном времени.
- **Хранение:** Состояние колоды и карт хранится **в памяти** (в `GameRoom`), а не в БД. Счет (`score`) сохраняется в БД.

---

## 2. Новые сущности и Enums

### 2.1. Enums (`src/main/java/ru/poker/sportpoker/enums/`)

1.  **`Suit`** (Масти):
    *   `SPADES` (Пики, черная)
    *   `HEARTS` (Черви, красная)
    *   `DIAMONDS` (Бубны, красная)
    *   `CLUBS` (Трефы, черная)
2.  **`CardRank`** (Достоинство):
    *   `TWO` ... `TEN` (Номинал)
    *   `JACK` (11 очков)
    *   `QUEEN` (12 очков)
    *   `KING` (13 очков)
    *   `ACE` (14 очков)
    *   `JOKER` (30 очков)
3.  **`CardColor`** (Цвет):
    *   `RED`
    *   `BLACK`

### 2.2. DTO (`src/main/java/ru/poker/sportpoker/dto/`)

1.  **`CardDto`**:
    ```java
    Suit suit;
    CardRank rank;
    int points;
    boolean isJoker;
    boolean revealed; // true только для владельца
    ```
2.  **`GameMappingDto`**:
    ```java
    Map<Suit, Exercises> suitToExercises;
    ```
3.  **`PlayerStatsDto`**:
    ```java
    UUID playerId;
    int score;
    CardDto currentCard;
    ```
4.  **`GameRankingDto`**:
    ```java
    List<PlayerRanking> rankings; // {rank, playerId, score}
    ```

### 2.3. Изменения в Domain (`src/main/java/ru/poker/sportpoker/domain/`)

1.  **`GameRoom`**:
    *   `Map<Suit, Exercises> exerciseMapping` (JSON в БД или transient).
    *   `Deque<Card> deck` (Колода в памяти).
    *   `int cardsDealt` (Счетчик выданных карт, чтобы знать, когда колода кончилась).
2.  **`GameRoomPlayer`**:
    *   `int score` (Очки игрока).
    *   `CardDto currentCard` (Текущая открытая карта игрока).
    *   `boolean hasRedJoker` (Флаг для оптимизации проверки джокера, опционально).
    *   `boolean hasBlackJoker` (Флаг для оптимизации проверки джокера, опционально).
    *   `Map<Suit, Boolean> completedExercises` (Какие упражнения выполнены за текущую раздачу, для джокеров).

---

## 3. Сервисы и Логика

### 3.1. `DeckService` (Логика карт)
*   `createDeck()`: Создает 54 карты (52 стандартные + 2 джокера).
*   `shuffleDeck()`: Перемешивает колоду.
*   `dealCard()`: Выдает карту сверху.
*   `getPoints(Card card)`: Возвращает очки.

### 3.2. `ExerciseMapper` (Привязка)
*   `mapExercises(Set<Exercises> exercises)`:
    *   Берет 4 упражнения.
    *   Берет 4 масти.
    *   Перемешивает масти.
    *   Возвращает `Map<Suit, Exercises>`.

### 3.3. `ScoringService` (Очки)
*   `calculatePoints(Card card)`:
    *   Номинал -> номинал.
    *   J -> 11, Q -> 12, K -> 13, A -> 14.
    *   Joker -> 30.
*   `validateJokerCompletion(Card card, Map<Suit, Boolean> completed)`:
    *   Если Joker: проверяет, выполнены ли обе соответствующие масти (красные или черные).

### 3.4. `GameRoomSseService` (SSE)
*   `addSubscriber(UUID roomId, SseEmitter emitter)`: Добавляет клиента в список подписчиков комнаты.
*   `removeSubscriber(UUID roomId, SseEmitter emitter)`: Удаляет.
*   `sendToRoom(UUID roomId, String eventName, Object data)`: Рассылает всем в комнате.
*   `sendToPlayer(UUID roomId, UUID playerId, String eventName, Object data)`: Рассылает конкретному игроку.

---

## 4. Интеграция в процесс игры (`GameRoomServiceImpl`)

### 4.1. Старт игры (`readyToGame` -> `PLAY`)
1.  Проверка готовности.
2.  Если `PLAY`:
    *   `ExerciseMapper.mapExercises(exercises)`.
    *   `DeckService.createDeck()` + `shuffle`.
    *   Раздача стартовых карт: `GameRoomPlayer.currentCard = dealCard()`.
    *   Сброс очков: `score = 0`.
    *   Очистка `completedExercises`.
    *   SSE: `sendToRoom(..., "MAPPING", mappingDto)`.
    *   SSE: `sendToPlayer(..., "CARD", cardDto)`.

### 4.2. Игровой цикл (`completeCard`)
*Новый метод:* `POST /api/room/{id}/complete`
1.  Игрок нажимает "Выполнено" (передается только `roomId`).
2.  Получаем карту игрока (`currentCard`).
3.  **Если обычная карта (например, 10 червей)**:
    *   Масть карты определяет упражнение (через маппинг).
    *   Номинал карты (10) = количество повторений.
    *   Начисление: `score += points` (10 очков).
4.  **Если `Joker` (Красный/Черный)**:
    *   Задача: Игрок выполняет **два** упражнения (для Красного: обе красные масти, для Черного: обе черные) на количество, указанное на карте.
    *   Начисление: `score += 30` очков.
5.  **Действие**:
    *   Выдаем новую случайную карту (`deck.deal()`).
    *   SSE: Обновление счета и новой карты игроку.

### 4.3. Конец игры (`GameEndEvent`)
1.  Сортировка игроков по `score` DESC.
2.  SSE: `sendToRoom(..., "RANKING", rankingDto)`.

---

## 5. Пошаговый план реализации (Step-by-Step)

### Этап 1: Базовая структура
1.  Создать Enums: `Suit`, `CardRank`, `CardColor`.
2.  Создать DTO: `CardDto`, `GameMappingDto`, `PlayerStatsDto`.
3.  Добавить поля в `GameRoom` (`exerciseMapping`, `deck`) и `GameRoomPlayer` (`score`, `currentCard`).

### Этап 2: Логика карт
1.  Создать `DeckService` (создание, перемешивание, выдача).
2.  Создать `ExerciseMapper` (рандомизация).
3.  Реализовать логику очков (учет Джокеров).

### Этап 3: Интеграция с игрой ✅
1.  Обновить `GameRoomServiceImpl.readyToGame()`:
    *   Инициализация колоды.
    *   Раздача карт.
    *   Подписка игрока на SSE.
    *   Отправка SSE маппинга и карт.
2.  Создать метод `completeCard(UUID roomId)`.
    *   Подсчет очков.
    *   Выдача новой карты.
    *   Обновление SSE (SCORE всем + CARD текущему).
3.  Обновить `ActivityUsersServiceImpl.endGame()`:
    *   Сбор рейтинга.
    *   Отправка финального SSE.

### Этап 4: SSE (откладывается)
1.  Создать `GameRoomSseService` (управление эмиттерами).
2.  Создать `SseController`:
    *   `GET /api/room/{id}/sse` (подписка).
3.  Интегрировать вызовы `send()` в `GameRoomServiceImpl`.

⚠️ **Важно:** SSE будет реализовано в отдельной ветке как отдельная стадия. Подписка на SSE происходит автоматически при нажатии "готов".

### Этап 5: Тестирование и полировка
1.  Проверка сценария Джокеров.
2.  Проверка восстановления SSE.
3.  Документация (OpenAPI).

---

## 6. Вопросы и уточнения (Примечания)
1.  **Джокер**: Игрок получает карту "Красный Джокер". Он выполняет упражнение "Подтягивания" (Пики). Очки = 15. Карта остается на руках. Затем выполняет "Отжимания" (Черви). Очки = 15 (итого 30). Карта сгорает, выдается новая.
2.  **Колода**: Хранится в `GameRoom` (transient или поле, которое не сохраняется в БД, а восстанавливается при загрузке).
3.  **SSE**: При разрыве соединения клиент должен уметь переподключиться. Сервис должен хранить эмиттеры.
