<div align="center">

# ⚡ FLUX360
### Personal Finance Management System

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/Auth-JWT-black?style=flat-square&logo=jsonwebtokens)](https://jwt.io/)
[![Flyway](https://img.shields.io/badge/Migration-Flyway-red?style=flat-square&logo=flyway)](https://flywaydb.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)

**FLUX360** is a full-stack personal finance management platform that consolidates income tracking, expense management, budgets, investments, precious metals, and financial goals into a single, intelligent dashboard.

[Features](#features) · [Tech Stack](#tech-stack) · [Architecture](#architecture) · [Getting Started](#getting-started) · [API Overview](#api-overview) · [Roadmap](#roadmap)

</div>

---

## Features

### User Module

| Feature | Description |
|---|---|
| Authentication | JWT-based registration & login with BCrypt password encryption |
| Income Management | Track and categorize income sources |
| Expense Tracking | Log and analyze spending patterns |
| Budget Planning | Set and monitor budgets with real-time progress |
| Financial Goals | Define savings goals and track contributions |
| Investment Portfolio | Manage stocks and investment holdings |
| Mutual Fund Tracking | Live NAV data via MFAPI integration |
| Gold & Silver Prices | Real-time precious metal prices via GoldAPI with fallback support |
| Dashboard Analytics | 12-month grouped bar chart and financial summaries |

### Admin Module

| Feature | Description |
|---|---|
| User Management | View, block, unblock, and delete user accounts |
| Audit Logs | Track admin actions, user management events, and system changes |
| System Settings | Configure cache TTLs, price correction factors, and USD-INR rate |
| Cache Control | Clear application cache on demand |

---

## Tech Stack

**Backend**

- Java 21
- Spring Boot 3.2
- Spring Security + JWT
- Spring Data JPA / Hibernate
- Flyway (database migrations)
- Caffeine Cache

**Database**

- MySQL 8.0

**Frontend**

- HTML / CSS / Vanilla JavaScript

**External APIs**

- [GoldAPI](https://www.goldapi.io/) — Live gold & silver prices (INR)
- [MFAPI](https://www.mfapi.in/) — Mutual fund NAV data

---

## Architecture

```
┌─────────────────────────────────┐
│      Frontend (HTML/CSS/JS)     │
└────────────────┬────────────────┘
                 │  HTTP/REST
┌────────────────▼────────────────┐
│     Spring Boot REST APIs       │
└────────────────┬────────────────┘
                 │
┌────────────────▼────────────────┐
│   Spring Security + JWT Filter  │
└────────────────┬────────────────┘
                 │
┌────────────────▼────────────────┐
│         Service Layer           │
│    (Business Logic + Cache)     │
└────────────────┬────────────────┘
                 │
┌────────────────▼────────────────┐
│      JPA / Hibernate ORM        │
└────────────────┬────────────────┘
                 │
┌────────────────▼────────────────┐
│          MySQL Database         │
└─────────────────────────────────┘
```

---

## Security

- **JWT Authentication** — Stateless token-based auth on all protected endpoints
- **Role-Based Access Control** — `ROLE_USER` and `ROLE_ADMIN` separation
- **Admin-Only Endpoints** — User management and system settings restricted to admins
- **BCrypt Password Hashing** — Passwords never stored in plain text
- **Spring Security Filter Chain** — Custom security configuration per route

---

## Caching Strategy

FLUX360 uses **Caffeine Cache** to reduce external API calls and improve response times.

| Cache | Purpose | Configurable TTL |
|---|---|---|
| Gold Price | Live gold price (INR) | ✅ via Admin Settings |
| Silver Price | Live silver price (INR) | ✅ via Admin Settings |
| App Config | System-wide settings | ✅ via Admin Settings |

Admins can also trigger a full cache flush from the admin dashboard without restarting the application.

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- MySQL 8.0+
- A [GoldAPI](https://www.goldapi.io/) key (free tier available)

### 1. Clone the Repository

```bash
git clone https://github.com/<your-username>/FLUX360.git
cd FLUX360
```

### 2. Configure the Database

Create the database:

```sql
CREATE DATABASE flux360;
```

Update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/flux360
spring.datasource.username=root
spring.datasource.password=your_password
```

Flyway will automatically apply all migrations on startup.

### 3. Configure API Keys

```properties
# GoldAPI
goldapi.key=YOUR_GOLDAPI_KEY

gold.api.url=https://www.goldapi.io/api/XAU/INR
gold.fallback.url=https://api.gold-api.com/price/XAU

silver.api.url=https://www.goldapi.io/api/XAG/INR
silver.fallback.url=https://api.gold-api.com/price/XAG

# Mutual Fund API
mfapi.base.url=https://api.mfapi.in/mf
```

### 4. Run the Application

```bash
mvn clean install
mvn spring-boot:run
```

The application starts at `http://localhost:8080`.

---

## API Overview

All endpoints (except `/auth/**`) require a valid JWT in the `Authorization: Bearer <token>` header.

| Method | Endpoint | Description | Role |
|---|---|---|---|
| `POST` | `/auth/register` | Register a new user | Public |
| `POST` | `/auth/login` | Login and receive JWT | Public |
| `GET` | `/api/income` | Get all income records | User |
| `POST` | `/api/income` | Add income record | User |
| `GET` | `/api/expenses` | Get all expenses | User |
| `POST` | `/api/budgets` | Create a budget | User |
| `GET` | `/api/goals` | Get financial goals | User |
| `GET` | `/api/investments` | Get investment portfolio | User |
| `GET` | `/api/metals/gold` | Get live gold price | User |
| `GET` | `/api/metals/silver` | Get live silver price | User |
| `GET` | `/api/admin/users` | List all users | Admin |
| `PUT` | `/api/admin/users/{id}/block` | Block a user | Admin |
| `PUT` | `/api/admin/users/{id}/unblock` | Unblock a user | Admin |
| `DELETE` | `/api/admin/users/{id}` | Delete a user | Admin |
| `GET` | `/api/admin/audit-logs` | View audit logs | Admin |
| `GET` | `/api/admin/config` | View system settings | Admin |
| `PUT` | `/api/admin/config/{key}` | Update a single setting | Admin |
| `POST` | `/api/admin/config/bulk` | Bulk update settings | Admin |
| `POST` | `/api/admin/config/cache/evict` | Clear application cache | Admin |

---

## Database Migrations

FLUX360 uses **Flyway** for versioned, repeatable database migrations.

- All migration scripts live in `src/main/resources/db/migration/`
- Naming convention: `V{version}__{description}.sql`
- Flyway runs automatically on application startup

---

## Roadmap

- [ ] React Frontend
- [ ] Redis Cache Integration
- [ ] Docker Compose setup
- [ ] Cloud Hosting (AWS / Railway)
- [ ] Stock Market Integration
- [ ] Portfolio Analytics & Charts
- [ ] Notification System (email alerts for budget overruns)

---

## Learning Outcomes

Building FLUX360 provided hands-on experience with:

- Spring Boot REST API development
- Spring Security and JWT authentication
- JPA, Hibernate, and relational database design
- Flyway for database version control
- Caffeine caching and cache invalidation strategies
- Third-party API integration with fallback handling
- Role-based access control and admin dashboard design

---

## Author

**Rishanth S**  
Aspiring Software Development Engineer — Java Backend, Spring Boot, System Design

[![GitHub](https://img.shields.io/badge/GitHub-@rishanths-181717?style=flat-square&logo=github)](https://github.com/rishanth11)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-0A66C2?style=flat-square&logo=linkedin)](https://www.linkedin.com/in/Rishanth11)

---

<div align="center">

⭐ If you find this project useful, consider giving it a star!

</div>
