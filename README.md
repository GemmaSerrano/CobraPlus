<p align="center">
  <img src="https://github.com/user-attachments/assets/131cffae-fcb9-4638-8a5c-44b997936a29" width="120"/>
</p>

# CobraPlus

Android application developed for managing extra salary concepts, work activity records and report generation.

## Overview

CobraPlus is a native Android application created as a real-world solution for tracking extraordinary work activities and automatically calculating additional salary income.

The application replaces manual Excel or paper-based tracking systems with a cloud-connected mobile solution.

## Features

### Administrator Features
- Base concept management
- Base report management
- Firebase database supervision

<details>
<summary><b>🎬 View Admin Demo Video</b></summary>
<br>
<p align="center">
<video src="https://github.com/user-attachments/assets/24fc7a15-65eb-43b7-9449-ece1a816f9a6" width="100%" controls></video>
</p>
</details>

### User Features
- Personal salary concept management
- Daily activity registration
- Automatic report generation
- PDF and Excel export
- Secure personal data access

<details>
<summary><b>🎬 View User Demo Video</b></summary>
<br>
<p align="center">
<video src="https://github.com/user-attachments/assets/a3d8434d-81ae-4cba-b77c-edfce7f529b4" width="100%" controls></video>
</p>
</details>



## Security

CobraPlus uses Firebase Authentication and Cloud Firestore security rules to ensure that each user can only access their own records and reports.

```javascript
allow read, update, delete: if signedIn() &&
    resource.data.userId == request.auth.uid;
```

## Technologies

- Java
- Android Studio
- Firebase Authentication
- Cloud Firestore
- Apache POI
- PDF generation libraries

## Architecture

- Native Android application
- Cloud-based NoSQL database
- Role-based access system
- Firebase backend integration

## Future Improvements

- Firebase Storage integration
- Tablet layout optimization
- Push notifications
- Multiplatform adaptation

## Author

Gemma Serrano
