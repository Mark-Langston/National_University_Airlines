# 📘 National University Airlines Scheduler
*A Java Swing application for managing flight seat reservations.*

---

## ✈️ Project Overview
**National University Airlines** is a lightweight desktop tool written in Java that allows airline schedulers to view flights, manage seat availability, and record passenger information — all stored locally in a flat file (`database.txt`).  
Unlike commercial scheduling systems, this app is **free**, **offline**, and does **not** rely on cloud databases.

---

## 🖼️ Application Preview

![National University Airlines Home Screen](screenshots/screenshot1.png)

*Figure: Current Home Screen interface showing available flights.*

### 💺 Seats Screen
![Seats Screen Example](screenshots/screenshot2.png)

*Shows seats for a selected flight, with seat status and passenger info.*

### 👤 Seat Editor
![Seat Editor Dialog](screenshots/screenshot3.png)

*Allows editing of passenger details with validation and autosave.*

---

## 🎥 App Demo

Watch a demo of the **National University Airlines** application here:

[![App Demo Video](https://img.youtube.com/vi/ukya4rRWxyA/0.jpg)](https://www.youtube.com/watch?v=ukya4rRWxyA)

> 🎬 *Click the image above to watch the demo on YouTube.*

---

## 👥 Team 2 – CSC449 Software Engineering
Developed collaboratively as part of a group assignment to demonstrate modular design, version control, and sprint-based development.

---

## 🎯 Current Sprint Status
| Issue | Description | Status |
|--------|--------------|---------|
| #1 | App entry point | ✅ Done |
| #2 | Flight model | ✅ Done |
| #3 | Seat & Passenger models | ✅ Done |
| #4 | File storage (read CSV / autoload) | ✅ Done |
| #5 | File storage (write CSV / autosave) | ✅ Done |
| #6 | DatabaseService (backend controller) | ✅ Done |
| #7 | HomeFrame UI (flight listing screen) | ✅ Done |
| #8 | SeatsFrame | ✅ Done |
| #9 | SeatEditorDialog | ✅ Done |
| #10 | Autoload on app launch | ✅ Done |
| #11 | Validation | ✅ Done |
| #12 | Menu: File -> Exit | ✅ Done |
| #13 | Sample database.txt in repo | 🚧 Cancelled |
| #14 | AddFlightDialog | ✅ Done |
| #15 | Delete Flight | ✅ Done |
| #16 | DatabaseService: add/delete APIs + seat generation | ✅ Done |
| #17 | Validation Rules for Flight Creation | ✅ Done |
| #18 | HomFrame: Integrate "Add Flight" and "Delete Flight" | ✅ Done |
| #19 | FileStorage Compatibility Check | ✅ Done |
| #20 | README & Screenshots Update | 🚧 Backlog | 

---

![CI](https://github.com/Mark-Langston/National_University_Airlines/actions/workflows/ci.yml/badge.svg)

---

## 🧩 Architecture Overview
The project follows a **modular, layered design**:

---

## 🚀 Quick Setup Guide

### 🔧 1. Install Java
This project runs on modern Java.

- Recommended: **JDK 21 or later**

### 📥 2. Retrieve the Repository
Clone the repository from GitHub:
```bash
git clone https://github.com/Mark-Langston/National_University_Airlines.git
cd National_University_Airlines/src



