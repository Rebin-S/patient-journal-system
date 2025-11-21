# Patient Journal System

Full-stack application for managing patient information and clinical notes.  
The system was built using **Spring Boot**, **React** and a **relational database** (MySQL/PostgreSQL), and runs in **Docker** with separate containers for frontend, backend and database.

## Features

### User roles
The system supports three types of users:
- **Patient**
- **Doctor**
- **Staff**

### Core functionality
- **Authentication & user management**
- **Patients can view their own information**
- **Doctors & staff can create notes and diagnoses**
- **Doctors can see all information for a single patient**
- **Patients can send messages to staff**
- **Staff can reply to patient messages**
- REST endpoints for Patient, Observation, Encounter, Condition, Practitioner, Organization, Location

## Tech Stack
**Backend:** Spring Boot, Spring Web, Spring Data JPA, Hibernate  
**Frontend:** React (TypeScript/JavaScript)  
**Database:** MySQL/PostgreSQL  
**DevOps:** Docker & docker-compose  

## Architecture
