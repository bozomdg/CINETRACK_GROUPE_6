# CinéTrack

## Description
CinéTrack is a Java Swing desktop application for managing a small cinema. It provides a graphical interface to:
- Authenticate users (admin or gestionnaire).
- Manage movies: add, edit, delete, filter by genre.
- Manage screenings (séances): schedule, edit, delete, view availability.
- Manage cinema staff accounts (gestionnaires) when logged in as an admin.

The UI follows a modern look using the `ModernUI` helper class with custom colors, rounded panels, and styled tables.

## Features
- **Login** – Users can log in as `admin` or `gestionnaire`.
- **Movie Management** – CRUD operations with validation (duration, rating). Movies are displayed in a table with formatted duration.
- **Screening Management** – Schedule screenings, avoid double‑booking of a room (throws `SalleErreur`).
- **Account Management** – Admins can add and remove gestionnaires.
- **Responsive UI** – Colour‑coded navigation, status bar, and dynamic home‑page cards.

## Prerequisites
- **Java Development Kit** (JDK 11 or newer). The project uses only standard Java SE classes and Swing.
- **PostgreSQL** (any recent version). The application expects a database named `cinetrack`.
- **PostgreSQL JDBC driver** – the JAR `postgresql-42.7.10.jar` is already included in the `lib/` folder.

## Database Setup
1. Install PostgreSQL and create a database named `cinetrack`:
   ```sql
   CREATE DATABASE cinetrack;
   ```
2. Run the SQL script `src/sql/create_database.sql` to create the required tables and insert test data. From a terminal:
   ```bash
   psql -U postgres -d cinetrack -f src/sql/create_database.sql
   ```
   The script creates two user tables (`admins`, `gestionnaires`) and sample movies and rooms.
3. The default credentials (as inserted by the script) are:
   - **Admin** – username: `admin`, password: `admin123`
   - **Gestionnaire** – username: `gestion`, password: `gestion123`

> **NOTE**: Passwords are stored in plain text for demonstration purposes. In a production scenario you should store hashed passwords (e.g., BCrypt).

## Configuration
The connection parameters are hard‑coded in `src/connexion/DatabaseConnection.java`:
```java
private static final String URL = "jdbc:postgresql://localhost:5432/cinetrack";
private static final String USERNAME = "postgres";
private static final String PASSWORD = "root";
```
If your PostgreSQL instance uses different credentials, edit this file accordingly and recompile.

## Build & Run
You can compile and run the application with any IDE (Eclipse/IntelliJ) or from the command line.
### Using the command line (javac & java)
```bash
# Compile all sources (adjust classpath to include the JDBC driver)
javac -cp "lib/postgresql-42.7.10.jar" -d bin $(find src -name "*.java")
# Run the main class
java -cp "bin;lib/postgresql-42.7.10.jar" main.Main
```
### Using an IDE
1. Open the project folder (`cineTrack`).
2. Add the PostgreSQL JAR (`lib/postgresql-42.7.10.jar`) to the project libraries.
3. Set the source folder to `src` and the output folder to `bin` (or use the IDE defaults).
4. Run `main.Main`.

## Usage Walk‑through
1. **Launch** – The login window (`FenetreConnexion`) appears.
2. **Log in** – Enter admin or gestionnaire credentials.
3. **Main window** – The sidebar provides navigation to:
   - **Films** – Manage movies.
   - **Séances** – Manage screenings.
   - **Gestionnaires** – (admin only) manage staff accounts.
4. **Home page** – Shows cards linking to the above sections; click a card to navigate.
5. **Status bar** – Shows the current view status and app version.

## Project Structure
```
├─ src
│  ├─ connexion/            # Database connection helper
│  ├─ controlleur/          # MVC controllers
│  ├─ modele/               # POJOs and DAOs
│  ├─ vue/                  # Swing UI components
│  └─ exception/            # Custom runtime exceptions
├─ bin                      # Compiled classes (generated)
├─ lib                      # PostgreSQL JDBC driver
├─ src/sql/create_database.sql  # Database schema & seed data
└─ README.md                # This file
```

## Extending the Application
- **Password security** – Replace plain‑text storage with a hashing library.
- **External configuration** – Move DB credentials to a properties file or environment variables.
- **Packaging** – Create a runnable JAR with the dependencies bundled (e.g., using Maven Shade plugin).
- **Testing** – Add JUnit tests for DAOs and controllers.

## License
This project is provided for educational purposes. Feel free to adapt and improve it.
