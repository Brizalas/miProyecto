# Guía maestra del proyecto Gestor de alumnos

Esta guía documenta el proyecto en su **estado actual y funcional**.

No parte del CRUD antiguo con `ArrayList` ni desarrolla el flujo basado en muchos `@RequestParam`. El objetivo es servir como referencia directa para construir una aplicación similar y para retomar este proyecto exactamente desde el punto actual.

---

# 1. Estado actual del proyecto

```text
Create ✅
Read   ✅
Update ✅
Delete ✅

Spring Boot            ✅
Thymeleaf              ✅
Spring Data JPA        ✅
Hibernate              ✅
H2 en memoria          ✅
LocalDate              ✅
Validación en Create   ✅
Validación en Update   ✅
Mensajes de error      ✅
```

Arquitectura actual:

```text
Navegador
↓
Controller
↓
AlumnoRepository
↓
JPA / Hibernate
↓
H2
```

Arquitectura prevista:

```text
Navegador
↓
Controller
↓
AlumnoService
↓
AlumnoRepository
↓
Base de datos
```

---

# 2. Estructura del proyecto

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
    │       └── repository
    │           └── AlumnoRepository.java
    └── resources
        ├── application.properties
        ├── static
        └── templates
            ├── index.html
            └── editar.html
```

Responsabilidades:

```text
Alumno.java
→ define los datos y las reglas de validación

AlumnoRepository.java
→ accede a la base de datos

HomeController.java
→ recibe peticiones y coordina el flujo

index.html
→ alta, listado, eliminar y acceso a editar

editar.html
→ formulario de modificación

application.properties
→ configura H2, JPA y Thymeleaf
```

---

# 3. Dependencias necesarias

En `pom.xml`:

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

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

</dependencies>
```

Compilar:

```bash
./mvnw clean compile
```

Arrancar:

```bash
./mvnw spring-boot:run
```

---

# 4. Configuración de H2 y JPA

Archivo:

```text
src/main/resources/application.properties
```

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

La base de datos está en memoria:

```text
Aplicación encendida
→ los datos existen

Aplicación reiniciada
→ los datos desaparecen
```

Consola H2:

```text
http://localhost:8080/h2-console/
```

Datos:

```text
JDBC URL: jdbc:h2:mem:gestoralumnosdb
User:     sa
Password: vacío
```

Consulta útil:

```sql
SELECT * FROM ALUMNO;
```

---

# 5. Entidad `Alumno`

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

    @NotBlank(message = "El nom es obligatori")
    private String nombre;

    @NotBlank(message = "El cognom es obligatori")
    private String apellido;

    @NotNull(message = "Data de naixament obligatoria")
    @Past(message = "La data de naixament ha de ser anterior a avui")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "La disciplina es obligatoria")
    private String modalidad;

    @NotBlank(message = "El professor es obligatori")
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

    @Override
    public String toString() {
        return "Alumno{" 
                + "id=" + id
                + ", nombre=" + nombre
                + ", apellido=" + apellido
                + ", fechaNacimiento=" + fechaNacimiento
                + ", modalidad=" + modalidad
                + ", profesor=" + profesor
                + '}';
    }
}
```

---

# 6. Reglas de validación

## `@NotBlank`

Se usa con `String`.

Rechaza:

```text
null
""
"     "
```

## `@NotNull`

Exige que exista un valor.

Se usa para `LocalDate fechaNacimiento`.

Incorrecto:

```java
@NotBlank
private LocalDate fechaNacimiento;
```

Correcto:

```java
@NotNull
private LocalDate fechaNacimiento;
```

## `@Past`

Exige que la fecha sea anterior al día actual.

---

# 7. Repository

```java
package com.cristian.gestoralumnos.repository;

import com.cristian.gestoralumnos.model.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlumnoRepository
        extends JpaRepository<Alumno, Long> {
}
```

Métodos disponibles:

```java
findAll();
findById(id);
save(alumno);
deleteById(id);
count();
existsById(id);
```

---

# 8. Controller completo

```java
package com.cristian.gestoralumnos.controller;

import com.cristian.gestoralumnos.model.Alumno;
import com.cristian.gestoralumnos.repository.AlumnoRepository;
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

    private final AlumnoRepository alumnoRepository;

    public HomeController(AlumnoRepository alumnoRepository) {
        this.alumnoRepository = alumnoRepository;
    }

    @GetMapping("/")
    public String mostrarInicio(Model model) {

        List<Alumno> alumnosGuardados =
                alumnoRepository.findAll();

        model.addAttribute(
                "titolPagina",
                "Gestor d'alumnes"
        );

        model.addAttribute(
                "missatge",
                "Benvinguda a la meva app de gestió d'alumnes"
        );

        model.addAttribute(
                "totalAlumnes",
                alumnosGuardados.size()
        );

        model.addAttribute(
                "alumnes",
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

            List<Alumno> alumnosGuardados =
                    alumnoRepository.findAll();

            model.addAttribute(
                    "titolPagina",
                    "Gestor d'alumnes"
            );

            model.addAttribute(
                    "missatge",
                    "Benvinguda a la meva app de gestió d'alumnes"
            );

            model.addAttribute(
                    "totalAlumnes",
                    alumnosGuardados.size()
            );

            model.addAttribute(
                    "alumnes",
                    alumnosGuardados
            );

            return "index";
        }

        alumnoRepository.save(nuevoAlumno);

        return "redirect:/";
    }

    @PostMapping("/eliminar-alumne")
    public String eliminarAlumne(
            @RequestParam Long id
    ) {

        alumnoRepository.deleteById(id);

        return "redirect:/";
    }

    @GetMapping("/editar-alumne")
    public String mostrarFormularioEditarAlumne(
            @RequestParam Long id,
            Model model
    ) {

        Alumno alumnoEncontrado =
                alumnoRepository
                        .findById(id)
                        .orElse(null);

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
                alumnoRepository
                        .findById(alumneEditar.getId())
                        .orElse(null);

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

        alumnoRepository.save(alumnoEncontrado);

        return "redirect:/";
    }
}
```

---

# 9. Conexión entre formulario y objeto

```text
Formulario
↓
data-th-object
↓
data-th-field
↓
@ModelAttribute
↓
Objeto Alumno completo
```

Create:

```text
Model:       "nuevoAlumno"
HTML:        ${nuevoAlumno}
Controller:  @ModelAttribute("nuevoAlumno")
```

Update:

```text
Model:       "alumneEditar"
HTML:        ${alumneEditar}
Controller:  @ModelAttribute("alumneEditar")
```

Los nombres deben coincidir exactamente.

---

# 10. Validación completa

```text
Entidad
@NotBlank / @NotNull / @Past
↓
Controller
@Valid
↓
BindingResult
↓
resultado.hasErrors()
↓
Vista
 data-th-errors
```

`BindingResult` debe ir inmediatamente después del objeto validado.

Correcto:

```java
@Valid Alumno alumno,
BindingResult resultado
```

Incorrecto:

```java
@Valid Alumno alumno,
Model model,
BindingResult resultado
```

---

# 11. Formulario de alta

```html
<h2>Afegir alumne</h2>

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
        Error en el nom
    </p>

    <label for="apellido">Cognom:</label>
    <input
        type="text"
        id="apellido"
        data-th-field="*{apellido}">

    <p
        data-th-if="${#fields.hasErrors('apellido')}"
        data-th-errors="*{apellido}">
        Error en el cognom
    </p>

    <label for="fechaNacimiento">
        Data de naixement:
    </label>

    <input
        type="date"
        id="fechaNacimiento"
        data-th-field="*{fechaNacimiento}">

    <p
        data-th-if="${#fields.hasErrors('fechaNacimiento')}"
        data-th-errors="*{fechaNacimiento}">
        Error en la data
    </p>

    <label for="modalidad">Modalitat:</label>
    <input
        type="text"
        id="modalidad"
        data-th-field="*{modalidad}">

    <p
        data-th-if="${#fields.hasErrors('modalidad')}"
        data-th-errors="*{modalidad}">
        Error en la modalitat
    </p>

    <label for="profesor">Professor:</label>
    <input
        type="text"
        id="profesor"
        data-th-field="*{profesor}">

    <p
        data-th-if="${#fields.hasErrors('profesor')}"
        data-th-errors="*{profesor}">
        Error en el professor
    </p>

    <button type="submit">
        Afegir
    </button>
</form>
```

---

# 12. Listado, eliminar y editar

```html
<ul>
    <li data-th-each="alumne : ${alumnes}">

        <strong data-th-text="${alumne.nombre}">
            Nom
        </strong>

        <span data-th-text="${alumne.apellido}">
            Cognom
        </span>

        -

        <span data-th-text="${alumne.fechaNacimiento}">
            Data de naixement
        </span>

        -

        <span data-th-text="${alumne.modalidad}">
            Modalitat
        </span>

        - Professor/a:

        <span data-th-text="${alumne.profesor}">
            Professor
        </span>

        <form action="/eliminar-alumne" method="post">
            <input
                type="hidden"
                name="id"
                data-th-value="${alumne.id}">

            <button type="submit">
                Eliminar
            </button>
        </form>

        <form action="/editar-alumne" method="get">
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
```

---

# 13. Formulario de edición

```html
<!DOCTYPE html>
<html lang="ca">
    <head>
        <title>Editar alumne</title>
        <meta charset="UTF-8">
        <meta
            name="viewport"
            content="width=device-width, initial-scale=1.0">
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
                data-th-if="${#fields.hasErrors('nombre')}"
                data-th-errors="*{nombre}">
                Error en el nom
            </p>

            <label for="apellido">Cognom:</label>
            <input
                type="text"
                id="apellido"
                data-th-field="*{apellido}">

            <p
                data-th-if="${#fields.hasErrors('apellido')}"
                data-th-errors="*{apellido}">
                Error en el cognom
            </p>

            <label for="fechaNacimiento">
                Data de naixement:
            </label>

            <input
                type="date"
                id="fechaNacimiento"
                data-th-field="*{fechaNacimiento}">

            <p
                data-th-if="${#fields.hasErrors('fechaNacimiento')}"
                data-th-errors="*{fechaNacimiento}">
                Error en la data
            </p>

            <label for="modalidad">Modalitat:</label>
            <input
                type="text"
                id="modalidad"
                data-th-field="*{modalidad}">

            <p
                data-th-if="${#fields.hasErrors('modalidad')}"
                data-th-errors="*{modalidad}">
                Error en la modalitat
            </p>

            <label for="profesor">Professor:</label>
            <input
                type="text"
                id="profesor"
                data-th-field="*{profesor}">

            <p
                data-th-if="${#fields.hasErrors('profesor')}"
                data-th-errors="*{profesor}">
                Error en el professor
            </p>

            <button type="submit">
                Guardar canvis
            </button>
        </form>

        <a href="/">
            Tornar a l'inici
        </a>
    </body>
</html>
```

---

# 14. Flujo completo de Create

```text
GET /
↓
Controller crea new Alumno()
↓
Model: "nuevoAlumno"
↓
index.html
↓
Usuario rellena campos
↓
POST /afegir-alumne
↓
@ModelAttribute construye Alumno
↓
@Valid comprueba reglas
↓
BindingResult
```

Con errores:

```text
resultado.hasErrors() = true
↓
se vuelve a cargar la lista
↓
return "index"
↓
Thymeleaf muestra errores
```

Sin errores:

```text
save(nuevoAlumno)
↓
id = null
↓
Hibernate genera INSERT
↓
redirect:/
```

---

# 15. Flujo completo de Update

Abrir edición:

```text
Botón Editar
↓
GET /editar-alumne?id=...
↓
findById(id)
↓
alumnoEncontrado
↓
Model: "alumneEditar"
↓
editar.html
```

Guardar:

```text
POST /actualizar-alumne
↓
@ModelAttribute("alumneEditar")
↓
@Valid
↓
BindingResult
```

Con errores:

```text
return "editar"
↓
se conservan los valores
↓
se muestran los mensajes
```

Sin errores:

```text
findById(alumneEditar.getId())
↓
alumnoEncontrado
↓
copiar valores con setters
↓
save(alumnoEncontrado)
↓
Hibernate genera UPDATE
```

---

# 16. Por qué Update busca de nuevo el alumno

```text
alumneEditar
→ contiene los datos enviados por el formulario

alumnoEncontrado
→ es el registro real recuperado de H2
```

Se valida primero el objeto recibido.

Después se copian los valores:

```java
alumnoEncontrado.setNombre(
        alumneEditar.getNombre()
);
```

Finalmente:

```java
alumnoRepository.save(alumnoEncontrado);
```

---

# 17. Delete

```text
Botón Eliminar
↓
POST /eliminar-alumne
↓
input hidden name="id"
↓
@RequestParam Long id
↓
deleteById(id)
↓
redirect:/
```

Aquí `@RequestParam` sigue siendo adecuado porque solo se recibe un dato.

---

# 18. Read

```text
GET /
↓
findAll()
↓
List<Alumno>
↓
Model: "alumnes"
↓
data-th-each
↓
Listado HTML
```

---

# 19. Coincidencias obligatorias

Create:

```text
Model:       "nuevoAlumno"
HTML:        ${nuevoAlumno}
Controller:  @ModelAttribute("nuevoAlumno")
```

Update:

```text
Model:       "alumneEditar"
HTML:        ${alumneEditar}
Controller:  @ModelAttribute("alumneEditar")
```

Propiedad fecha:

```text
Entidad:   fechaNacimiento
Getter:    getFechaNacimiento()
Setter:    setFechaNacimiento()
Formulario:*{fechaNacimiento}
Listado:   ${alumne.fechaNacimiento}
```

---

# 20. Errores importantes encontrados

## `@NotBlank` aplicado a `LocalDate`

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

## Update sin validación

Sin `@Valid` y `BindingResult`, Hibernate puede detectar el error al guardar y devolver Whitelabel.

## Formulario moderno con Controller antiguo

HTML:

```html
data-th-object
data-th-field
```

Controller:

```java
@ModelAttribute
```

No conviene mezclarlo con varios `@RequestParam` para el mismo formulario.

## `BindingResult` mal colocado

Debe ir inmediatamente después del objeto validado.

## Volver a `index` sin reconstruir el Model

Cuando el POST devuelve `index`, no se ejecuta automáticamente el GET `/`.

Hay que volver a cargar:

```text
alumnes
totalAlumnes
titolPagina
missatge
```

## Error tipográfico

Ejemplo real:

```html
name="fefchaNacimiento"
```

Con `data-th-field` se reduce este riesgo.

---

# 21. Guía de depuración

## Whitelabel al enviar formulario

Revisar:

```text
data-th-object
@ModelAttribute
BindingResult
tipo de anotación
nombres de propiedades
```

## Se guarda, pero falla al mostrar

Revisar:

```text
data-th-text
getter correspondiente
propiedad eliminada o renombrada
```

## Create funciona, Update no

Revisar:

```text
id oculto
@ModelAttribute("alumneEditar")
@Valid
BindingResult
findById
setters
save
```

## Problemas con la fecha

Revisar:

```text
LocalDate
@NotNull
@Past
type="date"
data-th-field="*{fechaNacimiento}"
```

## NetBeans muestra código antiguo

```bash
./mvnw clean compile
```

O usar:

```text
Clean and Build
```

---

# 22. Prueba completa

Create:

```text
Campos vacíos
→ aparecen mensajes

Fecha futura
→ aparece error

Datos válidos
→ alumno guardado
```

Read:

```text
El alumno aparece
El contador aumenta
```

Update:

```text
Campos vacíos
→ aparecen mensajes

Fecha futura
→ aparece error

Datos válidos
→ cambios guardados
```

Delete:

```text
Eliminar alumno
→ desaparece
→ contador disminuye
```

H2:

```sql
SELECT * FROM ALUMNO;
```

---

# 23. Estado exacto para retomar el proyecto

```text
Entidad JPA                          ✅
ID autogenerado                     ✅
Repository                          ✅
H2                                  ✅
Create                              ✅
Read                                ✅
Update                              ✅
Delete                              ✅
LocalDate                           ✅
Validación de todos los campos      ✅
Mensajes en index.html              ✅
Mensajes en editar.html             ✅
@ModelAttribute                     ✅
@Valid                              ✅
BindingResult                       ✅
data-th-object                      ✅
data-th-field                       ✅
```

Pendiente:

```text
HomeController todavía contiene
lógica que debe pasar a un Service
```

---

# 24. Próximo paso recomendado

```text
1. Gestionar correctamente alumnos inexistentes.
2. Crear AlumnoService.
3. Mover la lógica fuera del Controller.
```

Ahora se usa:

```java
.orElse(null)
```

Y:

```java
if (alumnoEncontrado == null) {
    return "redirect:/";
}
```

La siguiente mejora será decidir cómo responder cuando el ID no existe:

```text
Mensaje
Redirección con aviso
Excepción propia
Página 404
```

Después se creará:

```text
service/AlumnoService.java
```

Arquitectura futura:

```text
HomeController
↓
AlumnoService
↓
AlumnoRepository
↓
H2
```

---

# 25. Resumen mental

```text
Alumno
→ define datos y reglas

Repository
→ habla con la base de datos

Controller
→ dirige la petición

Model
→ transporta objetos al HTML

data-th-object
→ enlaza formulario y objeto

data-th-field
→ enlaza input y propiedad

@ModelAttribute
→ construye el objeto recibido

@Valid
→ ejecuta las reglas

BindingResult
→ recoge los errores

save()
→ INSERT o UPDATE

findAll()
→ READ

findById()
→ busca un registro

deleteById()
→ DELETE
```

Punto de reanudación:

```text
CRUD completo y validado
↓
Create y Update protegidos
↓
Siguiente paso:
gestión de IDs inexistentes
↓
Después:
AlumnoService
```
