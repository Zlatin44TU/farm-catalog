# Farm Profiles (`/api/farms`) Implementation Report

This report outlines all created and modified files, architectural decisions, business rule implementations, and verification results for the Farm Profiles API endpoints (`/api/farms`), conforming to [API Ednpoints Sample.docx](file:///home/zlatin44/Documents/Java/farm-catalog/server/Docs/API%20Ednpoints%20Sample.docx), [Use Cases.docx](file:///home/zlatin44/Documents/Java/farm-catalog/server/Docs/Use%20Cases.docx) (**UC-02: Управление на профил на ферма**), and [Project Constraints.docx](file:///home/zlatin44/Documents/Java/farm-catalog/server/Docs/Project%20Constraints.docx).

---

## 1. Summary of Changes & Purpose by File

| File | Status | Layer | Purpose |
| :--- | :--- | :--- | :--- |
| [FarmRepository.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/repository/FarmRepository.java) | **Modified** | Repository / Data Access | Added Spring Data JPA derived query methods for paginated farm browsing and filtering: `findByRegionId`, `findByNameContainingIgnoreCase`, `findByRegionIdAndNameContainingIgnoreCase`, and `existsByName`. |
| [FarmService.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/service/FarmService.java) | **Created** | Service / Business Logic | Encapsulates core business rules:<br>• **Farm Creation**: Enforces 1:1 farm uniqueness (`ResourceAlreadyExistsException` if user already has a farm); validates region existence; persists farm; links user to farm.<br>• **Buyer Elevation**: Automatically promotes `BUYER` to `FARMER` role upon creating a farm.<br>• **Catalog Browsing**: Implements paginated retrieval with `regionId` and `search` query filters, enforcing a strict 50-item page size clamp to guard against DoS.<br>• **Owner Updates**: Allows farmers to update their own farm profile with secure JWT principal resolution. |
| [FarmController.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/controller/FarmController.java) | **Created** | Web / REST Controller | Exposes RESTful endpoints under `/api/farms` with Jakarta validation (`@Valid`), HTTP status codes (200, 201 with `Location` header, 404, 409), and method-level access control (`@PreAuthorize`). |
| [SecurityConfig.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/main/java/com/catalog/farm/security/SecurityConfig.java) | **Modified** | Security Configuration | Configures route-level authorization ordering: explicitly matches `.requestMatchers("/api/auth/me", "/api/farms/me").authenticated()` prior to the public `.requestMatchers(HttpMethod.GET, "/api/farms/**").permitAll()` wildcard rule to guarantee protected status for personal dashboard routes. |
| [FarmServiceTest.java](file:///home/zlatin44/Documents/Java/farm-catalog/server/farm/src/test/java/com/catalog/farm/service/FarmServiceTest.java) | **Created** | Automated Testing | Comprehensive unit test suite with 12 test cases covering buyer-to-farmer role promotion, farmer farm creation, 409 conflict checks, 404 region/farm checks, ownership updates, dynamic filtering, and page size clamping. |

---

## 2. API Endpoints Reference

```text
GET /api/farms
├── Access: Public (Guest, Buyer, Farmer, Admin)
├── Query Params: ?regionId={id}&search={term}&page={0}&size={10}
├── Logic: Filters by region and case-insensitive name match; clamps page size to max 50
└── Response: 200 OK -> Page<FarmResponseDTO>

GET /api/farms/{id}
├── Access: Public (Guest, Buyer, Farmer, Admin)
├── Path Variable: id (Farm ID)
├── Logic: Retrieves farm details with region information
└── Response: 200 OK -> FarmResponseDTO (or 404 Not Found)

GET /api/farms/me
├── Access: Authenticated
├── Logic: Identifies user from JWT token and returns their associated farm
└── Response: 200 OK -> FarmResponseDTO (or 404 Not Found if no farm associated)

POST /api/farms
├── Access: BUYER, FARMER
├── Request Body: FarmRequestDTO (regionId, name, description, address, contactEmail, contactWebsite, imgUrl)
├── Logic:
│   1. Rejects request if user is already associated with an existing farm (409 Conflict)
│   2. Validates that the specified Region exists (404 Not Found if missing)
│   3. Persists FarmProfile and associates user.farm = savedFarm
│   4. If user role is BUYER, automatically promotes to FARMER (user.role = FARMER)
└── Response: 201 Created -> FarmResponseDTO with Location header: /api/farms/{id}

PUT /api/farms/me
├── Access: FARMER (Owner)
├── Request Body: FarmRequestDTO
├── Logic: Resolves identity from JWT; updates mutable fields (name, description, address, contact info, region)
└── Response: 200 OK -> FarmResponseDTO (or 404 Not Found if user has no farm)
```

---

## 3. Key Architectural & Business Rule Highlights

1. **Buyer-to-Farmer Onboarding**:
   - Registered users with the `BUYER` role are explicitly allowed to create a farm via `POST /api/farms` if they do not yet belong to any farm.
   - Upon successful creation, the backend updates `user.setRole(UserRoles.FARMER)`.
   - Because `CustomUserDetailsService` loads authorities from the database per request, the user's existing JWT token immediately gains `ROLE_FARMER` permissions for subsequent operations (e.g., adding products or updating the farm) without requiring a logout/login cycle.

2. **Strict 1:1 Farm Profile Uniqueness**:
   - Each farmer is restricted to owning exactly one farm.
   - Users already associated with a farm (`user.getFarm() != null`) are blocked from creating a duplicate farm profile, returning HTTP `409 Conflict` (`ResourceAlreadyExistsException: "User is already associated with an existing farm"`).

3. **Protection of Resource Ownership**:
   - The owner is never trusted from the client-supplied payload (`FarmRequestDTO.ownerId` is ignored).
   - Identity is resolved securely from `SecurityContextHolder` / `CustomUserDetails` based on the validated JWT.

4. **DoS Protection via Pagination Clamping**:
   - Per [Project Constraints.docx](file:///home/zlatin44/Documents/Java/farm-catalog/server/Docs/Project%20Constraints.docx), public catalog listings clamp client-requested page sizes to a maximum of 50 items.

---

## 4. Verification & Testing

All unit tests across the application were executed and passed successfully with zero failures:

```text
[INFO] Running com.catalog.farm.service.FarmServiceTest (12 tests) - SUCCESS
[INFO] Running com.catalog.farm.service.AuthServiceTest (6 tests) - SUCCESS
[INFO] Running com.catalog.farm.security.JwtServiceTest (3 tests) - SUCCESS
[INFO] Running com.catalog.farm.dto.DtoValidationTest (8 tests) - SUCCESS
[INFO] Results: Tests run: 29, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 5. Conclusion

The implementation of the `/api/farms` endpoints is complete, robust, and fully compliant with the project specifications and constraints. It enables seamless onboarding of buyers into producers/farmers, enforces strong resource ownership, prevents duplicate farm registrations, provides paginated public catalog exploration, and guarantees secure, authenticated management of farm profiles.
