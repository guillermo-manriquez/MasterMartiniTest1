# Team plan: rebuilding Master Martini in 3 parts

Practice for the final test: each person rebuilds their own module in a shared team repository, on their own branch, from their own GitHub account, with pull requests reviewed by the others. The existing app (this repository) is the **reference**: read it to understand a solution, then write your version yourself. Don't copy whole files; the test checks that you can build it.

| | Area | Native resource | Trello cards |
|---|---|---|---|
| **Persona 1** | Datos y cuentas: setup, Room, repositories, session, password hashing, login and register | — | 1, 3, 4, 6, 9 |
| **Persona 2** | Catálogo y cotización: navigation, catalog, detail, favorites, quote form | Location | 8, 10, 11, 12, 13, 17 |
| **Persona 3** | UI, validación, contacto y perfil: theme, validators, form components, contact, profile | Camera | 2, 5, 7, 14, 15, 16, 18 |

---

## 1. Trello board setup

- **Lists:** Backlog · En progreso · En revisión · Hecho
- **Labels:** `UI`, `Validación`, `Persistencia`, `Nativo`, `Animación`, `Tests`, `Docs`
- Add all three members and the teacher. Assign each card to its owner below.
- Move a card to **En progreso** when you create its branch, to **En revisión** when you open the pull request, and to **Hecho** when it's merged.
- Paste the pull request link in the card.

## 2. Git workflow

1. One person creates the team repository on GitHub (empty) and adds the others as collaborators. Protect `main`: require a pull request and 1 approval.
2. For each card: `git switch main`, `git pull`, `git switch -c <branch from the card>`.
3. Commit small, technical messages in the imperative, e.g. `feat(auth): add PBKDF2 password hasher`, `test(validation): cover RUT check digit K`.
4. Push and open a pull request into `main`. Description: what it does, how you tested it, screenshot if it's UI.
5. **Another member reviews**: runs it, comments, approves. The author merges.
6. After a merge, everyone runs `git pull` on `main` and rebases or merges `main` into their open branch.

Before every pull request: `./gradlew assembleDebug testDebugUnitTest` passes.

## 3. Order (dependencies)

Work in phases so nobody waits long. Cards in the same phase can run in parallel.

| Phase | Persona 1 | Persona 2 | Persona 3 |
|---|---|---|---|
| **1. Base** | Card 1 (setup) — **merge first** | Card 8 (navigation with placeholder screens) | Card 2 (theme, strings) · Card 5 (validators + tests) |
| **2. Data and components** | Card 3 (Room + seed) · Card 4 (repositories, session) · Card 6 (hashing + tests) | — start reading Room and the catalog reference | Card 7 (form components) |
| **3. Screens** | Card 9 (login, register + test) | Card 10 (catalog) · Card 11 (detail, favorites) | Card 14 (contact) |
| **4. Features** | Review and help | Card 12 (quote) · Card 13 (location) | Card 15 (profile, logout) · Card 16 (camera) |
| **5. Finish** | Demo rehearsal | Card 17 (list and dialog animations) | Card 18 (README) |

Persona 1 has fewer cards because phase 1–2 blocks everyone else: get cards 1, 3 and 4 merged early, then review the other two's pull requests and lead the demo rehearsal.

---

## 4. Cards

Paths are relative to `app/src/main/java/com/example/mastermartini/` unless they start with `app/` or `docs/`.

### Persona 1 — Datos y cuentas

**Card 1 · Configurar dependencias, minSdk 26 y build** — `Persistencia`
Branch `feature/project-setup`
- [ ] New Compose project, package `com.example.mastermartini`, minSdk 26, compileSdk/targetSdk 37
- [ ] Add Room + KSP, Navigation Compose + kotlinx-serialization, DataStore, lifecycle-viewmodel-compose, play-services-location, material-icons-extended, Coil, kotlinx-coroutines-test (reference: `gradle/libs.versions.toml`, `app/build.gradle.kts`)
- [ ] `.gitignore` excludes `build/`, `.gradle/`, `.kotlin/`, `local.properties`
- [ ] Empty package folders: `data/`, `di/`, `model/`, `navigation/`, `ui/`, `util/`, `viewmodel/`
- **Done when:** `assembleDebug` passes and the empty app runs.

**Card 3 · Base de datos Room + seed de catálogo** — `Persistencia`
Branch `feature/room-database`
- [ ] Entities in `data/local/entity/`: user (unique email), product, favorite, quote item, quote request + its items
- [ ] DAOs in `data/local/dao/`, `Converters.kt` for `LocalDate`
- [ ] `AppDatabase.kt` with a create callback that inserts the 18 products and the demo user (`SeedData.kt`)
- [ ] Schema exported to `app/schemas/`
- **Done when:** after a fresh install, the database file contains 18 products (check with App Inspection → Database Inspector).

**Card 4 · Repositorios y sesión con DataStore** — `Persistencia`
Branch `feature/repositories-session`
- [ ] Domain models in `model/`, mappers in `data/repository/Mappers.kt`
- [ ] `UserRepository`, `ProductRepository` (accent-insensitive search, category filter, favorites), `QuoteRepository` (add or increment, quantity 1–999, submit draft) — interface + Room implementation each
- [ ] `data/session/SessionManager.kt` with DataStore (save, read, clear the logged-in user id)
- [ ] `di/AppContainer.kt` creates everything; `MasterMartiniApp.kt` owns it
- **Done when:** the other two can get repositories from `AppContainer` in a ViewModel factory.

**Card 6 · Hash de contraseñas PBKDF2 + tests** — `Validación` `Tests`
Branch `feature/password-hashing`
- [ ] `util/security/PasswordHasher.kt` interface, `Pbkdf2PasswordHasher.kt` (PBKDF2-HMAC-SHA256, 120,000 iterations, 16-byte random salt, constant-time compare)
- [ ] `PasswordHasherTest`: hash doesn't contain the password, same salt verifies, different salts differ, wrong password fails, RFC 7914 test vector
- [ ] Hashing runs on `Dispatchers.Default`, not the main thread
- **Done when:** tests pass; the repository stores hash + salt, never the password.

**Card 9 · Pantallas de Login y Registro + test de RegisterViewModel** — `UI` `Validación` `Tests`
Branch `feature/auth-screens` · needs cards 4, 6 (yours) and 5, 7 (Persona 3)
- [ ] `LoginViewModel`, `RegisterViewModel` with `StateFlow<UiState>` and a one-time navigation event
- [ ] `LoginScreen`, `RegisterScreen` using Persona 3's `ValidatedTextField`
- [ ] Duplicate email → error on the email field; wrong credentials → one generic message
- [ ] `RegisterViewModelTest` with a fake repository: empty submit, valid data, duplicate email, success navigates
- **Done when:** you can register, log out, log in again, and the session survives an app restart.

### Persona 2 — Catálogo y cotización

**Card 8 · Navegación, AuthGate y barra inferior** — `UI` `Animación`
Branch `feature/navigation`
- [ ] Type-safe routes in `navigation/Routes.kt`
- [ ] `AppNavHost.kt`: AuthGate decides Login or Catálogo; login and logout clear the back stack
- [ ] `BottomBar.kt` with 4 tabs (Catálogo, Cotización, Contacto, Perfil); each tab keeps its state
- [ ] Slide/fade transitions between screens, fade between tabs
- [ ] Placeholder screens until the real ones are merged
- **Done when:** you can move between all tabs and Back never returns to Login after logging in.

**Card 10 · Catálogo con búsqueda, filtros y estado vacío** — `UI`
Branch `feature/catalog` · needs card 4
- [ ] `CatalogViewModel`: search text + category combined with the repository flow
- [ ] `CatalogScreen`: search box, category chips, product count, cards, loading indicator, empty state with "Limpiar filtros"
- [ ] List animates when filters change (`animateItem()`)
- **Done when:** searching "pasteleria" (no accent) finds the "Mix pastelería" products; a nonsense search shows the empty state.

**Card 11 · Detalle de producto, favoritos y agregar a cotización** — `UI` `Persistencia` `Animación`
Branch `feature/product-detail`
- [ ] `ProductDetailViewModel` reads the product id from the route
- [ ] Favorite heart with a spring "pop" animation; saved in Room
- [ ] "Agregar a cotización" adds 1 or increments; button shows "Agregado" / "Cantidad actualizada"; snackbar "Ver cotización"
- [ ] Badge with the item count on the Cotización tab, bouncing when it grows
- **Done when:** favorites and the quote survive an app restart.

**Card 12 · Formulario de cotización, selector de fecha y registro** — `Validación` `Persistencia`
Branch `feature/quote-form` · needs card 7
- [ ] `QuoteViewModel`: lines with editable quantity (1–999), +/−, remove; form with company name, RUT, delivery date, address
- [ ] Date picker (Persona 3's `DateField`): from tomorrow, no Sundays
- [ ] Empty quote → banner "Agrega al menos un producto"; invalid fields show their errors
- [ ] Submit saves the request and clears the draft; dialog "Solicitud de cotización registrada"
- **Done when:** a registered request appears in the database and the tab is empty again.

**Card 13 · Autocompletar dirección con ubicación** — `Nativo`
Branch `feature/location`
- [ ] Location permissions in the manifest; `util/location/LocationProvider.kt` (current location with timeout + Geocoder in es-CL)
- [ ] Shared permission helper `util/permissions/PermissionHandler.kt` — **Persona 3 reuses it for the camera**, so merge it early
- [ ] Denied once → explanation dialog; denied permanently → snackbar "Abrir ajustes"; location off → "Activar"; no address found → coordinates
- **Done when:** every case above works on the emulator with location set to Santiago.

**Card 17 · Animaciones de lista y diálogo de éxito** — `Animación`
Branch `feature/quote-animations`
- [ ] Removed quote items shrink and fade out
- [ ] Check icon springs in on the confirmation dialog
- **Done when:** both are visible (tip: Developer options → Animator duration scale 10x).

### Persona 3 — UI, validación, contacto y perfil

**Card 2 · Tema visual y strings en español** — `UI`
Branch `feature/theme`
- [ ] `ui/theme/`: brand colors (green `#004D42`, gold `#B69F58`), light theme only, typography
- [ ] All texts in `res/values/strings.xml`, in Spanish
- [ ] Wordmark component `MasterMartiniWordmark` and the academic disclaimer text
- **Done when:** no hard-coded text in screens.

**Card 5 · Validadores centralizados + tests unitarios** — `Validación` `Tests`
Branch `feature/validators`
- [ ] `util/validation/Validators.kt` (pure Kotlin): name, email, phone CL, password + confirmation, RUT with check digit, company name, delivery date, address, quantity, message — rules table in the README
- [ ] `FieldState` (value, error, touched) and `ValidationResult`
- [ ] `ValidatorsTest`: valid, invalid and boundary values for every rule (RUT with `K`, date today/tomorrow/Sunday, quantity 0/1/999/1000)
- **Done when:** tests pass; no rule lives in a composable.

**Card 7 · Componentes ValidatedTextField, DateField, EmptyState** — `UI` `Validación` `Animación`
Branch `feature/form-components` · needs card 5
- [ ] `ValidatedTextField`: icon, animated border color, ✓ / ⚠ icon, animated error message, password toggle
- [ ] `DateField` with the Material 3 date picker; `LoadingButton` (text ↔ spinner); `EmptyState`; `ErrorBanner`
- [ ] Errors appear after leaving the field or submitting, not while typing the first time
- **Done when:** Persona 1 and 2 can use them in their forms (tell them when it's merged).

**Card 14 · Datos de contacto reales + formulario de contacto e intents** — `Validación` `Nativo`
Branch `feature/contact`
- [ ] `model/CompanyInfo.kt` with the public contact details from mastermartini.cl
- [ ] `ContactViewModel` + `ContactScreen`: name, email, phone, message (10–500 with counter), pre-filled from the logged-in user
- [ ] `util/ContactIntents.kt`: email draft, dialer, WhatsApp, map; message if no app can open it
- **Done when:** the email app opens with the message ready. **Never tap Enviar** — it goes to the real company.

**Card 15 · Perfil, favoritos y cerrar sesión** — `UI`
Branch `feature/profile`
- [ ] `ProfileViewModel` + `ProfileScreen`: avatar with initials, account data, favorites list, disclaimer card
- [ ] Logout with a confirmation dialog; clears the session and the back stack
- **Done when:** after logout, Back exits the app and reopening shows Login.

**Card 16 · Foto de perfil con cámara** — `Nativo`
Branch `feature/camera` · needs card 13's `PermissionHandler`
- [ ] `CAMERA` permission + `FileProvider` (`res/xml/file_paths.xml`) for `files/profile/`
- [ ] `util/camera/ProfilePhotoStore.kt`; `TakePicture` contract; photo shown with Coil
- [ ] Denied once → explanation; permanently → "Abrir ajustes"; no camera app → message
- **Done when:** the photo survives an app restart.

**Card 18 · README y checklist de entrega** — `Docs`
Branch `docs/readme`
- [ ] README in Spanish: description, disclaimer, team, features per rubric item, architecture, permissions, how to run, demo credentials
- [ ] Links to the repo and the Trello board
- [ ] Delivery checklist (`docs/PLAN.md` §12) reviewed with the team
- **Done when:** someone who never saw the project can run it from the README alone.

---

## 5. Before the test

- Each person explains their module to the other two (10 minutes each): data flow from screen to database, and one decision they made.
- Rehearse the demo script in `docs/PLAN.md` §11 together on one emulator.
- Each person can answer: where is validation done, why `StateFlow`, how the permission flow works, how data survives a restart.
