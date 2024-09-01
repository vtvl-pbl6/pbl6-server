## How to run the application:

### Prerequisites

- Docker (for Docker Compose)
- Java Development Kit (JDK) 21 (for Maven)
- Maven 3.6.1 or higher (for Maven build)
- PostgreSQL database (can be run with Docker)
- Redis (can be run with Docker)

### 1. Clone the repository:

```bash
git clone https://github.com/vtvl-pbl6/pbl6-server.git
```

### 2. Change directory to project:

```bash
cd pbl6-server
```

### 3. Run using Docker Compose (recommended for development):

- Configure `.env`:
    - Create a `.env` file in the root directory of the project with examples in `.env.example` file.
    - Add the environment variables from docker-compose.yml file to the `.env` file
- Run the application:
  ```bash
  docker-compose up -d
  ```

### 4. Run using Maven:

- Configure `application-dev.yml`:
    - Copy `application.yml` to `application-dev.yml`
    - Fill in the missing values in `application-dev.yml`
- Run the application:
  ```bash
  mvn spring-boot:run -Dspring-boot.run.profiles=dev
  ```

### 5. Access the API:

- Once the application is running, the API is available at `http://localhost:8080/api`.