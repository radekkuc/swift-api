# Swift API

## Project Overview
This is a **Spring Boot application** that provides a REST API for managing SWIFT codes. The project supports both **local execution** and **Dockerized deployment**.
---

## Running the Project Locally
### **1 Prerequisites**
Make sure you have installed:
- [Java 17+](https://adoptopenjdk.net/)
- [PostgreSQL](https://www.postgresql.org/)
- [Maven](https://maven.apache.org/)
- [Git](https://git-scm.com/)
- Spring Boot Dependencies:
  - `spring-boot-starter-data-jpa`
  - `spring-boot-starter-validation`
  - `spring-boot-starter-web`
  - `postgresql`
  - `commons-io`
  - `poi-ooxml`
 
  - ### **2 Setup and Run the Database**
1. Create a new PostgreSQL database:
   ```sql
   CREATE DATABASE swift_db;
   ```
2. Start PostgreSQL and connect:
   ```bash
   sudo systemctl start postgresql
   psql -U postgres -d swift_db
   ```
3. Verify the database is running:
   ```sql
   SELECT datname FROM pg_database;
   ```
4. Update the credentials in `src/main/resources/application-local.properties` if needed:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/swift_db
   spring.datasource.username=postgres
   spring.datasource.password=1234
   ```
