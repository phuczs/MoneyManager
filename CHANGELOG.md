# Changelog

All notable changes to the MoneyManager project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Planned
- Data export functionality
- Offline support improvements
- Enhanced data visualization with charts

## [1.0.0] - 2025-01-04

### Added
- **Authentication System**
  - Firebase authentication with email/password
  - User registration and login
  - Password reset functionality
  - Secure user session management

- **Transaction Management**
  - Add income and expense transactions
  - Edit and delete transactions
  - Real-time balance calculation
  - Transaction history with filtering

- **Category System**
  - Default expense categories (Food, Transportation, etc.)
  - Default income categories (Salary, Freelance, etc.)
  - Custom category creation
  - Category-based transaction filtering

- **Dashboard**
  - Real-time balance display
  - Income vs expense summary
  - Recent transactions list
  - Quick access to add new transactions

- **Modern UI**
  - Jetpack Compose declarative UI
  - Material Design 3 theming
  - Dark/light theme support
  - Responsive design for different screen sizes

- **Architecture**
  - Clean architecture with MVVM pattern
  - Hilt dependency injection
  - Repository pattern for data access
  - Firebase Firestore for cloud storage

### Technical Details
- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 36)
- **Language**: Kotlin
- **Build Tool**: Gradle with Kotlin DSL
- **Backend**: Firebase (Auth + Firestore)

### Known Issues
- Limited offline functionality
- Requires internet connection for full features
- DateTimeFormatter compatibility issue resolved

### Fixed
- Balance calculation now uses all transactions (not just recent 5)
- Firestore composite index requirements resolved
- Android API compatibility issues fixed
- Authentication error handling improved

---

## Version Guidelines

### Version Numbers
- **Major (X.0.0)**: Breaking changes, major new features
- **Minor (1.X.0)**: New features, backwards compatible
- **Patch (1.0.X)**: Bug fixes, small improvements

### Change Categories
- **Added**: New features
- **Changed**: Changes in existing functionality
- **Deprecated**: Soon-to-be removed features
- **Removed**: Removed features
- **Fixed**: Bug fixes
- **Security**: Security vulnerabilities fixes