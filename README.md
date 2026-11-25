<div align="center">

  <img src="app/src/main/res/drawable-nodpi/brand_logo.png" width="120" alt="RouteGuard Logo" style="border-radius: 20%; margin-bottom: 12px;"/>

  <h1>🛡️ RouteGuard — Native Mobile App</h1>

  <p align="center">
    <strong>Plataforma móvil nativa de monitoreo telemático y seguridad para transporte escolar</strong><br>
    Construida con <b>Modern Android Development (MAD)</b>, <b>Jetpack Compose</b>, <b>Clean Architecture</b> y <b>Domain-Driven Design (DDD)</b>.
  </p>

  <p align="center">
    <a href="LICENSE"><img alt="License" src="https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge"/></a>
    <a href="https://developer.android.com/index.html"><img alt="Platform" src="https://img.shields.io/badge/Platform-Android_7.0%2B_(API_24--37)-green.svg?style=for-the-badge&logo=android"/></a>
    <a href="https://kotlinlang.org/"><img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.4.20-orange.svg?style=for-the-badge&logo=kotlin"/></a>
    <a href="https://developer.android.com/jetpack/compose"><img alt="Compose" src="https://img.shields.io/badge/UI-Jetpack_Compose-4285F4.svg?style=for-the-badge&logo=jetpackcompose"/></a>
    <a href="https://developer.android.com/training/data-storage/room"><img alt="Room" src="https://img.shields.io/badge/Storage-Room_2.6.1_Offline--First-3DDC84.svg?style=for-the-badge&logo=sqlite"/></a>
  </p>
  <p align="center">
    <a href="https://firebase.google.com/docs/cloud-messaging"><img alt="FCM" src="https://img.shields.io/badge/Push-Firebase_FCM-FFCA28.svg?style=for-the-badge&logo=firebase&logoColor=black"/></a>
    <a href="https://www.mapbox.com/"><img alt="Mapbox" src="https://img.shields.io/badge/Maps-Mapbox_v11.31.1-000000.svg?style=for-the-badge&logo=mapbox"/></a>
    <a href="https://stripe.com/"><img alt="Stripe" src="https://img.shields.io/badge/Payments-Stripe_SDK_21.4.1-635BFF.svg?style=for-the-badge&logo=stripe"/></a>
    <a href="https://developer.android.com/guide/topics/resources/localization"><img alt="i18n" src="https://img.shields.io/badge/i18n-ES_%2F_EN_Reactivo-00897B.svg?style=for-the-badge"/></a>
  </p>

</div>

<br/>

<hr style="border: 0; height: 1px; background: #e0e0e0; margin: 24px 0;" />

## 📱 Descripción del Proyecto

**RouteGuard** es una solución integral diseñada para brindar tranquilidad y trazabilidad en el transporte escolar diario para **Administradores**, **Conductores** y **Padres de Familia / Apoderados**. Permite planificar rutas y asignar personal, registrar el check-in/check-out de estudiantes en cada parada de manera *offline-first* (con rectificación/deshacer de abordaje y descenso grupal en colegio), emitir alertas de pánico/avisos, y ofrecer a los padres seguimiento GPS en vivo, llamada directa al conductor y notificaciones push con geocercas inteligentes.

<br/>

## 🚀 Matriz de Tecnologías & Modern Android Development (MAD)

La aplicación sigue las directrices oficiales de **Google MAD**:

<table style="width: 100%; border-collapse: collapse; margin-top: 12px;">
  <thead>
    <tr style="background: linear-gradient(90deg, #0d47a1, #1976d2); color: white;">
      <th style="padding: 12px; text-align: left; border: 1px solid #ddd;">Pilar / Capa</th>
      <th style="padding: 12px; text-align: left; border: 1px solid #ddd;">Tecnología Adoptada</th>
      <th style="padding: 12px; text-align: left; border: 1px solid #ddd;">Uso Real en la App</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td style="padding: 12px; border: 1px solid #ddd;"><b>💎 Lenguaje & Reactividad</b></td>
      <td style="padding: 12px; border: 1px solid #ddd;"><code>Kotlin 2.4.20</code> + <code>Coroutines</code> + <code>StateFlow</code></td>
      <td style="padding: 12px; border: 1px solid #ddd;">100% Kotlin con programación reactiva consciente del ciclo de vida (<code>collectAsStateWithLifecycle</code>) y <code>kotlinx.serialization</code>.</td>
    </tr>
    <tr style="background-color: #f9fbfd;">
      <td style="padding: 12px; border: 1px solid #ddd;"><b>🎨 UI Declarativa</b></td>
      <td style="padding: 12px; border: 1px solid #ddd;"><code>Jetpack Compose (BOM 2026.09.00)</code> + <code>Material 3</code></td>
      <td style="padding: 12px; border: 1px solid #ddd;">Pantallas 100% Compose con componentes reutilizables (<code>BrandHeader</code>, tarjetas elevadas, chips de estado) y asistente interactivo paso a paso.</td>
    </tr>
    <tr>
      <td style="padding: 12px; border: 1px solid #ddd;"><b>🧭 Navegación</b></td>
      <td style="padding: 12px; border: 1px solid #ddd;"><code>Navigation Compose (Type-Safe)</code></td>
      <td style="padding: 12px; border: 1px solid #ddd;">Rutas fuertemente tipadas con <code>@Serializable</code> por rol (<code>AppNavHost</code> y <code>MainShell</code>).</td>
    </tr>
    <tr style="background-color: #f9fbfd;">
      <td style="padding: 12px; border: 1px solid #ddd;"><b>💉 Inyección (DI)</b></td>
      <td style="padding: 12px; border: 1px solid #ddd;"><code>Dagger Hilt 2.60.1</code> + <code>KSP 2.3.6</code></td>
      <td style="padding: 12px; border: 1px solid #ddd;">Inyección de módulos de red, base local, Mapbox, sesión y ViewModels (<code>hilt-navigation-compose 1.4.0</code>).</td>
    </tr>
    <tr>
      <td style="padding: 12px; border: 1px solid #ddd;"><b>🌐 Red & API REST</b></td>
      <td style="padding: 12px; border: 1px solid #ddd;"><code>Retrofit 3.0.0</code> + <code>Gson</code> + <code>OkHttp</code></td>
      <td style="padding: 12px; border: 1px solid #ddd;">Consumo de endpoints REST (<code>routeguard-web-services</code>) con <code>AuthInterceptor</code> para inyección de JWT Bearer y detección de 401.</td>
    </tr>
    <tr style="background-color: #f9fbfd;">
      <td style="padding: 12px; border: 1px solid #ddd;"><b>💾 Persistencia Offline-First</b></td>
      <td style="padding: 12px; border: 1px solid #ddd;"><code>Room 2.6.1</code> (SQLite) + <code>SharedPreferences</code></td>
      <td style="padding: 12px; border: 1px solid #ddd;"><code>AppDatabase</code> (<code>routeguard.db</code>) almacena <code>TripEntity</code>, <code>WaypointEntity</code>, <code>LocationRecordEntity</code>, <code>BoardingRecordEntity</code> y <code>NotificationEntity</code> (DAOs <code>TripDao</code> y <code>NotificationDao</code>). <code>SessionManager</code> persiste JWT, rol, tutorial e idioma.</td>
    </tr>
    <tr>
      <td style="padding: 12px; border: 1px solid #ddd;"><b>🗺️ Cartografía & Rutas</b></td>
      <td style="padding: 12px; border: 1px solid #ddd;"><code>Mapbox Maps SDK v11.31.1</code> + <code>Directions / Optimization / Geocoding API</code></td>
      <td style="padding: 12px; border: 1px solid #ddd;"><code>RouteGuardMap</code> dibuja paraderos, polilíneas viales y flota en vivo; <code>MapboxClient</code> busca direcciones, calcula ETA vial y sugiere el orden óptimo de paradas.</td>
    </tr>
    <tr style="background-color: #f9fbfd;">
      <td style="padding: 12px; border: 1px solid #ddd;"><b>📍 GPS & Acciones Android</b></td>
      <td style="padding: 12px; border: 1px solid #ddd;"><code>LocationManager</code> (Foreground Service) + <code>ActivityResultContracts</code> + Intents</td>
      <td style="padding: 12px; border: 1px solid #ddd;"><code>TrackingForegroundService</code> transmite y guarda puntos GPS en segundo plano; navegación en Google Maps (<code>google.navigation:</code>), llamada telefónica (<code>ACTION_DIAL</code>) y selector de foto de perfil desde cámara o galería.</td>
    </tr>
    <tr>
      <td style="padding: 12px; border: 1px solid #ddd;"><b>🔔 Notificaciones Push</b></td>
      <td style="padding: 12px; border: 1px solid #ddd;"><code>Firebase Cloud Messaging (FCM 24.0.2)</code></td>
      <td style="padding: 12px; border: 1px solid #ddd;"><code>AppFirebaseMessagingService</code> y <code>FcmPushAdapter</code> procesan alertas críticas, avisos del conductor y eventos de geocerca por canales de prioridad.</td>
    </tr>
    <tr style="background-color: #f9fbfd;">
      <td style="padding: 12px; border: 1px solid #ddd;"><b>💳 Pasarela de Pago</b></td>
      <td style="padding: 12px; border: 1px solid #ddd;"><code>Stripe Android SDK 21.4.1</code> (<code>PaymentSheet</code>)</td>
      <td style="padding: 12px; border: 1px solid #ddd;">Procesamiento real de suscripciones institucionales en modo Sandbox mediante <code>PaymentIntent</code> y hoja de pago Stripe.</td>
    </tr>
    <tr>
      <td style="padding: 12px; border: 1px solid #ddd;"><b>🌍 Internacionalización (i18n)</b></td>
      <td style="padding: 12px; border: 1px solid #ddd;"><code>strings.xml</code> (ES/EN) + <code>LocaleHelper</code> + <code>LocalResources</code></td>
      <td style="padding: 12px; border: 1px solid #ddd;">Cambio reactivo ES ↔ EN en tiempo real desde Perfil sin reiniciar la actividad, con paridad verificada mediante prueba unitaria (<code>StringsXmlParityTest</code>).</td>
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
│   ├── database/                          # Instancia Room AppDatabase (routeguard.db)
│   ├── designsystem/                      # Temas Material 3, mapas Mapbox y asistente interactivo
│   ├── di/                                # Módulos Hilt transversales (NetworkModule, LocalModule, MapboxModule)
│   ├── localization/                      # LocaleHelper, AppStringsProvider y LocalizedException (ES/EN)
│   ├── network/                           # Cliente Retrofit, AuthInterceptor y MapboxClient
│   └── session/                           # SessionManager (JWT, rol, tutorial e idioma persistido)
│
├── 📦 navigation/                         # AppNavHost, MainShell y navegación multi-rol
├── 📦 identityaccessmanagement/           # Contexto: Login, registro admin, perfil, foto y cuidadores (IAM)
├── 📦 fleetroutemanagement/               # Contexto: Rutas, paradas en mapa, búsqueda y optimización de orden
├── 📦 stakeholderassetmanagement/         # Contexto: Conductores, padres, alumnos y grupos escolares
├── 📦 tripexecutionmonitoring/            # Contexto: Viaje conductor (Room + GPS Service), monitor flota y padre
├── 📦 notificationscommunication/         # Contexto: Alertas Push (FCM), pánico, avisos y geocercas
└── 📦 subscriptionplanmanagement/         # Contexto: Planes corporativos y pasarela Stripe PaymentSheet
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