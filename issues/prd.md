# PRD – Master Martini Chile B2B App (EP2, DSY1105)

Status: **Approved 2026-10-29.** This PRD is the source of truth; `docs/PLAN.md` was reconciled against it.
Sources: planning interview (2026-10-29), `docs/PLAN.md`, assignment PDF *Ev. Parcial 2_Estudiante_Encargo_DSY1105*, current project files.

All seven open questions (OPEN-1 to OPEN-7) were answered by the developer on 2026-10-29; their resolutions are recorded inline and listed at the end of the Implementation Decisions section.

> Academic project for DSY1105 (Duoc UC). Not officially affiliated with Master Martini Chile.

---

## Problem Statement

### Business problem
Master Martini Chile (a subsidiary of Unigrà, Italy) sells ingredients and ready-made pastry/dessert products **to businesses**: bakeries and pastry shops, gelaterias, food service and retail. Its website (mastermartini.cl) is mostly informational, organized as Nosotros, Panadería y Pastelería, Gelatería, Food Service, Retail and Contacto. Business customers have no mobile way to:

- browse the product range by product family (butter, margarine, melange, vegetable creams, pastry and bread mixes, mirror glazes, dessert bases, gelatin, hazelnuts, etc.);
- shortlist products they care about;
- request a price quote for several products with the business data a supplier needs (business name, RUT, delivery date, delivery address);
- contact the company quickly.

Because the business is B2B, customers do not buy through a cart and checkout; they ask for a **quote**.

### Academic problem
The app is the deliverable for **Evaluación Parcial 2** of DSY1105 (8% of the course grade). It must show a functional, well-structured Android app built with Kotlin, Jetpack Compose and MVVM, scored on:

| Rubric item | Weight |
|---|---|
| Coherent UI and navigation | 15% |
| Complete forms with per-field visual validation, messages and icons | 15% |
| Validation logic centralized and decoupled from the UI | 10% |
| Functional animations that give fluidity and feedback | 10% |
| Modular structure + local persistence | 15% |
| GitHub + Trello with real, distributed activity | 20% |
| At least two native device resources, integrated securely | 15% |

### Constraints
- About 3 days to delivery from the early hours of 2026-10-29 (Chile time).
- The developer is building the app locally first; how the team shares the work is the team's decision. The repo history must reflect who really wrote each piece (no fabricated or reassigned commits).
- Any change to the repo by any member after the delivery date gives everyone a 1.0. A missed group AVA upload or an unactivated individual AVA link also gives a 1.0.
- A defense follows the delivery (first Parcial 3 session), so every member must be able to explain the code.
- This is an intermediate delivery that will be extended for the final evaluation (EFT), so the architecture must be easy to extend.
- The assignment says pairs; the team has 3 people and is confirming this with the teacher.

---

## Solution

An Android app in Spanish, built with Kotlin, Jetpack Compose, Material 3 and MVVM, that works fully offline:

1. **Local accounts:** register and log in against a local Room database (passwords hashed with PBKDF2 plus a random salt); the session is kept in DataStore. Login is not on the real website, but the assignment requires it.
2. **In-app catalog:** 18 seeded products organized into the website's 4 categories, with search, category filters, product detail and per-user favorites. A secondary "Descargar catálogo" action opens the company website.
3. **Quote request:** products are added to a draft quote; the user adjusts quantities per item and completes a validated form (business name, RUT, delivery date, address). Submitting saves the request in Room and clears the draft.
4. **Contact:** a validated contact form that opens the user's email app with the message pre-filled, plus direct actions to call, WhatsApp and view the address.
5. **Native resources:** the camera takes the profile photo, and location fills in the quote's delivery address, both with full permission handling.
6. **Animations** wherever the user gets feedback: field validation, buttons, favorites, add-to-quote, navigation and lists.

---

## User Stories

Personas: **Comprador** (a buyer at a bakery, gelateria, food service or retail business) and **Evaluador** (the teacher grading the delivery).

### Accounts and session
1. As a buyer, I want to create an account with my name, email, phone and password, so that my favorites and quotes are mine.
2. As a buyer, I want each field to show immediately whether it is valid, with an icon and a clear message, so that I can fix mistakes before submitting.
3. As a buyer, I want to be told if my email is already registered, so that I log in instead of creating a duplicate.
4. As a buyer, I want to log in with my email and password and get a clear message if they are wrong, without being told which one was wrong.
5. As a buyer, I want to stay logged in when I reopen the app, so that I don't type my password every time.
6. As a buyer, I want to log out from my profile, and not be able to return to the app with Back after logging out.
7. As a buyer, I want to see and hide my password while typing it.

### Catalog
8. As a buyer, I want to browse products grouped by Panadería y Pastelería, Gelatería, Food Service and Retail, so that I find what applies to my business.
9. As a buyer, I want to search products by name or family, so that I find a product quickly.
10. As a buyer, I want to filter by category with chips and combine that with search.
11. As a buyer, I want to see an empty state when nothing matches my search, instead of a blank screen.
12. As a buyer, I want to open a product to see its family, description and presentation (e.g. "Caja 10 kg").
13. As a buyer, I want to mark products as favorites and see them in my profile, and the favorites should survive closing the app.
14. As a buyer, I want to open the company's full catalog on the website from the app.

### Quote
15. As a buyer, I want to add a product to my quote from its detail screen and get clear feedback that it was added and where it went; adding it again increases its quantity by 1.
16. As a buyer, I want to see how many products are in my quote from the bottom bar.
17. As a buyer, I want to change the quantity of each product or remove it from my quote.
18. As a buyer, I want to enter my business name, RUT, delivery date and delivery address, with every field validated (including the RUT check digit and a date no earlier than tomorrow, not on a Sunday).
19. As a buyer, I want to fill in the delivery address from my current location, so that I don't have to type it.
20. As a buyer, if location is denied, turned off, unavailable or can't be turned into an address, I want a clear message and still be able to type the address myself.
21. As a buyer, I want a clear confirmation that my quote request was registered, and my draft quote should be empty afterwards.
22. As a buyer, I want my draft quote to survive closing the app.
22a. As a buyer, if my quote has no products, I want to be told so and taken to the catalog, and I should not be able to submit it.

### Contact
23. As a buyer, I want to send a message with my name, email, phone and message, validated per field, through my email app.
24. As a buyer, I want to call, WhatsApp or see the company's address with one tap.

### Profile and camera
25. As a buyer, I want to take a profile photo with the camera, and have it remain after restarting the app.
26. As a buyer, if I deny camera permission I want to understand why it is needed and be able to retry; if I denied it permanently I want a shortcut to the app settings.
27. As a buyer, I want to see my data, my favorites and a note that this is an academic project.

### Evaluator
28. As the evaluator, I want every form to show per-field visual validation with icons and messages.
29. As the evaluator, I want to find all validation rules in one non-UI class with unit tests.
30. As the evaluator, I want to see a clear MVVM package structure and data that persists across app restarts.
31. As the evaluator, I want to see two native resources working, including their permission-denied paths.
32. As the evaluator, I want a README that explains the project, the students, the features, how to run it and demo credentials.
33. As the evaluator, I want technical commit messages and a Trello board that reflect who did each task.

---

## Implementation Decisions

### Platform and build
- Android Studio, Kotlin 2.4.21 (raised from the template's 2.2.10 in T1: current AndroidX/kotlinx releases ship Kotlin 2.4 metadata), AGP 9.5 (built-in Kotlin support, no `kotlin-android` plugin), Gradle 9.6, compileSdk/targetSdk 37.
- **minSdk 26** (raised from the template's 24; resolved OPEN-1). `PBKDF2WithHmacSHA256` and `java.time.LocalDate` need API 26. The rubric names no Android version; the demo runs on an emulator. Trade-off: the app cannot be installed on Android 7.x; if that is ever needed, switch back to 24 with core library desugaring and `PBKDF2WithHmacSHA1`.
- Package `com.example.mastermartini` is kept (decided).
- Dependencies to add: Room 2.8.5 + KSP 2.3.12 (no `room-ktx`: it is empty since Room 2.7), Navigation Compose 2.8+ with the kotlinx-serialization plugin, DataStore Preferences, `lifecycle-viewmodel-compose`, `lifecycle-runtime-compose`, `play-services-location`, `material-icons-extended`, Coil; test-only `kotlinx-coroutines-test`. Outdated `core-ktx` 1.10.1, `lifecycle` 2.6.1 and `activity-compose` 1.8.0 get updated.
- **The first implementation step is a clean `assembleDebug` with all dependencies added**, before any feature work. KSP/AGP 9 compatibility is the biggest technical risk.

### Architecture
- MVVM: Composable screen → ViewModel exposing `StateFlow<UiState>` → repository interface → Room DAO / DataStore.
- **Manual dependency injection** with an `AppContainer` owned by `MasterMartiniApp : Application`; ViewModels are created with `viewModelFactory { initializer { … } }`. No Hilt (trade-off: less automation, no build risk, easy to explain; can migrate for the EFT).
- Repositories (`UserRepository`, `ProductRepository`, `QuoteRepository`) are interfaces with Room implementations, so they can be faked in tests.
- Packages: `data` (local/entity, local/dao, session, repository), `model`, `viewmodel`, `ui` (components, screens, theme), `navigation`, `util` (validation, security, permissions, location), `di`.

### Navigation
- Type-safe routes (`@Serializable`).
- **AuthGate** start destination reads the session: logged in → Catálogo, otherwise → Login (no login flash for logged-in users).
- Auth flow without a bottom bar: Login ↔ Register. Successful login/register → Catálogo with `popUpTo(0)`.
- Main flow: bottom bar with 4 tabs, **Catálogo, Cotización (badge with item count), Contacto, Perfil**. Product detail is pushed above the tabs with a back arrow.
- Logout clears the session → Login with `popUpTo(0)`.
- A separate Nosotros/Home tab was offered and **not** chosen.

### Data model (Room, schema v1)
| Entity | Fields |
|---|---|
| User | id, fullName, email (unique), phone, passwordHash, salt, photoUri? |
| Product | id, name, family, category (enum of 4), description, presentation |
| Favorite | userId + productId (composite key) |
| QuoteItem (draft) | userId, productId, quantity |
| QuoteRequest | id, userId, businessName, rut, deliveryDate, address, lat?, lng?, createdAt |
| QuoteRequestItem | requestId, productId, productName, quantity (copy taken at submit) |

- Quantity is **per item**, not a single form field (changed from the original idea).
- The product detail screen has **no quantity selector** (resolved OPEN-3). "Agregar a cotización" inserts the product with quantity 1, or adds 1 if it is already in the draft (capped at 999), with feedback "✓ Agregado" / "✓ Cantidad actualizada". Quantities are edited and validated only in the Cotización tab.
- Submitting copies the draft items into `QuoteRequestItem` and clears the draft in one transaction. Nothing is sent to the company; the confirmation reads **"Solicitud de cotización registrada"** (resolved OPEN-2).
- **A quote needs at least one item** (resolved OPEN-4). With an empty draft, Cotización shows "Tu cotización está vacía" with an "Ir al catálogo" button above the form, which stays visible. Submitting still validates every field and also shows an error banner "⚠ Agrega al menos un producto"; nothing is saved.
- Contact messages are **not** stored; they go through an email intent.
- **Company contact details** (resolved OPEN-6): phone, WhatsApp, email and address come from the public Contacto page of mastermartini.cl, collected during the build and approved by the developer before they go into `strings.xml`. Anything the site does not publish becomes a clearly marked placeholder. Risk: the email intent is pre-addressed to the real company, so during demos the presenter must not tap "Enviar" in the email app.
- Seed: 18 products (8 Panadería y Pastelería, 4 Gelatería, 3 Food Service, 3 Retail) plus a demo user, inserted when the database is created. Product names follow the families on the Italian site; **descriptions are written by us**, not copied.

### Validation
- `Validators` is a pure Kotlin object with no Android imports, returning `ValidationResult.Valid | Invalid(message)`.
- Each form has a `UiState` made of `FieldState(value, error, touched)`, held in the ViewModel's `StateFlow`.
- Errors show **after a field loses focus for the first time**; Submit marks all fields as touched. The Submit button is never disabled.
- Rules: name ≥ 3 letters/spaces; email regex; password ≥ 8 with ≥ 1 letter and ≥ 1 digit; confirm password must match; Chilean mobile (`+56 9 XXXX XXXX` or `9XXXXXXXX`); RUT format + modulus-11 check digit; business name ≥ 3; delivery date ≥ tomorrow and not a Sunday; address ≥ 5; quantity 1–999; message 10–500.
- `ValidatedTextField` is the one shared field component: leading icon, trailing ✓/⚠, animated border color, animated error text, password visibility toggle.
- Forms in scope: **Login, Register, Quote, Contact** (plus per-item quantity).
- **Delivery date** (resolved OPEN-5): a read-only field with a 📅 icon that opens a Material 3 `DatePickerDialog`. `SelectableDates` blocks today, past days and Sundays. The chosen date is still passed through `Validators.deliveryDate`, so the field shows ✓/⚠ like the others; submitting without a date shows "Selecciona una fecha de entrega". Trade-off: past/Sunday errors cannot be triggered on screen, only in unit tests.

### Native resources and security
- **Camera:** `CAMERA` permission requested at runtime; `TakePicture` into a `FileProvider` URI in app-private storage; the URI is saved on the user. Handles denied (rationale + retry), permanently denied (settings shortcut) and no camera app (message, no crash). A gallery picker is **not** included.
- **Location:** fine + coarse permissions (approximate-only accepted); `FusedLocationProviderClient.getCurrentLocation`; `Geocoder` to an address; **if the geocoder returns nothing, fill in "lat, lng"** so the field stays editable; GPS-off message with a settings shortcut; spinner while loading; 10-second timeout. The demo sets the emulator location to Santiago beforehand.
- A shared `PermissionHandler` serves both.
- Security: PBKDF2 hash with salt, no plain-text passwords, session in DataStore, photo in app-private storage, no storage permissions.

### Visual identity and content
- Material 3 **light** theme with colors taken from mastermartini.cl; dark theme is a stretch goal.
- No copied company logo: a text wordmark "Master Martini" plus a Material icon.
- Disclaimer in the README and on Perfil.
- No product photos: category icons instead (images were cut first).
- All strings in Spanish in `strings.xml`.
- "Descargar catálogo" opens the company website (not a direct PDF link, which may break).

### Animations
Kept always: (1) field border/icon color and error text, (2) Submit → spinner, (3) favorite "pop", (4) "✓ Agregado" + badge bounce, (5) screen and tab transitions. Cut in this order if short on time: (8) quote item removal → (6) catalog filtering and chips → (7) success dialog.

### Scope priorities
- Must ship: register/login, catalog list + detail, quote form, contact form, camera, location, form animations, README.
- Should ship: search + filters, favorites, transitions, remaining animations.
- Never cut: forms, validation, MVVM, Room, both native resources, technical commits.

### Version control and delivery
- Local `git init` from the first step; small commits with technical messages (`build:`, `feat(scope):`, `fix:`, `refactor:`, `test:`, `docs:`), authored by whoever actually wrote the code. No remote is created or pushed without the developer's say-so.
- README in Spanish: description, disclaimer, student names (placeholders), features per rubric item, architecture, permissions, run steps, demo credentials.
- Delivery checklist (freeze, AVA zip without build output, group + individual AVA links) lives in `docs/PLAN.md`.

### Resolved questions
- ~~OPEN-1 – minSdk~~ → **Resolved: minSdk 26.**
- ~~OPEN-2 – What "sending" a quote means~~ → **Resolved: save locally only; confirmation says "Solicitud de cotización registrada" (never "enviada").** Sending to the company is deferred to the EFT.
- ~~OPEN-3 – Quantity on the detail screen~~ → **Resolved: no selector on detail.** "Agregar" adds 1; if already in the quote, +1 and feedback "✓ Cantidad actualizada". Quantities are edited only in Cotización.
- ~~OPEN-4 – Submitting an empty quote~~ → **Resolved: not allowed.** Empty state + "Ir al catálogo" above a still-visible form; submit validates the fields and shows a "⚠ Agrega al menos un producto" banner.
- ~~OPEN-5 – Delivery date input~~ → **Resolved: Material 3 DatePicker** in a read-only field; invalid days not selectable; `Validators.deliveryDate` still drives ✓/⚠.
- ~~OPEN-6 – Company contact data~~ → **Resolved: real public details from mastermartini.cl**, read during the build and shown to the developer for approval before use; anything not published becomes a marked placeholder. Never invented.
- ~~OPEN-7 – ViewModel tests~~ → **Resolved: one test class, `RegisterViewModelTest`, with a fake `UserRepository`.**

---

## Testing Decisions

- **Unit tests (JUnit, required):** every rule in `Validators`, covering valid, invalid and boundary values. Includes RUT check digits (including "K"), dates (today/tomorrow/Sunday), phone formats, password rules and quantity limits. `PasswordHasher`: same password + salt gives the same hash; different salts give different hashes; verification fails with the wrong password.
- **ViewModel tests (resolved OPEN-7):** one class, `RegisterViewModelTest`, using a fake `UserRepository` and `kotlinx-coroutines-test` (`StandardTestDispatcher` as Main). Cases: submit with empty fields marks every field touched and shows errors; valid data calls `register` on the repository; a duplicate email puts the error on the email field; success emits the "navigate to Catálogo" event. Other ViewModels are not unit-tested in EP2 (trade-off: time); the same pattern can be extended in the EFT.
- **Instrumented/UI tests:** not planned for EP2 (the template example tests stay or get removed).
- **Manual demo script** (also the acceptance test), one pass per rubric item:
  1. Register with empty fields → all errors; fix them one by one → ✓ icons.
  2. Duplicate email → error. Wrong password → generic error.
  3. Browse, search, filter, open detail, favorite, add to quote.
  4. Quote: edit quantities, location autofill (allow; then deny / GPS off), invalid RUT and date, submit → confirmation, draft empty.
  5. Contact: validation, then the email app opens pre-filled. **Do not tap "Enviar"** in the email app (it is addressed to the real company).
  6. Profile: camera (deny once → rationale; allow → photo), favorites, logout → Back does not return.
  7. Kill and reopen the app → session, favorites, draft and photo persist.
- **Build checks before delivery:** `./gradlew assembleDebug` and `./gradlew test` pass on a clean clone.
- **Device:** Android emulator with Play Services (for fused location), location set to Santiago, camera enabled.

---

## Out of Scope (EP2)

- Any backend, real authentication service or remote API; data is local only.
- Prices, payments, cart/checkout or order tracking (B2B quotes only).
- Sending the quote to Master Martini by email or backend (deferred to the EFT).
- "Mis cotizaciones" history screen (requests are stored but not listed).
- Push/local notifications.
- Product photos and hotlinked images; using the real company logo.
- Gallery photo picker.
- Dark theme (stretch goal only).
- Password reset, email verification, account lockout, profile editing beyond the photo.
- A Nosotros/Home screen.
- Hilt or other DI frameworks; multi-module Gradle setup.
- Per-person task assignment in this plan, and creating the GitHub repo or Trello board (the team's job).
- Copying the assignment PDF into the repo (not decided).
