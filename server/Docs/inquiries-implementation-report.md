# Inquiries (`/api/inquiries`) Implementation Report

This report outlines all created and modified files, architectural decisions, business rule implementations, and verification results for the Inquiries API endpoints (`/api/inquiries`), conforming to:
- [API Ednpoints Sample.docx](file:///home/zlatin44/Documents/Java/farm-catalog/server/Docs/API%20Ednpoints%20Sample.docx)
- [Project Constraints.docx](file:///home/zlatin44/Documents/Java/farm-catalog/server/Docs/Project%20Constraints.docx)
- [Project Structure.docx](file:///home/zlatin44/Documents/Java/farm-catalog/server/Docs/Project%20Structure.docx)
- [Use Cases.docx](file:///home/zlatin44/Documents/Java/farm-catalog/server/Docs/Use%20Cases.docx) (**UC-07: Изпращане на запитване**, **UC-08: Обработка на запитване**)
- [Test Cases.docx](file:///home/zlatin44/Documents/Java/farm-catalog/server/Docs/Test%20Cases.docx) (**TS-09**, **TS-10**)

---

## 1. Summary of Changes & Purpose by File

| File | Status | Layer | Purpose |
| :--- | :--- | :--- | :--- |
| [InquiryStatus.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/entity/enums/InquiryStatus.java) | **Created** | Domain Model / Enums | Defines canonical enum values (`PENDING`, `ACCEPTED`, `REJECTED`) replacing typo in `Stauts.java`. |
| [Inquiry.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/entity/Inquiry.java) | **Modified** | Entity / JPA | Standardized `@Enumerated(EnumType.STRING)` mapping to `InquiryStatus`, defaulting to `PENDING`. Foreign keys to `users` and `products`. |
| [InquiryDTO.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/dto/request/InquiryDTO.java) | **Modified** | DTO / Request | Added `@Positive` constraint on `requestedQuantity`, `@Size(max = 1000)` on `message`, and convenience constructors. |
| [InquiryStatusUpdateDTO.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/dto/request/InquiryStatusUpdateDTO.java) | **Created** | DTO / Request | Encapsulates status transition payload (`@NotNull InquiryStatus status`) for `PATCH /api/inquiries/{id}/status`. |
| [InquiryResponseDTO.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/dto/response/InquiryResponseDTO.java) | **Modified** | DTO / Response | Added `farmId` and `farmName` projection for complete buyer and farmer context, mapped to `InquiryStatus`. |
| [SelfInquiryNotAllowedException.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/exception/SelfInquiryNotAllowedException.java) | **Created** | Exception | Thrown when a farmer attempts to create an inquiry for their own product (`TS-10`). |
| [InvalidOperationException.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/exception/InvalidOperationException.java) | **Created** | Exception | Thrown on invalid status updates (e.g. attempting to revert to `PENDING` or modifying an already settled inquiry). |
| [GlobalExceptionHandler.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/exception/GlobalExceptionHandler.java) | **Modified** | Exception Handling | Handled `SelfInquiryNotAllowedException` and `InvalidOperationException` returning RFC 7807 `ProblemDetail` with `400 Bad Request`. |
| [InquiryRepository.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/repository/InquiryRepository.java) | **Modified** | Repository / Data Access | Added paginated queries: `findByUserId` (sent inquiries) and `findByProductFarmId` (received inquiries), and `findByIdAndProductFarmId`. |
| [InquiryService.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/service/InquiryService.java) | **Created** | Service / Business Logic | Encapsulates all inquiry business operations, ownership checks, self-inquiry validation, status lifecycle, and page size clamping ($\le 50$). |
| [InquiryController.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/controller/InquiryController.java) | **Created** | Web / REST Controller | Exposes RESTful endpoints under `/api/inquiries` with `@PreAuthorize`, `@Valid`, and appropriate HTTP statuses (201 Created with Location header, 200 OK). |
| [SecurityConfig.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/security/SecurityConfig.java) | **Modified** | Security Configuration | Added explicit rule ensuring `/api/inquiries/**` requires authentication. |
| [InquiryServiceTest.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/test/java/com/catalog/farm/service/InquiryServiceTest.java) | **Created** | Automated Testing | Comprehensive unit test suite with 12 test cases verifying self-inquiry blocking, ownership checks, status transitions, pagination limits, and error flows. |
| [DtoValidationTest.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/test/java/com/catalog/farm/dto/DtoValidationTest.java) | **Modified** | Automated Testing | Added test cases for positive requested quantities, message length limits ($\le 1000$), and status payload validations. |

---

## 2. API Endpoints Reference

```text
POST /api/inquiries
├── Access: BUYER (hasRole('BUYER'))
├── Request Body: InquiryDTO (productId, requestedQuantity, message)
├── Validation:
│   ├── requestedQuantity: @NotNull, @Positive, @DecimalMin("0.01"), @Digits(integer=8, fraction=2)
│   ├── message: @Size(max=1000)
│   └── productId: @NotNull, @Positive
├── Logic:
│   1. Resolves caller buyer from SecurityContext (JWT token).
│   2. Validates product exists (404 Not Found if missing).
│   3. Validates self-inquiry restriction: if buyer is associated with the product's farm, throws SelfInquiryNotAllowedException (400 Bad Request / TS-10).
│   4. Persists Inquiry with status = PENDING.
└── Response: 201 Created -> InquiryResponseDTO with Location header: /api/inquiries/{id}

GET /api/inquiries/sent
├── Access: BUYER (hasRole('BUYER'))
├── Query Params: ?page={0}&size={10}&sort={...}
├── Logic:
│   1. Resolves caller buyer from JWT.
│   2. Clamps requested page size to max 50 items (DoS protection).
│   3. Returns inquiries where buyer_id == caller.id.
└── Response: 200 OK -> Page<InquiryResponseDTO>

GET /api/inquiries/received
├── Access: FARMER (hasRole('FARMER'))
├── Query Params: ?page={0}&size={10}&sort={...}
├── Logic:
│   1. Resolves caller farmer and associated farm from JWT.
│   2. Clamps requested page size to max 50 items.
│   3. Returns inquiries for all products belonging to caller's farm.
└── Response: 200 OK -> Page<InquiryResponseDTO>

PATCH /api/inquiries/{id}/status
├── Access: FARMER (hasRole('FARMER'))
├── Path Variable: id (Inquiry ID)
├── Request Body: InquiryStatusUpdateDTO (status: ACCEPTED or REJECTED)
├── Validation: @NotNull status, cannot be PENDING
├── Logic:
│   1. Resolves caller farmer from JWT.
│   2. Validates inquiry exists (404 Not Found).
│   3. Resource Ownership: validates inquiry.product.farm belongs to caller. Throws AccessDeniedException (403 Forbidden) if not owned.
│   4. Status Transition: validates current status is PENDING. If already ACCEPTED or REJECTED, throws InvalidOperationException (400 Bad Request).
│   5. Saves and returns updated inquiry.
└── Response: 200 OK -> InquiryResponseDTO
```

---

## 3. Key Business Rules & Constraints Enforced

1. **Self-Inquiry Guard (`TS-10`)**:
   - A farmer who also creates or browses cannot submit an inquiry for products produced by their own farm profile.
   - Throws `SelfInquiryNotAllowedException`, mapped to `400 Bad Request`.

2. **Resource Ownership Enforcement**:
   - Only the farmer who owns the farm associated with the product has authorization to view received inquiries or accept/reject them.
   - Unauthorized attempts throw `AccessDeniedException`, mapped to `403 Forbidden`.

3. **Status Lifecycle Integrity**:
   - New inquiries start strictly with `PENDING`.
   - Inquiries can only transition from `PENDING` to `ACCEPTED` or `REJECTED`. Reverting to `PENDING` or altering settled inquiries is rejected via `InvalidOperationException` (`400 Bad Request`).

4. **DoS Protection via Pagination Clamping**:
   - Inquiries sent and received pagination clamps any requested `size > 50` down to `50`.

---

## 4. Test Verification Results

All 56 unit and DTO validation tests executed and passed with 0 errors and 0 failures:

```text
[INFO] Running com.catalog.farm.service.FarmServiceTest (12 tests) - SUCCESS
[INFO] Running com.catalog.farm.service.InquiryServiceTest (12 tests) - SUCCESS
[INFO] Running com.catalog.farm.service.ProductServiceTest (13 tests) - SUCCESS
[INFO] Running com.catalog.farm.service.AuthServiceTest (6 tests) - SUCCESS
[INFO] Running com.catalog.farm.security.JwtServiceTest (3 tests) - SUCCESS
[INFO] Running com.catalog.farm.dto.DtoValidationTest (10 tests) - SUCCESS
[INFO] Results: Tests run: 56, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
