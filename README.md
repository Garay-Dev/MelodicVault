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
- Agregado `thymeleaf-extras-springsecurity6` (necesaria para `sec:authorize` en las vistas).

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


# Cambios realizados v2

Este documento resume los cambios de lógica hechos en MelodicVault (entidad, base de datos y controllers) y la razón de cada uno. No incluye cambios de estilos ni de plantillas.

---

## 1. Restricción de pista única por álbum (entidad `Cancion`)

**Qué se cambió**

Se agregó una restricción única compuesta sobre `id_album` y `numero_pista` en la entidad:

```java
@Entity
@Table(name = "cancion",
       uniqueConstraints = { @UniqueConstraint(columnNames = {"id_album", "numero_pista"}) })
public class Cancion { ... }
```

Requiere el import `jakarta.persistence.UniqueConstraint`.

**Por qué**

- Sin esta restricción se podían registrar dos canciones con el mismo número de pista dentro de un mismo álbum, lo que deja el listado del álbum incoherente.
- La unicidad se definió **por álbum** y no sobre `numero_pista` a secas. Con `@Column(unique = true)` en ese campo, la pista 1 del álbum A habría bloqueado la pista 1 del álbum B, que es un caso totalmente válido.
- Se escribió con llaves `{ @UniqueConstraint(...) }` y con el import correcto de `jakarta.persistence`, porque sin el import el compilador rechazaba la anotación.

---

## 2. Restricción incluida en el script SQL

**Qué se cambió**

Se modificó el script `bd_melodicvault` para que la restricción esté dentro del `CREATE TABLE cancion`, y se volvió a correr el script completo:

```sql
CREATE TABLE cancion (
    id_cancion         INT AUTO_INCREMENT PRIMARY KEY,
    titulo             VARCHAR(100) NOT NULL,
    numero_pista       INT          NOT NULL,
    duracion_segundos  INT,
    id_album           INT          NOT NULL,
    CONSTRAINT fk_cancion_album FOREIGN KEY (id_album) REFERENCES album(id_album)
        ON DELETE CASCADE,
    CONSTRAINT uq_cancion_album_pista UNIQUE (id_album, numero_pista)
);
```

**Por qué**

- La base de datos es la que garantiza de verdad la unicidad. La anotación de la entidad documenta la regla, pero con `ddl-auto=update` Hibernate no es fiable para crear o modificar restricciones sobre tablas que ya existen.
- Al estar la restricción dentro del `CREATE TABLE`, quien ejecute el script desde cero obtiene la base completa, sin pasos manuales adicionales.
- Los nombres de columna (`id_album`, `numero_pista`) coinciden con los de la entidad.
- Los datos de prueba del script no violan la restricción: cada álbum tiene sus pistas sin repetir.

---

## 3. Validación y manejo de errores en `CancionController`

**Qué se cambió**

En `guardar` se agregaron dos cosas.

Primero, una validación para que no se pueda guardar sin elegir álbum:

```java
if (cancion.getAlbum() == null || cancion.getAlbum().getIdAlbum() == null) {
    result.rejectValue("album", "requerido", "Debe seleccionar un álbum");
}
```

Segundo, el `service.save(cancion)` se envolvió en un `try/catch`:

```java
try {
    service.save(cancion);
} catch (DataAccessException e) {
    result.rejectValue("numeroPista", "duplicado",
            "No se pudo guardar. Revisa que la pista no esté repetida en este álbum.");
    model.addAttribute("albumes", albumService.listar());
    model.addAttribute("modo", cancion.getIdCancion() == null ? "registrar" : "editar");
    return "form-cancion";
}
```

Requiere el import `org.springframework.dao.DataAccessException`.

**Por qué**

- Si el usuario deja "Selecciona un álbum", Spring crea igual un objeto `Album` vacío (con `idAlbum` nulo), por lo que `@NotNull` sobre `album` no lo detecta y el guardado fallaba. La validación manual lo cubre y muestra el mensaje bajo el campo.
- Con la restricción activa en la base, guardar una pista repetida lanza una excepción. Sin el `catch`, el usuario veía una página de error 500.
- Se captura `DataAccessException`, la clase padre de las excepciones de acceso a datos de Spring, porque Spring puede traducir el error de la base a distintos tipos (`DataIntegrityViolationException`, `JpaSystemException`, etc.). Así el `catch` no depende de cuál de ellos salga.
- Al devolver la vista `form-cancion` con el mismo objeto `cancion` (en lugar de redirigir), el formulario conserva lo que el usuario escribió y el mensaje aparece bajo el campo "Número de pista" para que lo corrija. También se conservan la lista de álbumes y el modo (registrar o editar).
- Editar una canción sin cambiar su número de pista sigue funcionando, porque la actualización es sobre el mismo registro.

---

## 4. Manejo de errores en `AlbumCompletoController`

**Qué se cambió**

En `guardar`, el bloque `try` ahora tiene dos `catch` separados:

```java
try {
    transaccionalService.registrarAlbumConCanciones(form.getAlbum(), validas);
} catch (IllegalArgumentException e) {
    model.addAttribute("error", "No se guardó nada: " + e.getMessage());
    model.addAttribute("bandas", bandaService.listar());
    return "form-album-completo";
} catch (DataAccessException e) {
    model.addAttribute("error", "No se guardó nada: revisa que no haya pistas repetidas en el álbum.");
    model.addAttribute("bandas", bandaService.listar());
    return "form-album-completo";
}
```

Requiere el import `org.springframework.dao.DataAccessException`.

**Por qué**

- El `catch` original trataba `ConstraintViolationException`, que no cumplía su función: al pasar por los repositorios de Spring Data, las excepciones de Hibernate se traducen a la jerarquía `DataAccessException`, así que ese `catch` nunca se activaba. Además, si el import era `jakarta.validation`, correspondía a la validación de beans y no a la restricción `UNIQUE` de MySQL.
- Los `catch` están separados para mostrar un mensaje fijo y limpio en el caso de duplicados. Mostrar `e.getMessage()` ahí expondría al usuario el texto técnico del error SQL.
- `registrarAlbumConCanciones` es `@Transactional`, por lo que si una canción falla, el rollback deshace también el álbum que ya se había guardado. El mensaje "No se guardó nada" es cierto.

---

## Resumen

- **`uniqueConstraints` (`id_album`, `numero_pista`)** en `Cancion.java`: evita pistas repetidas en un mismo álbum sin bloquearlas entre álbumes distintos.
- **Restricción dentro del `CREATE TABLE cancion`** en el script SQL: la base garantiza la regla y el script corre completo, sin pasos extra.
- **Validación de álbum obligatorio** en `CancionController`: evita guardar una canción sin álbum seleccionado.
- **`catch (DataAccessException)`** en `CancionController`: muestra el error en el campo, conservando los datos del formulario, en vez de un error 500.
- **`catch` separados para `IllegalArgumentException` y `DataAccessException`** en `AlbumCompletoController`: mensaje limpio para el usuario y rollback completo si algo falla.



## Correcciones de bugs encontrados en el camino
- `UsuarioDetailsService`: faltaban imports de `UsernameNotFoundException` y `User`.
- `IBanda`: el método original (`findWithAlbumesById`) no coincidía con el campo real (`idBanda`), habría lanzado `PropertyReferenceException`.
- `estadisticasPorBanda()`: estaba mal ubicada en `IBanda` (la query es `FROM Album`); movida a `IAlbum`.
- Script SQL: faltaban las tablas `banda`/`album` en el script que solo tenía `usuario`/`cancion`, y el `DROP DATABASE` inicial las borraba sin recrearlas.


