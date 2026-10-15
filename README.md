<div align="center">

  <img src="app/src/main/res/drawable-nodpi/brand_logo.png" width="120" alt="RouteGuard Logo" style="border-radius: 20%; margin-bottom: 12px;"/>

  <h1>🛡️ RouteGuard — Native Mobile App</h1>

  <p align="center">
    <strong>Plataforma móvil nativa de monitoreo telemático y seguridad para transporte escolar</strong><br>
    Construida con <b>Modern Android Development (MAD)</b>, <b>Jetpack Compose</b>, <b>Clean Architecture</b> y <b>Domain-Driven Design (DDD)</b>.
  </p>

  <p align="center">
    <a href="LICENSE"><img alt="License" src="https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge"/></a>
    <a href="https://developer.android.com/index.html"><img alt="Platform" src="https://img.shields.io/badge/Platform-Android_14%2B-green.svg?style=for-the-badge&logo=android"/></a>
    <a href="https://kotlinlang.org/"><img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-100%25-orange.svg?style=for-the-badge&logo=kotlin"/></a>
    <a href="https://developer.android.com/jetpack/compose"><img alt="Compose" src="https://img.shields.io/badge/UI-Jetpack_Compose-4285F4.svg?style=for-the-badge&logo=jetpackcompose"/></a>
    <a href="https://stripe.com/"><img alt="Stripe" src="https://img.shields.io/badge/Payments-Stripe_Checkout-635BFF.svg?style=for-the-badge&logo=stripe"/></a>
  </p>

</div>

<br/>

<hr style="border: 0; height: 1px; background: #e0e0e0; margin: 24px 0;" />

## 📱 Descripción del Proyecto

**RouteGuard** es una solución integral diseñada para brindar tranquilidad y trazabilidad en el transporte escolar diario. Permite a los **conductores** registrar el check-in/check-out de los estudiantes en cada parada de manera *offline-first*, y a los **padres de familia** recibir notificaciones push en tiempo real con geocercas inteligentes y seguimiento en vivo del vehículo.

<br/>

## 🚀 Matriz de Modern Android Development (MAD)

La aplicación sigue las directrices oficiales de **Google MAD**, evaluadas y auditadas mediante **Google Android Lint** con **0 errores de análisis estático**:

<table style="width: 100%; border-collapse: collapse; margin-top: 12px;">
  <thead>
    <tr style="background: linear-gradient(90deg, #0d47a1, #1976d2); color: white;">
      <th style="padding: 12px; text-align: left; border: 1px solid #ddd;">Pilar MAD</th>
      <th style="padding: 12px; text-align: left; border: 1px solid #ddd;">Tecnología Adoptada</th>
      <th style="padding: 12px; text-align: left; border: 1px solid #ddd;">Propósito & Beneficio Técnico</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td style="padding: 12px; border: 1px solid #ddd;"><b>💎 Lenguaje</b></td>
      <td style="padding: 12px; border: 1px solid #ddd;"><code>Kotlin 2.x</code> + <code>Coroutines</code> + <code>Flow</code></td>
      <td style="padding: 12px; border: 1px solid #ddd;">100% de la base de código libre de Java legacy. Programación reactiva con <code>StateFlow</code> en ciclo de vida consciente (<code>collectAsStateWithLifecycle</code>).</td>
    </tr>
    <tr style="background-color: #f9fbfd;">
      <td style="padding: 12px; border: 1px solid #ddd;"><b>🎨 UI Declarativa</b></td>
      <td style="padding: 12px; border: 1px solid #ddd;"><code>Jetpack Compose</code> + <code>Material 3</code></td>
      <td style="padding: 12px; border: 1px solid #ddd;">Eliminación total de vistas XML para pantallas. Diseño reactivo atómico con soporte nativo para Modo Oscuro y componentes reutilizables.</td>
    </tr>
    <tr>
      <td style="padding: 12px; border: 1px solid #ddd;"><b>🧭 Navegación</b></td>
      <td style="padding: 12px; border: 1px solid #ddd;"><code>Navigation Compose (Type-Safe)</code></td>
      <td style="padding: 12px; border: 1px solid #ddd;">Navegación fuertemente tipada con objetos y clases serializables (<code>@Serializable</code>), evitando strings mágicos propensos a errores.</td>
    </tr>
    <tr style="background-color: #f9fbfd;">
      <td style="padding: 12px; border: 1px solid #ddd;"><b>💉 Inyección (DI)</b></td>
      <td style="padding: 12px; border: 1px solid #ddd;"><code>Dagger Hilt 2.60</code></td>
      <td style="padding: 12px; border: 1px solid #ddd;">Inyección de dependencias modular que asegura inversión de control en repositorios, casos de uso y ViewModels desacoplados.</td>
    </tr>
    <tr>
      <td style="padding: 12px; border: 1px solid #ddd;"><b>💾 Persistencia</b></td>
      <td style="padding: 12px; border: 1px solid #ddd;"><code>Room 2.6</code> + <code>KSP</code></td>
      <td style="padding: 12px; border: 1px solid #ddd;">Soporte <b>Offline-First</b> para telemetría y check-in de estudiantes sin cobertura móvil, compilado ultra rápido mediante <b>Kotlin Symbol Processing</b>.</td>
    </tr>
    <tr style="background-color: #f9fbfd;">
      <td style="padding: 12px; border: 1px solid #ddd;"><b>🔔 Notificaciones</b></td>
      <td style="padding: 12px; border: 1px solid #ddd;"><code>Firebase Cloud Messaging (FCM)</code></td>
      <td style="padding: 12px; border: 1px solid #ddd;">Recepción en segundo plano de alertas críticas, avisos de arribo y eventos de abordaje en canales con prioridad alta.</td>
    </tr>
    <tr>
      <td style="padding: 12px; border: 1px solid #ddd;"><b>💳 Pasarela de Pago</b></td>
      <td style="padding: 12px; border: 1px solid #ddd;"><code>Stripe Android PaymentSheet</code></td>
      <td style="padding: 12px; border: 1px solid #ddd;">Procesamiento seguro de suscripciones empresariales en modo Sandbox con validación de tarjetas y flujo seguro de <code>PaymentIntent</code>.</td>
    </tr>
    <tr style="background-color: #f9fbfd;">
      <td style="padding: 12px; border: 1px solid #ddd;"><b>🗺️ Cartografía</b></td>
      <td style="padding: 12px; border: 1px solid #ddd;"><code>Mapbox Maps SDK v11</code></td>
      <td style="padding: 12px; border: 1px solid #ddd;">Visualización de rutas optimizadas, dibujo de polilíneas y ubicación en tiempo real de vehículos escolares.</td>
    </tr>
  </tbody>
</table>

<br/>

---

## 🏛️ Arquitectura por Bounded Contexts (DDD)

El proyecto implementa los conceptos de **Domain-Driven Design (DDD)** integrados con **Clean Architecture**:

<div align="center">
  <img src="https://raw.githubusercontent.com/upc-pre-202620-1acc0238-4945-routegolem/routeguard-report/main/resources/assets/images/chapter-3/mobile-prototyping.png" alt="Arquitectura y Flujo RouteGuard" width="800" style="border-radius: 8px; border: 1px solid #ddd;"/>
</div>

<br/>

<details open>
<summary><b>📂 Ver Estructura de Paquetes DDD</b></summary>

```text
pe.edu.upc.routeguard/
│
├── 📦 core/                               # Núcleo compartido del sistema
│   ├── database/                          # Instancia Room AppDatabase y migraciones
│   ├── designsystem/                      # Temas Material 3, tipografías, botones e iconos
│   ├── di/                                # Módulos Hilt transversales (Red, Local, Sesión)
│   ├── network/                           # Cliente Retrofit y adaptadores de error
│   └── session/                           # SessionManager con persistencia encriptada
│
├── 📦 identityaccessmanagement/           # Contexto: Autenticación, registro y roles (IAM)
├── 📦 fleetroutemanagement/               # Contexto: Trazado de rutas, paradas y flota vehicular
├── 📦 stakeholderassetmanagement/         # Contexto: Administración de apoderados, alumnos y choferes
├── 📦 tripexecutionmonitoring/            # Contexto: Ejecución de viaje, telemetría y sincronización offline
├── 📦 notificationscommunication/         # Contexto: Alertas Push (FCM), canales y avisos al padre
└── 📦 subscriptionplanmanagement/         # Contexto: Planes corporativos y pasarela de pago Stripe
```
</details>

<br/>

---

## ⚙️ Configuración del Entorno de Desarrollo

### 1. Variables de Entorno (`local.properties`)
Crea o edita el archivo `local.properties` en la raíz del proyecto para definir los tokens de acceso sin exponerlos en el repositorio de GitHub:

```properties
sdk.dir=C\:\\Users\\<tu-usuario>\\AppData\\Local\\Android\\Sdk
API_BASE_URL=https://routeguard-web-services.onrender.com/api/v1/
MAPBOX_ACCESS_TOKEN=pk.eyJ1IjoieW91ci11c2VyIiwiYSI6InlvdXItdG9rZW4ifQ...
STRIPE_PUBLISHABLE_KEY=pk_test_...
```

### 2. Comandos de Compilación & Verificación

<table>
  <thead>
    <tr style="background-color: #24292e; color: white;">
      <th style="padding: 8px; text-align: left;">Acción</th>
      <th style="padding: 8px; text-align: left;">Comando Gradle</th>
      <th style="padding: 8px; text-align: left;">Propósito</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td style="padding: 8px;"><b>Compilación Kotlin / KSP</b></td>
      <td style="padding: 8px;"><code>./gradlew compileDebugKotlin</code></td>
      <td style="padding: 8px;">Verifica la sintaxis, tipado y generación de código KSP/Hilt.</td>
    </tr>
    <tr>
      <td style="padding: 8px;"><b>Auditoría de Calidad</b></td>
      <td style="padding: 8px;"><code>./gradlew lintDebug</code></td>
      <td style="padding: 8px;">Ejecuta el análisis estático oficial de Google Android Lint.</td>
    </tr>
    <tr>
      <td style="padding: 8px;"><b>Pruebas Unitarias</b></td>
      <td style="padding: 8px;"><code>./gradlew testDebugUnitTest</code></td>
      <td style="padding: 8px;">Corre la suite de pruebas unitarias sobre casos de uso y DTOs.</td>
    </tr>
    <tr>
      <td style="padding: 8px;"><b>Generación de APK</b></td>
      <td style="padding: 8px;"><code>./gradlew assembleDebug</code></td>
      <td style="padding: 8px;">Empaqueta el ejecutable APK para pruebas en dispositivo o emulador.</td>
    </tr>
  </tbody>
</table>

<br/>

---

## 👥 Equipo de Desarrollo — RouteGolem (UPC)

<table align="center" style="border-collapse: collapse;">
  <tr align="center">
    <td width="25%">
      <img src="https://github.com/marc3lllob7.png" width="100px;" style="border-radius: 50%;" alt="Marcelo Pareja"/><br />
      <b>Marcelo Pareja</b><br />
      <sub>Lead Architect & Mobile Dev</sub>
    </td>
    <td width="25%">
      <img src="https://github.com/Dela050406.png" width="100px;" style="border-radius: 50%;" alt="Mathias De la Cruz"/><br />
      <b>Mathias De la Cruz</b><br />
      <sub>Backend & DevOps</sub>
    </td>
    <td width="25%">
      <img src="https://github.com/ManuelFT4.png" width="100px;" style="border-radius: 50%;" alt="Jhony Francia"/><br />
      <b>Jhony Francia</b><br />
      <sub>UX/UI & QA Engineer</sub>
    </td>
    <td width="25%">
      <img src="https://github.com/Bynickram02.png" width="100px;" style="border-radius: 50%;" alt="Nickolas Ramirez"/><br />
      <b>Nickolas Ramirez</b><br />
      <sub>Mobile Dev & Docs Lead</sub>
    </td>
  </tr>
</table>

<br/>

<div align="center">
  <sub>Desarrollado para el curso <b>Aplicaciones para Dispositivos Móviles (CC238)</b> — Universidad Peruana de Ciencias Aplicadas (UPC).</sub>
</div>