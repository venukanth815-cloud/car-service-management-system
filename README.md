# AutoElite – Car Service Management System

A full-stack web application for booking and managing car services, built with Spring Boot, Thymeleaf and MySQL.

**Live demo:** https://autoelite-carservice.onrender.com (free tier: first load may take ~1 minute to wake up)

## Features
- User registration and login (Spring Security, BCrypt password hashing)
- Role-based access: **User** and **Admin**
- Book a car service, view and cancel bookings, book again
- Payments and printable invoices
- Profile management
- Forgot / reset password via email
- Admin pages: all bookings, all users, update booking status

## Tech stack
Java 17, Spring Boot 3, Spring Data JPA (Hibernate), Spring Security, Thymeleaf, MySQL, HTML/CSS/JavaScript, Docker, Render

## Run locally
1. Install Java 17 and MySQL, and create an empty database.
2. Set these environment variables:
   `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `MAIL_USERNAME`, `MAIL_PASSWORD`
   (`MAIL_PASSWORD` is a Gmail app password.)
3. Run `./mvnw spring-boot:run` and open http://localhost:8081

## Project structure
- `controller/` – handles web requests
- `service/` – business logic (email, user details)
- `repository/` – database access
- `entity/` – database tables (Customer, Booking, Payment)
- `config/` – security rules
- `templates/` – Thymeleaf pages
