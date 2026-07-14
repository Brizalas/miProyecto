# Guía maestra: cómo construir un CRUD con Spring Boot, Thymeleaf, JPA y Service

Esta guía explica cómo construir desde cero una aplicación CRUD completa y organizada.

El ejemplo usa una entidad llamada `Alumno`, pero la misma estructura sirve para productos, libros, clientes, cursos, reservas, tareas o cualquier otro registro.

La arquitectura se monta correctamente desde el principio:

```text
Navegador
↓
Controller
↓
Service
↓
Repository
↓
JPA / Hibernate
↓
Base de datos
```

---

# 1. Qué significa CRUD

```text
Create
→ crear un registro

Read
→ consultar registros

Update
→ modificar un registro

Delete
→ eliminar un registro
```

En una web:

```text
Formulario de alta
→ Create

Listado
→ Read

Formulario de edición
→ Update

Botón eliminar
→ Delete
```

---

# 2. Responsabilidad de cada capa

## Entity

Representa los datos que se guardan.

```text
Clase Java
→ tabla

Objeto
→ fila

Atributo
→ columna
```

## Repository

Accede a la base de datos.

```text
findAll()
findById()
save()
deleteById()
existsById()
```

## Service

Contiene las operaciones y reglas de la aplicación.

```text
listar
buscar
guardar
eliminar
coordinar repositories
aplicar reglas
```

## Controller

Recibe peticiones HTTP y coordina la respuesta.

```text
recibe formularios
ejecuta validación
llama al Service
prepara el Model
devuelve vistas
redirecciona
```

## Thymeleaf

Conecta los objetos Java con el HTML.

```text
muestra datos
recorre listas
rellena formularios
conserva valores
muestra errores
```

---

# 3. Orden recomendado de construcción

```text
1. Crear el proyecto.
2. Añadir dependencias.
3. Configurar la base de datos.
4. Crear la Entity.
5. Añadir validaciones.
6. Crear el Repository.
7. Crear el Service completo.
8. Crear el Controller usando solo el Service.
9. Crear la vista principal.
10. Implementar Create y Read.
11. Implementar Delete.
12. Crear la vista de edición.
13. Implementar Update.
14. Proteger IDs inexistentes.
15. Probar todos los flujos.
16. Refactorizar código repetido.
```

No es necesario conectar primero el Controller al Repository y migrarlo después.

---

# 4. Crear el proyecto

Configuración:

```text
Project: Maven
Language: Java
Java: 21
Packaging: Jar
```

Dependencias:

```text
Spring Web MVC
Thymeleaf
Spring Data JPA
Validation
H2 Database
Spring Boot DevTools
```

---

# 5. Dependencias del `pom.xml`

```xml
<dependencies>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-h2console</artifactId>
        <scope>runtime</scope>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>

</dependencies>
```

Comandos:

```bash
./mvnw clean compile
./mvnw spring-boot:run
```

---

# 6. Estructura del proyecto

```text
src
└── main
    ├── java
    │   └── com/cristian/gestoralumnos
    │       ├── GestoralumnosApplication.java
    │       ├── controller
    │       │   └── HomeController.java
    │       ├── model
    │       │   └── Alumno.java
    │       ├── repository
    │       │   └── AlumnoRepository.java
    │       └── service
    │           └── AlumnoService.java
    └── resources
        ├── application.properties
        ├── static
        └── templates
            ├── index.html
            └── editar.html
```

---

# 7. Configurar H2 y JPA

`application.properties`:

```properties
spring.application.name=gestoralumnos

spring.datasource.url=jdbc:h2:mem:gestoralumnosdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console/

spring.thymeleaf.cache=false
```

Consola:

```text
http://localhost:8080/h2-console/
```

Conexión:

```text
JDBC URL: jdbc:h2:mem:gestoralumnosdb
User: sa
Password: vacío
```

La base de datos está en memoria:

```text
aplicación encendida
→ datos disponibles

aplicación reiniciada
→ datos eliminados
```

Consulta:

```sql
SELECT * FROM ALUMNO;
```

---

# 8. Crear la Entity

`model/Alumno.java`:

```java
package com.cristian.gestoralumnos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

@Entity
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nom és obligatori")
    private String nombre;

    @NotBlank(message = "El cognom és obligatori")
    private String apellido;

    @NotNull(message = "La data de naixement és obligatòria")
    @Past(message = "La data ha de ser anterior a avui")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "La disciplina és obligatòria")
    private String modalidad;

    @NotBlank(message = "El professor és obligatori")
    private String profesor;

    public Alumno() {
    }

    public Alumno(
            Long id,
            String nombre,
            String apellido,
            LocalDate fechaNacimiento,
            String modalidad,
            String profesor
    ) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.modalidad = modalidad;
        this.profesor = profesor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getModalidad() {
        return modalidad;
    }

    public void setModalidad(String modalidad) {
        this.modalidad = modalidad;
    }

    public String getProfesor() {
        return profesor;
    }

    public void setProfesor(String profesor) {
        this.profesor = profesor;
    }
}
```

## Anotaciones principales

```java
@Entity
```

Indica que la clase representa una tabla.

```java
@Id
```

Marca la clave primaria.

```java
@GeneratedValue(strategy = GenerationType.IDENTITY)
```

Permite que la base de datos genere el ID.

```java
public Alumno() {
}
```

JPA necesita un constructor vacío.

---

# 9. Validación

## `@NotBlank`

Para textos obligatorios:

```java
@NotBlank
private String nombre;
```

Rechaza:

```text
null
""
"     "
```

## `@NotNull`

Para objetos obligatorios:

```java
@NotNull
private LocalDate fechaNacimiento;
```

## `@Past`

Exige una fecha anterior al día actual:

```java
@Past
private LocalDate fechaNacimiento;
```

Incorrecto:

```java
@NotBlank
private LocalDate fechaNacimiento;
```

Correcto:

```java
@NotNull
@Past
private LocalDate fechaNacimiento;
```

---

# 10. Crear el Repository

`repository/AlumnoRepository.java`:

```java
package com.cristian.gestoralumnos.repository;

import com.cristian.gestoralumnos.model.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlumnoRepository
        extends JpaRepository<Alumno, Long> {
}
```

```java
JpaRepository<Alumno, Long>
```

significa:

```text
Alumno
→ entidad administrada

Long
→ tipo del ID
```

Spring crea automáticamente la implementación.

---

# 11. Crear el Service completo

`service/AlumnoService.java`:

```java
package com.cristian.gestoralumnos.service;

import com.cristian.gestoralumnos.model.Alumno;
import com.cristian.gestoralumnos.repository.AlumnoRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AlumnoService {

    private final AlumnoRepository alumnoRepository;

    public AlumnoService(
            AlumnoRepository alumnoRepository
    ) {
        this.alumnoRepository = alumnoRepository;
    }

    public List<Alumno> listarAlumnos() {
        return alumnoRepository.findAll();
    }

    public Alumno encontrarAlumno(Long id) {
        return alumnoRepository
                .findById(id)
                .orElse(null);
    }

    public Alumno guardarAlumno(Alumno alumno) {
        return alumnoRepository.save(alumno);
    }

    public void eliminarAlumno(Long id) {
        alumnoRepository.deleteById(id);
    }
}
```

## `listarAlumnos()`

```java
return alumnoRepository.findAll();
```

Devuelve una lista completa.

## `encontrarAlumno(Long id)`

```java
return alumnoRepository.findById(id).orElse(null);
```

`findById()` devuelve:

```java
Optional<Alumno>
```

`orElse(null)` devuelve:

```text
Alumno
o
null
```

## `guardarAlumno()`

```java
return alumnoRepository.save(alumno);
```

`save()` realiza:

```text
INSERT
→ si el ID es null

UPDATE
→ si el ID identifica un registro existente
```

## `eliminarAlumno()`

```java
public void eliminarAlumno(Long id)
```

Debe ser `void` porque `deleteById()` no devuelve ningún objeto.

---

# 12. Crear el Controller completo

`controller/HomeController.java`:

```java
package com.cristian.gestoralumnos.controller;

import com.cristian.gestoralumnos.model.Alumno;
import com.cristian.gestoralumnos.service.AlumnoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final AlumnoService alumnoService;

    public HomeController(
            AlumnoService alumnoService
    ) {
        this.alumnoService = alumnoService;
    }

    @GetMapping("/")
    public String mostrarInicio(Model model) {

        List<Alumno> alumnosGuardados =
                alumnoService.listarAlumnos();

        prepararModeloInicio(
                model,
                alumnosGuardados
        );

        model.addAttribute(
                "nuevoAlumno",
                new Alumno()
        );

        return "index";
    }

    @PostMapping("/afegir-alumne")
    public String afegirAlumne(
            @Valid
            @ModelAttribute("nuevoAlumno")
            Alumno nuevoAlumno,

            BindingResult resultado,

            Model model
    ) {

        if (resultado.hasErrors()) {

            prepararModeloInicio(
                    model,
                    alumnoService.listarAlumnos()
            );

            return "index";
        }

        alumnoService.guardarAlumno(
                nuevoAlumno
        );

        return "redirect:/";
    }

    @PostMapping("/eliminar-alumne")
    public String eliminarAlumne(
            @RequestParam Long id
    ) {

        Alumno alumnoEncontrado =
                alumnoService.encontrarAlumno(id);

        if (alumnoEncontrado == null) {
            return "redirect:/";
        }

        alumnoService.eliminarAlumno(id);

        return "redirect:/";
    }

    @GetMapping("/editar-alumne")
    public String mostrarFormularioEditarAlumne(
            @RequestParam Long id,
            Model model
    ) {

        Alumno alumnoEncontrado =
                alumnoService.encontrarAlumno(id);

        if (alumnoEncontrado == null) {
            return "redirect:/";
        }

        model.addAttribute(
                "alumneEditar",
                alumnoEncontrado
        );

        return "editar";
    }

    @PostMapping("/actualizar-alumne")
    public String actualizarAlumne(
            @Valid
            @ModelAttribute("alumneEditar")
            Alumno alumneEditar,

            BindingResult resultado
    ) {

        if (resultado.hasErrors()) {
            return "editar";
        }

        Alumno alumnoEncontrado =
                alumnoService.encontrarAlumno(
                        alumneEditar.getId()
                );

        if (alumnoEncontrado == null) {
            return "redirect:/";
        }

        alumnoEncontrado.setNombre(
                alumneEditar.getNombre()
        );

        alumnoEncontrado.setApellido(
                alumneEditar.getApellido()
        );

        alumnoEncontrado.setFechaNacimiento(
                alumneEditar.getFechaNacimiento()
        );

        alumnoEncontrado.setModalidad(
                alumneEditar.getModalidad()
        );

        alumnoEncontrado.setProfesor(
                alumneEditar.getProfesor()
        );

        alumnoService.guardarAlumno(
                alumnoEncontrado
        );

        return "redirect:/";
    }

    private void prepararModeloInicio(
            Model model,
            List<Alumno> alumnos
    ) {

        model.addAttribute(
                "titolPagina",
                "Gestor d'alumnes"
        );

        model.addAttribute(
                "missatge",
                "Benvinguda a la meva app"
        );

        model.addAttribute(
                "totalAlumnes",
                alumnos.size()
        );

        model.addAttribute(
                "alumnes",
                alumnos
        );
    }
}
```

El Controller depende solo del Service:

```text
HomeController
↓
AlumnoService
↓
AlumnoRepository
```

---

# 13. Crear `index.html`

```html
<!DOCTYPE html>
<html lang="ca">
    <head>
        <title data-th-text="${titolPagina}">
            Gestor
        </title>

        <meta charset="UTF-8">
    </head>

    <body>

        <h1 data-th-text="${titolPagina}">
            Gestor
        </h1>

        <p data-th-text="${missatge}">
            Missatge
        </p>

        <p>
            Total:
            <span data-th-text="${totalAlumnes}">
                0
            </span>
        </p>

        <form
            action="/afegir-alumne"
            method="post"
            data-th-object="${nuevoAlumno}">

            <label for="nombre">Nom:</label>
            <input
                type="text"
                id="nombre"
                data-th-field="*{nombre}">

            <p
                data-th-if="${#fields.hasErrors('nombre')}"
                data-th-errors="*{nombre}">
                Error
            </p>

            <label for="apellido">Cognom:</label>
            <input
                type="text"
                id="apellido"
                data-th-field="*{apellido}">

            <p
                data-th-if="${#fields.hasErrors('apellido')}"
                data-th-errors="*{apellido}">
                Error
            </p>

            <label for="fechaNacimiento">
                Data:
            </label>

            <input
                type="date"
                id="fechaNacimiento"
                data-th-field="*{fechaNacimiento}">

            <p
                data-th-if="${#fields.hasErrors('fechaNacimiento')}"
                data-th-errors="*{fechaNacimiento}">
                Error
            </p>

            <label for="modalidad">Modalitat:</label>
            <input
                type="text"
                id="modalidad"
                data-th-field="*{modalidad}">

            <p
                data-th-if="${#fields.hasErrors('modalidad')}"
                data-th-errors="*{modalidad}">
                Error
            </p>

            <label for="profesor">Professor:</label>
            <input
                type="text"
                id="profesor"
                data-th-field="*{profesor}">

            <p
                data-th-if="${#fields.hasErrors('profesor')}"
                data-th-errors="*{profesor}">
                Error
            </p>

            <button type="submit">
                Afegir
            </button>
        </form>

        <ul>
            <li data-th-each="alumne : ${alumnes}">

                <strong data-th-text="${alumne.nombre}">
                    Nom
                </strong>

                <span data-th-text="${alumne.apellido}">
                    Cognom
                </span>

                <span data-th-text="${alumne.fechaNacimiento}">
                    Data
                </span>

                <span data-th-text="${alumne.modalidad}">
                    Modalitat
                </span>

                <span data-th-text="${alumne.profesor}">
                    Professor
                </span>

                <form
                    action="/eliminar-alumne"
                    method="post">

                    <input
                        type="hidden"
                        name="id"
                        data-th-value="${alumne.id}">

                    <button type="submit">
                        Eliminar
                    </button>
                </form>

                <form
                    action="/editar-alumne"
                    method="get">

                    <input
                        type="hidden"
                        name="id"
                        data-th-value="${alumne.id}">

                    <button type="submit">
                        Editar
                    </button>
                </form>
            </li>
        </ul>
    </body>
</html>
```

---

# 14. Enlace entre formulario y objeto

```html
data-th-object="${nuevoAlumno}"
```

establece el objeto del formulario.

```html
data-th-field="*{nombre}"
```

enlaza un campo con una propiedad.

Controller:

```java
@ModelAttribute("nuevoAlumno")
Alumno nuevoAlumno
```

Coincidencia obligatoria:

```text
Model:
"nuevoAlumno"

HTML:
${nuevoAlumno}

Controller:
@ModelAttribute("nuevoAlumno")
```

---

# 15. Create y validación

```java
@Valid
@ModelAttribute("nuevoAlumno")
Alumno nuevoAlumno,

BindingResult resultado
```

`@Valid` ejecuta las reglas.

`BindingResult` recoge los errores.

Debe ir inmediatamente después del objeto validado.

Con errores:

```java
if (resultado.hasErrors()) {
    prepararModeloInicio(
            model,
            alumnoService.listarAlumnos()
    );

    return "index";
}
```

Sin errores:

```java
alumnoService.guardarAlumno(nuevoAlumno);
```

Como el ID es `null`, Hibernate genera un `INSERT`.

---

# 16. Read

```text
GET /
↓
Controller
↓
alumnoService.listarAlumnos()
↓
Repository.findAll()
↓
List<Alumno>
↓
Model
↓
data-th-each
```

---

# 17. Delete

El formulario envía un único dato:

```html
<input
    type="hidden"
    name="id"
    data-th-value="${alumne.id}">
```

El Controller lo recibe con:

```java
@RequestParam Long id
```

Después:

```java
alumnoService.eliminarAlumno(id);
```

---

# 18. Crear `editar.html`

```html
<!DOCTYPE html>
<html lang="ca">
    <head>
        <title>Editar alumne</title>
        <meta charset="UTF-8">
    </head>

    <body>

        <h1>Editar alumne</h1>

        <form
            action="/actualizar-alumne"
            method="post"
            data-th-object="${alumneEditar}">

            <input
                type="hidden"
                data-th-field="*{id}">

            <label for="nombre">Nom:</label>
            <input
                type="text"
                id="nombre"
                data-th-field="*{nombre}">

            <p
                data-th-errors="*{nombre}">
                Error
            </p>

            <label for="apellido">Cognom:</label>
            <input
                type="text"
                id="apellido"
                data-th-field="*{apellido}">

            <p
                data-th-errors="*{apellido}">
                Error
            </p>

            <label for="fechaNacimiento">Data:</label>
            <input
                type="date"
                id="fechaNacimiento"
                data-th-field="*{fechaNacimiento}">

            <p
                data-th-errors="*{fechaNacimiento}">
                Error
            </p>

            <label for="modalidad">Modalitat:</label>
            <input
                type="text"
                id="modalidad"
                data-th-field="*{modalidad}">

            <p
                data-th-errors="*{modalidad}">
                Error
            </p>

            <label for="profesor">Professor:</label>
            <input
                type="text"
                id="profesor"
                data-th-field="*{profesor}">

            <p
                data-th-errors="*{profesor}">
                Error
            </p>

            <button type="submit">
                Guardar canvis
            </button>
        </form>

        <a href="/">
            Tornar
        </a>
    </body>
</html>
```

El ID oculto es obligatorio:

```html
data-th-field="*{id}"
```

---

# 19. Update

El objeto recibido contiene el ID:

```java
alumneEditar.getId()
```

Se busca el registro real:

```java
Alumno alumnoEncontrado =
        alumnoService.encontrarAlumno(
                alumneEditar.getId()
        );
```

Después se copian los cambios y se guarda:

```java
alumnoService.guardarAlumno(
        alumnoEncontrado
);
```

Como tiene un ID existente, Hibernate genera un `UPDATE`.

---

# 20. Gestión de IDs inexistentes

Una URL puede escribirse manualmente:

```text
/editar-alumne?id=999
```

Por eso se comprueba:

```java
if (alumnoEncontrado == null) {
    return "redirect:/";
}
```

Esto evita enviar un objeto nulo a Thymeleaf.

---

# 21. Post / Redirect / Get

Después de crear, actualizar o eliminar:

```java
return "redirect:/";
```

Ventajas:

```text
evita repetir el formulario al refrescar
vuelve a consultar la base de datos
actualiza el listado
actualiza el contador
```

---

# 22. Errores frecuentes

## `BindingResult` mal colocado

Correcto:

```java
@Valid Alumno alumno,
BindingResult resultado
```

## Volver a `index` sin reconstruir el Model

Un POST que devuelve `index` no ejecuta `GET /`.

## Olvidar el ID oculto

Sin:

```html
data-th-field="*{id}"
```

Update no puede identificar el registro.

## Esperar que `deleteById()` devuelva un objeto

Devuelve:

```java
void
```

## Devolver directamente `findById()`

Devuelve:

```java
Optional<Alumno>
```

Para obtener un objeto o `null`:

```java
findById(id).orElse(null)
```

## Mezclar Repository y Service en Controller

Evitar:

```text
Controller
├── Repository
└── Service
```

Usar:

```text
Controller
↓
Service
↓
Repository
```

---

# 23. Pruebas

```text
Create
→ campos vacíos
→ fecha futura
→ datos válidos

Read
→ listado visible
→ contador correcto

Update
→ valores actuales
→ errores visibles
→ cambios guardados

Delete
→ registro eliminado
→ contador actualizado

ID inexistente
→ redirección
→ sin Whitelabel

H2
→ SELECT * FROM ALUMNO;
```

---

# 24. Resumen mental

```text
Entity
→ datos y validaciones

Repository
→ acceso a base de datos

Service
→ operaciones y reglas

Controller
→ peticiones, Model y vistas

Thymeleaf
→ HTML dinámico

@ModelAttribute
→ construye objetos desde formularios

@Valid
→ ejecuta validaciones

BindingResult
→ recoge errores

findAll()
→ listar

findById()
→ buscar uno

save()
→ insertar o actualizar

deleteById()
→ eliminar
```

---

# 25. Arquitectura final

```text
Navegador
↓
HomeController
↓
AlumnoService
↓
AlumnoRepository
↓
Spring Data JPA
↓
Hibernate
↓
H2
```

Resultado:

```text
Create                         ✅
Read                           ✅
Update                         ✅
Delete                         ✅
Entity JPA                     ✅
Repository                     ✅
Service completo               ✅
Controller desacoplado         ✅
Validación                     ✅
Errores en formularios         ✅
IDs inexistentes controlados   ✅
Post / Redirect / Get          ✅
```

Este patrón puede reutilizarse para construir cualquier CRUD similar cambiando la entidad, sus atributos, sus validaciones y sus vistas.
