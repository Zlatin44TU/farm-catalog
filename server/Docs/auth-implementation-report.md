# Local User Authentication Implementation Report

This report outlines all the modified and created files during the implementation of local user authentication (Registration, Login, and `/me` endpoint with JWT) for the Farm Catalog application, as defined by [API Ednpoints Sample.docx](file:///home/zlatin44/Documents/Java/farm-catalog/server/Docs/API%20Ednpoints%20Sample.docx) and [Use Cases.docx](file:///home/zlatin44/Documents/Java/farm-catalog/server/Docs/Use%20Cases.docx) (**UC-01**).

---

## 1. Summary of Changes & Purpose by File

### A. Configuration & Build Files

| File | Status | Purpose |
| :--- | :--- | :--- |
| [pom.xml](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/pom.xml) | **Modified** | Added `spring-boot-starter-security`, JJWT libraries (`jjwt-api`, `jjwt-impl`, `jjwt-jackson` version `0.12.6`), and `spring-security-test` for authentication, token generation, and testing. |
| [application.properties](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/resources/application.properties) | **Modified** | Bound JWT configuration keys (`jwt.secret` and `jwt.expiration-ms`) to environment variables with a 4-hour default expiration (`14400000 ms`). |
| [.env](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/.env) | **Modified** | Configured local development environment variables: `JWT_SECRET` (256-bit key) and `JWT_EXPIRATION_MS=14400000`. |

---

### B. Security & JWT Core Components

| File | Status | Purpose |
| :--- | :--- | :--- |
| [JwtService.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/security/jwt/JwtService.java) | **Created** | Provides cryptographic token utilities: creates signed HMAC-SHA JWTs with 4-hour expiration, extracts subject (user email) and claims, and verifies token validity and expiration. |
| [CustomUserDetails.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/security/service/CustomUserDetails.java) | **Created** | Implements Spring Security's `UserDetails` contract, wrapping the application's [User.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/entity/User.java) entity and mapping roles to `ROLE_BUYER`, `ROLE_FARMER`, or `ROLE_ADMIN`. |
| [CustomUserDetailsService.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/security/service/CustomUserDetailsService.java) | **Created** | Implements `UserDetailsService`, loading user credentials and roles from MySQL via [UserRepository.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/repository/UserRepository.java) by email. |
| [JwtAuthenticationFilter.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/security/jwt/JwtAuthenticationFilter.java) | **Created** | Intercepts HTTP requests (`OncePerRequestFilter`), inspects the `Authorization: Bearer <token>` header, validates the JWT, and populates the `SecurityContextHolder` with authentication details. |
| [JwtAuthenticationEntryPoint.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/security/jwt/JwtAuthenticationEntryPoint.java) | **Created** | Implements `AuthenticationEntryPoint` to return clean RFC 7807 `ProblemDetail` JSON responses (HTTP `401 Unauthorized`) when unauthenticated clients attempt to access protected endpoints. |
| [SecurityConfig.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/security/SecurityConfig.java) | **Created** | Configures the HTTP security filter chain: disables CSRF (for stateless REST APIs), sets `SessionCreationPolicy.STATELESS`, registers the JWT filter before `UsernamePasswordAuthenticationFilter`, exposes `BCryptPasswordEncoder` and `AuthenticationManager` beans, and configures URL authorization (permitting public registration/login, Swagger UI, and public GET endpoints while requiring authentication for `/api/auth/me`). |

---

### C. Repository, Service & Controller Layers

| File | Status | Purpose |
| :--- | :--- | :--- |
| [UserRepository.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/repository/UserRepository.java) | **Modified** | Added `boolean existsByEmail(String email)` to support fast pre-checks during registration. |
| [AuthService.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/service/AuthService.java) | **Created** | Houses core business logic for authentication:<br>• **Registration**: Blocks self-assignment of `ADMIN` role with `400 Bad Request`; defaults omitted roles to `BUYER`; validates email uniqueness; hashes passwords with BCrypt; links farm profile if provided; returns JWT and user profile upon creation.<br>• **Login**: Validates credentials via `AuthenticationManager` and issues a 4-hour JWT token with profile info.<br>• **User Profile**: Fetches the currently authenticated user's profile details. |
| [AuthController.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/controller/AuthController.java) | **Created** | Exposes the REST endpoints at `/api/auth`:<br>• `POST /api/auth/register` (HTTP 201 Created)<br>• `POST /api/auth/login` (HTTP 200 OK)<br>• `GET /api/auth/me` (HTTP 200 OK) |
| [GlobalExceptionHandler.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/exception/GlobalExceptionHandler.java) | **Modified** | Added exception handlers for `BadCredentialsException` (401), `IllegalArgumentException` (400), and `AccessDeniedException` (403) to ensure consistent `ProblemDetail` JSON responses. |

---

### D. Automated Unit Tests

| File | Status | Purpose |
| :--- | :--- | :--- |
| [JwtServiceTest.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/test/java/com/catalog/farm/security/JwtServiceTest.java) | **Created** | Verifies HMAC-SHA key signing, 4-hour expiration setting (`14,400,000 ms`), username extraction, and token validity checks. |
| [AuthServiceTest.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/test/java/com/catalog/farm/service/AuthServiceTest.java) | **Created** | Unit tests covering:<br>1. Successful registration returning JWT and default `BUYER` role.<br>2. Rejection of registration attempts with `ADMIN` role (`IllegalArgumentException`).<br>3. Prevention of duplicate email registrations (`ResourceAlreadyExistsException`).<br>4. Successful login returning JWT token.<br>5. Bad credentials rejection (`BadCredentialsException`).<br>6. Fetching current user profile via `/me`. |

---

## 2. API Endpoints Reference

```text
POST /api/auth/register
├── Access: Public (Guest)
├── Request: UserRegisterDTO (email, password, fullName, phoneNumber, optional role, optional farmId)
├── Logic: Rejects ADMIN, defaults role to BUYER, checks duplicate email, hashes password with BCrypt
└── Response: 201 Created -> JwtAuthResponseDTO (token, tokenType: "Bearer", user profile)

POST /api/auth/login
├── Access: Public (Guest)
├── Request: UserLoginDTO (email, password)
├── Logic: Authenticates via AuthenticationManager/BCrypt, generates 4-hour JWT
└── Response: 200 OK -> JwtAuthResponseDTO (token, tokenType: "Bearer", user profile)

GET /api/auth/me
├── Access: Authenticated (Bearer Token)
├── Logic: Resolves identity from SecurityContextHolder
└── Response: 200 OK -> UserProfileResponseDTO (user details, role, farm info)
```

---

## 3. Conclusion

The local authentication system is fully implemented and conforms to the project specification and user constraints:
- **Scope Compliant**: Only local email/password credentials with JWT tokens are implemented; no external OAuth dependencies were added.
- **Security Best Practices**: Passwords are securely hashed with `BCryptPasswordEncoder`, sessions are strictly stateless, tokens expire after 4 hours, and secrets are externalized via environment variables.
- **Role Enforcement**: `ADMIN` self-registration is prevented at the service layer, and default role assignment is handled safely.
- **Verified**: All 17 automated tests (including `AuthServiceTest`, `JwtServiceTest`, and existing DTO validation tests) pass successfully.
