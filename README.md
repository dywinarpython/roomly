# RoomLy — сервис бронирования номеров (RU / EN)

---

## Краткое описание

**RoomLy** — современная платформа для поиска, бронирования и управления номерами, реализованная на базе **Spring Boot (Java)**. Приложение использует реляционную базу данных для хранения основной информации, кеширование для повышения производительности и S3‑совместимое облачное хранилище для медиафайлов. Авторизация реализована через **Keycloak** (OAuth2 / JWT).

**Ключевые возможности:**

* Поиск и фильтрация номеров по различным критериям
* Административная панель для управления номерами и бронированиями
* Кеширование данных (повышение производительности)
* Хранение медиафайлов в S3‑совместимом облаке

---

## Что в репозитории

В репозитории содержится бэкенд на Spring Boot: контроллеры, сервисы, репозитории (Spring Data JPA), конфигурация безопасности (Keycloak), интеграция с S3‑совместимым хранилищем и конфигурация для запуска в Docker.

---

## Стек технологий

* Java, Spring Boot (Spring MVC, Spring Data JPA)
* PostgreSQL (реляционная БД)
* Redis (кеширование)
* Keycloak (авторизация / OAuth2)
* S3‑совместимое облачное хранилище для медиа
* Docker / Docker Compose

---

## Docker:

Docker image доступен на Docker Hub: `dywinar/roomly:latest`.

**Быстрый запуск (пример):**

1. Скачайте образ:

```bash
docker pull dywinar/roomly:latest
```

2. Подготовьте файл `.env` в корне проекта (в нём хранятся секреты и параметры подключения).

3. Запустите через `docker-compose` (в корне проекта должен быть `docker-compose.yml`):

```bash
docker-compose up -d
```

После запуска приложение будет доступно по адресу:

```
http://localhost:8000
```

> Важно: **URL Keycloak не должен быть `localhost` внутри контейнера.** Для работы в Docker укажите рабочий адрес/имя контейнера Keycloak (например, `http://keycloak:8080/realms/roomly`) или настройте корректный внешний адрес, доступный контейнеру.

---

## Недочёты и рекомендации (важно)

* В текущей версии проекта отсутствует асинхронная обработка больших медиа‑файлов: при загрузке файлов HTTP‑запрос держится до завершения загрузки. При больших файлах или медленном канале это блокирует поток веб‑сервера и ухудшает отзывчивость.

* Проверяйте корректность конфигурации облачного хранилища (endpoint, регион). Ошибка подписи S3 (`The request signature we calculated does not match...`) обычно связана с неправильными ключами, регионом, временем на сервере или некорректным формированием ключа объекта.

---

## Быстрая проверка при проблемах

1. Если получаете 403 от S3 — проверьте: ключи, регион, endpoint, системное время (clock skew).
2. Если контейнер не видит нужных переменных — убедитесь, что `.env` лежит в той же папке, где запускается `docker-compose`, и пересоберите/перезапустите контейнеры (`docker-compose down -v && docker-compose up -d --build`).

---

## Планы развития

* Реализовать неблокирующую/асинхронную загрузку медиа (presigned URLs или очереди)
* Создать пользовательский фронтенд (веб/мобильный)
* Внедрить более умный поиск (например, Elasticsearch)
* Возможный переход на микросервисную архитектуру для горизонтального масштабирования

---

# RoomLy — Booking Service

## Short description

**RoomLy** is a modern room booking and management platform built with **Spring Boot (Java)**. It uses a relational database for core data, caching to improve performance, and S3‑compatible cloud storage for media. Authentication is provided by **Keycloak** (OAuth2 / JWT).

**Key features:**

* Room search and filtering
* Admin panel for room and booking management
* Caching for performance
* Media storage in S3‑compatible cloud

---

## What's in the repository

Backend (Spring Boot) with controllers, services, repositories (Spring Data JPA), Keycloak security config, S3 integration and Docker configuration.

---

## Tech stack

* Java, Spring Boot (Spring MVC, Spring Data JPA)
* PostgreSQL
* Redis
* Keycloak (OAuth2/JWT)
* S3‑compatible cloud storage
* Docker / Docker Compose

---

## Docker:

Docker image is available on Docker Hub: `dywinar/roomly:latest`.

**Quick start:**

1. Pull the image:

```bash
docker pull dywinar/roomly:latest
```

2. Prepare a `.env` file with runtime parameters and secrets (DB, Redis, Keycloak, storage keys).

3. Run with Docker Compose:

```bash
docker-compose up -d
```

Application will be available at:

```
http://localhost:8000
```

**Note:** Keycloak URL must be reachable from the application container (do not use `localhost` as container address). For example use `http://keycloak:8080/realms/roomly` when Keycloak runs as a Docker service in the same compose network.

---

## Shortcoming and recommendations

* Media upload is currently blocking: large file uploads keep HTTP requests open and consume server threads. Consider using presigned S3 URLs, background workers, or a message queue for file processing.

* Secure secrets with secret managers and avoid storing them in the repository.

---

## Troubleshooting tips

* S3 403 signature errors: check access keys, region, endpoint and system time.
* Docker environment variables: ensure `.env` is present in the compose folder and recreate containers after changes.

---

## Roadmap

* Non‑blocking media uploads (presigned URLs, background processing)
* Frontend client (web/mobile)
* Advanced search (e.g., Elasticsearch)
* Microservices split for scalability
