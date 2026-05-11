# ![TrackHarbor Logo](./images/logo.png)

# TrackHarbor

TrackHarbor is a full-stack desktop application that helps users organize and manage their job and internship application process in one place. Users can track positions, manage notes, monitor application statuses, and generate AI-powered tips for specific opportunities.

The application is built with JavaFX for the desktop user interface and Firebase for authentication and cloud data storage.

---

## User Flow Diagram

![User Flow Diagram](./images/user-flow-diagram.png)

---

## Features

- User authentication with Firebase Authentication
- Secure account-based data management
- Track job and internship applications
- Spreadsheet-style application table
- CRUD operations for positions and notes
- Application statuses and date tracking
- AI-generated application tips
- Cloud-based Firestore database integration
- Modern JavaFX desktop interface

---

## Tech Stack

### Frontend
- JavaFX
- FXML
- CSS

### Backend / Cloud
- Firebase Authentication
- Firebase Firestore

### AI Integration
- Google Gemini API

### Build Tools
- Maven

### Language
- Java 21

---

## Dependencies

### Main Dependencies
- JavaFX
- Firebase Admin SDK
- Google Cloud Firestore
- Google Auth Library
- Jackson Databind
- dotenv-java

Example Maven dependencies:

```xml
<dependencies>

    <!-- JavaFX -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>21</version>
    </dependency>

    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>21</version>
    </dependency>

    <!-- Firebase -->
    <dependency>
        <groupId>com.google.firebase</groupId>
        <artifactId>firebase-admin</artifactId>
        <version>9.2.0</version>
    </dependency>

    <!-- Firestore -->
    <dependency>
        <groupId>com.google.cloud</groupId>
        <artifactId>google-cloud-firestore</artifactId>
        <version>3.15.7</version>
    </dependency>

    <!-- dotenv -->
    <dependency>
        <groupId>io.github.cdimascio</groupId>
        <artifactId>dotenv-java</artifactId>
        <version>3.0.0</version>
    </dependency>

</dependencies>
```

---

## Firebase Structure

```text
users/{userId}
│
├── firstName
├── lastName
├── email
├── createdAt
│
└── positions/{positionId}
    │
    ├── name
    ├── link
    ├── applied
    ├── dateApplied
    ├── status
    ├── aiTips
    ├── createdAt
    ├── updatedAt
    │
    └── notes/{noteId}
        │
        ├── content
        └── createdAt
```

---

## How to Start the Application

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/TrackHarbor.git
cd TrackHarbor
```

### 2. Configure Firebase

Create a Firebase project and enable:

- Firebase Authentication
- Cloud Firestore

Download your Firebase service account JSON file and place it inside:

```text
config/firebase-service-account.json
```

### 3. Configure Environment Variables

Create a `.env` file in the project root:

```env
GEMINI_API_KEY=your_api_key_here
```

### 4. Install Dependencies

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn javafx:run
```

---

## Application Screenshots

### Login Page

![Login Page](./images/login-page.png)

---

### Dashboard Page

![Dashboard Page](./images/dashboard-page.png)

---

### Table Page

![Table Page](./images/table-page.png)

---

### Notes Page

![Notes Page](./images/notes-page.png)

---

## API Design Diagram

![API Design Diagram](./images/api-design-diagram.png)

---

## Project Structure

```text
src/
│
├── main/
│   ├── java/
│   │   └── com.trackharbor.trackharbor
│   │       ├── controllers
│   │       ├── models
│   │       ├── services
│   │       ├── config
│   │       └── utils
│   │
│   └── resources/
│       ├── styles
│       ├── images
│       └── fxml
│
└── test/
```

---

## Future Improvements

- Resume upload and parsing
- Analytics dashboard
- Interview preparation tools
- Email notifications
- Cloud deployment support
- Multi-user collaboration
- Advanced AI career recommendations

---

## Contributors

- William Jijon
- TrackHarbor Development Team

---

## License

This project is licensed under the MIT License.
