# Сценарии работы приложения Sport Poker

## Описание

Приложение для проведения групповых спортивных игр с таймером. Пользователи регистрируются через Keycloak, создают игровые комнаты, приглашают игроков и проводят совместные тренировки.

---

## Фазы комнаты (StatusGame)

| Статус | Описание |
|--------|----------|
| `PREP` | Фаза подготовки, сбор игроков |
| `PLAY` | Игра началась, запущен таймер |
| `END` | Игра окончена |

---

## 1. Регистрация и авторизация

### 1.1 Регистрация нового пользователя
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "string",
  "password": "string",
  "email": "string"
}
```
- Создает пользователя в Keycloak
- Возвращает `200 OK` с сообщением `"User registered"`
- **Валидация**: username, password, email не могут быть пустыми

### 1.2 Авторизация (вход)
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "string",
  "password": "string"
}
```
- Аутентификация через Keycloak (Resource Owner Password Credentials Grant)
- Возвращает `AccessTokenResponse` (JWT токен)
- **Валидация**: username, password не могут быть пустыми

### 1.3 Получение информации о текущем пользователе
```
GET /api/user
Authorization: Bearer <token>
```
- Возвращает `UserView` (userId, username, email)

---

## 2. Управление аватаром

### 2.1 Загрузка аватара
```
POST /api/user/{id}/add-avatar
Authorization: Bearer <token>
Content-Type: multipart/form-data

avatar: <file>
```
- Загружает изображение в MinIO
- Возвращает `UploadFileResponse` (fileName, fileType, size)
- **Валидация**: пользователь должен быть владельцем аватара, файл не пустой

### 2.2 Получение аватара
```
GET /api/user/{id}/avatar
Authorization: Bearer <token>
```
- Возвращает изображение из MinIO

---

## 3. Игровые комнаты

### 3.1 Создание комнаты
```
POST /api/room
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "string",
  "gameTime": 30,
  "password": "string (optional)",
  "exercises": ["PULL_UPS", "PUSH_UP", "SQUAT"]
}
```
- Создает комнату и добавляет создателя как первого игрока
- Статус комнаты: `PREP`
- **Валидация**: name не пустой, gameTime > 0, exercises не пустой
- Возвращает `201 Created`

### 3.2 Получение информации о комнате
```
GET /api/room/{id}
Authorization: Bearer <token>
```
- Возвращает `GameRoomView`:
  - `roomId`, `status`, `name`, `gameTime`
  - `creator` — информация о создателе
  - `players` — список игроков с флагом `ready`
  - `minutesLeft` — оставшееся время (во время игры)

### 3.3 Получение списка комнат
```
GET /api/room?statusGame=PREP&name=test&page=0&size=10
Authorization: Bearer <token>
```
- Пагинация через Spring Data
- Фильтрация по `statusGame` и `name` (опционально)
- Возвращает `Page<GameRoomShortView>`

### 3.4 Обновление комнаты (только создатель)
```
PUT /api/room
Authorization: Bearer <token>
Content-Type: application/json

{
  "id": "uuid",
  "name": "string",
  "gameTime": 30,
  "password": "string",
  "exercises": ["PULL_UPS", "PUSH_UP"]
}
```
- Обновляет параметры комнаты
- **Валидация**: только создатель, статус `PREP`
- Возвращает `202 Accepted`

### 3.5 Удаление комнаты (только создатель)
```
DELETE /api/room/{id}
Authorization: Bearer <token>
```
- Удаляет комнату и всех игроков
- **Валидация**: только создатель, статус `PREP`
- Возвращает `201 Created`

### 3.6 Генерация ссылки-приглашения
```
GET /api/room/{id}/link
Authorization: Bearer <token>
```
- Генерирует JWT-токен со ссылкой для входа
- Ссылка действительна **1 час**
- **Валидация**: только создатель, статус `PREP`

### 3.7 Вход в комнату по ссылке (токену)
```
PUT /api/room/join/{token}
Authorization: Bearer <token>
```
- Извлекает roomId из JWT-токена
- Добавляет текущего пользователя как игрока
- **Валидация**: валидный токен, статус `PREP`
- Возвращает `202 Accepted`

### 3.8 Вход в комнату по паролю
```
PUT /api/room/{id}/join
Authorization: Bearer <token>
Content-Type: text/plain

<password>
```
- Проверяет пароль комнаты
- Добавляет текущего пользователя как игрока
- **Валидация**: валидный пароль, статус `PREP`
- Возвращает `202 Accepted`

### 3.9 Отметка готовности к игре
```
POST /api/room/{id}/ready
Authorization: Bearer <token>
```
- Устанавливает флаг `ready = true` для текущего игрока
- **Если все игроки готовы** → статус меняется на `PLAY`, запускается таймер
- Возвращает `202 Accepted` с `true/false` (все ли готовы)

### 3.10 Покинуть комнату
```
POST /api/room/{id}/left
Authorization: Bearer <token>
```
- Удаляет текущего пользователя из комнаты
- **Валидация**: статус `PREP`
- Возвращает `200 OK`

### 3.11 Выкинуть игрока (только создатель)
```
POST /api/room/{id}/kick/{userId}
Authorization: Bearer <token>
```
- Удаляет указанного игрока из комнаты
- **Валидация**: только создатель, игрок является участником, статус `PREP`
- Возвращает `200 OK`

---

## 4. Упражнения (Exercises)

### 4.1 Получение списка упражнений
```
GET /api/enums/exercises
Authorization: Bearer <token>
```
- Возвращает список доступных упражнений:
  - `PULL_UPS` — Подтягивания
  - `PUSH_UP` — Отжимания
  - `SQUAT` — Приседания
  - `DEADLIFT` — Становая тяга
  - `BURPEE` — Берпи
  - `KETTLEBELL_SWING` — Мах гирей
  - `BOX_JUMP` — Запрыгивание на ящик
  - `HANDSTAND` — Стойка на руках
  - `ROPE_CLIMB` — Лазание по канату

---

## Полный сценарий игры (End-to-End)

### Сценарий: Проведение игровой сессии

```
1. Пользователь А регистрируется
   POST /api/auth/register → username: "alice", password: "123", email: "alice@mail.ru"

2. Пользователь А авторизуется
   POST /api/auth/login → получает JWT токен

3. Пользователь А создает комнату
   POST /api/room
   {
     "name": "Утренняя тренировка",
     "gameTime": 30,
     "password": "secret123",
     "exercises": ["PULL_UPS", "PUSH_UP", "SQUAT"]
   }
   → 201 Created

4. Пользователь А получает ссылку-приглашение
   GET /api/room/{roomId}/link
   → "http://localhost:8083/room/join/<token>"

5. Пользователь Б регистрируется
   POST /api/auth/register → username: "bob", password: "123", email: "bob@mail.ru"

6. Пользователь Б авторизуется
   POST /api/auth/login → получает JWT токен

7. Пользователь Б входит в комнату по ссылке
   PUT /api/room/join/{token}
   → 202 Accepted

8. Пользователь Б входит в комнату по паролю (альтернативный способ)
   PUT /api/room/{roomId}/join
   Body: "secret123"
   → 202 Accepted

9. Игрок А получает информацию о комнате
   GET /api/room/{roomId}
   → {
       "roomId": "...",
       "status": "PREP",
       "name": "Утренняя тренировка",
       "gameTime": 30,
       "creator": {"userId": "...", "username": "alice"},
       "players": [
         {"userId": "...", "username": "alice", "ready": false},
         {"userId": "...", "username": "bob", "ready": false}
       ],
       "minutesLeft": null
     }

10. Игрок А отмечает готовность
    POST /api/room/{roomId}/ready
    → 202 Accepted, body: false (не все готовы)

11. Игрок Б отмечает готовность
    POST /api/room/{roomId}/ready
    → 202 Accepted, body: true (все готовы, игра началась!)

12. Игра идет, таймер запущен
    GET /api/room/{roomId}
    → "status": "PLAY", "minutesLeft": 29, 28, ...

13. Игра окончена (таймер истек)
    → "status": "END"

14. Игрок А удаляет комнату
    DELETE /api/room/{roomId}
    → 201 Created
```

---

## Возможные дополнительные сценарии

### Просмотр списка всех комнат
```
GET /api/room?statusGame=PREP&page=0&size=5
```

### Обновление комнаты создателем
```
PUT /api/room
{
  "id": "{roomId}",
  "name": "Новое название",
  "gameTime": 45,
  "exercises": ["BURPEE", "DEADLIFT"]
}
```

### Удаление игрока создателем
```
POST /api/room/{roomId}/kick/{userId}
```

### Загрузка аватара
```
POST /api/user/{userId}/add-avatar
Content-Type: multipart/form-data
avatar: <image.jpg>
```

### Получение аватара
```
GET /api/user/{userId}/avatar
```

---

## Валидации и ограничения

| Операция | Валидация |
|----------|-----------|
| Регистрация | username, password, email не пустые |
| Вход | username, password не пустые |
| Создание комнаты | name не пустой, gameTime > 0, exercises не пустой |
| Обновление комнаты | только создатель, статус PREP |
| Удаление комнаты | только создатель, статус PREP |
| Вход в комнату | статус PREP |
| Ссылка-приглашение | только создатель, статус PREP |
| Готовность | игрок или создатель, статус PREP |
| Покинуть комнату | игрок, статус PREP |
| Выкинуть игрока | только создатель, игрок является участником, статус PREP |

---

## Технические особенности

- **Аутентификация**: Keycloak OAuth2 JWT
- **Хранение данных**: PostgreSQL
- **Файлы**: MinIO (аватары)
- **Конкурентный доступ**: Optimistic Lock (@Version) с retry (3 попытки)
- **Таймер игры**: ScheduledExecutorService, запускается при готовности всех игроков
- **Событие окончания**: Spring ApplicationEvent (GameEndEvent)
- **Валидация**: Chain of Responsibility pattern (Handler)
- **Пагинация**: Spring Data Pageable
