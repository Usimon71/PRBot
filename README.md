# Bot for tracking pullrequests via Telegram 🤖

## 🚀 Features

- GitHub OAuth authentication
- Subscribe to repositories
- Notifications about new pull requests in Telegram
- Easy-to-use interface for interacting with the bot

## 🛠️ Technologies

- Java 23
- Spring Boot 3.1.4
- **Spring WebFlux** — reactive version of Spring Web (for handling HTTP requests)
- **Spring Data R2DBC** — for interacting with PostgreSQL in a reactive manner using R2DBC
- **PostgreSQL** (using R2DBC for asynchronous interactions)
- **Liquibase** — for database migrations
- JUnit + Mockito — for testing