# 🏥 Online Healthcare Management System — Engineering Roadmap

> **Specification Reference:** Problem Statement #3 — *Online Healthcare Management System (GUVI Geek Network)*  
> **Repository:** `https://github.com/amanyadavaa/Healthcare-System.git`  
> **Target Architecture:** Java 21 / Spring Boot 3.x Multi-Tier Architecture • Spring Security 6 (JWT & RBAC) • MySQL 8 • Spring Data JPA • RESTful APIs • Modern Responsive Dashboard UI • Dockerized Deployment

---

## 📋 Table of Contents
1. [Project Overview & Architecture](#project-overview--architecture)
2. [Commit Protocol & Development Workflow](#commit-protocol--development-workflow)
3. [Phase 1: Architecture, Infrastructure & Foundations](#phase-1-architecture-infrastructure--foundations)
4. [Phase 2: Patient Module & Patient Dashboard](#phase-2-patient-module--patient-dashboard)
5. [Phase 3: Doctor Module & Doctor Dashboard](#phase-3-doctor-module--doctor-dashboard)
6. [Phase 4: Admin Governance, Oversight & Analytics](#phase-4-admin-governance-oversight--analytics)
7. [Phase 5: User Interface, Responsive Dashboards & Front-End Integration](#phase-5-user-interface-responsive-dashboards--front-end-integration)
8. [Phase 6: Quality Assurance, Automated Testing & Security Hardening](#phase-6-quality-assurance-automated-testing--security-hardening)
9. [Phase 7: Packaging, Containerization & Production Deployment](#phase-7-packaging-containerization--production-deployment)
10. [Master Progress Tracking Matrix](#master-progress-tracking-matrix)

---

## 🏗️ Project Overview & Architecture

The **Online Healthcare Management System** is a full-featured clinical and appointment management platform serving three primary user roles:
1. **Patient:** Books/reschedules appointments, manages profile details, reviews medical history and prescriptions, and submits doctor ratings/feedback.
2. **Doctor:** Manages consultation availability/schedule, views appointment calendar, examines patient records, enters diagnosis notes and prescriptions, and tracks patient feedback.
3. **Administrator:** Oversees user accounts and role assignments, monitors system-wide appointments, configures clinic settings, and examines operational performance analytics.

```
┌────────────────────────────────────────────────────────────────────────┐
│                        PRESENTATION TIER (UI)                          │
│  [Admin Dashboard]       [Doctor Dashboard]       [Patient Dashboard]  │
│  - User Management       - Schedule Management    - Appointment Booking│
│  - Appointments Desk     - Patient Records EMR    - Medical History    │
│  - System Settings       - Appointment Workflow   - Profile Manager    │
│  - Analytics & Reports   - Patient Feedback       - Doctor Feedback    │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │ HTTPS / REST (JSON) + JWT
┌──────────────────────────────────▼─────────────────────────────────────┐
│                      APPLICATION SERVICES (SPRING BOOT 3)              │
│  ┌──────────────────────┐  ┌──────────────────┐  ┌──────────────────┐  │
│  │ Security & RBAC      │  │ Appointment Core │  │ EMR & Records    │  │
│  │ (Spring Security 6)  │  │ Scheduling Engine│  │ Medical History  │  │
│  └──────────────────────┘  └──────────────────┘  └──────────────────┘  │
│  ┌──────────────────────┐  ┌──────────────────┐  ┌──────────────────┐  │
│  │ User Management      │  │ System Settings  │  │ Performance &    │  │
│  │ Profiles & Roles     │  │ Clinic Config    │  │ Analytics Engine │  │
│  └──────────────────────┘  └──────────────────┘  └──────────────────┘  │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │ Spring Data JPA / Hibernate
┌──────────────────────────────────▼─────────────────────────────────────┐
│                           PERSISTENCE TIER                             │
│       MySQL 8.0 Relational Database (Flyway / Schema Migrations)        │
│   [users] [roles] [doctors] [patients] [appointments] [records] [settings]│
└────────────────────────────────────────────────────────────────────────┘
```

---

## 📌 Commit Protocol & Development Workflow

To ensure strict traceability and maintainable git history:
- Every sub-phase **must** be implemented along with its corresponding test suite.
- Work is verified and tests pass prior to staging and committing.
- Commit messages follow conventional commit formatting:
  ```bash
  git add .
  git commit -m "feat(phase-X.Y): <description of completed deliverable> completed"
  git push origin main
  ```

---

## 🚀 Phase 1: Architecture, Infrastructure & Foundations

### 🔹 Sub-Phase 1.1: Project Skeleton, Build Automation & Workspace Standards
- **Objective:** Establish the Spring Boot project layout, Maven wrapper, root folder structure, and Git hygiene.
- **Deliverables:**
  - Maven `pom.xml` configured with Spring Boot 3.x, Java 21, Spring Data JPA, Spring Security, Validation, MySQL Connector, Lombok, SpringDoc OpenAPI, and JUnit 5.
  - Maven Wrapper script (`mvnw`, `mvnw.cmd`, `.mvn/wrapper/`).
  - `.gitignore` configured for Java, Maven, IDEs (IntelliJ, VS Code, Eclipse), logs, and OS artifacts.
  - Multi-tier package structure:
    `com.healthcare.system.{config, controller, dto, entity, repository, service, exception, security, util}`.
- **Test Cases to Build:**
  - `ContextLoadsTest`: Verify Spring application context loads without beans missing.
  - `EnvironmentPropertiesTest`: Verify active Spring profiles (`dev`, `prod`, `test`) load expected property keys.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "feat(phase-1.1): project skeleton, build automation, and package structure completed"
  ```
- **Acceptance Criteria:** `./mvnw clean compile` succeeds cleanly without warnings or errors.

---

### 🔹 Sub-Phase 1.2: Relational Database Modeling & Migration Architecture
- **Objective:** Design the relational schema adhering to 3NF and establish schema migration scripts.
- **Deliverables:**
  - Relational tables:
    - `users`: ID, email, password_hash, first_name, last_name, phone, role, status, created_at, updated_at.
    - `patients`: ID, user_id (FK), date_of_birth, gender, blood_group, emergency_contact, address.
    - `doctors`: ID, user_id (FK), specialization, qualification, experience_years, consultation_fee, department.
    - `doctor_schedules`: ID, doctor_id (FK), day_of_week, start_time, end_time, slot_duration_minutes, is_available.
    - `appointments`: ID, patient_id (FK), doctor_id (FK), appointment_date, start_time, end_time, status (`PENDING`, `CONFIRMED`, `COMPLETED`, `CANCELLED`, `RESCHEDULED`), reason, notes.
    - `medical_records`: ID, appointment_id (FK), patient_id (FK), doctor_id (FK), diagnosis, prescription, treatment_notes, created_at.
    - `patient_feedbacks`: ID, appointment_id (FK), patient_id (FK), doctor_id (FK), rating (1-5), comment, created_at.
    - `system_settings`: ID, setting_key, setting_value, description, updated_by, updated_at.
  - Flyway migration scripts under `src/main/resources/db/migration/V1__init_schema.sql`.
- **Test Cases to Build:**
  - `SchemaIntegrityTest`: Validates table creation, foreign key constraints, unique constraints (`email`, `appointment slot uniqueness`), and check constraints.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "feat(phase-1.2): relational database schema and migration scripts completed"
  ```
- **Acceptance Criteria:** Database migrations execute smoothly on both H2 test database and MySQL 8.

---

### 🔹 Sub-Phase 1.3: Core JPA Entities & Repository Layer
- **Objective:** Implement domain entity models with JPA annotations, auditing, and Spring Data JPA repositories.
- **Deliverables:**
  - Entity classes: `User`, `Role`, `Patient`, `Doctor`, `DoctorSchedule`, `Appointment`, `MedicalRecord`, `PatientFeedback`, `SystemSetting`.
  - Base entity `BaseAuditableEntity` (`createdAt`, `updatedAt`, `@CreatedDate`, `@LastModifiedDate`).
  - Repository interfaces extending `JpaRepository` and `JpaSpecificationExecutor` with custom queries (e.g. `findAvailableSlots`, `findAppointmentsByDoctorAndDate`, `findByEmail`).
- **Test Cases to Build:**
  - `UserRepositoryTest`: Test CRUD operations, email case-insensitive lookup, duplicate email handling.
  - `AppointmentRepositoryTest`: Test slot overlapping query, status filtering, doctor schedule queries.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "feat(phase-1.3): JPA entities, relations, and repository interfaces completed"
  ```
- **Acceptance Criteria:** All entity mappings pass `@DataJpaTest` with zero lazy-loading or mapping exceptions.

---

### 🔹 Sub-Phase 1.4: Security Architecture, JWT Engine & RBAC Authorization
- **Objective:** Configure Spring Security 6 with stateless JWT authentication and strict Role-Based Access Control (`ADMIN`, `DOCTOR`, `PATIENT`).
- **Deliverables:**
  - `JwtTokenProvider` & `JwtAuthenticationFilter` for Bearer token generation, parsing, validation, and expiry checks.
  - Password encoding with `BCryptPasswordEncoder` (cost factor 12).
  - Security configuration (`SecurityFilterChain`) defining public endpoints (`/api/auth/**`, `/swagger-ui/**`) and restricted role endpoints (`/api/admin/**` -> `ROLE_ADMIN`, `/api/doctor/**` -> `ROLE_DOCTOR`, `/api/patient/**` -> `ROLE_PATIENT`).
  - Authentication controller: `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh-token`, `/api/auth/me`.
- **Test Cases to Build:**
  - `JwtTokenProviderTest`: Verify token generation, extraction of claims, and rejection of expired/tampered tokens.
  - `AuthSecurityTest`: Test accessing protected endpoint without token returns 401; wrong role returns 403; correct role returns 200.
  - `AuthenticationServiceTest`: Unit test login success with BCrypt check and failure on bad credentials.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "feat(phase-1.4): security configuration, JWT authentication, and RBAC completed"
  ```
- **Acceptance Criteria:** Unauthorized requests are rejected with standardized 401/403 responses.

---

### 🔹 Sub-Phase 1.5: Unified API Contract, DTO Validation & Global Exception Handling
- **Objective:** Standardize API responses and centralized exception handling.
- **Deliverables:**
  - Generic envelope `ApiResponse<T>` (`success`, `message`, `data`, `timestamp`, `errorCode`).
  - Centralized `@RestControllerAdvice` (`GlobalExceptionHandler`) handling `MethodArgumentNotValidException`, `ResourceNotFoundException`, `ConflictException`, `BadCredentialsException`, `AccessDeniedException`.
  - Standard request/response DTOs with Jakarta Bean Validation annotations (`@NotBlank`, `@Email`, `@Pattern`, `@FutureOrPresent`).
- **Test Cases to Build:**
  - `GlobalExceptionHandlerTest`: Validate JSON response structure for 400 validation errors, 404 not found, and 409 conflict errors.
  - `DtoValidationTest`: Validate email format, blank fields, and password strength requirements.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "feat(phase-1.5): unified API response, DTO validation, and exception handling completed"
  ```
- **Acceptance Criteria:** Every error in the system returns predictable, structured JSON without leaking stack traces.

---

## 🧑‍⚕️ Phase 2: Patient Module & Patient Dashboard

### 🔹 Sub-Phase 2.1: Patient Profile Management & Account Security
- **Objective:** Allow patients to view, update personal and contact details, and update passwords.
- **Deliverables:**
  - Service methods: `getPatientProfile(userId)`, `updatePatientProfile(userId, ProfileUpdateRequest)`, `changePassword(userId, PasswordChangeRequest)`.
  - Controller: `GET /api/patient/profile`, `PUT /api/patient/profile`, `POST /api/patient/change-password`.
  - Fields handled: full name, phone number, gender, blood group, address, emergency contact.
- **Test Cases to Build:**
  - `PatientProfileServiceTest`: Verify successful profile update; verify update fails if patient record does not exist.
  - `PasswordChangeValidationTest`: Test old password verification and validation of new password confirmation.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "feat(phase-2.1): patient profile management and security endpoints completed"
  ```
- **Acceptance Criteria:** Patient can view profile and update details with instant confirmation and database persistence.

---

### 🔹 Sub-Phase 2.2: Doctor Discovery & Availability Directory
- **Objective:** Enable patients to browse doctors by specialization, view doctor profiles, and query available appointment slots.
- **Deliverables:**
  - Doctor search API with filtering by specialization, department, and doctor name.
  - Slot calculation service: Takes `doctorId` and `date`, inspects doctor's working schedule, subtracts already booked appointments, and returns open time slots.
  - Controller: `GET /api/patient/doctors`, `GET /api/patient/doctors/{id}`, `GET /api/patient/doctors/{id}/available-slots?date=YYYY-MM-DD`.
- **Test Cases to Build:**
  - `SlotCalculationTest`: Test slot calculation on doctor's working day, non-working day, and when slots are partially booked.
  - `DoctorSearchFilterTest`: Test search by query string, specialty filter, and empty query result.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "feat(phase-2.2): doctor discovery and slot availability calculation engine completed"
  ```
- **Acceptance Criteria:** Available slots returned strictly match doctor working hours and exclude existing booked appointments.

---

### 🔹 Sub-Phase 2.3: Appointment Booking Engine with Conflict Prevention
- **Objective:** Allow patients to book appointments with validation preventing double booking and invalid past dates.
- **Deliverables:**
  - Booking service with transactional locking (`@Transactional`) to prevent race conditions when two patients book the same slot simultaneously.
  - Controller: `POST /api/patient/appointments` (Input: `doctorId`, `appointmentDate`, `startTime`, `reason`).
  - Appointment confirmation response including unique appointment reference number, doctor details, and scheduled time.
- **Test Cases to Build:**
  - `AppointmentBookingServiceTest`: Test successful booking; test booking in the past throws error; test booking outside doctor schedule throws error.
  - `DoubleBookingConcurrencyTest`: Test parallel booking requests for the exact same doctor and time slot — exactly one succeeds, the other receives 409 Conflict.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "feat(phase-2.3): appointment booking engine with concurrency conflict prevention completed"
  ```
- **Acceptance Criteria:** Patient receives appointment confirmation with status `PENDING` or `CONFIRMED`; double-booking is strictly prohibited.

---

### 🔹 Sub-Phase 2.4: Patient Appointment History & Status Tracking
- **Objective:** Enable patients to view all past and upcoming appointments, filter by status, and cancel/reschedule.
- **Deliverables:**
  - Controller: `GET /api/patient/appointments` (supports query params: `status`, `timeframe=UPCOMING|PAST`, `page`, `size`), `PUT /api/patient/appointments/{id}/cancel`, `PUT /api/patient/appointments/{id}/reschedule`.
  - Business rules: Cancellation allowed only if appointment is at least 2 hours before scheduled time; cancellation updates status to `CANCELLED`.
- **Test Cases to Build:**
  - `AppointmentHistoryFilterTest`: Test pagination and filtering between upcoming and historical appointments.
  - `AppointmentCancellationTest`: Test cancellation within allowed window; test cancellation rejection for already completed appointments.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "feat(phase-2.4): patient appointment history, cancellation, and rescheduling completed"
  ```
- **Acceptance Criteria:** Patients can retrieve paginated appointment history and execute cancellations conforming to business policies.

---

### 🔹 Sub-Phase 2.5: Patient Medical History & Records Portal
- **Objective:** Provide patients with read-only access to their complete medical history, diagnoses, and prescriptions.
- **Deliverables:**
  - Controller: `GET /api/patient/medical-records`, `GET /api/patient/medical-records/{id}`, `GET /api/patient/medical-records/appointment/{appointmentId}`.
  - Structured response containing diagnosis details, prescription list (medicine, dosage, duration, instructions), doctor notes, and consultation date.
- **Test Cases to Build:**
  - `MedicalHistoryAccessTest`: Test patient can view their own records; test patient cannot access another patient's medical records (IDOR protection).
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "feat(phase-2.5): patient medical records and prescription history portal completed"
  ```
- **Acceptance Criteria:** Patients securely view full historical clinical records with IDOR safeguards in place.

---

### 🔹 Sub-Phase 2.6: Patient Feedback & Doctor Rating System
- **Objective:** Allow patients to rate and review doctors after completed appointments.
- **Deliverables:**
  - Controller: `POST /api/patient/appointments/{appointmentId}/feedback`.
  - Validation: Rating must be integer 1 to 5; appointment must be in `COMPLETED` status; patient cannot submit duplicate feedback for the same appointment.
  - Average rating calculation updated on Doctor entity.
- **Test Cases to Build:**
  - `FeedbackSubmissionTest`: Verify feedback allowed only on completed appointments; verify rating range constraint (reject 0 or 6).
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "feat(phase-2.6): patient feedback and doctor rating submission engine completed"
  ```
- **Acceptance Criteria:** Feedback persists successfully, links to appointment, and updates doctor's cumulative rating.

---

## 👨‍⚕️ Phase 3: Doctor Workflow & Doctor Dashboard

### 🔹 Sub-Phase 3.1: Doctor Schedule & Slot Availability Configuration
- **Objective:** Allow doctors to set weekly recurring schedules, define slot durations, and toggle on/off availability.
- **Deliverables:**
  - Entity & Service: `DoctorScheduleService` (`getSchedule(doctorId)`, `updateSchedule(doctorId, List<ScheduleSlotDTO>)`, `setDayOff(doctorId, date)`).
  - Controller: `GET /api/doctor/schedule`, `PUT /api/doctor/schedule`, `POST /api/doctor/schedule/time-off`.
  - Support for custom slot durations (15m, 30m, 45m, 60m) and morning/afternoon session boundaries.
- **Test Cases to Build:**
  - `DoctorScheduleServiceTest`: Verify setting valid schedules; verify start time after end time is rejected; verify overlapping shift blocks are rejected.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "feat(phase-3.1): doctor schedule management and slot configuration completed"
  ```
- **Acceptance Criteria:** Doctor can configure weekly availability and block out time-off periods accurately.

---

### 🔹 Sub-Phase 3.2: Appointment Queue Management & Workflow Transitions
- **Objective:** Provide doctors with view and control of appointment requests (confirm, complete, reschedule, cancel).
- **Deliverables:**
  - Controller: `GET /api/doctor/appointments` (filters: `date`, `status`, `range`), `PUT /api/doctor/appointments/{id}/confirm`, `PUT /api/doctor/appointments/{id}/reschedule`, `PUT /api/doctor/appointments/{id}/cancel`, `PUT /api/doctor/appointments/{id}/complete`.
  - State machine validation: Only `PENDING` can transition to `CONFIRMED`; only `CONFIRMED` can transition to `COMPLETED`.
- **Test Cases to Build:**
  - `AppointmentWorkflowStateTest`: Test valid status transitions; test invalid status transitions (e.g. `CANCELLED` cannot transition to `COMPLETED`).
  - `DoctorAppointmentListTest`: Test doctor only sees appointments booked for them.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "feat(phase-3.2): doctor appointment queue and status transition workflow completed"
  ```
- **Acceptance Criteria:** Doctor successfully manages patient appointment lifecycle with strict state machine validation.

---

### 🔹 Sub-Phase 3.3: Doctor Patient Medical Records & EMR Entry
- **Objective:** Enable doctors to view patient history and create/update medical records, clinical notes, and prescriptions during or after consultation.
- **Deliverables:**
  - Controller: `GET /api/doctor/patients/{patientId}/records`, `POST /api/doctor/appointments/{appointmentId}/medical-record`, `PUT /api/doctor/medical-records/{id}`.
  - Data payload: diagnosis, symptoms, clinical observations, vitals (blood pressure, pulse, temperature), prescription items (drug name, dose, frequency, days), lab test suggestions.
- **Test Cases to Build:**
  - `MedicalRecordCreationTest`: Test doctor creates record for completed appointment; verify patient record history is updated.
  - `MedicalRecordSecurityTest`: Verify doctor can only create records for patients who had an appointment with them.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "feat(phase-3.3): doctor medical records and prescription authoring system completed"
  ```
- **Acceptance Criteria:** Medical records are authored, versioned, linked to appointments, and retrievable in patient history.

---

### 🔹 Sub-Phase 3.4: Doctor Feedback & Reviews Viewer
- **Objective:** Enable doctors to view ratings and patient feedback received.
- **Deliverables:**
  - Controller: `GET /api/doctor/feedbacks` (with pagination, sort by date/rating, overall average rating score, total reviews count).
  - Feedback DTO masking sensitive patient information if requested.
- **Test Cases to Build:**
  - `DoctorFeedbackViewerTest`: Test doctor retrieves all reviews linked to their appointments; test summary stats (average rating, star breakdown).
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "feat(phase-3.4): doctor reviews and patient feedback dashboard endpoint completed"
  ```
- **Acceptance Criteria:** Doctor views list of verified patient feedback alongside cumulative average rating score.

---

## 🏛️ Phase 4: Admin Governance, Oversight & Analytics

### 🔹 Sub-Phase 4.1: Admin User Management (Patients, Doctors, Staff)
- **Objective:** Provide full administrative control over user accounts, role modifications, and status changes.
- **Deliverables:**
  - Controller: `GET /api/admin/users`, `POST /api/admin/users`, `GET /api/admin/users/{id}`, `PUT /api/admin/users/{id}`, `DELETE /api/admin/users/{id}`, `PATCH /api/admin/users/{id}/status`.
  - Search and filter by role (`ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_ADMIN`), active/inactive status, and name/email query.
  - Soft-delete or account suspension logic (`ACTIVE`, `SUSPENDED`, `DEACTIVATED`).
- **Test Cases to Build:**
  - `AdminUserManagementServiceTest`: Test user creation with specific role; test updating user profile; test status toggle; test preventing self-deletion of last admin.
  - `AdminUserSearchFilterTest`: Test pagination and role filtering.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "feat(phase-4.1): admin user management CRUD and role provisioning completed"
  ```
- **Acceptance Criteria:** Admin can view, create, edit, deactivate, and query user accounts with audit confirmation messages.

---

### 🔹 Sub-Phase 4.2: Admin Master Appointment Oversight Desk
- **Objective:** Give administrators global visibility and control over all appointments in the system.
- **Deliverables:**
  - Controller: `GET /api/admin/appointments`, `PUT /api/admin/appointments/{id}/reschedule`, `PUT /api/admin/appointments/{id}/cancel`, `POST /api/admin/appointments/manual-book`.
  - Global filters: doctor, patient, date range, department, status.
- **Test Cases to Build:**
  - `AdminAppointmentOversightTest`: Test admin can view across all doctors and patients; test admin overriding appointment schedule; test slot validation remains enforced.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "feat(phase-4.2): admin master appointment oversight and scheduling desk completed"
  ```
- **Acceptance Criteria:** Admin can view and override appointments across the organization.

---

### 🔹 Sub-Phase 4.3: Admin System Settings & Clinic Configuration
- **Objective:** Provide a dynamic configuration panel for clinic-wide operational settings.
- **Deliverables:**
  - Settings keys: `CLINIC_NAME`, `CLINIC_PHONE`, `CLINIC_EMAIL`, `DEFAULT_APPOINTMENT_DURATION`, `BOOKING_LEAD_TIME_HOURS`, `MAX_DAYS_IN_ADVANCE_BOOKING`, `ENABLE_EMAIL_NOTIFICATIONS`.
  - Controller: `GET /api/admin/settings`, `PUT /api/admin/settings`, `GET /api/admin/settings/{key}`.
  - Cache refresh mechanism to apply settings without application restart.
- **Test Cases to Build:**
  - `SystemSettingsServiceTest`: Test updating configuration key-values; test validating numeric settings (e.g. slot duration > 0).
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "feat(phase-4.3): admin system settings and clinic configuration panel completed"
  ```
- **Acceptance Criteria:** System settings can be updated by admin and immediately alter application behavior.

---

### 🔹 Sub-Phase 4.4: Performance Analytics & Reporting Engine
- **Objective:** Generate operational analytics and statistical reports on appointments, patient counts, doctor workloads, and revenue.
- **Deliverables:**
  - Analytics service computing:
    - Total appointments breakdown (completed, cancelled, upcoming).
    - Daily / weekly / monthly appointment trend lines.
    - Doctor utilization rate (hours booked vs hours available).
    - Patient registration velocity.
    - Departmental appointment distribution.
  - Controller: `GET /api/admin/analytics/overview`, `GET /api/admin/analytics/trends`, `GET /api/admin/analytics/doctor-utilization`, `GET /api/admin/analytics/export?format=csv`.
- **Test Cases to Build:**
  - `AnalyticsCalculationTest`: Test calculation formulas for utilization, completion rate, and date-range aggregations with mock data.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "feat(phase-4.4): healthcare performance analytics and reporting engine completed"
  ```
- **Acceptance Criteria:** Admin receives accurate numerical and time-series analytical data formatted for chart rendering.

---

## 🎨 Phase 5: User Interface, Responsive Dashboards & Front-End Integration

### 🔹 Sub-Phase 5.1: Unified UI Design System, Layouts & Auth Pages
- **Objective:** Build responsive layouts, clean styling (Tailwind / Modern CSS), navigation bars, and authentication views.
- **Deliverables:**
  - Common layout with header, role-based sidebar, user avatar, and notification badges.
  - Login Page, Registration Page (Patient registration with medical intake fields), and Forgot Password Page.
  - JWT token storage management (secure HTTP-only cookie or local storage with auto-refresh and logout redirection on 401).
- **Test Cases to Build:**
  - `AuthFlowUITest`: Test login form validation (empty fields, invalid email format); test successful login redirection to role-specific dashboard.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "feat(phase-5.1): UI design system, responsive layouts, and auth pages completed"
  ```
- **Acceptance Criteria:** Clean, modern responsive interface with seamless login and role-based redirect.

---

### 🔹 Sub-Phase 5.2: Dedicated Patient Dashboard UI
- **Objective:** Construct the 4 key sections of the Patient Dashboard specified in requirements.
- **Deliverables:**
  1. **Appointment Booking:** Step-by-step wizard (Select Department -> Select Doctor -> Pick Date & Slot -> Enter Reason -> Confirm).
  2. **Appointment History:** Data table of past and upcoming appointments with status badges (`PENDING`, `CONFIRMED`, `COMPLETED`, `CANCELLED`) and action buttons (Cancel, Reschedule, View Details).
  3. **Medical History:** Timeline/Card view of historical visits, diagnoses, prescriptions, and doctor notes.
  4. **Profile Management:** Profile form for editing personal details, contact information, and password.
  5. **Feedback Modal:** 5-star rating widget with text review for completed appointments.
- **Test Cases to Build:**
  - `PatientBookingFlowTest`: Test selecting doctor updates available slots dynamically; test submitting booking renders confirmation dialog.
  - `PatientProfileFormValidationTest`: Test form field validation for telephone and email inputs.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "feat(phase-5.2): patient dashboard UI (booking, history, records, profile) completed"
  ```
- **Acceptance Criteria:** All patient dashboard features function interactively with real-time API communication.

---

### 🔹 Sub-Phase 5.3: Dedicated Doctor Dashboard UI
- **Objective:** Construct the 4 key sections of the Doctor Dashboard specified in requirements.
- **Deliverables:**
  1. **Schedule Management:** Interactive weekly calendar view of scheduled appointments with slot toggle for managing availability.
  2. **Patient Records (EMR):** Searchable table of patient records with modal to view past history and record new diagnosis/prescription notes.
  3. **Appointment Overview:** List view of today's, upcoming, and past appointments with patient cards and action triggers (Confirm, Reschedule, Complete).
  4. **Patient Feedback:** Feedback card list showing patient reviews, comments, and star ratings with aggregate rating gauge.
- **Test Cases to Build:**
  - `DoctorScheduleCalendarTest`: Test calendar renders booked appointments in proper time slots.
  - `MedicalRecordModalTest`: Test adding prescription line items and saving updates patient records table.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "feat(phase-5.3): doctor dashboard UI (schedule calendar, EMR records, appointments, feedback) completed"
  ```
- **Acceptance Criteria:** Doctor dashboard allows seamless schedule inspection, patient record entry, and queue handling.

---

### 🔹 Sub-Phase 5.4: Dedicated Admin Dashboard UI
- **Objective:** Construct the 4 key sections of the Admin Dashboard specified in requirements.
- **Deliverables:**
  1. **User Management:** Tabular interface listing all accounts (Admin, Doctor, Patient) with search, filter, Add User modal, Edit modal, and Delete/Deactivate button.
  2. **Appointment Management:** Master oversight table displaying all clinic appointments with scheduling/rescheduling controls.
  3. **System Settings:** Config panel to edit clinic parameters, appointment booking windows, and notification toggles.
  4. **Performance Analytics:** Visual graphs and KPI cards (Total Patients, Today's Appointments, Active Doctors, Monthly Revenue/Trends, Doctor Utilization Chart).
- **Test Cases to Build:**
  - `AdminUserTableFilterTest`: Test searching users by name and filtering by role updates table rows.
  - `AnalyticsChartRenderTest`: Test KPI cards and charts render correctly with mocked analytics data.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "feat(phase-5.4): admin dashboard UI (user management, appointments, settings, analytics) completed"
  ```
- **Acceptance Criteria:** Admin dashboard gives complete oversight with smooth table management and analytics graphs.

---

## 🧪 Phase 6: Quality Assurance, Automated Testing & Security Hardening

### 🔹 Sub-Phase 6.1: Service & Domain Unit Testing Suite
- **Objective:** Attain >85% code coverage across all core business services.
- **Deliverables:**
  - Unit tests using JUnit 5 and Mockito for:
    - `UserServiceImplTest`, `PatientServiceImplTest`, `DoctorServiceImplTest`.
    - `AppointmentServiceImplTest` (comprehensive boundary tests: off-hours, overlapping slots, invalid cancellations).
    - `MedicalRecordServiceImplTest`, `FeedbackServiceImplTest`, `SystemSettingsServiceImplTest`.
- **Test Cases to Build:**
  - Negative tests: null pointer safeguards, boundary time values, non-existent entity IDs.
  - Positive tests: nominal paths with verified method calls on mocked repositories.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "test(phase-6.1): comprehensive service and domain unit test suite completed"
  ```
- **Acceptance Criteria:** `./mvnw test` executes all unit tests with 100% pass rate.

---

### 🔹 Sub-Phase 6.2: Repository & Database Integration Testing
- **Objective:** Validate database queries, indexes, constraints, and transactions.
- **Deliverables:**
  - `@DataJpaTest` test suite running against in-memory H2 / MySQL Testcontainer.
  - Verification of custom JPQL queries, pagination queries, and date-range queries.
- **Test Cases to Build:**
  - `AppointmentRepositoryIntegrationTest`: Validates finding appointments between dates and detecting overlapping slots.
  - `UserRepositoryIntegrationTest`: Validates unique constraints on user emails.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "test(phase-6.2): repository and database integration testing completed"
  ```
- **Acceptance Criteria:** All database queries execute successfully with expected SQL generation and constraint enforcement.

---

### 🔹 Sub-Phase 6.3: Web Tier & RBAC Security Integration Testing
- **Objective:** Test API endpoints, HTTP status codes, payload serialization, and role authorization using `MockMvc`.
- **Deliverables:**
  - `MockMvc` integration tests for every REST controller.
  - Matrix validation verifying:
    - Patients cannot hit `/api/doctor/**` or `/api/admin/**`.
    - Doctors cannot hit `/api/admin/**`.
    - Unauthenticated users receive 401 on protected endpoints.
- **Test Cases to Build:**
  - `AdminControllerSecurityTest`: Test admin endpoint returns 403 when called with Patient JWT token.
  - `PatientBookingApiTest`: Test booking endpoint validates request body and returns 201 Created with valid token.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "test(phase-6.3): web tier and RBAC security integration tests completed"
  ```
- **Acceptance Criteria:** All API endpoints respond with correct status codes and strictly enforce RBAC security.

---

### 🔹 Sub-Phase 6.4: Cross-Role End-to-End Workflow Testing
- **Objective:** Automate full user journeys traversing all three user types.
- **Deliverables:**
  - Automated workflow test simulating:
    1. Admin creates doctor account and sets working hours.
    2. Patient registers, discovers doctor, and books an appointment slot.
    3. Doctor logs in, views scheduled appointment, confirms it, and later records clinical notes + prescription upon completion.
    4. Patient logs in, checks updated medical history, and submits 5-star feedback.
    5. Admin reviews updated performance analytics showing completed appointment and satisfaction score.
- **Test Cases to Build:**
  - `FullHealthcareLifecycleE2ETest`: Executes the complete multi-role scenario sequentially using MockMvc or RestAssured.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "test(phase-6.4): cross-role end-to-end healthcare workflow tests completed"
  ```
- **Acceptance Criteria:** Complete lifecycle test completes from registration through feedback without errors.

---

### 🔹 Sub-Phase 6.5: Security Hardening & OWASP Compliance
- **Objective:** Audit and harden the application against OWASP Top 10 vulnerabilities.
- **Deliverables:**
  - Cross-Site Scripting (XSS) prevention with input HTML sanitization.
  - SQL Injection prevention verified via JPA parameterized queries.
  - CSRF protection configuration appropriate for stateless JWT APIs.
  - Rate limiting on authentication endpoints (`/api/auth/login`) using Bucket4j or custom sliding window interceptor to mitigate brute-force attacks.
  - Security headers configured (Content Security Policy, X-Frame-Options, X-Content-Type-Options, Strict-Transport-Security).
- **Test Cases to Build:**
  - `SecurityHardeningTest`: Test XSS script injection in notes/reasons is escaped/sanitized; test rate limiter locks out after 5 consecutive failed logins.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "sec(phase-6.5): OWASP security hardening, rate limiting, and sanitization completed"
  ```
- **Acceptance Criteria:** Security scanner / audit passes with zero critical or high vulnerabilities.

---

## 🚢 Phase 7: Packaging, Containerization & Production Deployment

### 🔹 Sub-Phase 7.1: Production Configuration, Spring Actuator & Health Checks
- **Objective:** Prepare production-ready application profiles, externalized environment configuration, and health monitoring.
- **Deliverables:**
  - `application-prod.yml` configured with environment variable bindings (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`).
  - Spring Boot Actuator enabled for `/actuator/health`, `/actuator/info`, `/actuator/metrics`.
  - Production logging configuration (`logback-spring.xml`) with rolling file appenders and JSON format for log aggregators.
- **Test Cases to Build:**
  - `ActuatorHealthCheckTest`: Test `/actuator/health` returns status `UP` including database connection check.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "chore(phase-7.1): production configurations, actuator health checks, and logging completed"
  ```
- **Acceptance Criteria:** Application starts cleanly under `prod` profile using environment variables and exposes health probes.

---

### 🔹 Sub-Phase 7.2: Interactive API Documentation with OpenAPI 3.0 / Swagger
- **Objective:** Provide a complete, interactive API documentation interface with JWT authentication support.
- **Deliverables:**
  - SpringDoc OpenAPI configuration with title, description, version, and security scheme (`bearerAuth`).
  - `@Operation`, `@ApiResponse`, and `@Parameter` annotations on all controllers.
  - Swagger UI accessible at `/swagger-ui/index.html` allowing testing of all endpoints directly in the browser.
- **Test Cases to Build:**
  - `OpenApiDocumentationTest`: Test `/v3/api-docs` returns valid OpenAPI 3.0 JSON specification containing all controllers and security schemes.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "docs(phase-7.2): OpenAPI 3.0 and interactive Swagger UI documentation completed"
  ```
- **Acceptance Criteria:** Swagger UI renders cleanly, permits JWT Bearer token authentication, and executes test calls against all APIs.

---

### 🔹 Sub-Phase 7.3: Docker Containerization & Docker Compose Multi-Container Orchestration
- **Objective:** Package the entire system into optimized Docker containers ready for one-command deployment.
- **Deliverables:**
  - Multi-stage `Dockerfile`:
    - Stage 1: Build JAR using Maven and Java 21 JDK.
    - Stage 2: Minimal JRE runtime image (e.g. `eclipse-temurin:21-jre-alpine`), non-root user execution, memory tuning flags.
  - `docker-compose.yml` orchestrating:
    - Service 1: `backend` (Spring Boot App, depends on database health).
    - Service 2: `db` (MySQL 8.0 with persistent volume `mysql_data`, health check configured with `mysqladmin ping`).
    - Service 3: `frontend` (Nginx serving UI and reverse-proxying API calls).
  - `.dockerignore` file.
  - Environment sample file `.env.example`.
- **Test Cases to Build:**
  - `ContainerSmokeTest`: Automated script verifying `docker compose up -d` starts all containers, waits for healthy status, and returns 200 from health endpoint.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "deploy(phase-7.3): multi-stage Dockerfile and docker-compose orchestration completed"
  ```
- **Acceptance Criteria:** Running `docker compose up --build -d` provisions the entire stack with zero manual steps.

---

### 🔹 Sub-Phase 7.4: CI/CD Pipeline (GitHub Actions)
- **Objective:** Automate build, test execution, code quality checks, and Docker image build on every commit/push.
- **Deliverables:**
  - `.github/workflows/ci.yml`:
    - Triggers on push and pull request to `main`.
    - Steps: Checkout code -> Setup Java 21 -> Run Maven unit & integration tests -> Build Docker image -> Upload test reports.
- **Test Cases to Build:**
  - Pipeline verification: Local validation using `act` or triggering initial GitHub Actions workflow run.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "ci(phase-7.4): GitHub Actions CI/CD automated build and test pipeline completed"
  ```
- **Acceptance Criteria:** GitHub Actions workflow executes and shows green badge on GitHub repository.

---

### 🔹 Sub-Phase 7.5: Production Deployment Guide, Seed Data & Final Verification
- **Objective:** Finalize deployment documentation, database seed scripts (default Admin, test Doctors, test Patients), and release verification.
- **Deliverables:**
  - `data-seed.sql` script with initial clinic settings, 1 Admin account, 3 sample Doctors (with specializations and schedules), and 2 sample Patients.
  - `DEPLOYMENT.md` guide containing step-by-step instructions for:
    - Local development setup.
    - Docker Compose production launch.
    - Cloud deployment instructions (AWS EC2 / Render / DigitalOcean / Railway).
    - Backup and restore procedures for MySQL data volume.
  - Final end-to-end smoke test report.
- **Test Cases to Build:**
  - `ProductionSeedVerificationTest`: Test application boots with seed script, verifies default Admin credentials work, and sample doctors are bookable.
- **Git Commit Command:**
  ```bash
  git add .
  git commit -m "docs(phase-7.5): seed data, deployment runbook, and final release verification completed"
  ```
- **Acceptance Criteria:** Entire system is verified production-ready, fully documented, and clone-to-run in under 3 minutes.

---

## 📊 Master Progress Tracking Matrix

| Phase | Sub-Phase | Title | Status | Commit Reference |
| :---: | :---: | :--- | :---: | :--- |
| **1** | **1.1** | Project Skeleton & Build Automation | `[x] Completed` | `feat(phase-1.1): ...` |
| **1** | **1.2** | Relational Database Modeling & Migration | `[ ] Pending` | `feat(phase-1.2): ...` |
| **1** | **1.3** | JPA Entities & Repository Layer | `[ ] Pending` | `feat(phase-1.3): ...` |
| **1** | **1.4** | Security Architecture, JWT & RBAC | `[ ] Pending` | `feat(phase-1.4): ...` |
| **1** | **1.5** | Unified API Contract, DTOs & Exceptions | `[ ] Pending` | `feat(phase-1.5): ...` |
| **2** | **2.1** | Patient Profile Management & Security | `[ ] Pending` | `feat(phase-2.1): ...` |
| **2** | **2.2** | Doctor Discovery & Availability Directory | `[ ] Pending` | `feat(phase-2.2): ...` |
| **2** | **2.3** | Appointment Booking Engine (Conflict Safe) | `[ ] Pending` | `feat(phase-2.3): ...` |
| **2** | **2.4** | Patient Appointment History & Actions | `[ ] Pending` | `feat(phase-2.4): ...` |
| **2** | **2.5** | Patient Medical History & Records Portal | `[ ] Pending` | `feat(phase-2.5): ...` |
| **2** | **2.6** | Patient Feedback & Doctor Rating System | `[ ] Pending` | `feat(phase-2.6): ...` |
| **3** | **3.1** | Doctor Schedule & Availability Setup | `[ ] Pending` | `feat(phase-3.1): ...` |
| **3** | **3.2** | Doctor Appointment Queue & Transitions | `[ ] Pending` | `feat(phase-3.2): ...` |
| **3** | **3.3** | Doctor Medical Records & EMR Entry | `[ ] Pending` | `feat(phase-3.3): ...` |
| **3** | **3.4** | Doctor Feedback & Reviews Viewer | `[ ] Pending` | `feat(phase-3.4): ...` |
| **4** | **4.1** | Admin User Management Console | `[ ] Pending` | `feat(phase-4.1): ...` |
| **4** | **4.2** | Admin Master Appointment Oversight | `[ ] Pending` | `feat(phase-4.2): ...` |
| **4** | **4.3** | Admin System Settings Configuration | `[ ] Pending` | `feat(phase-4.3): ...` |
| **4** | **4.4** | Performance Analytics & Reporting Engine | `[ ] Pending` | `feat(phase-4.4): ...` |
| **5** | **5.1** | UI Design System, Layouts & Auth Pages | `[ ] Pending` | `feat(phase-5.1): ...` |
| **5** | **5.2** | Patient Dashboard UI | `[ ] Pending` | `feat(phase-5.2): ...` |
| **5** | **5.3** | Doctor Dashboard UI | `[ ] Pending` | `feat(phase-5.3): ...` |
| **5** | **5.4** | Admin Dashboard UI | `[ ] Pending` | `feat(phase-5.4): ...` |
| **6** | **6.1** | Service & Domain Unit Testing Suite | `[ ] Pending` | `test(phase-6.1): ...` |
| **6** | **6.2** | Repository & Database Integration Testing | `[ ] Pending` | `test(phase-6.2): ...` |
| **6** | **6.3** | Web Tier & RBAC Security Testing | `[ ] Pending` | `test(phase-6.3): ...` |
| **6** | **6.4** | Cross-Role End-to-End Workflow Tests | `[ ] Pending` | `test(phase-6.4): ...` |
| **6** | **6.5** | Security Hardening & OWASP Compliance | `[ ] Pending` | `sec(phase-6.5): ...` |
| **7** | **7.1** | Production Config, Actuator & Logging | `[ ] Pending` | `chore(phase-7.1): ...` |
| **7** | **7.2** | OpenAPI 3.0 & Swagger UI Documentation | `[ ] Pending` | `docs(phase-7.2): ...` |
| **7** | **7.3** | Docker & Docker Compose Containerization | `[ ] Pending` | `deploy(phase-7.3): ...` |
| **7** | **7.4** | CI/CD Pipeline (GitHub Actions) | `[ ] Pending` | `ci(phase-7.4): ...` |
| **7** | **7.5** | Production Seed Data & Deployment Guide | `[ ] Pending` | `docs(phase-7.5): ...` |

---

## 🎯 Verification & Sub-Phase Execution Rules
For every sub-phase:
1. Implement the specified code and configurations.
2. Implement and execute the designated automated test cases.
3. Validate acceptance criteria.
4. Mark the sub-phase status in this file as `[x] Completed`.
5. Execute the git commit command prescribed for that sub-phase.
