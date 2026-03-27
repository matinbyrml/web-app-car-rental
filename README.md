# Microservices Car Rental System

A full-stack microservices-based car rental platform developed as part of the Web Applications II course.  
The system simulates real-world rental operations including fleet management, reservations, payments, and secure user access.

---

## Overview

This project implements a scalable and modular car rental system using a microservices architecture.  
It supports multiple user roles and business workflows such as:

- Car catalogue and fleet management  
- Reservation lifecycle (booking → pickup → return)  
- Payment processing with transactional consistency  
- User management and role-based access control  
- Real-time communication between services  

---

## Architecture

The system is composed of multiple independent services:

### Backend Microservices
- ReservationService  
  Manages car catalogue, fleet, and reservations  

- UserManagementService  
  Handles users (customers, staff, managers)  

- PaymentService  
  Processes payments via PayPal integration  

- APIGateway  
  Central entry point for routing and authentication  

### Frontend
- ReservationFrontend  
  Single Page Application (SPA) built with React and TypeScript  

### Data Layer
- PostgreSQL (Dockerized)  
- Separate schemas/databases per service  

### Communication
- REST APIs (synchronous)  
- Apache Kafka + Debezium (asynchronous, event-driven)  
- Outbox Pattern for distributed transactions  

---

## Tech Stack

### Backend
- Kotlin + Spring Boot  
- Spring Data JPA  
- Spring Security (OAuth2, JWT)  
- Flyway (database migrations)  

### Frontend
- React + TypeScript  
- Vite  
- React Router  

### Infrastructure
- Docker and Docker Compose  
- Apache Kafka  
- Debezium (CDC)  
- Keycloak (IAM)  

### External Services
- PayPal Sandbox (payment gateway)  

---

## Security

- OAuth 2.0 and OpenID Connect (OIDC)  
- Keycloak for authentication and authorization  
- Role-Based Access Control (RBAC):
  - CUSTOMER  
  - STAFF  
  - FLEET_MANAGER  
  - MANAGER  

---

## Core Features

### Fleet and Catalogue
- Manage car models and vehicles  
- Track availability, maintenance, and usage  

### Reservations
- Search availability  
- Create, update, and cancel bookings  
- Handle pickup and return workflows  

### Payments
- PayPal integration (sandbox)  
- Two-step flow: authorize and capture  
- Transactional consistency using Kafka  

### User Management
- CRUD operations for users  
- Eligibility checks for rentals  

### Analytics (optional extensions)
- Fleet usage insights  
- Customer behavior tracking  

---

## Running the Project

### 1. Clone the repository
```bash
git clone <your-repo-url>
cd <repo-name>
2. Start infrastructure
docker compose up --build
3. Run services

Backend services:

./gradlew bootRun

Frontend:

cd ReservationFrontend
npm install
npm run dev
Configuration
Keycloak
Runs on: http://localhost:9090
Configure:
Realm
Roles
Client (gateway-client)
PayPal Sandbox

Set in application.properties:

paypal.client.id=YOUR_CLIENT_ID
paypal.client.secret=YOUR_SECRET
paypal.environment=sandbox
Testing
Unit tests (JUnit)
Integration tests (Testcontainers)
API testing (MockMvc / REST clients)

Run tests:

./gradlew test
Project Structure
├── APIGateway/
├── ReservationService/
├── UserManagementService/
├── PaymentService/
├── ReservationFrontend/
├── docker-compose.yaml
└── README.md
Future Improvements
Observability (Prometheus and Grafana)
Distributed tracing
Advanced analytics dashboards
Production-ready deployment (Kubernetes)
Author

Matin Bayramli
Software Engineer | MSc Computer Engineering (AI)

License

This project is developed for academic purposes.
