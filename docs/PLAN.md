# Master Martini Chile – Implementation Plan (EP2, DSY1105)

Agreed during the planning interview on 2026-10-29 and **reconciled with the approved PRD (`issues/prd.md`)**. If this plan and the PRD disagree, the PRD wins. App UI text is in Spanish; this plan is in English.

> Academic project for DSY1105 (Duoc UC). Not officially affiliated with Master Martini Chile.

---

## 1. Context and scope

B2B app for Master Martini Chile (Unigrà subsidiary): customers (bakeries, gelaterias, food service, retail) browse a catalog organized by product family and register **quote requests** instead of buying through a cart. Everything is local; nothing is sent to the company.

### Scope tiers (about 3 days of work)

| Tier | Contents |
|---|---|
| **Must ship** | Register/login (local), catalog list + detail from Room, quote form, contact form, camera (profile photo), location (address autofill), form animations, Validators + PasswordHasher + RegisterViewModel tests, README |
| **Should ship** | Search + 4-category filter chips, favorites, screen transitions, remaining animations |
| **Cut first** | Notifications, product images (use category icons), "Mis cotizaciones" history, gallery photo picker, dark theme |

Never cut: forms, validation, MVVM, Room, both native resources, technical commits.

Deferred to the EFT: sending quotes to the company (email or backend), more ViewModel tests, Hilt.

---

## 2. Architecture

- **Pattern:** MVVM. Composable screens → ViewModel (`StateFlow<UiState>`) → Repository (interface) → Room DAO / DataStore.
- **DI:** manual `AppContainer` created in `MasterMartiniApp : Application`; ViewModels built with `viewModelFactory { initializer { … } }`. No Hilt.
- **Package:** `com.example.mastermartini` (kept).
- **SDK levels:** **minSdk 26** (raised from 24; needed by `PBKDF2WithHmacSHA256` and `java.time`), compileSdk/targetSdk 37.
- **Stack:** Kotlin 2.4.21 (raised from 2.2.10 in T1), AGP 9.5, Compose BOM, Material 3, Room 2.8.5 + KSP 2.3.12, Navigation Compose 2.10 with type-safe routes (kotlinx-serialization plugin), DataStore Preferences, lifecycle-viewmodel-compose / lifecycle-runtime-compose, play-services-location, material-icons-extended, Coil. Test-only: JUnit, kotlinx-coroutines-test. Update outdated `core-ktx`, `lifecycle`, `activity-compose`.

### Package structure

```
com.example.mastermartini
├── MasterMartiniApp.kt              // Application, owns AppContainer
├── MainActivity.kt
├── di/AppContainer.kt
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt           // Room DB + seed callback
│   │   ├── SeedData.kt              // 18 products + demo user
│   │   ├── Converters.kt            // ProductCategory, LocalDate
│   │   ├── entity/                  // UserEntity, ProductEntity, FavoriteEntity,
│   │   │                            // QuoteItemEntity, QuoteRequestEntity, QuoteRequestItemEntity
│   │   └── dao/                     // UserDao, ProductDao, FavoriteDao, QuoteDao
│   ├── session/SessionManager.kt    // DataStore: logged-in userId
│   └── repository/                  // UserRepository(+Impl), ProductRepository(+Impl), QuoteRepository(+Impl)
├── model/                           // ProductCategory enum, domain models, QuoteItemWithProduct
├── viewmodel/                       // AuthGate, Login, Register, Catalog, ProductDetail, Quote, Contact, Profile
├── ui/
│   ├── components/                  // ValidatedTextField, DateField, LoadingButton, ProductCard, CategoryChips,
│   │                                // FavoriteButton, EmptyState, ErrorBanner, PermissionRationaleDialog, SuccessDialog
│   ├── screens/{auth,catalog,quote,contact,profile}/
│   └── theme/
├── navigation/                      // Routes.kt, AppNavHost.kt, BottomBar.kt
└── util/
    ├── validation/                  // Validators.kt, ValidationResult.kt, FieldState.kt
    ├── security/PasswordHasher.kt   // PBKDF2WithHmacSHA256 + random salt
    ├── permissions/PermissionHandler.kt
    └── location/LocationHelper.kt   // fused location + Geocoder with fallback
```

---

## 3. Navigation (type-safe routes)

| Route | Screen | Bottom bar |
|---|---|---|
| `AuthGate` (start) | Reads the DataStore session → `Catalog` or `Login` | No |
| `Login` | Email + password, link to Register; generic error on wrong credentials | No |
| `Register` | Name, email, phone, password, confirm password; duplicate-email error | No |
| `Catalog` | Search, category chips, product list, empty state when nothing matches, "Descargar catálogo" (opens website) | Yes |
| `ProductDetail(id: Long)` | Detail, ♥ favorite, "Agregar a cotización" (**+1, no quantity selector**) | No (back arrow) |
| `Quote` | Draft items (edit qty / remove) or empty state with "Ir al catálogo"; quote form always visible | Yes (badge with item count) |
| `Contact` | Contact form + call / WhatsApp / map / email actions with mastermartini.cl details | Yes |
| `Profile` | Camera photo, user data (read-only), "Mis favoritos", disclaimer, logout | Yes |

Rules:
- A successful login/register navigates to `Catalog` with `popUpTo(0)` so Back cannot return to auth.
- Logout clears the session and navigates to `Login` with `popUpTo(0)`.
- Tabs use `launchSingleTop` + `restoreState`.

---

## 4. Data model (Room, schema v1)

| Entity | Fields | Notes |
|---|---|---|
| `UserEntity` | id, fullName, email (unique index), phone, passwordHash, salt, photoUri? | PBKDF2 hash, never plain text |
| `ProductEntity` | id, name, family, category (enum), description, presentation | Seeded on DB create |
| `FavoriteEntity` | userId, productId | Composite PK, FKs with cascade |
| `QuoteItemEntity` | userId, productId, quantity | Draft quote. "Agregar" inserts with qty 1, or +1 if present (cap 999) |
| `QuoteRequestEntity` | id, userId, businessName, rut, deliveryDate, address, lat?, lng?, createdAt | Saved on submit |
| `QuoteRequestItemEntity` | requestId, productId, productName, quantity | Copy of the draft at submit time; draft then cleared (one `@Transaction`) |

Rules:
- A quote needs **at least one item**. With an empty draft, submit validates every field and shows the banner "⚠ Agrega al menos un producto"; nothing is saved.
- Successful submit shows **"Solicitud de cotización registrada"** (never "enviada"). Nothing is sent to the company.
- Contact messages are not stored: the form validates and then opens an email intent pre-filled with the message (`ACTION_SENDTO`).

### Seed catalog (18 products, descriptions written by us)

| Category | Products |
|---|---|
| Panadería y Pastelería (8) | Mantequilla laminada, Margarina para hojaldre, Melange para masas, Crema vegetal para batir, Mix bizcochuelo, Mix pan integral, Glaseado espejo chocolate, Glaseado espejo frutilla |
| Gelatería (4) | Base para helado de crema, Pasta de avellana, Pasta de pistacho, Cobertura de chocolate |
| Food Service (3) | Base para panna cotta, Mix mousse de chocolate, Gelatina neutra |
| Retail (3) | Crema vegetal 1 L, Mix brownie, Avellanas tostadas |

Demo user: `demo@mastermartini.cl` with a test password stored only in `SeedData.kt` and the README.

### Company contact details

Phone, WhatsApp, email and address come from the public Contacto page of mastermartini.cl. They are collected during the build and **approved by the developer before going into `strings.xml`**. Anything not published becomes a clearly marked placeholder. Never invented.

---

## 5. Validation (centralized)

- `Validators` is a pure Kotlin `object` with no Android imports. Each function returns `ValidationResult.Valid | Invalid(message)`.
- `FieldState(value, error, touched)` lives inside each `UiState` (`LoginUiState`, `RegisterUiState`, `QuoteUiState`, `ContactUiState`), exposed as `StateFlow` from the ViewModel.
- **When errors show:** validate on every change; show the error only after the field loses focus once (`touched`); Submit touches all fields. The Submit button is never disabled.
- **Repository-level errors** go to the right field or a banner: duplicate email → email field; wrong credentials → generic "Correo o contraseña incorrectos" (does not say which).

| Rule | Spec |
|---|---|
| name | ≥ 3 chars, letters and spaces only |
| email | regex |
| password | ≥ 8 chars, ≥ 1 letter and ≥ 1 digit |
| confirmPassword | equals password |
| phoneCL | `+56 9 XXXX XXXX` or `9XXXXXXXX` |
| rut | format (`12.345.678-5` / `12345678-5`) + modulus-11 check digit (incl. `K`) |
| businessName | ≥ 3 chars |
| deliveryDate | required, ≥ tomorrow, not a Sunday |
| address | ≥ 5 chars |
| quantity | 1–999 |
| message | 10–500 chars |

`ValidatedTextField`: leading icon for the field type; trailing ✓ (green) / ⚠ (red); animated border color; `AnimatedVisibility` error text; password visibility toggle; keyboard type and IME action.

**Delivery date:** a read-only `DateField` with a 📅 icon that opens a Material 3 `DatePickerDialog`. `SelectableDates` blocks today, past days and Sundays. The chosen date still goes through `Validators.deliveryDate` so the field shows ✓/⚠; submitting without a date shows "Selecciona una fecha de entrega".

Forms: **Login, Register, Quote, Contact** (plus per-item quantity fields in Cotización).

---

## 6. Native resources (2 + security)

**Camera (Profile photo)**
- `CAMERA` declared and requested at runtime → `TakePicture` into a `FileProvider` URI under `filesDir/profile/` → `photoUri` saved on the user; shown with Coil.
- Denied once → rationale dialog with retry. Permanently denied → snackbar "Abrir ajustes" (app settings intent). No camera app → error message, no crash.

**Location (Quote address)**
- 📍 trailing icon on the address field → request FINE + COARSE (approximate-only grant accepted) → `FusedLocationProviderClient.getCurrentLocation` → `Geocoder` → address. If the geocoder returns nothing, fill in `"lat, lng"` so the user can edit it.
- GPS off → message "Activa la ubicación" + location settings intent. Loading spinner replaces the icon; 10-second timeout.

**Security:** PBKDF2 password hashing with salt, session in DataStore, photo in app-private storage, no storage permissions.

Shared `PermissionHandler` in `util/permissions` for both flows.

---

## 7. Animations

| # | Animation | Priority |
|---|---|---|
| 1 | Field border/icon `animateColorAsState` + `AnimatedVisibility` error text | Keep |
| 2 | Submit button → spinner via `AnimatedContent` | Keep |
| 3 | Favorite ♥ spring scale "pop" + color | Keep |
| 4 | "Agregar a cotización" → "✓ Agregado" / "✓ Cantidad actualizada" + quote tab badge bounce | Keep |
| 5 | NavHost slide/fade transitions; fade between tabs | Keep |
| 6 | Catalog `animateItem()` on filter + animated chip selection | Cut 2nd |
| 7 | Quote success dialog with check icon spring-scaling in | Cut 3rd |
| 8 | Quote item removal: shrink + fade (`AnimatedVisibility`; the items share a scrolling `Column` with the form, so `animateItem()` does not apply) | Cut 1st |

---

## 8. Testing

| Test | Type | Cases |
|---|---|---|
| `ValidatorsTest` | JUnit | Every rule: valid, invalid and boundary values; RUT check digits incl. `K`; date today/tomorrow/Sunday; phone formats; quantity 0/1/999/1000 |
| `PasswordHasherTest` | JUnit | Same password + salt → same hash; different salts → different hashes; wrong password fails verification |
| `RegisterViewModelTest` | JUnit + kotlinx-coroutines-test, fake `UserRepository` | Empty submit touches all fields and shows errors; valid data calls `register`; duplicate email → error on email field; success emits "navigate to Catálogo" |

No instrumented/UI tests in EP2. The manual demo script in §11 is the acceptance test.

---

## 9. Task list (ordered by dependency)

Each task is one or more small commits with technical messages (`build:`, `feat(scope):`, `fix:`, `refactor:`, `test:`, `docs:`).

| # | Task | Depends on |
|---|---|---|
| T1 | `git init`; add `.kotlin/` to `.gitignore`; minSdk 26; dependencies (Room+KSP, Navigation, serialization, DataStore, lifecycle-compose, location, icons, Coil, coroutines-test); clean `assembleDebug` | – |
| T2 | Theme: colors from mastermartini.cl, typography, wordmark, Spanish `strings.xml` | T1 |
| T3 | Package skeleton, `MasterMartiniApp`, `AppContainer` | T1 |
| T4 | Entities, DAOs, converters, `AppDatabase`, seed callback (18 products), schema export to `app/schemas` | T3 |
| T5 | Repositories (interfaces + Room implementations), `SessionManager` | T4 |
| T6 | `Validators`, `ValidationResult`, `FieldState` + `ValidatorsTest` | T1 |
| T7 | `Pbkdf2PasswordHasher` implementing the `PasswordHasher` interface from T5 + `PasswordHasherTest`; wire `UserRepository` into `AppContainer`; seed the demo user (moved here from T4) | T1, T4, T5 |
| T8 | `ValidatedTextField`, `DateField`, `LoadingButton`, `EmptyState`, `ErrorBanner` | T2, T6 |
| T9 | Routes, `AppNavHost`, `BottomBar`, `AuthGate`, placeholder screens, transitions | T3, T5 |
| T10 | Login + Register (ViewModels + screens; duplicate-email and generic login errors) + `RegisterViewModelTest` | T5, T7, T8, T9 |
| T11 | Catalog: list, search, category chips, empty state, "Descargar catálogo" | T5, T9 |
| T12 | Product detail: favorite toggle, add to quote (+1, cap 999, "✓ Cantidad actualizada") | T11 |
| T13 | Quote: draft items, empty state + banner, form with DatePicker, submit transaction, "Solicitud de cotización registrada", badge | T8, T12 |
| T14 | Location: `PermissionHandler`, `LocationHelper`, address autofill | T13 |
| T15 | Contact: collect mastermartini.cl details **and get developer approval**, then form + email/call/WhatsApp/map intents | T8, T9 |
| T16 | Profile: user data, favorites list, logout, disclaimer | T10, T12 |
| T17 | Camera: FileProvider, permission flows, photo saved and shown | T14 (`PermissionHandler`), T16 |
| T18 | Remaining animations (6–8) | T11, T13 |
| T19 | Polish pass: empty states, IME actions, accessibility labels, rotation sanity check | all |
| T20 | README.md + delivery checklist | all |

Order of the must-ship path: T1 → T3 → T4 → T5 → T6/T7 → T2 → T8 → T9 → T10 → T11 → T12 → T13 → T14 → T15 → T16 → T17 → T20.

---

## 10. Trello cards (unassigned)

Lists: **Backlog · En progreso · En revisión · Hecho**. Labels: `UI`, `Validación`, `Persistencia`, `Nativo`, `Animación`, `Tests`, `Docs`.

1. Configurar dependencias, minSdk 26 y build (Room, KSP, Navigation, DataStore) – `Persistencia`
2. Tema visual y strings en español – `UI`
3. Base de datos Room + seed de catálogo – `Persistencia`
4. Repositorios y sesión con DataStore – `Persistencia`
5. Validadores centralizados + tests unitarios – `Validación` `Tests`
6. Hash de contraseñas PBKDF2 + tests – `Validación` `Tests`
7. Componentes ValidatedTextField, DateField, EmptyState – `UI` `Validación` `Animación`
8. Navegación, AuthGate y barra inferior – `UI` `Animación`
9. Pantallas de Login y Registro + test de RegisterViewModel – `UI` `Validación` `Tests`
10. Catálogo con búsqueda, filtros y estado vacío – `UI`
11. Detalle de producto, favoritos y agregar a cotización – `UI` `Persistencia` `Animación`
12. Formulario de cotización, selector de fecha y registro – `Validación` `Persistencia`
13. Autocompletar dirección con ubicación – `Nativo`
14. Datos de contacto reales + formulario de contacto e intents – `Validación` `Nativo`
15. Perfil, favoritos y cerrar sesión – `UI`
16. Foto de perfil con cámara – `Nativo`
17. Animaciones de lista y diálogo de éxito – `Animación`
18. README y checklist de entrega – `Docs`

Each card: a checklist, the related branch/PR (if used), and the person who actually did it.

---

## 11. Definition of done per rubric item

| Rubric item (weight) | Done when | Demo |
|---|---|---|
| UI + navigation (15%) | 4 tabs + auth + detail; consistent theme; empty states; no dead ends; Back works everywhere; Spanish strings | Login → browse → detail → quote → contact → profile → logout |
| Forms with visual validation (15%) | Login, Register, Quote, Contact: every field has icon, ✓/⚠, animated border, message | Submit an empty form, then fix fields one by one |
| Centralized validation (10%) | All rules in `Validators`; ViewModels own `StateFlow<UiState>`; no rule in a composable; `ValidatorsTest` and `RegisterViewModelTest` pass | Show `Validators.kt`, a ViewModel and the test run |
| Animations (10%) | Items 1–5 working (6–8 if time) | Favorite pop, add-to-quote badge, transitions, errors appearing |
| Modularity + Room (15%) | Package structure above; repositories behind interfaces; data survives app restart | Add favorite/quote, kill the app, reopen |
| GitHub + Trello (20%) | Technical commit messages; real authorship; Trello board with the cards above | Commit history + board |
| 2+ native resources (15%) | Camera + location work including denied, permanently denied and GPS-off paths | Deny once, then allow; emulator location set to Santiago |

### Demo script (manual acceptance test)

1. Register with empty fields → all errors; fix them one by one → ✓ icons.
2. Duplicate email → error on email. Wrong password → generic error.
3. Browse, search (incl. no results), filter, open detail, favorite, add to quote twice ("✓ Cantidad actualizada").
4. Quote: submit empty → banner; edit quantities; pick a date; location autofill (allow; then deny / GPS off); invalid RUT; submit → "Solicitud de cotización registrada", draft empty.
5. Contact: validation, then the email app opens pre-filled. **Do not tap "Enviar"** (it is addressed to the real company).
6. Profile: camera (deny once → rationale; allow → photo), favorites, logout → Back does not return.
7. Kill and reopen the app → session, favorites, draft and photo persist.

---

## 12. Delivery checklist

The unchecked items are for the team: they need the team, real accounts or the AVA platform.

- [x] `./gradlew assembleDebug` and `./gradlew testDebugUnitTest` pass on a clean clone (32 tests)
- [x] README.md: description, disclaimer, features per rubric item, architecture, permissions, run steps, demo credentials, minSdk 26
- [ ] README.md: replace the placeholders (student names and roles, GitHub link, Trello link)
- [ ] Public GitHub repo; teacher and every member have access
- [ ] Trello board public or shared with the teacher, link in README
- [ ] **Repo frozen before the deadline** – any change after delivery means a 1.0 for everyone
- [ ] Source zip for the group AVA link excludes `build/`, `.gradle/`, `.kotlin/`, `.idea/`, `local.properties`; uploaded by one member
- [ ] **Every member activates their individual AVA link** (name, team number, app name)
- [ ] Emulator (API 26+, with Play Services) prepared: location set to Santiago, camera available; demo rehearsed for each rubric item
