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
 
### **2 Setup and Run the Database**
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

### **3 Run the Application**
Use Maven to build and start the project:
  ```bash
  mvn clean package
  mvn spring-boot:run
```
Your API will be available at: **`http://localhost:8081`**

---

## Running with Docker
### **1 Prerequisites**
Ensure you have installed:
  - [Docker](https://www.docker.com/)
  - [Docker Compose](https://docs.docker.com/compose/install/)

### **2 Build and Run with Docker**
  ```bash
  docker-compose up --build
  ```
This starts **PostgreSQL** and **Swift API** in containers. The API will be available at **`http://localhost:8080`**.

### **3 Stopping the Containers**
To stop the running containers, execute:
  ```bash
  docker-compose down
  ```

---

## API Endpoints
### **Retrieve SWIFT Code Details**
  ```http
  GET /v1/swift-codes/{swiftCode}
  ```

### **Retrieve All SWIFT Codes for a Country**
  ```http
  GET /v1/swift-codes/country/{countryISO2code}
  ```

### **Add a New SWIFT Code** (Using Terminal)
  ```bash
  curl -X POST http://localhost:8081/v1/swift-codes \
       -H "Content-Type: application/json" \
       -d '{
             "swiftCode": "TEST1234",
             "bankName": "Test Bank",
             "countryISO2": "US",
             "countryName": "United States",
             "address": "123 Test St"
           }'
  ```
### **Delete a SWIFT Code**
  ```http
  DELETE /v1/swift-codes/{swiftCode}
  ```

### **Import SWIFT Codes from a File**
  ```bash
  curl -X POST http://localhost:8081/v1/swift-codes/import \
       -F "file=@path/to/your/swift_codes.xlsx"
  ```
Replace `path/to/your/swift_codes.xlsx` with the actual file path.

---
## Testing the API
After starting the application, you can test endpoints using:
  - **Postman**
  - **cURL**
  - Directly from the browser (`GET` requests only)

Example test using **cURL**:
  ```bash
  curl -X GET http://localhost:8081/v1/swift-codes/BCCSCLR1XXX
  ```

## Troubleshooting
### **Database Issues**
- Ensure PostgreSQL is running:
  ```bash
  docker ps
  ```

### **Port Conflicts**
- If **port 8080 is already in use**, modify the `server.port` in `application.properties`.



