# RevPay - Financial Payment Application

## Overview

RevPay is a full-stack monolithic financial web application that enables secure digital payments and money management for both personal and business users. Built with Java 21, Spring Boot 3, and Angular, it provides comprehensive financial services with role-based access control.

## Architecture

### Backend Architecture
- **Framework**: Spring Boot 3.4+ with Java 21
- **Security**: Spring Security with JWT authentication
- **Database**: MySQL with JPA/Hibernate
- **Transaction Management**: Thread-safe operations with pessimistic locking
- **Logging**: Log4J for audit logging
- **Testing**: JUnit 4 for unit tests

### Frontend Architecture
- **Framework**: Angular 16
- **UI**: Simple, responsive components
- **HTTP Client**: Angular HttpClient for API communication

## Entity Relationship Diagram (ERD)

### Core Entities
1. **Users** - Central user entity with role-based access (PERSONAL/BUSINESS)
2. **Wallets** - One-to-one with Users, stores account balance
3. **Transactions** - Records all money movements with status tracking
4. **PaymentMethods** - Stores encrypted card information
5. **Notifications** - Event-driven notification system
6. **Invoices** - Business-specific invoicing system
7. **LoanApplications** - Business loan management
8. **MoneyRequests** - Peer-to-peer money requests

### Relationships
- User ↔ Wallet (1:1)
- User → Transactions (1:N as sender/receiver)
- User → PaymentMethods (1:N)
- User → Notifications (1:N)
- Business → Invoices (1:N)
- Business → LoanApplications (1:N)

## API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/auth/validate` - Token validation

### Transactions
- `POST /api/transactions/send` - Send money
- `POST /api/transactions/add-funds` - Add funds to wallet
- `POST /api/transactions/withdraw` - Withdraw funds
- `GET /api/transactions/user/{userId}` - Get user transactions
- `GET /api/transactions/user/{userId}/type/{type}` - Get transactions by type
- `GET /api/transactions/{transactionId}` - Get transaction details

### Wallet Management
- `GET /api/wallet/user/{userId}` - Get wallet details
- `GET /api/wallet/balance/{userId}` - Get wallet balance

### Notifications
- `GET /api/notifications/user/{userId}` - Get user notifications
- `PUT /api/notifications/{id}/read` - Mark notification as read
- `GET /api/notifications/unread/{userId}` - Get unread notifications

## Features

### Personal Account Features
- User registration and authentication
- Send/request money to other users
- Wallet balance management
- Payment method management (cards)
- Transaction history with filtering
- Add/withdraw funds
- Real-time notifications
- Profile management

### Business Account Features
- All personal features plus:
- Invoice generation and management
- Customer payment acceptance
- Business loan applications
- Transaction analytics
- Revenue reporting
- Business verification process

## Security Features

### Authentication & Authorization
- JWT-based authentication
- Role-based access control (RBAC)
- Transaction PIN verification
- Encrypted sensitive data storage

### Transaction Security
- Pessimistic locking on wallet balances
- ACID compliance for all transactions
- Audit logging for all financial operations
- Real-time fraud detection alerts

### Data Protection
- Encrypted card numbers
- Secure password hashing with BCrypt
- HTTPS enforcement
- CORS configuration

## Database Schema

The application uses MySQL with the following key tables:
- `users` - User accounts and profiles
- `wallets` - Wallet balances and account numbers
- `transactions` - All financial transactions
- `payment_methods` - Encrypted card information
- `notifications` - User notifications
- `invoices` - Business invoices
- `loan_applications` - Business loan requests

## Installation & Setup

### Prerequisites
- Java 21+
- Maven 3.6+
- MySQL 8.0+
- Node.js 16+ (for frontend)
- Angular CLI 16+

### Backend Setup
1. Clone the repository
2. Create MySQL database `revpay`
3. Update database credentials in `application.properties`
4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### Frontend Setup
1. Navigate to `frontend` directory
2. Install dependencies:
   ```bash
   npm install
   ```
3. Run the application:
   ```bash
   ng serve
   ```

## Testing

### Backend Tests
```bash
mvn test
```

### Frontend Tests
```bash
cd frontend
ng test
```

## Configuration

### Application Properties
Key configuration options in `application.properties`:
- Database connection settings
- JWT secret and expiration
- CORS configuration
- Logging levels

### Security Configuration
- JWT token expiration: 24 hours
- Password minimum length: 6 characters
- Transaction PIN: 4 digits
- Session management: Stateless (JWT)

## Development Guidelines

### Code Style
- Clean, modular code structure
- Service layer for business logic
- Repository pattern for data access
- DTO classes for API communication
- Comprehensive error handling

### Best Practices
- Thread-safe transaction processing
- Proper exception handling
- Input validation
- Audit logging
- Unit test coverage

## Future Enhancements

### Planned Features
- Mobile application
- Advanced analytics dashboard
- International payment support
- Multi-currency support
- Advanced fraud detection
- API rate limiting

### Performance Optimizations
- Database connection pooling
- Caching strategies
- Load balancing
- Microservices migration

## Support

For technical support or questions:
- Check the API documentation
- Review the database schema
- Consult the troubleshooting guide

## License

This project is developed for educational purposes as part of the RevPay financial application demonstration.
