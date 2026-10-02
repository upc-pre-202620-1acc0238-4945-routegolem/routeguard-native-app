# RouteGuard Mobile (Android)

Front-end de RouteGuard en Kotlin + Jetpack Compose, con **Clean Architecture + DDD**.
Cada Bounded Context del informe es un módulo bajo `features/` con las mismas cuatro capas:

```
features/<bounded-context>/
├── presentation/    Compose screens, ViewModels, UiState, NavGraph
├── application/     Use cases (+ puertos de aplicación)
├── domain/          Aggregates, entities, value objects, events, contratos de repositorio
└── infrastructure/  Retrofit (remote), Room (local), repositories, mappers, DI (Hilt)
```

| Paquete | Bounded Context | Rol que lo usa |
|---|---|---|
| `iam` | Identity & Access Management | Todos (sign in, registro de administrador) |
| `stakeholder` | Stakeholder & Asset Management | Administrador (conductores, padres, estudiantes, grupos) |
| `fleet` | Fleet & Route Management | Administrador (rutas, paradas, vehículo, días, activación) |
| `trip` | Trip Execution & Monitoring | Conductor (viaje, abordaje, GPS, offline sync, incidencias) |
| `notifications` | Notifications & Communication | Padre y conductor (alertas, pánico, avisos) |
| `subscription` | Subscription & Plan Management | Administrador (planes) |

`shared/domain` contiene el Shared Kernel (`BaseEntity`, `AggregateRoot`, `DomainEvent`, `BaseRepository`, `Coordinates`).
`core/` tiene red (OkHttp + JWT), sesión, base de datos Room, Hilt y el design system.

## Cómo correrlo todo en local

1. Backend + conexión del emulador (MySQL local debe estar corriendo):

   ```powershell
   powershell -ExecutionPolicy Bypass -File ..\run-local.ps1          # arranca el backend
   powershell -ExecutionPolicy Bypass -File ..\run-local.ps1 -ResetDb  # además reinicia los datos sembrados
   ```

   El script compila y levanta `routeguard-backend` en el puerto 8080. El emulador llega a tu PC con
   `http://10.0.2.2:8080/api/v1/` (la `BASE_URL` de `NetworkModule`), sin `adb`.
2. Abre `routeguard-mobile-android` en Android Studio y ejecuta la app (AGP 9.3.0) en el emulador. El backend debe estar
   corriendo en `http://localhost:8080` (perfil `http` de Rider o `dotnet run` en `RouteGuard.Platform`).
   Si el emulador no conecta, revisa que el Firewall de Windows permita el puerto 8080 de entrada.

Usuarios sembrados por el backend:

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | `admin@routeguard.pe` | `admin123` |
| Conductor | `driver@routeguard.pe` | `driver123` |
| Padre | `parent@routeguard.pe` | `parent123` |

Los conductores y padres nuevos los crea el administrador desde la app: se les genera una contraseña automática que se muestra una sola vez.

## Navegación por rol

Después del login, `MainShell` arma la barra inferior según el rol:
administrador (Personas, Rutas, Planes), conductor (Viaje, Alertas), padre (Alertas).

## Backend

`routeguard-backend` es una copia local del backend guía (ASP.NET Core + EF Core + MySQL, base `routeguard_mobile`).
Se agregó `Trip/Interfaces/Rest/TripTrackingController.cs` con lo que el móvil necesita y la guía no tenía:
`POST trips/{id}/locations`, `GET trips/{id}/locations/latest`, `POST trips/{id}/offline-sync`,
`POST trips/{id}/panic`, `POST trips/{id}/broadcast`, `GET trips/live` (monitor del administrador) y
`GET parents/{id}/active-trip` (seguimiento del padre), además de la geocerca de 500 m. Todo se guarda en la base según el diseño del report
(2.6.1.6.2 y 2.6.2.6.2): `location_records`, `waypoints` (paradas del viaje, PENDING/VISITED), `offline_sync_batches`,
`geofence_alerts`, `notification_templates` (textos es-PE con {placeholders}) y `device_tokens` (tokens FCM,
`POST users/{id}/device-tokens`). Los viajes tienen `cancelled_at` y las notificaciones `title`, `priority_level`,
`data_payload`. Diferencia con el diagrama: MySQL guarda latitud y longitud en columnas `double` en vez de `GEOMETRY(POINT)` de PostGIS.

Reglas del backend que la app respeta: una ruta solo se edita en borrador; hay que asignar el conductor antes que
los estudiantes; activar exige parada, vehículo, conductor, días y hora; el abordaje solo se registra con el viaje en curso.

## Mapas (Mapbox)

`RouteGuardMap` (`core/designsystem/components/map`) es el componente compartido: dibuja la ruta (paradas rotuladas y
trazado), vehículos, un punto elegido y los toques en el mapa, y encuadra todo el contenido ("Ver todo") o sigue al vehículo ("Seguir").

| Rol | Pantalla | Qué hace |
|---|---|---|
| Conductor | Viaje | Mapa con su posición, próxima parada con distancia y tiempo estimado, paradas visitadas, botón para navegar con Google Maps |
| Padre | Seguimiento | Dónde está el vehículo de su hijo, estado de cada hijo, próxima parada con distancia/ETA y antigüedad de la señal |
| Administrador | En vivo | Todos los viajes en curso en un mapa, con avance y velocidad de cada uno |
| Administrador | Rutas > detalle | Mapa de las paradas; se toca el mapa para elegir la ubicación de una parada nueva |

**APIs de Mapbox que usa la app** (mismo token; un cliente único en `core/network/mapbox`, sin enviar el JWT de RouteGuard):

| API | Para qué | Dónde |
|---|---|---|
| Directions | Trazado por calles y tiempo/distancia reales a la próxima parada (se consulta cada 20 s como máximo) | `MapboxAdapter` de Trip (puerto en `application`, `MapboxAdapterImpl` en `infrastructure`), mapas del conductor, padre y administrador |
| Geocoding v6 | Buscar una dirección al crear una parada y sugerir el nombre al tocar el mapa | Fleet (`AddressSearch`) |
| Optimization | "Sugerir mejor orden de paradas" (US-04), manteniendo fija la primera y la última; solo en rutas en borrador, hasta 12 paradas | Fleet (`RoutePlanner`) |

Si Mapbox no responde, los mapas usan líneas rectas y los tiempos estimados por distancia. Las pruebas unitarias
(`./gradlew :app:testDebugUnitTest`) cubren el mapeo de las respuestas de Mapbox y la lógica de progreso de ruta.

El backend evalúa la geocerca de 500 m: cuando el vehículo se acerca a una parada, los padres de la ruta reciben una
notificación ("El vehículo se acerca a la parada...", o "Llegada al colegio" si la parada se llama "Colegio...").

Necesita un **token público de Mapbox** (empieza con `pk.`; el SDK v11 ya no pide token secreto):

1. Crea una cuenta gratuita en mapbox.com y copia tu *Default public token*.
2. Agrégalo a `local.properties` (no se sube a git) y vuelve a compilar. Pégalo completo y una sola vez; ya trae el `pk.`:

   ```properties
   MAPBOX_ACCESS_TOKEN=pk.xxxxxxxxxxxxxxxx
   ```

   También se acepta como variable de entorno `MAPBOX_ACCESS_TOKEN`.

Sin token, la pantalla muestra un aviso en lugar del mapa y el resto funciona igual.

## Pendiente

- **Firebase (FCM)**: agregar el SDK y `google-services.json`; un `FirebaseMessagingService` que llame a
  `HandlePushMessageUseCase`. Hoy las notificaciones salen de la lista in-app y de la bandeja del sistema.
- **Pago**: no hay pasarela; el cobro es simulado (la suscripción se crea activa).
- `usesCleartextTraffic` está activo solo para desarrollo (HTTP).
