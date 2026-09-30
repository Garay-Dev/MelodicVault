# LPI_CL1_VidalCarlos — Mantenimiento de Embarcaciones

Caso de Laboratorio 2 · Lenguaje de Programación II · **Carlos Vidal**
Spring Boot 3 + Maven + JPA (Hibernate) + Thymeleaf + MySQL.

Tema visual: **Capitanía de Puerto** (registro marítimo).

---

## Requisitos
- Java 17 (o superior)
- MySQL en `localhost:3306`, usuario `root`, clave `mysql`
- Spring Tool Suite (STS) o cualquier IDE con Maven
- Conexión a internet la primera vez (Maven descarga dependencias)

## Base de datos
No necesitas crear nada a mano: al arrancar, la app **crea la BD `BDT2_Vidal`,
la tabla `barco_vidal` y carga 7 embarcaciones de ejemplo** automáticamente
(`createDatabaseIfNotExist=true` + `data.sql`).

Si tu profesor pide el script SQL como entregable, está en:
`database/BDT2_Vidal.sql` (crea la BD, la tabla y los datos manualmente).

> Si tu clave de MySQL no es `mysql`, cámbiala en
> `src/main/resources/application.properties`.

## Cómo ejecutar

### Opción A — Spring Tool Suite
1. `File → Import → Existing Maven Projects` y selecciona esta carpeta.
2. Espera a que Maven baje las dependencias.
3. Clic derecho en el proyecto → `Run As → Spring Boot App`.

### Opción B — Línea de comandos
```bash
mvn spring-boot:run
```

Luego abre el navegador en:  **http://localhost:8080/**

## Funcionalidades
- Listar embarcaciones (bitácora)
- Registrar nueva embarcación (con validaciones)
- Editar embarcación
- Eliminar embarcación (con confirmación)

## Campos de la tabla `barco_vidal`
| Campo        | Columna        | Tipo          | Descripción                  |
|--------------|----------------|---------------|------------------------------|
| Código       | `cod_barco`    | INT (auto)    | Código de la embarcación     |
| Nombre       | `nom_barco`    | VARCHAR(60)   | Nombre de la embarcación     |
| Nacionalidad | `nacionalidad` | VARCHAR(40)   | Nacionalidad                 |
| Capitán      | `capitan`      | VARCHAR(60)   | Nombre del capitán           |
| Eslora       | `eslora`       | DECIMAL(6,2)  | Largo (m)                    |
| Manga        | `manga`        | DECIMAL(6,2)  | Ancho máximo (m)             |

## Estructura del código
```
pe.cibertec.vidal
├── modelo            → Barco (entidad JPA)
├── interfaces        → IBarco (repositorio, capa de datos)
├── interfacesService → IBarcoService (contrato del servicio)
├── service           → BarcoService (lógica)
└── controller        → BarcoController, HomeController
```
