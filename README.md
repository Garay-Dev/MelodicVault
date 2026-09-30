# Melodic Vault — Registro de cambios


## Cómo correr el proyecto

**Requisitos:** JDK 17, Maven, MySQL 8+, un IDE que importe proyectos Maven (Eclipse, IntelliJ, VS Code).

1. **Clonar el repo** y ubicarse en la carpeta del proyecto.

2. **Crear la base de datos primero** — la app no crea el esquema, lo espera ya creado. Abre `database/bd_melodicvault.sql`, copia su contenido en tu cliente de MySQL (Workbench, DBeaver, línea de comandos, el que uses) y ejecútalo completo. Esto crea `bd_melodicvault` con las 4 tablas (`banda`, `album`, `usuario`, `cancion`) y los datos semilla (3 bandas, 4 álbumes, 2 usuarios, 3 canciones).

3. **Configurar `application.properties`**: cambiar `spring.datasource.password` por tu contraseña local de MySQL. Confirmar que la URL diga `bd_melodicvault` en minúsculas (tiene que coincidir exacto con el nombre del paso 2).

4. **Importar el proyecto en el IDE** (si es la primera vez que lo abres): en Eclipse, File → Import → Maven → Existing Maven Projects → selecciona la carpeta clonada → Finish. La primera vez descarga las dependencias de Maven, necesita internet y puede tardar unos minutos.

5. **Ejecutar**: `MelodicVaultProject.java` → clic derecho → Run As → Java Application. Levanta en `http://localhost:8090`.

6. **Verificar que arrancó bien**: en la consola no debe haber errores de Hibernate ni de conexión a MySQL (con `show-sql=true` vas a ver el `SELECT`/`INSERT` de cada acción).

7. **Probar el login**, en `http://localhost:8090/login`:
   - `admin` / `admin123` → rol ADMIN, ve los botones de crear/editar/eliminar.
   - `lector` / `lector123` → rol USER, solo lectura (si intenta `/albumes/nuevo` a mano, le da 403).



Si el paso 5 falla con clases no encontradas (`ClassNotFoundException`, `NoClassDefFoundError`), casi seguro el paso 4 no terminó de descargar las dependencias — revisa el nodo "Maven Dependencies" en el árbol del proyecto; si tiene una X roja, clic derecho en el proyecto → Maven → Update Project. Si falla con `Unknown database` o `Table doesn't exist`, es que el paso 2 no se corrió o el nombre de la base en `application.properties` no coincide con el del script.

   

## Dependencias (`pom.xml`)
- Agregado `spring-boot-starter-security` (sin versión — hereda del BOM de `spring-boot-starter-parent`).
- Pendiente agregar: `thymeleaf-extras-springsecurity6` (necesaria para `sec:authorize` en las vistas).

## Entidades (`modelo`)
- **`Usuario.java`** (nueva) — `idUsuario`, `username` (único), `password` (hash BCrypt), `rol`.
- **`Cancion.java`** (nueva) — `idCancion`, `titulo` (`@NotBlank`, `@Size(max=100)`), `numeroPista` (`@NotNull`, `@Min(1)`), `duracionSegundos` (`@Min(0)`), `@ManyToOne` a `Album` (`@NotNull`).
- **`Album.java`** (modificada) — se agregó `@OneToMany(mappedBy = "album", cascade = CascadeType.REMOVE) @OrderBy("numeroPista ASC") List<Cancion> canciones`. Se usó `REMOVE` (no `ALL` + `orphanRemoval`) porque el formulario de editar álbum no envía canciones.
- **`Banda.java`** (modificada):
  - `albumes`: `fetch = FetchType.EAGER` → `FetchType.LAZY` (resuelve N+1).
  - Se quitó `orphanRemoval = true` (editar una banda podía borrarle los álbumes al llegar `null` desde el formulario).
  - `estado`: se agregó `@Size(max = 20)` para que coincida con la columna `VARCHAR(20)`.

## Repositorios (`interfaces`)
- **`IUsuario.java`** (nueva) — `findByUsername(String)`.
- **`ICancion.java`** (nueva) — CRUD estándar.
- **`IBanda.java`** (modificada) — se agregó `findWithAlbumesByIdBanda(Integer)` con `@EntityGraph(attributePaths = "albumes")`.
- **`IAlbum.java`** (modificada) — se agregó `estadisticasPorBanda()`, JPQL con join + `GROUP BY` + `COUNT`/`AVG` (nombre, cantidad de álbumes, rating promedio por banda).

## Servicios
- **`UsuarioDetailsService.java`** (nueva, `service`) — implementa `UserDetailsService` para Spring Security.
- **`CancionService.java`** + **`ICancionService.java`** (nuevas) — CRUD de canciones, mismo patrón que Álbum/Banda.
- **`AlbumTransaccionalService.java`** (nueva, `service`) — `registrarAlbumConCanciones(Album, List<Cancion>)` con `@Transactional` (Spring, no Jakarta); guarda álbum + canciones en una sola transacción, rollback si alguna pista es inválida.
- **`IAlbumService`/`AlbumService`** (modificadas) — se agregó `estadisticas()`, que expone `estadisticasPorBanda()`.

## Configuración
- **`SecurityConfig.java`** (nueva, `config`) — `@EnableWebSecurity`; bean `PasswordEncoder` (BCrypt); bean `SecurityFilterChain`:
  - Públicas: `/css/**`, `/img/**`, `/js/**`, `/`, `/acerca`, `/bandas`, `/albumes`, `/canciones`, `/*/detalle/**`.
  - Solo `ROLE_ADMIN`: `/*/nuevo`, `/*/editar/**`, `/*/guardar`, `/*/eliminar/**`, `/albumes/completo/**`.
  - `formLogin` en `/login`, `logout` con redirect a `/`.

## Controllers y DTO
- **`LoginController.java`** (nuevo) — `GET /login` → vista `login`.
- **`CancionController.java`** (nuevo) — CRUD completo (`/canciones`, `/nuevo`, `/editar/{id}`, `/guardar`, `/eliminar/{id}`). Mantenimiento #3.
- **`dto/AlbumCompletoForm.java`** (nuevo) — contenedor con `Album` + `List<Cancion>` para el formulario transaccional.
- **`AlbumCompletoController.java`** (nuevo, `/albumes/completo`) — `GET /nuevo` (arma 5 filas vacías de canciones) y `POST /guardar` (descarta filas vacías, autonumera pistas sin número, llama a `AlbumTransaccionalService`, captura `IllegalArgumentException` y `ConstraintViolationException` mostrando el error sin perder los datos del formulario).

## Vistas nuevas
- `login.html`
- `canciones.html`
- `form-cancion.html`
- `form-album-completo.html`

## Vistas modificadas
- **`index.html`** — sección de estadísticas por banda (tabla con nombre, cantidad de álbumes, rating promedio); se corrigió el `<link>` de `estilos.css` duplicado.
- **`detalle-album.html`** — tabla de canciones del álbum (número de pista, título, duración en `m:ss`).
- **`albumes.html`** — botones "Registrar álbum", "Registrar primer álbum", "Editar" y "Eliminar" envueltos en `sec:authorize="hasRole('ADMIN')"`; nuevo botón "Álbum con canciones" hacia `/albumes/completo/nuevo`.

## Base de datos (`database/bd_melodicvault.sql`)
- Script reescrito completo: `banda`, `album`, `usuario`, `cancion` en orden de dependencia, con FKs y `ON DELETE CASCADE`.
- Seeds: 3 bandas, 4 álbumes (igual que antes), 2 usuarios (`admin`/`admin123` ADMIN, `lector`/`lector123` USER, passwords en BCrypt) y 3 canciones de ejemplo.
- Corregido: mismatch de mayúsculas entre `bd_melodicvault` (BD real) y `BD_MelodicVault` (la que usaba `application.properties`).

## `application.properties`
- URL corregida a `bd_melodicvault` (minúsculas, coincide con el script).
- Se quitó `createDatabaseIfNotExist=true` (escondía el mismatch de nombre).
- Se quitó `defer-datasource-initialization` (solo aplica si hay `data.sql`).
- `ddl-auto=update` durante desarrollo; pasar a `none` para el entregable final/despliegue (no `validate`, porque `rating` es `DECIMAL(3,1)` en BD vs `Double` en la entidad).

## `Control de versiones (Git)`

- Creado `.gitignore` en la raíz del proyecto `(Maven target/, metadata de Eclipse/IntelliJ/VS Code, logs, zips)`.



## Correcciones de bugs encontrados en el camino
- `UsuarioDetailsService`: faltaban imports de `UsernameNotFoundException` y `User`.
- `IBanda`: el método original (`findWithAlbumesById`) no coincidía con el campo real (`idBanda`), habría lanzado `PropertyReferenceException`.
- `estadisticasPorBanda()`: estaba mal ubicada en `IBanda` (la query es `FROM Album`); movida a `IAlbum`.
- Script SQL: faltaban las tablas `banda`/`album` en el script que solo tenía `usuario`/`cancion`, y el `DROP DATABASE` inicial las borraba sin recrearlas.

## Pendiente

- **2 reportes con JasperReports** (bandas y álbumes/canciones) — único ítem de código pesado que queda.
- Despliegue en Microsoft Azure + workflow de GitHub Actions.
- Documentación de Maven para el informe (`mvn dependency:tree`, explicación del `pom.xml` y el BOM).
- Diagramas de casos de uso, modelo de base de datos, video demo reel.
- Secciones narrativas del informe (resumen, introducción, diagnóstico SEPTE/PEST, justificación, metodología).

