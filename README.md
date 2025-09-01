# Customer Service

**Port:** 8085  

---

## Overview
`customer-service` manages customer-related operations for the Ecommerce platform.  
It handles user signup, validation, login, and retrieval of users by type.
As of current implementaion type can be either CUSTOMER or VENDOR.
Vendor can create products and inventory details for the particular product that he owns.

---

## Endpoints

| Endpoint                     | Method | Description                                |
|-------------------------------|--------|--------------------------------------------|
| /customer/v1/signup           | POST   | Register a new customer/vendor                     |
| /customer/v1/validate         | GET    | Validate a customer's account              |
| /customer/v1/login            | GET    | Customer login endpoint, return a auth token |                     
| /customer/v1/get/users/{type} | GET    | Retrieve all users of a specific type      |

---
## Configuration

Configuration file: src/main/resources/application*.properties or application.yml.

The application properties will be taken from the profile from https://github.com/mksandeep9875-stack/config-server-properties.git using spring cloud config server

---
## Dependencies

Spring Boot Starter Web

Spring Boot Starter Actuator

Spring Boot Starter MongoDB (depending on your database)

Spring Cloud Config Client, Eureka Client


---

## How to Run

```bash
git clone <your-repo-url>
cd customer-service
mvn clean install
mvn spring-boot:run
