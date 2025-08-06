
# 📝 JournalSS App (Backend)

![Java](https://img.shields.io/badge/Java-17-blue?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.12-brightgreen?logo=springboot)
![MongoDB](https://img.shields.io/badge/MongoDB-Database-success?logo=mongodb)
![Docker](https://img.shields.io/badge/Dockerized-%E2%9C%94-blue?logo=docker)
![Made with Maven](https://img.shields.io/badge/Build-Maven-FF5733?logo=apachemaven)
![Swagger Docs](https://img.shields.io/badge/API-Swagger_UI-yellow?logo=swagger)

> 🚧 Deployed at:  
> 🔗 **[https://journalss-backend.onrender.com/journal/swagger-ui/index.html](https://journalss-backend.onrender.com/journal/swagger-ui/index.html)**
---

## 📖 About

This is the backend for the **JournalSS App** – a secure and private journaling platform built with **Spring Boot 3.3.12**.  
It provides REST APIs for user registration, login, journal entry management, and admin controls.

---

## 🚀 Features

- 🔐 JWT-based user authentication
- ✍️ CRUD operations for journal entries
- 📈 Integrated Sentiment Analysis API
- 👩‍💼 Admin dashboard for managing users
- 📄 Interactive Swagger UI for API testing
- 🛢️ MongoDB for fast, scalable storage
- 🐳 Docker-ready for easy deployment
- ⏰ Daily journal reminder emails (via scheduler)

---

## 🧱 Tech Stack

| Category      | Tech Used                           |
|---------------|-------------------------------------|
| Language      | Java 17                             |
| Framework     | Spring Boot 3.3.12, Spring Security |
| Database      | MongoDB                             |
| Build Tool    | Maven                               |
| Docs & Test   | Swagger (SpringDoc OpenAPI)         |
| Deployment    | Docker, Render                      |

---

## 🧪 Getting Started

### Clone the repository

```bash
git clone https://github.com/aastha-sin-09/JournalApp-SpringBoot.git
cd JournalApp-SpringBoot
```

### Set up environment variables

Create a `.env` file in the root directory with the following values:

```env
MONGO_URI=mongodb+srv://yourusername:yourpassword@cluster.mongodb.net/dbname
JWT_SECRET=your_jwt_secret
MAIL_USERNAME=youremail@example.com
MAIL_PASSWORD=your_app_password
SENTIMENT_API_KEY=your_sentiment_api_key
```

---

## ⚙️ Running the App

### 1. Using Maven

```bash
./mvnw spring-boot:run
```

Or, package and run:

```bash
./mvnw clean package
java -jar target/journalApp-0.0.1-SNAPSHOT.jar
```

### 2. Using Docker

```bash
docker build -t journal-app .
docker run -p 8081:8081 --env-file .env journal-app
```

---

## 📄 API Documentation (Swagger)

🔗 **[Swagger UI – Test All Endpoints](https://journalss-backend.onrender.com/journal/swagger-ui/index.html)**

Example endpoints:
- `POST /auth/signup` – Create a new user
- `POST /auth/login` – Get JWT token
- `GET /user/entries` – Get your journal entries (Requires JWT)
- `POST /journal/entry` – Create journal with sentiment analysis

You can interact with public and secured routes directly from the Swagger interface.

---

## 🧱 Project Structure

```
journalApp/
├── .env
├── .gitignore
├── Dockerfile
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/aastha/journalApp/
│   │   │   ├── JournalApplication.java
│   │   │   ├── config/
│   │   │   │   ├── GlobalCorsConfig.java
│   │   │   │   ├── JwtAuthFilter.java
│   │   │   │   ├── SpringSecurity.java
│   │   │   │   └── SwaggerConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── JournalEntryController.java
│   │   │   │   └── UserEntryController.java
│   │   │   ├── dto/
│   │   │   │   ├── SentimentResponse.java
│   │   │   │   ├── UserLogin.java
│   │   │   │   └── UserSignUp.java
│   │   │   ├── entity/
│   │   │   │   ├── JournalEntry.java
│   │   │   │   └── User.java
│   │   │   ├── repository/
│   │   │   │   ├── JournalEntryRepository.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── UserRepositoryCustom.java
│   │   │   │   └── UserRepositoryImpl.java
│   │   │   ├── Scheduler/
│   │   │   │   └── EmailScheduler.java
│   │   │   ├── service/
│   │   │   │   ├── EmailService.java
│   │   │   │   ├── JournalEntryService.java
│   │   │   │   ├── SentimentService.java
│   │   │   │   ├── UserDetailsServiceImpl.java
│   │   │   │   └── UserService.java
│   │   │   └── util/
│   │   │       └── JwtUtil.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
```

---

## 🌐 Deployment

This backend is deployed on **Render** using Docker.

🔗 **Live Swagger Docs:**  
[https://journalss-backend.onrender.com/journal/swagger-ui/index.html](https://journalss-backend.onrender.com/journal/swagger-ui/index.html)

To deploy your own version:
- Connect this repo to Render
- Use **Docker environment**
- Add required **environment variables**
- Render automatically builds and deploys from the Dockerfile
---

## 🤝 Contributors

- Aastha Singh — [@aastha-sin-09](https://github.com/aastha-sin-09)

---

## 🧩 Want to Contribute?

Contributions are welcome! Follow these steps:

1. Fork the repository
2. Create a new branch:  
   ```bash
   git checkout -b feature-name
   ```
3. Make your changes and commit:
   ```bash
   git commit -m "Add feature"
   ```
4. Push your branch:
   ```bash
   git push origin feature-name
   ```
5. Open a Pull Request on GitHub


---

## 📜 License

This project is private and intended for educational/personal use.
