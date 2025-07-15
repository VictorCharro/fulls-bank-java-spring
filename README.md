# Fulls Bank - Banking System API 🏦

## Current Implementation ✅

### Core Features
- **Account Management**
  - Create new bank accounts
  - Deposit/withdraw funds
  - Check account balance
- **Client Management**
  - Basic client registration
  - Account-client relationship

### Technical Stack
- Java 24 + Spring Boot 3
- Spring Data JPA (Hibernate)
- Lombok
- Jakarta Validation

### Key Components
```java
// Entities
Account.java       // Handles balance operations
Client.java        // Customer information
Transaction.java   // Financial records

// Enums
AccountType.java      // CHECKING, SAVINGS, SALARY
TransactionType.java  // DEPOSIT, WITHDRAWAL, TRANSFER

// Exceptions
AccountNotFound.java
InsufficientBalance.java
NegativeAmount.java
IdNotFound.java

```
Coming Next 🚧
1. REST Controllers:
   - AccountController (@RestController)
   - TransactionController
   - ClientController

2. Enhanced Services:
   - Transfer between accounts
   - Transaction history
   - Statement generation

3. API Improvements:
   - Swagger documentation
   - DTOs for requests/responses
   - Proper status codes

Future Plans 🛠️

◇ Email notifications

◇ PDF statements

◇ Security layer (JWT)

◇ Mobile integration

How to run: 

- git clone https://github.com/yourrepo/fulls-bank.git
- cd fulls-bank
- mvn spring-boot:run
