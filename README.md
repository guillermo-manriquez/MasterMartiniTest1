# Master Martini – App B2B de cotización de ingredientes

Aplicación Android para clientes empresa de **Master Martini Chile** (ingredientes para panadería, pastelería, gelatería y food service). Permite revisar el catálogo, guardar favoritos, armar una solicitud de cotización con dirección de despacho autocompletada por ubicación, contactar a la empresa y gestionar un perfil con foto tomada desde la cámara.

> **Aviso:** proyecto académico de la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles (Duoc UC)**, Evaluación Parcial 2. No está afiliado oficialmente a Master Martini Chile. Los datos de contacto son los públicos de [mastermartini.cl](https://mastermartini.cl/); el catálogo es una selección de ejemplo. Las solicitudes de cotización **se guardan solo en el dispositivo** y no se envían a la empresa.

## Equipo

| Integrante | Rol |
|---|---|
| _[Nombre Apellido]_ | _[rol]_ |
| _[Nombre Apellido]_ | _[rol]_ |
| _[Nombre Apellido]_ | _[rol]_ |

- Repositorio: _[enlace a GitHub]_
- Tablero Trello: _[enlace a Trello]_

## Funcionalidades por criterio de evaluación

### Interfaz y navegación
- Flujo de autenticación (Iniciar sesión / Crear cuenta) y cuatro pestañas: **Catálogo, Cotización, Contacto y Perfil**, más la pantalla de detalle de producto.
- Tema Material 3 con colores de la marca, textos en español y fechas en formato chileno (`dd/MM/aaaa`, selector de fecha en español).
- Estados vacíos con acción (catálogo sin resultados, cotización vacía, sin favoritos) e indicador de carga.
- La sesión se recuerda: al reabrir la app se entra directo al catálogo. Al cerrar sesión, "Atrás" no vuelve a la app.

### Formularios con validación visual
Login, Registro, Cotización y Contacto. Cada campo tiene ícono, borde y color animados, ícono ✓ o ⚠ y mensaje de error en español. Los errores aparecen al salir del campo o al intentar enviar, no mientras se escribe por primera vez.

| Campo | Regla |
|---|---|
| Nombre | Mínimo 3 letras; solo letras y espacios |
| Correo | Formato válido; no puede estar registrado (registro) |
| Teléfono | Celular chileno: `+56 9 1234 5678` o `912345678` (se ignoran espacios y guiones) |
| Contraseña | Mínimo 8 caracteres, con al menos una letra y un número; la confirmación debe coincidir (en el login solo se exige que no esté vacía) |
| Nombre de la empresa | Mínimo 3 caracteres |
| RUT empresa | `12.345.678-5` o `12345678-5`, con dígito verificador correcto (módulo 11, incluye `K`) |
| Fecha de entrega | Desde mañana, de lunes a sábado |
| Dirección de despacho | Mínimo 5 caracteres |
| Cantidad | Entero entre 1 y 999 |
| Mensaje (contacto) | 10 a 500 caracteres, con contador |

### Validación centralizada y estado
- Todas las reglas están en `util/validation/Validators.kt` (Kotlin puro, sin dependencias de Android). Ningún composable valida.
- Cada pantalla tiene un ViewModel que expone `StateFlow<UiState>`. Los eventos de una sola vez (navegar, abrir la app de correo) usan `Channel`.

### Animaciones
1. Campos: color del borde e ícono animados, mensaje de error que aparece y desaparece.
2. Botones de envío que cambian a un indicador de carga (`AnimatedContent`).
3. Corazón de favorito con efecto "pop" (resorte).
4. "Agregar a cotización" → "Agregado" / "Cantidad actualizada", y el contador de la pestaña Cotización rebota.
5. Transiciones entre pantallas (deslizar + desvanecer) y entre pestañas (desvanecer).
6. Lista del catálogo animada al buscar o filtrar.
7. Ícono ✓ que aparece con resorte al registrar una cotización.
8. Productos de la cotización que se encogen y desvanecen al quitarlos.

### Modularidad y persistencia (Room)
- Base de datos Room con usuarios, productos (18 precargados), favoritos, borrador de cotización y solicitudes registradas.
- Repositorios detrás de interfaces e inyección de dependencias manual (`di/AppContainer.kt`).
- Sesión guardada con DataStore. Favoritos, cotización, foto y sesión se mantienen al cerrar y reabrir la app.
- Contraseñas guardadas con hash **PBKDF2-HMAC-SHA256** (120.000 iteraciones, sal aleatoria de 16 bytes); nunca en texto plano.

### Recursos nativos
- **Ubicación:** en Cotización, el botón 📍 completa la dirección de despacho (ubicación actual + Geocoder). Si solo se obtienen coordenadas, se muestran para completarlas a mano.
- **Cámara:** en Perfil se toma la foto de perfil (guardada en el almacenamiento privado de la app mediante `FileProvider`).
- Ambos manejan permiso **denegado** (diálogo explicativo y reintento), **denegado permanentemente** (acceso directo a Ajustes) y casos sin servicio (ubicación desactivada → "Activar"; sin app de cámara → mensaje).
- **Intents:** correo prellenado, marcador telefónico, WhatsApp, mapa y sitio web de la empresa. Si no hay una app que los abra, se muestra un mensaje en vez de fallar.

## Arquitectura

**MVVM**: pantalla Compose → ViewModel (`StateFlow<UiState>`) → repositorio (interfaz) → Room DAO / DataStore.

```
com.example.mastermartini
├── MasterMartiniApp.kt        Application, crea el AppContainer
├── MainActivity.kt            Fuerza idioma es-CL y monta la navegación
├── data/
│   ├── local/                 Room: AppDatabase, entidades, DAOs, datos iniciales
│   ├── repository/            UserRepository, ProductRepository, QuoteRepository
│   └── session/               SessionManager (DataStore)
├── di/                        AppContainer (inyección manual)
├── model/                     Modelos de dominio, categorías, datos de la empresa
├── navigation/                Rutas tipadas, NavHost, barra inferior
├── ui/
│   ├── components/            Campos validados, selector de fecha, tarjetas, diálogos
│   ├── screens/               auth, catalog, quote, contact, profile
│   └── theme/                 Colores, tipografía, tema
├── util/
│   ├── validation/            Validators, FieldState
│   ├── security/              Hash PBKDF2
│   ├── permissions/           Flujo de permisos en tiempo de ejecución
│   ├── location/              Ubicación + Geocoder
│   ├── camera/                Archivos de foto de perfil
│   └── ContactIntents.kt      Correo, teléfono, WhatsApp, mapa
└── viewmodel/                 Un ViewModel por pantalla
```

**Tecnologías:** Kotlin 2.4, Jetpack Compose + Material 3, Navigation Compose (rutas tipadas con kotlinx-serialization), Room 2.8 + KSP, DataStore, Lifecycle/ViewModel, Google Play Services Location, Coil 3.

## Permisos

| Permiso | Uso | Cuándo se pide |
|---|---|---|
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Completar la dirección de despacho | Al tocar 📍 en Cotización (basta con ubicación aproximada) |
| `CAMERA` | Foto de perfil | Al tocar el botón de cámara en Perfil |

La app no pide acceso a internet. Mapas, correo, WhatsApp y el sitio web se abren en sus propias apps.

## Requisitos y ejecución

- Android Studio reciente con JDK 17 o superior.
- **minSdk 26 (Android 8.0)**, targetSdk 37.
- Emulador o dispositivo **con Google Play Services** (necesario para la ubicación). En el emulador: fijar la ubicación (por ejemplo, Santiago) en *Extended controls → Location* y tener cámara habilitada.

```bash
./gradlew assembleDebug        # compilar
./gradlew installDebug         # instalar en el emulador/dispositivo conectado
./gradlew testDebugUnitTest    # pruebas unitarias
```

También se puede abrir el proyecto en Android Studio y ejecutar la configuración `app`.

## Credenciales de demostración

| Correo | Contraseña |
|---|---|
| `demo@mastermartini.cl` | `Demo2026` |

También se puede crear una cuenta nueva desde "Crear cuenta". Las cuentas existen solo en el dispositivo.

## Pruebas unitarias

| Prueba | Qué cubre |
|---|---|
| `ValidatorsTest` | Cada regla: valores válidos, inválidos y límites (RUT con `K`, fecha hoy/mañana/domingo, teléfonos, cantidad 0/1/999/1000) |
| `PasswordHasherTest` | El hash no contiene la contraseña; misma sal verifica; cada registro usa otra sal; contraseña, sal o iteraciones incorrectas fallan; datos corruptos no provocan error; vector de prueba PBKDF2 del RFC 7914 |
| `RegisterViewModelTest` | Envío vacío muestra todos los errores sin llamar al repositorio; datos válidos se registran; correo duplicado marca el campo correo; registro exitoso inicia sesión y navega al catálogo; cambiar la contraseña revalida la confirmación |

## Fuera de alcance (EP2)

Envío real de cotizaciones a la empresa, historial de cotizaciones, imágenes de productos, tema oscuro, selector de fotos desde galería y notificaciones.
