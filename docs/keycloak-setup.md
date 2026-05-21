# Настройка Keycloak для sport-poker

Данная инструкция описывает пошаговую настройку Keycloak, необходимую для работы `KeycloakUserService`.

## Предварительные требования

- Docker и Docker Compose установлены
- Запущен `docker-compose-local.yml` (Keycloak доступен на `http://localhost:9090`)

```bash
docker compose -f docker-compose-local.yml up -d
```

Учётные данные администратора Keycloak: `admin` / `admin`.

> **Для продакшена** обязательно смените логин и пароль администратора — см. [раздел «Смена учётных данных администратора»](#7-смена-учётных-данных-администратора).

---

## 1. Создание Realm

1. Открыть **Admin Console**: `http://localhost:9090`
2. Войти как `admin` / `admin`
3. В левом верхнем углу нажать на выпадающий список рядом с "master" → **Create Realm**
4. Заполнить:
   - **Realm name**: `poker_realm`
5. Нажать **Create**

> Все дальнейшие действия выполняются внутри `poker_realm`.

---

## 2. Создание клиента `pokerClient` (admin-клиент)

Этот клиент используется бэкендом для **административных операций** (создание пользователей, получение информации о пользователях) через `client_credentials` grant.

1. Перейти в **Clients** → **Create client**
2. **General Settings**:
   - **Client ID**: `pokerClient`
   - **Client type**: OpenID Connect
3. **Capability config**:
   - **Client authentication**: `ON`
   - **Authorization**: `OFF`
   - **Authentication flow**: оставить только **Service accounts roles** (убрать Standard flow и Direct access grants)
4. Нажать **Save**
5. Перейти на вкладку **Credentials** → скопировать **Client secret**
   - Это значение свойства `keycloak.credentials.secret` в `application.properties`

### Назначение прав Service Account

Чтобы `pokerClient` мог управлять пользователями через Admin API:

1. Перейти в **Clients** → `pokerClient` → вкладка **Service account roles**
2. Нажать **Assign role**
3. В фильтре выбрать **Filter by clients**
4. Найти и назначить следующие роли из клиента `realm-management`:
   - `manage-users` — создание, удаление, изменение пользователей
   - `view-users` — просмотр пользователей
   - `query-users` — поиск пользователей

---

## 3. Создание клиента `userPokerClient` (пользовательский клиент)

Этот клиент используется для **аутентификации пользователей** через `password` grant (Resource Owner Password Credentials) и для **авторизации** через JWT-токены.

1. Перейти в **Clients** → **Create client**
2. **General Settings**:
   - **Client ID**: `userPokerClient`
   - **Client type**: OpenID Connect
3. **Capability config**:
   - **Client authentication**: `ON`
   - **Authorization**: `OFF`
   - **Authentication flow**: включить **Direct access grants** (для password-логина). Standard flow — по необходимости.
4. Нажать **Save**
5. Перейти на вкладку **Credentials** → скопировать **Client secret**
   - Это значение свойства `keycloak.credentials.user-secret` в `application.properties`

### Создание клиентской роли `user`

Роли извлекаются из JWT-токена из секции `resource_access.userPokerClient.roles` (см. `SecurityConfig.jwtAuthenticationConverter`).

1. Перейти в **Clients** → `userPokerClient` → вкладка **Roles**
2. Нажать **Create role**
3. **Role name**: `user`
4. Нажать **Save**

### Настройка маппера ролей в токен

Чтобы клиентские роли `userPokerClient` попадали в JWT-токен (`resource_access`):

1. Перейти в **Clients** → `userPokerClient` → вкладка **Client scopes**
2. Кликнуть на `userPokerClient-dedicated` (Dedicated scope)
3. Нажать **Add mapper** → **By configuration** → **User Client Role**
4. Заполнить:
   - **Name**: `client-roles`
   - **Client ID**: `userPokerClient`
   - **Token Claim Name**: `resource_access.userPokerClient.roles`
   - **Add to ID token**: `ON`
   - **Add to access token**: `ON`
5. Нажать **Save**

---

## 4. Назначение роли `user` всем новым пользователям (по умолчанию)

Чтобы пользователи, создаваемые через `KeycloakUserService.createUser()`, автоматически получали роль `user`:

1. Перейти в **Realm settings** → вкладка **User registration** (или **Default roles** в зависимости от версии)
2. В более новых версиях Keycloak: перейти в **Realm roles** → **default-roles-poker_realm** → **Associated roles** → **Assign role**
3. В фильтре выбрать **Filter by clients**
4. Найти `userPokerClient` → выбрать роль `user`
5. Нажать **Assign**

> **Альтернатива**: Если автоматическое назначение не требуется, роль `user` нужно назначать каждому пользователю вручную через **Users** → пользователь → **Role mapping** → **Assign role** → `userPokerClient: user`.

---

## 5. Настройка свойств приложения

В `application.properties` (или через переменные окружения) должны быть указаны:

```properties
# URL Keycloak-сервера
keycloak.auth-server-url=http://localhost:9090

# Имя realm
keycloak.realm=poker_realm

# Admin-клиент (client_credentials grant)
keycloak.resource=pokerClient
keycloak.credentials.secret=<CLIENT_SECRET из шага 2>

# Пользовательский клиент (password grant)
keycloak.resource-user=userPokerClient
keycloak.credentials.user-secret=<CLIENT_SECRET из шага 3>

# JWT issuer для валидации токенов
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9090/realms/poker_realm

# OAuth2 client registration (для Spring Security)
spring.security.oauth2.client.registration.keycloak.client-id=pokerClient
spring.security.oauth2.client.registration.keycloak.client-secret=<CLIENT_SECRET из шага 2>
spring.security.oauth2.client.provider.keycloak.issuer-uri=http://localhost:9090/realms/poker_realm
```

---

## 6. Проверка работоспособности

### Регистрация пользователя

```bash
curl -X POST http://localhost:8083/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "email": "test@example.com", "password": "test123"}'
```

### Аутентификация (получение токена)

```bash
curl -X POST http://localhost:8083/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "test123"}'
```

### Проверка токена напрямую через Keycloak

```bash
curl -X POST http://localhost:9090/realms/poker_realm/protocol/openid-connect/token \
  -d "client_id=userPokerClient" \
  -d "client_secret=<USER_CLIENT_SECRET>" \
  -d "grant_type=password" \
  -d "username=testuser" \
  -d "password=test123"
```

Декодировать полученный `access_token` на [jwt.io](https://jwt.io) и убедиться, что в нём присутствует:

```json
{
  "resource_access": {
    "userPokerClient": {
      "roles": ["user"]
    }
  }
}
```

---

## 7. Смена учётных данных администратора

Для локальной разработки достаточно `admin` / `admin`. **Для продакшена смена обязательна.**

### Способ 1. Через переменные окружения (до первого запуска)

В `docker-compose.yml` (или `docker-compose-local.yml`) изменить:

```yaml
environment:
  KEYCLOAK_ADMIN: my_secure_admin
  KEYCLOAK_ADMIN_PASSWORD: S0meStr0ngP@ssw0rd!
```

> **Важно:** переменные `KEYCLOAK_ADMIN` / `KEYCLOAK_ADMIN_PASSWORD` создают учётную запись только при **первом запуске** контейнера. Если Keycloak уже был запущен, изменение этих переменных не повлияет на существующего пользователя. В таком случае используйте способ 2 или пересоздайте контейнер с удалением данных.

### Способ 2. Через Admin Console (после запуска)

1. Войти в Admin Console: `http://localhost:9090` под текущим логином
2. Нажать на имя пользователя в правом верхнем углу → **Manage account**
3. В разделе **Personal info** — сменить логин
4. В разделе **Signing in** → **Password** → **Update** — сменить пароль

---

## Сводная таблица

| Сущность в Keycloak         | Значение              | Назначение                                     |
|------------------------------|-----------------------|------------------------------------------------|
| Realm                        | `poker_realm`         | Изолированное пространство для приложения      |
| Client `pokerClient`         | confidential, service account | Admin API: CRUD пользователей          |
| Client `userPokerClient`     | confidential, direct access   | Логин пользователей, JWT с ролями      |
| Client role `user`           | на `userPokerClient`  | Авторизация эндпоинтов (`hasRole("user")`)     |
| Service account roles        | `manage-users`, `view-users`, `query-users` | Права admin-клиента        |

---

## Частые проблемы

| Симптом | Причина | Решение |
|---------|---------|---------|
| `403 Forbidden` на `/api/room/**` | Роль `user` отсутствует в токене | Проверить маппер ролей и назначение роли пользователю |
| `Ошибка создания пользователя` | У `pokerClient` нет прав `manage-users` | Назначить service account роли (шаг 2) |
| `Invalid username or password` при логине | Неверный `client-secret` для `userPokerClient` | Проверить `keycloak.credentials.user-secret` |
| JWT issuer mismatch | URL в настройках не совпадает с Keycloak | URL должен быть доступен и с сервера, и из браузера |
