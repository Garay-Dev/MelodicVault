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


# Cambios realizados v3

Este documento resume los cambios hechos en MelodicVault para incorporar el **registro de usuarios** (seguridad, repositorio, DTO, controller y plantillas) y la razón de cada uno. A diferencia de la v2, incluye las plantillas `registro.html` y `login.html` porque son parte necesaria del flujo.

---

## 1. Ruta `/registro` pública en `SecurityConfig`

**Qué se cambió**

Se agregó `/registro` a la lista de rutas públicas:

```java
.requestMatchers("/", "/acerca", "/bandas", "/albumes", "/canciones", "/*/detalle/**", "/registro").permitAll()
```

**Por qué**

- La regla final `.anyRequest().authenticated()` bloqueaba `/registro` para quien no tenía sesión. Spring Security lo interceptaba y lo redirigía a `/login`, así que el enlace "Regístrate" parecía llevar al mismo login y el controller nunca se ejecutaba.
- Sin método explícito, `permitAll()` cubre el `GET` (formulario) y el `POST` (envío).
- El `POST` debe ir a `/registro` y **no** a `/registro/guardar`: la regla `/*/guardar` exige rol `ADMIN` y lo bloquearía.

---

## 2. Método `existsByUsername` en `IUsuario`

**Qué se cambió**

```java
boolean existsByUsername(String username);
```

**Por qué**

- Permite comprobar si el usuario ya existe antes de guardar, para mostrar un mensaje claro en el campo en lugar de un error de base de datos.
- Spring Data genera la consulta a partir del nombre del método; sin esta declaración el controller no compila.

---

## 3. Nuevo DTO `RegistroForm`

**Qué se cambió**

Se creó la clase `RegistroForm` (paquete `dto`) con tres campos y sus validaciones:

```java
public class RegistroForm {
    @NotBlank(message = "El usuario es obligatorio")
    @Size(min = 4, max = 50, message = "Entre 4 y 50 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9_.-]*$", message = "Solo letras, números, punto, guion y guion bajo")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 72, message = "Entre 6 y 72 caracteres")
    private String password;

    @NotBlank(message = "Confirma la contraseña")
    private String confirmar;

    // getters y setters
}
```

**Por qué**

- Se usa un DTO y **no** la entidad `Usuario` directamente. Si el formulario recibiera un `Usuario`, cualquiera podría enviar `rol=ADMIN` en el `POST` y convertirse en administrador. El DTO no tiene campo `rol`, así que ese dato nunca llega desde el navegador.
- El máximo de 72 caracteres en la contraseña responde al límite de BCrypt (72 bytes).
- El campo `confirmar` solo existe en el formulario; no se guarda en la base.

---

## 4. Nuevo `RegistroController`

**Qué se cambió**

```java
@Controller
public class RegistroController {
    @Autowired private IUsuario repo;
    @Autowired private PasswordEncoder encoder;

    @GetMapping("/registro")
    public String form(Model model) {
        model.addAttribute("registroForm", new RegistroForm());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registroForm") RegistroForm form,
                            BindingResult result, RedirectAttributes flash) {

        if (!result.hasErrors() && !form.getPassword().equals(form.getConfirmar()))
            result.rejectValue("confirmar", "nocoincide", "Las contraseñas no coinciden");

        if (!result.hasErrors() && repo.existsByUsername(form.getUsername()))
            result.rejectValue("username", "duplicado", "Ese usuario ya existe");

        if (result.hasErrors()) return "registro";

        Usuario u = new Usuario();
        u.setUsername(form.getUsername());
        u.setPassword(encoder.encode(form.getPassword()));
        u.setRol("LECTOR");

        try {
            repo.save(u);
        } catch (DataIntegrityViolationException e) {
            result.rejectValue("username", "duplicado", "Ese usuario ya existe");
            return "registro";
        }

        flash.addFlashAttribute("exito", "Cuenta creada. Ya puedes iniciar sesión.");
        return "redirect:/login";
    }
}
```

**Por qué**

- Las comprobaciones de coincidencia de contraseñas y de usuario duplicado solo se hacen si no hay errores previos de validación, para no mostrar mensajes encadenados.
- La contraseña se guarda siempre con `BCryptPasswordEncoder`, nunca en texto plano.
- El rol se fija en el servidor como `LECTOR`, **sin** el prefijo `ROLE_` (Spring lo agrega con `roles(...)` en `UsuarioDetailsService`). Como el sistema solo distingue `ADMIN` en `SecurityConfig`, `LECTOR` no recibe permisos de mantenimiento.
- El `try/catch` de `DataIntegrityViolationException` cubre una condición de carrera: si otra persona registra el mismo usuario entre el `existsByUsername` y el `save`, la restricción `UNIQUE` de `username` rechaza el segundo y el usuario ve el mensaje en el campo en vez de un error 500.
- Al registrar con éxito se redirige a `/login` con un mensaje flash, siguiendo el patrón `POST → redirect` del resto del sistema.
- Cuando hay errores se devuelve la vista `registro` (sin redirigir) para conservar el nombre de usuario escrito y mostrar los mensajes bajo cada campo.

---

## 5. Nueva plantilla `registro.html`

**Qué se cambió**

Se creó `src/main/resources/templates/registro.html` con el mismo `<head>`, cabecera y pie que `login.html`, y este formulario en el `<main>`:

```html
<form th:action="@{/registro}" th:object="${registroForm}" method="post">
    <input type="text"     th:field="*{username}"  class="form-control input-vault" autofocus>
    <div class="text-danger small" th:if="${#fields.hasErrors('username')}" th:errors="*{username}"></div>

    <input type="password" th:field="*{password}"  class="form-control input-vault">
    <div class="text-danger small" th:if="${#fields.hasErrors('password')}" th:errors="*{password}"></div>

    <input type="password" th:field="*{confirmar}" class="form-control input-vault">
    <div class="text-danger small" th:if="${#fields.hasErrors('confirmar')}" th:errors="*{confirmar}"></div>

    <a th:href="@{/login}" class="btn-cancelar">Cancelar</a>
    <button type="submit" class="btn-guardar">Crear cuenta</button>
</form>
```

**Por qué**

- `th:action="@{/registro}"` incluye automáticamente el token CSRF; con `action="/registro"` a secas el `POST` daría 403.
- `th:object` y `th:field` enlazan el formulario con `RegistroForm` y permiten mostrar los errores con `th:errors`.
- Thymeleaf no vuelve a rellenar los campos `type="password"`: si hay un error, el usuario reescribe las contraseñas, que es el comportamiento correcto.
- El botón **Cancelar** devuelve a `/login`.


---

## 6. Cambios en `login.html`

**Qué se cambió**

Debajo del alert de error, el mensaje de éxito:

```html
<div class="alert alert-success" th:if="${exito}" th:text="${exito}"></div>
```

Debajo del formulario, el enlace al registro:

```html
<p class="mt-3 text-center">¿No tienes cuenta? <a th:href="@{/registro}">Regístrate</a></p>
```

**Por qué**

- El mensaje flash `exito` enviado por `RegistroController` solo se ve si `login.html` lo muestra.
- El enlace es la puerta de entrada al registro. Opcionalmente puede añadirse también en el menú con `sec:authorize="!isAuthenticated()"`.


---

## Resumen

- `/registro` agregado a `permitAll()` en `SecurityConfig`: evita la redirección al login; el `POST` va a `/registro`, no a `/registro/guardar`.
- `existsByUsername` en `IUsuario`: permite detectar usuarios duplicados antes de guardar.
- `RegistroForm` (DTO): valida usuario y contraseñas, y evita que alguien se asigne el rol `ADMIN` desde el formulario.
- `RegistroController`: valida, cifra la contraseña con BCrypt, fija el rol `LECTOR` y maneja la carrera de usuarios duplicados.
- `registro.html`: formulario con CSRF, errores por campo y contraseñas que no se rellenan.
- `login.html`: muestra el mensaje de éxito y enlaza a "Regístrate".

# Cambios realizados v4

Este documento resume los cambios hechos en MelodicVault para incorporar **reportes PDF con JasperReports** y la razón de cada uno. La v3 cubrió el registro de usuarios y el login; la v4 cubre solo el módulo de reportes.

**Resultado:** cualquier usuario con sesión (Administrador o Lector) puede descargar dos reportes en PDF:

| Reporte | Ruta | Se descarga desde |
|---|---|---|
| Canciones de un álbum | `/reportes/album/{id}` | Detalle de álbum |
| Álbumes de una banda | `/reportes/banda/{id}` | Detalle de banda |

---

## 1. Dependencias de JasperReports en el `pom.xml`

**Qué se cambió**

```xml
<dependency>
    <groupId>net.sf.jasperreports</groupId>
    <artifactId>jasperreports</artifactId>
    <version>6.20.6</version>
</dependency>
<dependency>
    <groupId>net.sf.jasperreports</groupId>
    <artifactId>jasperreports-fonts</artifactId>
    <version>6.20.6</version>
</dependency>
```

**Por qué**

- Se usa la serie 6.x porque es estable con Spring Boot 3.3.4 y Java 17, y la 7.x cambió la estructura de módulos y el formato del `.jrxml`.
- `jasperreports-fonts` evita el error "Could not load the following font: DejaVu Sans" al exportar a PDF.

---

## 2. Consulta de canciones por álbum en `IAlbum`

**Qué se cambió**

```java
@Query("SELECT c FROM Cancion c WHERE c.album.idAlbum = :id ORDER BY c.numeroPista")
List<Cancion> listadoDeCanciones(@Param("id") int id);
```

Se declaró en `IAlbumService` y se implementó en `AlbumService`:

```java
@Override
public List<Cancion> listadoDeCanciones(int id) {
    return data.listadoDeCanciones(id);
}
```

**Por qué**

- Trae solo las canciones del álbum pedido, ordenadas por número de pista, no toda la tabla.
- JPQL usa el nombre de la **entidad** (`Cancion`) y sus atributos (`idAlbum`, `numeroPista`), no los de la tabla.
- Devuelve una `List`, no un `Optional`, porque un álbum tiene varias canciones.
- `AlbumService` solo entrega datos; la lógica del reporte vive en otro service.

---

## 3. Nuevo `ReporteService`

**Qué se cambió**

Se creó un service dedicado con un método por reporte:

```java
public byte[] exportarCancionesAlbum(int idAlbum) throws JRException { ... }
public byte[] exportarAlbumesBanda(int idBanda) throws JRException { ... }
```

Cada método:

1. Busca la entidad (`Album` o `Banda`) y lanza `IllegalArgumentException` si no existe.
2. Arma el mapa de **parámetros** (título, banda, país, género, año).
3. Compila el `.jrxml` desde `/reportes/...`.
4. Llena el reporte con `JRBeanCollectionDataSource` y exporta con `JasperExportManager.exportReportToPdf`.
5. Devuelve el PDF como `byte[]`.

**Por qué**

- Separa responsabilidades: `AlbumService` y `BandaService` hacen CRUD; `ReporteService` genera documentos.
- Un tercer reporte solo agrega otro método aquí, sin tocar los demás services.
- Los nombres de los `<field>` del `.jrxml` coinciden con los atributos de las entidades (`numeroPista`, `titulo`, `duracionSegundos`, `anio`, `tipo`, `rating`).

---

## 4. Nuevo `ReporteController`

**Qué se cambió**

```java
@Controller
@RequestMapping("/reportes")
public class ReporteController {

    @GetMapping("/album/{id}")
    public ResponseEntity<byte[]> reporteAlbum(@PathVariable int id) throws JRException { ... }

    @GetMapping("/banda/{id}")
    public ResponseEntity<byte[]> reporteBanda(@PathVariable int id) throws JRException { ... }
}
```

Ambos devuelven `Content-Type: application/pdf` con `Content-Disposition: inline`.

**Por qué**

- El controller solo recibe el id y delega; no tiene lógica de reporte.
- `inline` abre el PDF en el navegador en lugar de forzar la descarga.
- Es un controller aparte porque `AlbumController` y `BandaController` ya tienen su CRUD.

---

## 5. Seguridad: sin cambios en `SecurityConfig`

**Qué se verificó**

Las rutas `/reportes/album/{id}` y `/reportes/banda/{id}` no coinciden con ninguna regla específica:

- `/*/detalle/**`: el segundo segmento no es `detalle`.
- `/*/nuevo`, `/*/editar/**`, `/*/guardar`, `/*/eliminar/**`, `/albumes/completo/**`: tampoco.

**Por qué**

- Caen en `anyRequest().authenticated()`: entran **ADMIN y LECTOR**, y quien no tiene sesión es enviado a `/login`.
- Regla a recordar: el segundo segmento de una ruta de reportes no debe llamarse `detalle`, `nuevo`, `editar`, `guardar` ni `eliminar`.

---

## 6. Plantillas de reporte `.jrxml`

**Qué se cambió**

Se crearon en `src/main/resources/reportes/`:

| Archivo | Contenido |
|---|---|
| `reporte_canciones.jrxml` | Título del álbum, banda y tabla Pista / Título / Duración |
| `reporte_albumes.jrxml` | Banda, país, género, año de formación y tabla Año / Título / Tipo / Rating |

**Detalles de diseño**

- Estilo común `celda`, con borde de 0.5 y relleno lateral, para que la tabla tenga cuadros bien definidos.
- Cabecera de columnas en gris con texto en negrita.
- Pie de página con "Melodic Vault - Página N".
- La duración se guarda en segundos y se muestra como `m:ss` (por ejemplo, 245 s → `4:05`); si es nula, muestra `-`.
- El rating se muestra con un decimal; si es nulo, muestra `-`.
- `whenNoDataType="AllSectionsNoDetail"` en el reporte de álbumes: una banda sin álbumes genera un PDF con título y cabecera, en vez de un error.
- Un solo `<detail>` por reporte: Jasper no acepta dos.

---

## 7. Botones "Descargar PDF" en las vistas

**Qué se cambió**

En `detalle-album.html` y `detalle-banda.html`:

```html
<a sec:authorize="isAuthenticated()"
   th:href="@{/reportes/banda/{id}(id=${banda.idBanda})}"
   target="_blank" class="btn-secundario">
    Descargar PDF
</a>
```

**Por qué**

- `isAuthenticated()` y no `hasRole('ADMIN')`: el Lector también debe poder descargar.
- Quien no inició sesión no ve el botón.
- `target="_blank"` abre el PDF en otra pestaña y no saca al usuario del catálogo.
- El botón solo oculta la opción; el bloqueo real lo hace el servidor.

---

## 8. Problemas encontrados y cómo se resolvieron

| Síntoma | Causa | Solución |
|---|---|---|
| `JRBeanCollectionDataSource` en rojo | Faltaba el import del subpaquete `data` | `import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;` |
| `@PathVariable` y `MediaType` en rojo | Imports faltantes en el controller | Importar de `org.springframework.web.bind.annotation` y `org.springframework.http` |
| `BindException: La dirección ya se está usando` | Otra ejecución de la app ocupaba el puerto 8090 | Terminar la ejecución anterior antes de volver a correr |
| Error 500 al abrir el reporte | El `.jrxml` tenía dos bloques `<detail>` | Dejar solo el que usa `style="celda"` |

---

## Limitaciones conocidas

- El `.jrxml` se compila en **cada petición**. Para este proyecto es suficiente; en producción se guardaría el `JasperReport` compilado en un campo y se reutilizaría.
- `banda.getAlbumes()` es una relación `LAZY`: funciona porque Spring Boot mantiene activo `open-in-view` por defecto. Si se desactiva, habría que cargar la colección dentro de una transacción.
- Los reportes se exportan solo a PDF.

---

## Resumen

- **pom.xml:** `jasperreports` y `jasperreports-fonts` 6.20.6.
- **IAlbum / AlbumService:** `listadoDeCanciones(id)` con JPQL ordenado por pista.
- **ReporteService:** un método por reporte que compila, llena y exporta a PDF.
- **ReporteController:** `/reportes/album/{id}` y `/reportes/banda/{id}`.
- **SecurityConfig:** sin cambios; las rutas exigen sesión y sirven a ADMIN y LECTOR.
- **`reporte_canciones.jrxml` y `reporte_albumes.jrxml`:** tablas con bordes, duración en `m:ss`, y manejo de banda sin álbumes.
- **Vistas:** botón "Descargar PDF" visible para cualquier usuario con sesión.
