# 📚 NilaiKuApp

### Android-Based Student Grade Management System

**NilaiKuApp** is an Android application designed to help teachers manage and record student grades, including daily assignments and examination scores.

The application provides a centralized system for recording academic scores and helps organize student assessment data digitally.

---

## 📌 Overview

Managing student grades manually can make it difficult to organize, update, and monitor academic assessment data.

**NilaiKuApp** was developed to provide a digital solution for managing student grades through an Android application.

The system provides different functionality for teachers and students, allowing academic assessment information to be managed and accessed more efficiently.

This project was developed as part of a **competency certification project (UJIKOM LSP)** in the Information Systems program.

---

## 🎯 Objectives

The main objectives of NilaiKuApp are:

* Digitize the process of recording student grades.
* Help teachers manage daily and examination scores.
* Provide students with access to their academic assessment information.
* Organize academic data in a structured system.
* Reduce the need for manual grade recording.

---

## 👥 User Roles

The application provides functionality based on user roles.

### 👨‍🏫 Teacher

Teachers can:

* Input student grades.
* Record daily assignment scores.
* Record examination scores.
* Manage student assessment data.
* View academic results.

### 👨‍🎓 Student

Students can:

* View their academic grades.
* Monitor recorded assessment results.
* Access their academic information through the application.

---

## ✨ Features

### 📝 Grade Management

Teachers can record and manage student assessment scores through the application.

### 📊 Daily & Examination Scores

The system supports different types of assessments, including:

* Daily assignments
* Examination scores

### 👨‍🏫 Teacher Dashboard

Provides teachers with access to student and grade management functionality.

### 👨‍🎓 Student Dashboard

Provides students with access to their recorded academic results.

### 🧮 Grade Calculation

The application processes the entered assessment scores to determine the resulting student grade according to the implemented calculation rules.

---

## 🔄 Application Flow

The general application flow can be illustrated as follows:

```text id="8z9x1c"
              ┌───────────────┐
              │     Login     │
              └───────┬───────┘
                      │
             ┌────────┴────────┐
             ▼                 ▼
      ┌─────────────┐   ┌─────────────┐
      │   Teacher   │   │   Student   │
      └──────┬──────┘   └──────┬──────┘
             │                 │
             ▼                 ▼
      ┌─────────────┐   ┌─────────────┐
      │ Input Grade │   │ View Grades │
      └──────┬──────┘   └──────┬──────┘
             │                 │
             ▼                 │
      ┌─────────────┐           │
      │ Grade Data  │◄──────────┘
      └─────────────┘
```

---

## 🛠️ Technology Stack

| Technology         | Usage                        |
| ------------------ | ---------------------------- |
| **Kotlin**         | Application development      |
| **Android**        | Mobile application platform  |
| **Database**       | Store student and grade data |
| **Android Studio** | Development environment      |
| **Git & GitHub**   | Version control              |

> The database technology listed above can be updated once the exact database implementation used in the project is confirmed.

---

## 📂 Project Structure

```text id="c2s2jy"
NilaiKuApp/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       ├── res/
│   │       └── AndroidManifest.xml
│   │
│   └── build.gradle
│
├── screenshots/
│   ├── login.png
│   ├── teacher-dashboard.png
│   ├── input-grade.png
│   ├── grade-list.png
│   ├── student-dashboard.png
│   └── student-grade.png
│
├── gradle/
├── build.gradle
├── settings.gradle
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

Make sure the following tools are installed:

* Android Studio
* Android SDK
* JDK
* Android device or emulator

### Installation

Clone the repository:

```bash id="7o0p1a"
git clone https://github.com/Razor914/NilaiKuApp.git
```

Open the project using **Android Studio**.

Allow Gradle to synchronize the project dependencies.

Then run the application on an Android emulator or physical Android device.

---

## 🎓 Academic Project

**Project:** NilaiKuApp — Student Grade Management System
**Program:** Information Systems
**Project Type:** UJIKOM LSP
**Platform:** Android
**Programming Language:** Kotlin

This project was developed as part of the competency certification project (UJIKOM LSP).

---

## 👨‍💻 Author

**Rafif Musyaffa Septiandra Tri Leksono**

Information Systems Graduate

GitHub: [@Razor914](https://github.com/Razor914)

---

⭐ Feel free to explore the repository and learn more about the project.
