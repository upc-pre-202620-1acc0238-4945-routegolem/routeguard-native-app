<h1 align="center">RouteGuard Native App</h1>

<p align="center">  
  Aplicación móvil nativa construida con <b>Modern Android Development (MAD)</b>, <b>Jetpack Compose</b>, <b>Clean Architecture</b> y <b>Domain-Driven Design (DDD)</b> para la gestión y monitoreo telemático en tiempo real del transporte escolar.
</p>

<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
  <a href="https://developer.android.com/index.html"><img alt="Platform" src="https://img.shields.io/badge/Platform-Android-green.svg"/></a>
  <a href="https://android-arsenal.com/api?level=24"><img alt="API" src="https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat"/></a>
  <a href="https://kotlinlang.org/"><img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-100%25-orange.svg"/></a>
  <a href="https://developer.android.com/jetpack/compose"><img alt="Compose" src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg"/></a>
</p>

---

## 🚀 Tech Stack & Modern Android Development (MAD)

La arquitectura de la aplicación se alinea estrictamente a las recomendaciones oficiales de **Modern Android Development (MAD)** promovidas por Google:

* **Lenguaje & Asincronía:**
  * **100% Kotlin** con enfoque idiomático y funcional.
  * **Kotlin Coroutines & Flow:** Manejo reactivo de flujos de datos asíncronos en tiempo real mediante `StateFlow` y `collectAsStateWithLifecycle()`.
* **Interfaz de Usuario (Declarative UI):**
  * **Jetpack Compose:** Sistema de diseño declarativo con soporte para temas dinámicos (Claro/Oscuro) en Material 3.
  * **Navigation Compose:** Navegación tipada de extremo a extremo utilizando rutas `@Serializable` de Kotlin Serialization.
* **Inyección de Dependencias (DI):**
  * **Hilt / Dagger:** Inyección de dependencias modular y desacoplada en el ciclo de vida de la aplicación (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`).
* **Persistencia Local (Offline-First):**
  * **Room Database:** Base de datos relacional SQLite offline-first procesada mediante **KSP (Kotlin Symbol Processing)** para máxima velocidad de compilación y optimización de bytecode.
* **Integraciones Telemáticas & Servicios en la Nube:**
  * **Mapbox Maps SDK v11:** Renderizado de cartografía vectorial, rutas y paradas georreferenciadas.
  * **Firebase Cloud Messaging (FCM):** Notificaciones Push de alta prioridad para avisos de abordaje y alertas de pánico.
  * **Stripe Billing / Checkout:** Pasarela de pago nativa con `PaymentSheet` oficial y flujo seguro de `PaymentIntent`.
  * **Retrofit & OkHttp:** Comunicación cliente-servidor con manejo de interceptores de sesión JWT.

---

## 🏛️ Arquitectura & Bounded Contexts

El código fuente se organiza bajo los principios de **Clean Architecture** y **Domain-Driven Design (DDD)**, estructurado en cuatro capas concéntricas por cada Bounded Context:

```text
pe.edu.upc.routeguard/
├── core/                               # Núcleo transversal (Base de datos, red, sesión, tema)
├── identityaccessmanagement/           # IAM: Autenticación, registro y control de roles
├── fleetroutemanagement/               # Gestión de rutas escolares, paradas y vehículos
├── stakeholderassetmanagement/         # Gestión de conductores, apoderados y estudiantes
├── tripexecutionmonitoring/            # Ejecución telemática de viajes, telemetría GPS y Room offline
├── notificationscommunication/         # Notificaciones push nativas, FCM y alertas
└── subscriptionplanmanagement/         # Planes de suscripción y pasarela de pago Stripe
```

Cada módulo encapsula:
1. **Domain:** Entidades de negocio, Value Objects y contratos de repositorio (`Repository Interfaces`).
2. **Application:** Casos de uso atómicos (`UseCases`).
3. **Infrastructure:** Implementaciones concretas de API (`Retrofit`), DAOs (`Room`) y adaptadores de hardware/servicios (GPS, Mapbox, FCM).
4. **Presentation:** Pantallas reactivas (`@Composable`), estados de UI inmutables (`UiState`) y ViewModels con arquitectura MVI/MVVM.

---

## ⚙️ Configuración del Entorno & Compilación

### Requisitos Previos
* Android Studio Iguana / Koala / Ladybug o superior.
* JDK 17 o JDK 21 configurado en el entorno de Gradle.
* Android SDK 34/35/37 (Android 7.0 Nougat mínimo, target Android 15/17).

### Archivo de Configuración Local (`local.properties`)
Para ejecutar la aplicación localmente sin exponer credenciales sensibles en Git, define las siguientes claves en tu archivo `local.properties`:

```properties
sdk.dir=C\:\\Users\\<tu-usuario>\\AppData\\Local\\Android\\Sdk
API_BASE_URL=https://routeguard-web-services.onrender.com/api/v1/
MAPBOX_ACCESS_TOKEN=pk.eyJ1IjoieW91ci11c2VyIiwiYSI6InlvdXItdG9rZW4ifQ...
STRIPE_PUBLISHABLE_KEY=pk_test_...
```

### Comandos de Compilación & Calidad
```bash
# Compilar código Kotlin y KSP
./gradlew compileDebugKotlin

# Ejecutar la suite de pruebas unitarias
./gradlew testDebugUnitTest

# Auditoría oficial de calidad de código de Google
./gradlew lintDebug

# Ensamblar el APK de depuración
./gradlew assembleDebug
```

---

## 👥 Equipo de Desarrollo (RouteGolem - UPC)

* **Marcelo Fausto Pareja Calloapaza** - *Lead Architect & Frontend Developer*
* **Mathias De la Cruz** - *Backend & DevOps Engineer*
* **Jhony Manuel Francia Torres** - *UX/UI Designer & QA Engineer*
* **Nickolas Ramirez Ruíz** - *Mobile Developer & Documentation Lead*