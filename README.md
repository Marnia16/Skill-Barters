<div align="center">

<img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"/>
<img src="https://img.shields.io/badge/Spring-5.3.30-6DB33F?style=for-the-badge&logo=spring&logoColor=white"/>
<img src="https://img.shields.io/badge/Hibernate-5.6.15-59666C?style=for-the-badge&logo=hibernate&logoColor=white"/>
<img src="https://img.shields.io/badge/MySQL-AWS%20RDS-4479A1?style=for-the-badge&logo=mysql&logoColor=white"/>
<img src="https://img.shields.io/badge/Maven-3.x-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white"/>

<br/><br/>

# ✦ Skill Barters

### *Skill In · Value Out*

**A cross-platform desktop application for bartering skills between users — learn anything, teach everything.**

[Features](#-features) · [Architecture](#-architecture) · [Setup](#-setup) · [Usage](#-usage) · [Tech Stack](#-tech-stack) · [Concepts Covered](#-java-concepts-covered)

</div>

---

## 📖 Overview

**Skill Barters** is a Java Swing desktop application that enables users to exchange skills with each other. Instead of money, users trade their expertise — you teach someone coding, they teach you music. The platform handles discovery, secure authentication, real-time notifications, and live video sessions via Jitsi Meet, all backed by a cloud MySQL database on AWS RDS.

Built as an academic project covering 12 core Java enterprise concepts including OOP, Spring, Hibernate, JDBC, Multithreading, Collections, Swings, and more.

---

## ✨ Features

| Feature | Description |
|---|---|
| 🎬 **Cinematic Splash Screen** | Animated gold particle system with brand intro before login |
| 🔐 **OTP Email Verification** | New accounts verified via 6-digit OTP sent to Gmail |
| 🔑 **Forgot Password Flow** | Email OTP → reset password, 2-minute expiry |
| 🔍 **Smart Skill Search** | Live autocomplete suggestions as you type, category pill filters |
| 🧙 **Harry Potter Style UI** | Dark espresso coffee theme with glowing gold accents throughout |
| 🤝 **Mutual Connect System** | Jitsi Meet link generated only when BOTH users click Connect |
| 📹 **Cross-Device Jitsi Meet** | Meeting links stored in AWS RDS — works across any two laptops |
| 🔔 **Live Notifications** | Background polling thread, click bell to view styled popup |
| 👤 **Rich User Profiles** | Name (editable), gender (15 options), bio, 1st & 2nd teaching language |
| 🏷️ **Skill Categories** | 15 predefined + custom user-created categories |
| 📥 **Barter Requests** | Send, receive, accept, reject with real user names shown |
| ✉️ **Complaint System** | Sends formatted HTML email directly to admin via Gmail SMTP |
| 🌐 **AWS RDS Backend** | MySQL on AWS Free Tier — shared database across all devices |
| 📦 **Spring + Hibernate ORM** | Full dependency injection and object-relational mapping |

---

## 🏗️ Architecture

```
SkillBarterSystem/
├── src/main/java/com/skillbarter/
│   ├── model/              # Entities — User, Skill, BarterRequest, Notification
│   ├── exception/          # Custom exception hierarchy
│   ├── dao/                # JDBC DAOs — direct AWS RDS queries
│   │   └── ConnectRequestDAO.java   # Cross-device connect tracking
│   ├── service/            # Spring @Service business logic
│   │   ├── EmailOtpService.java     # Gmail SMTP OTP sender
│   │   └── NotificationService.java # Background polling thread
│   ├── config/             # Spring AppConfig
│   ├── util/               # HibernateUtil, DBConnection, PasswordUtil
│   └── ui/                 # Java Swing panels
│       ├── SplashScreen.java        # Cinematic animated intro
│       ├── MainApp.java             # JFrame + CardLayout navigation
│       ├── LoginPanel.java          # Login + Forgot Password OTP
│       ├── RegisterPanel.java       # Register + Email OTP verification
│       └── DashboardPanel.java      # All 5 tabs: Skills, Search, Requests, Profile, Complaints
└── src/main/resources/
    ├── db.properties        # AWS RDS connection URL
    ├── hibernate.cfg.xml    # Hibernate entity mapping
    └── schema.sql           # Database DDL
```

### Application Flow

```
Launch → SplashScreen (5s animated) → LoginPanel
       ↓ (new user)                    ↓ (existing user)
  RegisterPanel                   Email + Password
       ↓                               ↓
  OTP Email sent              → DashboardPanel
       ↓                        ├── My Skills
  Verify 6-digit OTP           ├── Search (autocomplete + Jitsi)
       ↓                        ├── Barter Requests
  Account created              ├── Profile
       ↓                        └── Complaints → Admin Email
  → LoginPanel
```

---

## 🗄️ Database Schema

```sql
users              -- registered users with profile info
skills             -- skills posted by users
barter_requests    -- skill exchange proposals between users
notifications      -- real-time alerts stored per user
connect_requests   -- cross-device mutual connect state for Jitsi
```

> All tables on **AWS RDS MySQL** (Free Tier) — shared across all devices.

---

## 🛠️ Setup

### Prerequisites

| Tool | Version | Download |
|---|---|---|
| Java JDK | 17 | [adoptium.net](https://adoptium.net) |
| Maven | 3.x | [maven.apache.org](https://maven.apache.org) |
| VS Code | Latest | [code.visualstudio.com](https://code.visualstudio.com) |
| Extension Pack for Java | Latest | VS Code Marketplace |
| AWS Account | Free Tier | [aws.amazon.com](https://aws.amazon.com) |

### Step 1 — Create AWS RDS MySQL Instance

1. AWS Console → RDS → **Create database**
2. Engine: **MySQL** · Template: **Free Tier**
3. DB identifier: `skillbarter-db` · Username: `admin`
4. Connectivity → Public access: **Yes**
5. Create a security group with inbound rule: **MySQL/Aurora port 3306 → Your IP**
6. Copy your **RDS Endpoint** from the database details page

### Step 2 — Configure Connection

Update **two files** with your RDS endpoint and password:

**`src/main/resources/db.properties`**
```properties
db.url=jdbc:mysql://YOUR-ENDPOINT.rds.amazonaws.com:3306/skillbarter_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=admin
db.password=YourPassword
db.driver=com.mysql.cj.jdbc.Driver
```

**`src/main/resources/hibernate.cfg.xml`**
```xml
<property name="hibernate.connection.url">
  jdbc:mysql://YOUR-ENDPOINT.rds.amazonaws.com:3306/skillbarter_db?useSSL=false&amp;...
</property>
<property name="hibernate.connection.username">admin</property>
<property name="hibernate.connection.password">YourPassword</property>
```

### Step 3 — Configure Gmail OTP (App Password)

1. Google Account → Security → **2-Step Verification → App Passwords**
2. Create a new App Password named `SkillBarters`
3. Open `EmailOtpService.java` and set:

```java
private static final String SENDER_EMAIL    = "your@gmail.com";
private static final String SENDER_PASSWORD = "xxxx xxxx xxxx xxxx"; // App Password only
```

> ⚠️ **Never commit real credentials to GitHub.** Use environment variables or a `.env` file in production.

### Step 4 — Create Database Tables

Connect to your RDS via MySQL Workbench and run:

```bash
# In MySQL Workbench → connect to your RDS → run:
src/main/resources/schema.sql
```

### Step 5 — Build and Run

```bash
# In VS Code terminal (Ctrl + `)
mvn clean install

# Run the app
mvn exec:java -Dexec.mainClass="com.skillbarter.ui.MainApp"
```

---

## 🚀 Usage

### First Time (New User)
1. Click **Create Account** on the login screen
2. Fill name, email, password → click **Send OTP to Email**
3. Check your inbox → enter the 6-digit code
4. Account created → go to **Login**
5. Fill your **Profile** — gender, bio, teaching languages

### Finding & Connecting
1. Go to **Search** tab
2. Type a skill name — autocomplete suggests as you type
3. Filter by category pill (Coding, Music, Art, etc.)
4. Click **CONNECT ✦** on a skill you want to learn
5. When the other user also clicks Connect → **Jitsi meeting link appears automatically** on both devices

### Barter Requests
1. Go to **Requests** tab
2. Click **Refresh** to load latest
3. Select an incoming request → click **Accept** or **Reject**

### Notifications
- Click the **🔔 bell** in the top bar anytime to view unread notifications
- Click **Mark all as read** to clear

---

## 💻 Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java 17 |
| **UI Framework** | Java Swing (custom dark espresso theme) |
| **Backend Framework** | Spring 5.3.30 (IoC, DI, Transaction Management) |
| **ORM** | Hibernate 5.6.15 |
| **Direct DB Access** | JDBC with PreparedStatement |
| **Database** | MySQL 8 on AWS RDS (Free Tier) |
| **Connection Pool** | HikariCP 5.0.1 |
| **Email** | JavaMail (javax.mail) via Gmail SMTP |
| **Video Calling** | Jitsi Meet (auto-generated links) |
| **Build Tool** | Apache Maven |
| **Security** | SHA-256 password hashing, Gmail App Passwords |

---

## 📚 Java Concepts Covered

| # | Concept | Where Used |
|---|---|---|
| 1 | **Classes and Objects** | All model classes — User, Skill, BarterRequest |
| 2 | **Inheritance** | `BaseEntity` ← User, Skill, BarterRequest, Notification |
| 3 | **Polymorphism** | `getDisplayInfo()`, `getEntityType()` overridden in each subclass |
| 4 | **Abstract Class + Interface** | `BaseEntity` (abstract), `IUserService`, `ISkillService`, `IBarterService` |
| 5 | **Packages** | `com.skillbarter.{model,service,dao,ui,exception,config,util}` |
| 6 | **Exceptions** | `SkillBarterException` hierarchy, `UserNotFoundException`, `DatabaseException` |
| 7 | **Multithreading** | `NotificationService` (ScheduledExecutorService), `SwingWorker`, Jitsi polling timer |
| 8 | **Collections** | `List<Skill>`, `HashMap<String,Integer>`, `ArrayList` throughout services |
| 9 | **Swings** | `JFrame`, `JPanel`, `JTable`, `JTabbedPane`, `JPopupMenu`, `CardLayout` |
| 10 | **Event Handling** | `ActionListener`, `MouseListener`, `KeyAdapter`, `DocumentListener` |
| 11 | **JDBC** | `UserDAO`, `SkillDAO`, `BarterRequestDAO`, `ConnectRequestDAO` → AWS RDS |
| 12 | **Spring + Hibernate** | `@Service`, `@Autowired`, `AppConfig`, `HibernateUtil`, `SessionFactory` |

---

## 📁 Key Files Reference

| File | Purpose |
|---|---|
| `SplashScreen.java` | Animated intro — particles, rings, spring-in emblem |
| `LoginPanel.java` | Login + 3-step forgot password OTP flow |
| `RegisterPanel.java` | Register + email OTP verification + success screen |
| `DashboardPanel.java` | Main app — 5 tabs, search autocomplete, Jitsi connect |
| `EmailOtpService.java` | OTP generation, Gmail SMTP, verify, expire |
| `ConnectRequestDAO.java` | DB-backed mutual connect for cross-device Jitsi |
| `NotificationService.java` | Background thread polling for notifications |
| `schema.sql` | Full DB DDL — all 5 tables |
| `db.properties` | AWS RDS connection config |
| `hibernate.cfg.xml` | Hibernate entity mapping + RDS config |

---

## 🔒 Security Notes

- Passwords hashed with **SHA-256** before storage — never stored as plain text
- Email verified via **OTP** before account creation — no fake accounts
- Gmail **App Password** used for SMTP — not your actual Gmail password
- OTPs are **single-use** and expire after **2 minutes**
- AWS RDS Security Group restricts port 3306 to known IPs

---

## 🤝 Team

Built as a group academic project demonstrating Java enterprise concepts end-to-end.

---

## 📄 License

This project was developed for educational purposes as part of a Java programming course.

---

<div align="center">

Made with ☕ and lots of dark espresso theme inspiration

**Skill Barters** · *Skill In · Value Out*

</div>
