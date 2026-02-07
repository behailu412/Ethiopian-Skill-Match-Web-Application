

---

# Ethiopian Skill Match – Web Application

Ethiopian Skill Match is a **Java-based web application** designed to connect **service seekers** with **skilled service providers** across Ethiopia.
The platform enables user registration, service listing, booking management, and role-based dashboards using a clean MVC architecture.

---

## 📌 Project Overview

The system bridges the gap between people who need services (plumbing, electrical work, maintenance, etc.) and verified service providers by offering:

* Secure authentication
* Role-based access (Admin, Provider, Seeker)
* Service booking and management
* Simple, responsive user interface

---

## 🏗️ System Architecture

* **Architecture Pattern:** MVC (Model–View–Controller)
* **Backend:** Java Servlets
* **Frontend:** HTML, CSS, JavaScript
* **Database:** MySQL
* **Server:** Apache Tomcat
* **Project Type:** Dynamic Web Project (Eclipse)

---

## 🛠️ Technologies Used

### Backend

* Java (Servlets)
* Jakarta Servlet API 6.0
* JDBC
* Gson (JSON handling)

### Frontend

* HTML5
* CSS3
* JavaScript

### Database

* MySQL
* MySQL Connector/J 8.0

### Tools & Environment

* Eclipse IDE
* Apache Tomcat
* Git & GitHub

---

## 👥 User Roles & Features

### 🔑 Authentication

* User registration and login
* Session-based authentication
* Role-based access control

### 🧑‍🔧 Service Provider

* Register as a provider
* Add and manage services
* View booking requests
* Provider dashboard

### 🧑‍💼 Service Seeker

* Register and login
* Browse available services
* Book services
* Seeker dashboard

### 🛠️ Admin

* Admin dashboard
* Manage users and services
* System monitoring

---

## 📁 Project Structure

```
EthiopianSkillMatch_Web/
│
├── src/
│   └── ethioskill/
│       ├── controllers/     # Servlet controllers
│       ├── models/          # Java model classes
│       └── utils/           # Utility & DB connection classes
│
├── webapp/
│   ├── pages/
│   │   ├── login.html
│   │   ├── signup.html
│   │   ├── admin-dashboard.html
│   │   ├── provider-dashboard.html
│   │   ├── seeker-dashboard.html
│   │   └── booking-form.html
│   │
│   ├── assets/              # CSS, JS, images
│   └── WEB-INF/
│       ├── lib/             # JAR dependencies
│       └── web.xml          # Servlet configuration
│
└── build/
```

---

## 🗄️ Database Configuration

1. Create a MySQL database:

   ```sql
   CREATE DATABASE ethio_skill_match;
   ```

2. Update database credentials in the DB utility class:

   ```java
   String url = "jdbc:mysql://localhost:3306/ethio_skill_match";
   String user = "root";
   String password = "your_password";
   ```

3. Import required tables (users, services, bookings, etc.)

---

## 🚀 How to Run the Project

1. Clone the repository:

   ```bash
   git clone https://github.com/your-username/EthiopianSkillMatch_Web.git
   ```

2. Open the project in **Eclipse IDE**

3. Add **Apache Tomcat** server

4. Configure MySQL database

5. Run the project on the server

6. Open in browser:

   ```
   http://localhost:8080/EthiopianSkillMatch_Web/
   ```

---

## 🔐 Security Features

* Password-based authentication
* Session management
* Server-side validation
* JDBC prepared statements (SQL injection prevention)

---

## 📈 Future Improvements

* OTP-based phone authentication
* Rating & review system
* Mobile app integration
* Payment gateway integration
* REST API version

---

## 🤝 Contribution

Contributions are welcome!

1. Fork the repository
2. Create a new feature branch
3. Commit your changes
4. Open a Pull Request

---

## 📄 License

This project is developed for **educational and academic purposes**.
You are free to modify and extend it.

---

## 🙌 Author

**Behailu Yifru**
Computer Science Student
## support
For support, email yifrubehailu412@gmail.com or call 0944794893.

