# Skill Barter System — Setup Guide
## VS Code + AWS RDS MySQL

---

## STEP 1 — Create AWS RDS MySQL Instance

1. Go to https://aws.amazon.com → Sign in → Search **RDS**
2. Click **Create database**
3. Choose:
   - Engine: **MySQL**
   - Template: **Free tier**
   - DB instance identifier: `skillbarter-db`
   - Master username: `admin`
   - Master password: (set something you'll remember, e.g. `Barter@2024`)
4. Under **Connectivity**:
   - Public access: **Yes**
   - VPC security group: Create new → name it `skillbarter-sg`
5. Click **Create database** — wait ~5 minutes

---

## STEP 2 — Open Port 3306 in Security Group

1. In AWS Console → EC2 → Security Groups → find `skillbarter-sg`
2. Click **Inbound rules** → **Edit inbound rules**
3. Add rule:
   - Type: MySQL/Aurora
   - Port: 3306
   - Source: **My IP** (for dev) or `0.0.0.0/0` (open, less secure)
4. Save rules

---

## STEP 3 — Get Your RDS Endpoint

1. RDS → Databases → `skillbarter-db`
2. Copy the **Endpoint** — looks like:
   `skillbarter-db.xxxxxxxxx.us-east-1.rds.amazonaws.com`

---

## STEP 4 — Update Config Files

Update TWO files with your endpoint and password:

**File 1:** `src/main/resources/db.properties`
```
db.url=jdbc:mysql://YOUR-ENDPOINT.rds.amazonaws.com:3306/skillbarter_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=admin
db.password=Barter@2024
```

**File 2:** `src/main/resources/hibernate.cfg.xml`
```xml
<property name="hibernate.connection.url">
  jdbc:mysql://YOUR-ENDPOINT.rds.amazonaws.com:3306/skillbarter_db?useSSL=false&amp;allowPublicKeyRetrieval=true&amp;serverTimezone=UTC
</property>
<property name="hibernate.connection.username">admin</property>
<property name="hibernate.connection.password">Barter@2024</property>
```

---

## STEP 5 — Setup VS Code

Install these VS Code extensions:
- **Extension Pack for Java** (Microsoft) — required
- **Maven for Java** (Microsoft) — required
- **Spring Boot Extension Pack** — optional but helpful

---

## STEP 6 — Create the Database Schema

Connect to your RDS using MySQL Workbench or VS Code's
**MySQL** extension (by formulahendry), then run:

```
src/main/resources/schema.sql
```

---

## STEP 7 — Build and Run

Open terminal in VS Code (`Ctrl + ~`):

```bash
# Build the project
mvn clean install

# Run the application
mvn exec:java -Dexec.mainClass="com.skillbarter.ui.MainApp"

# OR run the JAR directly
java -jar target/SkillBarterSystem-1.0-SNAPSHOT.jar
```

---

## Project Structure

```
SkillBarterSystem/
├── pom.xml
└── src/main/java/com/skillbarter/
    ├── model/          → User, Skill, BarterRequest, Notification, BaseEntity
    ├── exception/      → Custom exceptions (concept 6)
    ├── dao/            → JDBC DAOs — UserDAO, SkillDAO, BarterRequestDAO (concept 11)
    ├── service/        → Spring @Service beans (concepts 4, 12)
    ├── thread/         → NotificationService background polling (concept 7)
    ├── config/         → Spring AppConfig (concept 12)
    ├── util/           → HibernateUtil, DBConnection, PasswordUtil
    └── ui/             → Swings panels (concepts 9, 10)
        ├── MainApp.java
        ├── LoginPanel.java
        ├── RegisterPanel.java
        └── DashboardPanel.java
```

---

## Concepts Coverage Checklist

| # | Concept                          | Where Used |
|---|----------------------------------|------------|
| 1 | Classes and Objects              | All model classes |
| 2 | Inheritance                      | BaseEntity ← User, Skill, BarterRequest |
| 3 | Polymorphism                     | getDisplayInfo(), getEntityType() overrides |
| 4 | Abstract Class + Interface       | BaseEntity (abstract), IUserService, ISkillService, IBarterService |
| 5 | Packages                         | com.skillbarter.{model,service,dao,ui,exception,...} |
| 6 | Exceptions                       | SkillBarterException hierarchy, custom throws |
| 7 | Multithreading                   | NotificationService (ScheduledExecutorService, SwingWorker) |
| 8 | Collections                      | List<Skill>, HashMap<String,Integer> in DAOs/Services |
| 9 | Swings                           | JFrame, JPanel, JTable, JTabbedPane, CardLayout |
| 10| Event Handling                   | ActionListener, KeyListener, SwingWorker |
| 11| JDBC                             | UserDAO, SkillDAO, BarterRequestDAO → AWS RDS |
| 12| Spring + Hibernate               | @Service, @Autowired, AppConfig, HibernateUtil |
