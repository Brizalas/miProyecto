# Guía completa: construir un CRUD con Spring Boot, Thymeleaf, JPA y H2

Esta guía reúne en un único documento el recorrido completo seguido en el proyecto **Gestor de alumnos**.

El objetivo no es documentar solamente el código final. La guía conserva la evolución del proyecto para poder repetir el proceso en una aplicación similar y entender **por qué aparece cada pieza**.

## Estado actual del proyecto

```text
Create ✅
Read   ✅
Update ✅
Delete ✅

Spring Boot             ✅
Thymeleaf               ✅
Spring Data JPA         ✅
Hibernate               ✅
H2 en memoria           ✅
LocalDate               ✅
Fecha de nacimiento     ✅
Dependencia Validation  ✅
Reglas de validación    ⏳ siguiente paso
Service                 ⏳ pendiente
```

La arquitectura actual es:

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

La arquitectura prevista será:

```text
Navegador
↓
Controller
↓
Service
↓
Repository
↓
Base de datos
```

## Cómo utilizar esta guía

El recorrido está dividido en tres grandes etapas:

```text
ETAPA 1
Construir el CRUD en memoria
↓
Comprender rutas, formularios, Model y Thymeleaf

ETAPA 2
Migrar la aplicación a JPA y H2
↓
Comprender entidades, Repository y persistencia

ETAPA 3
Mejorar el modelo de datos
↓
LocalDate, fechaNacimiento y preparación de validaciones
```

La siguiente etapa del proyecto será:

```text
ETAPA 4
Validación de formularios
↓
@NotBlank
@NotNull
@Past
@Valid
@ModelAttribute
BindingResult
```

> Nota importante: las primeras etapas contienen código histórico con `ArrayList`, `edad` y `Date`. No representan el estado final actual. Se mantienen porque forman parte del recorrido de aprendizaje. El estado vigente del modelo aparece en la etapa 3.

---

# PARTE I. CRUD EN MEMORIA

Esta primera parte permite comprender el circuito web completo antes de introducir una base de datos.

Aplicación web creada con **Java**, **Spring Boot**, **Thymeleaf** y **Maven** para aprender a construir un CRUD completo.

Actualmente los alumnos se guardan en una lista de Java en memoria. Esto permite practicar el funcionamiento de una aplicación web antes de añadir una base de datos.

## Estado actual

```text
Create ✅  Crear alumnos
Read   ✅  Mostrar alumnos
Update ✅  Editar alumnos
Delete ✅  Eliminar alumnos
```

> Los datos desaparecen al reiniciar la aplicación porque todavía no existe una base de datos.

---

# 1. Estructura del proyecto

```text
src
├── main
│   ├── java
│   │   └── com/cristian/gestoralumnos
│   │       ├── GestoralumnosApplication.java
│   │       ├── controller
│   │       │   └── HomeController.java
│   │       └── model
│   │           └── Alumno.java
│   └── resources
│       ├── application.properties
│       ├── static
│       └── templates
│           ├── index.html
│           └── editar.html
└── test
    └── java
```

```text
src/main/java        → clases Java de la aplicación
src/main/resources   → configuración, HTML, CSS, JavaScript e imágenes
src/test/java        → pruebas
```

La carpeta `templates` contiene las páginas HTML procesadas por Thymeleaf.

La carpeta `static` se utilizará para CSS, JavaScript e imágenes.

---

# 2. Cómo funciona una petición web

La clase principal contiene el método `main()` y arranca Spring Boot:

```text
main()
↓
Spring Boot arranca
↓
Se inicia el servidor web
↓
La aplicación queda disponible en http://localhost:8080
```

Después, el flujo habitual es:

```text
Navegador
↓ petición HTTP
Controller
↓ añade datos al Model
Thymeleaf
↓ genera HTML
Navegador
```

Versión resumida:

```text
URL → Controller → Model → Thymeleaf → HTML
```

---

# 3. Modelo de datos: `Alumno`

La clase `Alumno` representa un alumno de la aplicación.

Cada objeto contiene:

```text
id
nombre
apellido
edad
fecha de nacimiento
modalidad
profesor
```

## Código de `Alumno.java`

```java
package com.cristian.gestoralumnos.model;

import java.util.Date;

public class Alumno {

    private long id;
    private String nombre;
    private String apellido;
    private String edad;
    private Date fechaNac;
    private String modalidad;
    private String profesor;

    public Alumno() {
    }

    public Alumno(
            long id,
            String nombre,
            String apellido,
            String edad,
            Date fechaNac,
            String modalidad,
            String profesor
    ) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.fechaNac = fechaNac;
        this.modalidad = modalidad;
        this.profesor = profesor;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public String getEdad() {
        return edad;
    }

    public void setEdad(String edad) {
        this.edad = edad;
    }

    public Date getFechaNac() {
        return fechaNac;
    }

    public void setFechaNac(Date fechaNac) {
        this.fechaNac = fechaNac;
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
        return "Alumno{" +
                "id=" + id +
                ", nombre=" + nombre +
                ", apellido=" + apellido +
                ", edad=" + edad +
                ", fechaNac=" + fechaNac +
                ", modalidad=" + modalidad +
                ", profesor=" + profesor +
                '}';
    }
}
```

Los getters permiten leer las propiedades del objeto.

Los setters permiten modificarlas durante la operación Update.

Por ejemplo:

```java
alumne.getNombre();
alumne.setNombre("Laura");
```

Cuando Thymeleaf utiliza:

```html
${alumne.nombre}
```

por detrás accede a:

```java
alumne.getNombre()
```

---

# 4. Lista temporal e identificadores

En `HomeController` guardamos los alumnos en una lista:

```java
private List<Alumno> alumnos = new ArrayList<>();
```

La lista está declarada como atributo del Controller para que no se vuelva a crear cada vez que se carga la página.

También utilizamos un contador independiente:

```java
private long siguienteId = 1;
```

Al crear un alumno:

```java
long nouId = siguienteId;
siguienteId++;
```

El primer alumno recibe el ID `1`, el segundo el `2`, etc.

No usamos:

```java
alumnos.size() + 1
```

porque después de eliminar un alumno podría generarse un ID duplicado.

Ejemplo:

```text
Antes:       1, 2, 3
Eliminar 2:  1, 3
Nuevo ID:    4
Resultado:   1, 3, 4
```

Los ID no tienen que ser consecutivos. Solo deben ser únicos.

---

# 5. Read: mostrar la página y los alumnos

## Ruta principal

```java
@GetMapping("/")
public String mostrarInicio(Model model) {
    model.addAttribute("titolPagina", "Gestor d'alumnes");
    model.addAttribute("missatge", "Benvinguda a la meva app de gestió d'alumnes");
    model.addAttribute("totalAlumnes", alumnos.size());
    model.addAttribute("alumnes", alumnos);

    return "index";
}
```

`@GetMapping("/")` indica que el método responde a:

```text
GET http://localhost:8080/
```

`return "index"` no devuelve el texto `index`. Le indica a Spring que debe cargar:

```text
src/main/resources/templates/index.html
```

## El objeto `Model`

`Model` transporta información desde el Controller hasta el HTML:

```java
model.addAttribute("alumnes", alumnos);
```

En Thymeleaf se recupera con el mismo nombre:

```html
${alumnes}
```

Los nombres deben coincidir exactamente, incluyendo mayúsculas y minúsculas.

## Mostrar la lista con Thymeleaf

```html
<li data-th-each="alumne : ${alumnes}">
    <strong data-th-text="${alumne.nombre}">Nom</strong>
</li>
```

Esto funciona como un `for each` de Java:

```java
for (Alumno alumne : alumnos) {
    System.out.println(alumne.getNombre());
}
```

```text
alumnes → lista completa
alumne  → alumno actual de cada vuelta
```

---

# 6. Create: añadir alumnos

## Formulario de `index.html`

```html
<form action="/afegir-alumne" method="post">
    <label for="nombre">Nom:</label>
    <input type="text" id="nombre" name="nombre">

    <label for="apellido">Cognom:</label>
    <input type="text" id="apellido" name="apellido">

    <label for="edad">Edat:</label>
    <input type="text" id="edad" name="edad">

    <label for="modalidad">Modalitat:</label>
    <input type="text" id="modalidad" name="modalidad">

    <label for="profesor">Professor:</label>
    <input type="text" id="profesor" name="profesor">

    <button type="submit">Afegir</button>
</form>
```

```text
action → ruta que recibirá el formulario
method → tipo de petición HTTP
name   → nombre enviado al Controller
id     → identifica el elemento dentro del HTML
```

La conexión depende de `name`:

```text
HTML name="nombre"
↓
Java @RequestParam String nombre
```

## Método del Controller

```java
@PostMapping("/afegir-alumne")
public String afegirAlumne(
        @RequestParam String nombre,
        @RequestParam String apellido,
        @RequestParam String edad,
        @RequestParam String modalidad,
        @RequestParam String profesor
) {
    long nouId = siguienteId;
    siguienteId++;

    Alumno nouAlumne = new Alumno(
            nouId,
            nombre,
            apellido,
            edad,
            null,
            modalidad,
            profesor
    );

    alumnos.add(nouAlumne);

    return "redirect:/";
}
```

Flujo:

```text
Formulario
↓ POST /afegir-alumne
@RequestParam recoge los datos
↓
Se crea un objeto Alumno
↓
Se añade a la lista
↓
redirect:/
↓ GET /
La página muestra la lista actualizada
```

Este patrón es habitual:

```text
POST → procesar → redirect → GET
```

---

# 7. Delete: eliminar alumnos

Cada alumno tiene su propio formulario de eliminación dentro del `data-th-each`:

```html
<form action="/eliminar-alumne" method="post">
    <input
        type="hidden"
        name="id"
        data-th-value="${alumne.id}">

    <button type="submit">Eliminar</button>
</form>
```

El campo oculto no se muestra, pero envía el ID del alumno:

```html
<input type="hidden" name="id" value="3">
```

El botón no elimina directamente. Solo envía el formulario.

## Método del Controller

```java
@PostMapping("/eliminar-alumne")
public String eliminarAlumne(@RequestParam long id) {
    alumnos.removeIf(alumne -> alumne.getId() == id);

    return "redirect:/";
}
```

La expresión:

```java
alumne -> alumne.getId() == id
```

significa:

```text
Para cada alumno de la lista,
comprueba si su ID coincide con el ID recibido.
Si coincide, elimínalo.
```

Flujo:

```text
Botón Eliminar
↓ POST /eliminar-alumne
El formulario envía el ID
↓
@RequestParam recibe el ID
↓
removeIf elimina el objeto correspondiente
↓
redirect:/
↓
La lista se muestra actualizada
```

---

# 8. Update, parte 1: abrir el formulario de edición

Cada alumno tiene un botón Editar:

```html
<form action="/editar-alumne" method="get">
    <input
        type="hidden"
        name="id"
        data-th-value="${alumne.id}">

    <button type="submit">Editar</button>
</form>
```

Aquí usamos GET porque todavía no modificamos datos. Solo pedimos la página de edición.

Al pulsar el botón del alumno con ID 3 se genera una petición parecida a:

```text
GET /editar-alumne?id=3
```

El botón envía el ID, pero no envía automáticamente el objeto `Alumno` completo. El Controller debe localizarlo dentro de la lista.

## Buscar el alumno

```java
@GetMapping("/editar-alumne")
public String mostrarFormularioEditarAlumne(
        @RequestParam long id,
        Model model
) {
    Alumno alumnoEncontrado = null;

    for (Alumno alumne : alumnos) {
        if (alumne.getId() == id) {
            alumnoEncontrado = alumne;
            break;
        }
    }

    model.addAttribute("alumneEditar", alumnoEncontrado);

    return "editar";
}
```

Paso a paso:

```java
Alumno alumnoEncontrado = null;
```

Prepara una variable para guardar el objeto encontrado.

```java
for (Alumno alumne : alumnos)
```

Recorre la lista.

```java
if (alumne.getId() == id)
```

Compara el ID de cada objeto con el ID recibido.

```java
alumnoEncontrado = alumne;
```

Guarda el objeto completo.

```java
break;
```

Detiene el bucle porque los ID son únicos.

```java
model.addAttribute("alumneEditar", alumnoEncontrado);
```

Envía el objeto a Thymeleaf con el nombre `alumneEditar`.

```java
return "editar";
```

Carga:

```text
src/main/resources/templates/editar.html
```

---

# 9. Update, parte 2: formulario `editar.html`

```html
<!DOCTYPE html>
<html lang="ca">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Editar alumne</title>
    </head>

    <body>
        <h1>Editar alumne</h1>

        <form action="/actualizar-alumne" method="post">

            <input
                type="hidden"
                name="id"
                data-th-value="${alumneEditar.id}">

            <label for="nombre">Nom:</label>
            <input
                type="text"
                id="nombre"
                name="nombre"
                data-th-value="${alumneEditar.nombre}">

            <label for="apellido">Cognom:</label>
            <input
                type="text"
                id="apellido"
                name="apellido"
                data-th-value="${alumneEditar.apellido}">

            <label for="edad">Edat:</label>
            <input
                type="text"
                id="edad"
                name="edad"
                data-th-value="${alumneEditar.edad}">

            <label for="modalidad">Modalitat:</label>
            <input
                type="text"
                id="modalidad"
                name="modalidad"
                data-th-value="${alumneEditar.modalidad}">

            <label for="profesor">Professor:</label>
            <input
                type="text"
                id="profesor"
                name="profesor"
                data-th-value="${alumneEditar.profesor}">

            <button type="submit">Guardar canvis</button>
        </form>

        <a href="/">Tornar a l'inici</a>
    </body>
</html>
```

`data-th-value` rellena los inputs con los datos actuales:

```html
data-th-value="${alumneEditar.nombre}"
```

Equivale aproximadamente a:

```java
alumnoEncontrado.getNombre();
```

El ID se mantiene oculto porque el usuario no debe modificarlo, pero el Controller lo necesita para localizar el objeto correcto.

---

# 10. Update, parte 3: guardar los cambios

El formulario de edición envía una petición POST a:

```text
/actualizar-alumne
```

El Controller recibe todos los campos:

```java
@PostMapping("/actualizar-alumne")
public String actualizarAlumne(
        @RequestParam long id,
        @RequestParam String nombre,
        @RequestParam String apellido,
        @RequestParam String edad,
        @RequestParam String modalidad,
        @RequestParam String profesor
) {
    for (Alumno alumne : alumnos) {
        if (alumne.getId() == id) {
            alumne.setNombre(nombre);
            alumne.setApellido(apellido);
            alumne.setEdad(edad);
            alumne.setModalidad(modalidad);
            alumne.setProfesor(profesor);
            break;
        }
    }

    return "redirect:/";
}
```

El método vuelve a buscar el objeto por su ID y utiliza sus setters:

```java
alumne.setNombre(nombre);
alumne.setApellido(apellido);
alumne.setEdad(edad);
alumne.setModalidad(modalidad);
alumne.setProfesor(profesor);
```

No crea un alumno nuevo y no elimina el anterior.

Modifica directamente el mismo objeto, que conserva su ID.

Flujo completo:

```text
Botón Editar
↓ GET /editar-alumne?id=...
Controller busca el objeto
↓
Model envía alumneEditar
↓
editar.html muestra los datos actuales
↓
El usuario modifica los campos
↓ POST /actualizar-alumne
Controller busca otra vez el objeto
↓
Los setters modifican sus propiedades
↓
redirect:/
↓
La página principal muestra los cambios
```

---

# 11. Código completo de `HomeController.java`

```java
package com.cristian.gestoralumnos.controller;

import com.cristian.gestoralumnos.model.Alumno;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private List<Alumno> alumnos = new ArrayList<>();
    private long siguienteId = 1;

    @GetMapping("/")
    public String mostrarInicio(Model model) {
        model.addAttribute("titolPagina", "Gestor d'alumnes");
        model.addAttribute("missatge", "Benvinguda a la meva app de gestió d'alumnes");
        model.addAttribute("totalAlumnes", alumnos.size());
        model.addAttribute("alumnes", alumnos);

        return "index";
    }

    @PostMapping("/afegir-alumne")
    public String afegirAlumne(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String edad,
            @RequestParam String modalidad,
            @RequestParam String profesor
    ) {
        long nouId = siguienteId;
        siguienteId++;

        Alumno nouAlumne = new Alumno(
                nouId,
                nombre,
                apellido,
                edad,
                null,
                modalidad,
                profesor
        );

        alumnos.add(nouAlumne);

        return "redirect:/";
    }

    @PostMapping("/eliminar-alumne")
    public String eliminarAlumne(@RequestParam long id) {
        alumnos.removeIf(alumne -> alumne.getId() == id);

        return "redirect:/";
    }

    @GetMapping("/editar-alumne")
    public String mostrarFormularioEditarAlumne(
            @RequestParam long id,
            Model model
    ) {
        Alumno alumnoEncontrado = null;

        for (Alumno alumne : alumnos) {
            if (alumne.getId() == id) {
                alumnoEncontrado = alumne;
                break;
            }
        }

        model.addAttribute("alumneEditar", alumnoEncontrado);

        return "editar";
    }

    @PostMapping("/actualizar-alumne")
    public String actualizarAlumne(
            @RequestParam long id,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String edad,
            @RequestParam String modalidad,
            @RequestParam String profesor
    ) {
        for (Alumno alumne : alumnos) {
            if (alumne.getId() == id) {
                alumne.setNombre(nombre);
                alumne.setApellido(apellido);
                alumne.setEdad(edad);
                alumne.setModalidad(modalidad);
                alumne.setProfesor(profesor);
                break;
            }
        }

        return "redirect:/";
    }
}
```

---

# 12. Código completo de `index.html`

```html
<!DOCTYPE html>
<html lang="ca">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title data-th-text="${titolPagina}">Gestor d'alumnes</title>
    </head>

    <body>
        <h1 data-th-text="${titolPagina}">Gestor d'alumnes</h1>

        <p data-th-text="${missatge}">Benvinguda</p>

        <hr>

        <p>
            Total d'alumnes registrats:
            <strong data-th-text="${totalAlumnes}">0</strong>
        </p>

        <h2>Afegir alumne</h2>

        <form action="/afegir-alumne" method="post">
            <label for="nombre">Nom:</label>
            <input type="text" id="nombre" name="nombre">

            <label for="apellido">Cognom:</label>
            <input type="text" id="apellido" name="apellido">

            <label for="edad">Edat:</label>
            <input type="text" id="edad" name="edad">

            <label for="modalidad">Modalitat:</label>
            <input type="text" id="modalidad" name="modalidad">

            <label for="profesor">Professor:</label>
            <input type="text" id="profesor" name="profesor">

            <button type="submit">Afegir</button>
        </form>

        <hr>

        <h2>Alumnes</h2>

        <ul>
            <li data-th-each="alumne : ${alumnes}">
                <strong data-th-text="${alumne.nombre}">Nom</strong>
                <span data-th-text="${alumne.apellido}">Cognom</span>
                -
                <span data-th-text="${alumne.edad}">Edat</span>
                anys -
                <span data-th-text="${alumne.modalidad}">Modalitat</span>
                - Professor/a:
                <span data-th-text="${alumne.profesor}">Professor</span>

                <form action="/eliminar-alumne" method="post">
                    <input
                        type="hidden"
                        name="id"
                        data-th-value="${alumne.id}">
                    <button type="submit">Eliminar</button>
                </form>

                <form action="/editar-alumne" method="get">
                    <input
                        type="hidden"
                        name="id"
                        data-th-value="${alumne.id}">
                    <button type="submit">Editar</button>
                </form>
            </li>
        </ul>
    </body>
</html>
```

---

# 13. Relación entre HTML y Controller

Las rutas y métodos deben coincidir:

```text
HTML                                      Controller

GET  /                         →          @GetMapping("/")
POST /afegir-alumne            →          @PostMapping("/afegir-alumne")
POST /eliminar-alumne          →          @PostMapping("/eliminar-alumne")
GET  /editar-alumne            →          @GetMapping("/editar-alumne")
POST /actualizar-alumne        →          @PostMapping("/actualizar-alumne")
```

También deben coincidir los nombres de los parámetros:

```text
HTML name="id"          → @RequestParam long id
HTML name="nombre"      → @RequestParam String nombre
HTML name="apellido"    → @RequestParam String apellido
HTML name="edad"        → @RequestParam String edad
HTML name="modalidad"   → @RequestParam String modalidad
HTML name="profesor"    → @RequestParam String profesor
```

Y los nombres del `Model`:

```text
Java                                      Thymeleaf

"titolPagina"              →             ${titolPagina}
"missatge"                 →             ${missatge}
"totalAlumnes"             →             ${totalAlumnes}
"alumnes"                  →             ${alumnes}
"alumneEditar"             →             ${alumneEditar}
```

---

# 14. GET y POST en este proyecto

Usamos GET para solicitar páginas o información:

```java
@GetMapping("/")
@GetMapping("/editar-alumne")
```

Usamos POST para enviar datos que crean, modifican o eliminan información:

```java
@PostMapping("/afegir-alumne")
@PostMapping("/actualizar-alumne")
@PostMapping("/eliminar-alumne")
```

Una causa habitual de `Whitelabel Error Page` es que el HTML envíe GET y el Controller espere POST, o al revés.

Ejemplo incorrecto:

```html
<form action="/editar-alumne" method="get">
```

```java
@PostMapping("/editar-alumne")
```

Ejemplo correcto:

```html
<form action="/editar-alumne" method="get">
```

```java
@GetMapping("/editar-alumne")
```

---

# 15. Errores frecuentes

## Whitelabel Error Page

Puede aparecer por:

```text
Ruta inexistente
GET y POST no coinciden
Plantilla HTML inexistente
Expresión Thymeleaf mal escrita
Parámetro name y @RequestParam diferentes
Variable del Model con nombre diferente
```

## Expresión Thymeleaf incorrecta

Incorrecto:

```html
${alumnes]
```

Correcto:

```html
${alumnes}
```

## Error tipográfico en `data-th-value`

Incorrecto:

```html
data-th-value="4{alumneEditar.profesor}"
```

Correcto:

```html
data-th-value="${alumneEditar.profesor}"
```

## Mayúsculas y minúsculas

Incorrecto:

```java
model.addAttribute("Alumnes", alumnos);
```

```html
${alumnes}
```

Correcto:

```java
model.addAttribute("alumnes", alumnos);
```

```html
${alumnes}
```

## Ubicación de `Alumno.java`

Correcto:

```text
src/main/java/com/cristian/gestoralumnos/model/Alumno.java
```

No debe estar en `src/test/java` ni en `src/main/resources`.

---

# 16. Configuración para desarrollo

En:

```text
src/main/resources/application.properties
```

se puede añadir:

```properties
spring.thymeleaf.cache=false
```

Esto evita que Thymeleaf reutilice versiones antiguas de las plantillas durante el desarrollo.

Flujo recomendado en NetBeans:

```text
Cambios en HTML
↓
Guardar
↓
Refrescar navegador o Run File
```

```text
Cambios en Java
↓
Guardar
↓
Run Project o reiniciar la aplicación
```

Si se modifica `pom.xml`, dependencias, paquetes o estructura de carpetas, conviene reiniciar el proyecto.

---

# 17. Qué hemos aprendido

```text
@Controller identifica un controlador web.
@GetMapping responde a peticiones GET.
@PostMapping responde a peticiones POST.
@RequestParam recoge datos enviados por formularios.
Model transporta datos del Controller al HTML.
Thymeleaf incrusta datos Java dentro del HTML.
data-th-text escribe contenido dentro de una etiqueta.
data-th-value coloca un valor dentro de un input.
data-th-each recorre una lista.
redirect:/ realiza una nueva petición a la página principal.
Los ID permiten localizar objetos concretos.
Los getters leen propiedades.
Los setters modifican propiedades.
```

Idea central:

```text
Java prepara o modifica datos
↓
Model los transporta cuando es necesario
↓
Thymeleaf genera el HTML
↓
El navegador muestra la aplicación
```

---

# 18. Limitaciones actuales

La aplicación funciona, pero todavía utiliza:

```java
private List<Alumno> alumnos = new ArrayList<>();
```

Por tanto:

```text
Los datos solo viven en memoria.
Se pierden al reiniciar el servidor.
El Controller contiene demasiadas responsabilidades.
No existe validación de formularios.
No existe control específico cuando un ID no se encuentra.
```

Esta solución es adecuada para aprender el recorrido completo de un CRUD, pero no es todavía la arquitectura final de producción.

---

# 19. Próximos pasos recomendados

Seguir este orden:

```text
1. Mejorar el HTML y mostrar los alumnos en una tabla.
2. Añadir validación a los formularios.
3. Convertir Alumno en una entidad con @Entity.
4. Añadir una base de datos H2.
5. Crear AlumnoRepository.
6. Crear AlumnoService.
7. Mover la lógica fuera del Controller.
8. Gestionar correctamente alumnos no encontrados.
9. Añadir informes y exportación CSV/PDF.
10. Preparar el despliegue en producción.
```

La futura arquitectura será:

```text
Controller
↓
Service
↓
Repository
↓
Base de datos
```

---

# 20. Resumen final del CRUD

## Create

```text
Formulario POST
↓
@RequestParam
↓
new Alumno(...)
↓
alumnos.add(...)
```

## Read

```text
GET /
↓
Model
↓
data-th-each
↓
Lista mostrada
```

## Update

```text
GET /editar-alumne?id=...
↓
Buscar objeto
↓
Mostrar editar.html
↓
POST /actualizar-alumne
↓
Aplicar setters
```

## Delete

```text
POST /eliminar-alumne
↓
Recibir ID
↓
removeIf(...)
```

El primer CRUD completo ya está terminado. La siguiente gran etapa será sustituir la lista temporal por una base de datos y separar la aplicación en capas.

---

# PARTE II. MIGRACIÓN DEL CRUD A JPA Y H2

Una vez comprendido el CRUD en memoria, la aplicación sustituye la `ArrayList` por una base de datos H2 y un Repository de Spring Data JPA.

Esta guía documenta paso a paso cómo transformar un CRUD que guarda alumnos en una lista de Java en memoria en un CRUD conectado a una base de datos H2 mediante Spring Data JPA.

El punto de partida era este:

```java
private List<Alumno> alumnos = new ArrayList<>();
```

La aplicación ya permitía:

```text
Create ✅
Read   ✅
Update ✅
Delete ✅
```

Pero tenía una limitación importante:

```text
Los datos desaparecían al reiniciar la aplicación.
```

La nueva arquitectura queda así:

```text
Navegador
↓
Controller
↓
AlumnoRepository
↓
JPA / Hibernate
↓
Base de datos H2
```

---

# 1. Objetivo de la migración

Antes:

```text
Controller
↓
ArrayList
```

Después:

```text
Controller
↓
Repository
↓
JPA / Hibernate
↓
H2
```

El proceso completo se realiza en este orden:

```text
1. Añadir JPA y H2 al pom.xml.
2. Configurar application.properties.
3. Convertir Alumno en una entidad.
4. Crear AlumnoRepository.
5. Adaptar Read.
6. Adaptar Create.
7. Adaptar Delete.
8. Adaptar Update.
9. Comprobar el CRUD completo.
10. Abrir la consola de H2.
```

---

# 2. Añadir JPA y H2 al `pom.xml`

Archivo:

```text
pom.xml
```

Dentro del bloque:

```xml
<dependencies>
```

se añaden las dependencias de JPA, H2 y la consola web de H2.

```xml
<!-- JPA: conecta las clases Java con la base de datos -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Base de datos H2 -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Consola web de H2 para Spring Boot 4 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-h2console</artifactId>
    <scope>runtime</scope>
</dependency>
```

El bloque principal puede quedar así:

```xml
<dependencies>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>

    <!-- JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- H2 -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Consola web de H2 -->
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
        <artifactId>spring-boot-starter-thymeleaf-test</artifactId>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc-test</artifactId>
        <scope>test</scope>
    </dependency>

</dependencies>
```

No es necesario añadir versiones manualmente.

Spring Boot gestiona las versiones compatibles mediante el bloque `parent`:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.7</version>
    <relativePath/>
</parent>
```

## Comprobar el `pom.xml`

Desde la carpeta raíz del proyecto:

```bash
./mvnw clean compile
```

El resultado correcto debe terminar con:

```text
BUILD SUCCESS
```

`clean compile` compila el proyecto, pero no arranca la aplicación.

Para arrancar Spring Boot:

```bash
./mvnw spring-boot:run
```

---

# 3. Configurar `application.properties`

Archivo:

```text
src/main/resources/application.properties
```

Contenido:

```properties
spring.application.name=gestoralumnos

# Base de datos H2 en memoria
spring.datasource.url=jdbc:h2:mem:gestoralumnosdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Configuración de JPA e Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Consola web de H2
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console/

# Evita la caché de Thymeleaf durante el desarrollo
spring.thymeleaf.cache=false
```

## Nombre de la aplicación

```properties
spring.application.name=gestoralumnos
```

Define el nombre de la aplicación dentro de Spring Boot.

## URL de conexión

```properties
spring.datasource.url=jdbc:h2:mem:gestoralumnosdb
```

La base de datos se llama:

```text
gestoralumnosdb
```

La palabra:

```text
mem
```

significa que la base de datos vive en memoria.

```text
Aplicación encendida
↓
La base de datos existe

Aplicación apagada
↓
Los datos desaparecen
```

Aunque los datos desaparezcan al apagar la aplicación, ya no están almacenados en una `ArrayList`, sino en una base de datos real.

## Usuario y contraseña

```properties
spring.datasource.username=sa
spring.datasource.password=
```

Datos de acceso:

```text
Usuario:     sa
Contraseña:  vacía
```

## Gestión de tablas

```properties
spring.jpa.hibernate.ddl-auto=update
```

Hibernate analiza las entidades y crea o actualiza las tablas correspondientes.

```text
Clase con @Entity
↓
Hibernate analiza sus atributos
↓
Crea o actualiza la tabla
```

## Mostrar SQL

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Estas propiedades muestran en la consola el SQL generado por Hibernate.

Ejemplos:

```sql
insert into alumno (...);

select ... from alumno;

update alumno ...;

delete from alumno ...;
```

## Consola web de H2

```properties
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console/
```

La consola queda disponible en:

```text
http://localhost:8080/h2-console/
```

En este proyecto fue necesario incluir la barra final:

```text
/h2-console/
```

---

# 4. Convertir `Alumno` en una entidad JPA

Archivo:

```text
src/main/java/com/cristian/gestoralumnos/model/Alumno.java
```

Se añaden estos imports:

```java
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
```

Sobre la clase se añade:

```java
@Entity
```

El atributo `id` queda así:

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

## Significado de las anotaciones

### `@Entity`

```java
@Entity
```

Indica que la clase representa una tabla de la base de datos.

### `@Id`

```java
@Id
```

Indica que el atributo es la clave primaria.

### `@GeneratedValue`

```java
@GeneratedValue(strategy = GenerationType.IDENTITY)
```

Indica que la base de datos generará automáticamente el identificador.

Antes se utilizaba:

```java
private long siguienteId = 1;
```

Ahora H2 genera los identificadores:

```text
Alumno nuevo con id = null
↓
JPA lo guarda
↓
H2 genera el ID
↓
ID 1, 2, 3...
```

## Diferencia entre `long` y `Long`

Antes:

```java
private long id;
```

Después:

```java
private Long id;
```

Diferencia:

```text
long → tipo primitivo; su valor inicial es 0
Long → objeto; puede contener null
```

Un alumno nuevo comienza con:

```java
id = null;
```

Esto permite que JPA entienda que debe crear un registro nuevo.

## Código completo de `Alumno.java`

```java
package com.cristian.gestoralumnos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Date;

@Entity
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellido;
    private String edad;
    private Date fechaNac;
    private String modalidad;
    private String profesor;

    public Alumno() {
    }

    public Alumno(
            Long id,
            String nombre,
            String apellido,
            String edad,
            Date fechaNac,
            String modalidad,
            String profesor
    ) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.fechaNac = fechaNac;
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

    public String getEdad() {
        return edad;
    }

    public void setEdad(String edad) {
        this.edad = edad;
    }

    public Date getFechaNac() {
        return fechaNac;
    }

    public void setFechaNac(Date fechaNac) {
        this.fechaNac = fechaNac;
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
        return "Alumno{" +
                "id=" + id +
                ", nombre=" + nombre +
                ", apellido=" + apellido +
                ", edad=" + edad +
                ", fechaNac=" + fechaNac +
                ", modalidad=" + modalidad +
                ", profesor=" + profesor +
                '}';
    }
}
```

---

# 5. Crear `AlumnoRepository`

Se crea el paquete:

```text
src/main/java/com/cristian/gestoralumnos/repository
```

Dentro se crea:

```text
AlumnoRepository.java
```

Código:

```java
package com.cristian.gestoralumnos.repository;

import com.cristian.gestoralumnos.model.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> {

}
```

## Significado de `JpaRepository<Alumno, Long>`

```java
JpaRepository<Alumno, Long>
```

El primer tipo:

```java
Alumno
```

indica la entidad que gestionará.

El segundo:

```java
Long
```

indica el tipo del identificador.

Debe coincidir con:

```java
@Id
private Long id;
```

## Métodos disponibles automáticamente

Aunque la interfaz esté vacía, Spring Data JPA proporciona métodos como:

```java
findAll();
findById(id);
save(alumno);
deleteById(id);
count();
existsById(id);
```

Ejemplos:

```java
alumnoRepository.findAll();
```

Obtiene todos los alumnos.

```java
alumnoRepository.findById(1L);
```

Busca el alumno con ID `1`.

```java
alumnoRepository.save(alumno);
```

Crea o actualiza un alumno.

```java
alumnoRepository.deleteById(1L);
```

Elimina el alumno con ID `1`.

No es necesario añadir `@Repository`, porque Spring detecta automáticamente las interfaces que extienden `JpaRepository`.

---

# 6. Inyectar el Repository en `HomeController`

En `HomeController.java` se añade el import:

```java
import com.cristian.gestoralumnos.repository.AlumnoRepository;
```

Dentro de la clase se declara:

```java
private final AlumnoRepository alumnoRepository;
```

Y se crea el constructor:

```java
public HomeController(AlumnoRepository alumnoRepository) {
    this.alumnoRepository = alumnoRepository;
}
```

Estructura:

```java
@Controller
public class HomeController {

    private final AlumnoRepository alumnoRepository;

    public HomeController(AlumnoRepository alumnoRepository) {
        this.alumnoRepository = alumnoRepository;
    }

    // Métodos del Controller
}
```

Esto se llama inyección de dependencias.

```text
HomeController necesita AlumnoRepository
↓
Spring crea el Repository
↓
Spring lo entrega al constructor
```

No se crea manualmente:

```java
new AlumnoRepository();
```

Spring se encarga de proporcionar su implementación.

---

# 7. Adaptar Read

Antes el método principal enviaba la lista temporal:

```java
model.addAttribute("totalAlumnes", alumnos.size());
model.addAttribute("alumnes", alumnos);
```

Ahora consulta la base de datos:

```java
@GetMapping("/")
public String mostrarInicio(Model model) {

    List<Alumno> alumnosGuardados = alumnoRepository.findAll();

    model.addAttribute("titolPagina", "Gestor d'alumnes");
    model.addAttribute(
            "missatge",
            "Benvinguda a la meva app de gestió d'alumnes"
    );
    model.addAttribute("totalAlumnes", alumnosGuardados.size());
    model.addAttribute("alumnes", alumnosGuardados);

    return "index";
}
```

## Qué hace `findAll()`

```java
alumnoRepository.findAll();
```

Recupera todos los registros de la tabla `ALUMNO`.

El resultado se guarda en:

```java
List<Alumno> alumnosGuardados
```

Después se envía a Thymeleaf:

```java
model.addAttribute("alumnes", alumnosGuardados);
```

## El HTML no necesita cambios

Thymeleaf continúa recibiendo una lista llamada:

```html
${alumnes}
```

Por tanto, sigue funcionando:

```html
<li data-th-each="alumne : ${alumnes}">
```

A Thymeleaf no le importa si la lista procede de una `ArrayList` o de una base de datos.

```text
Antes:
ArrayList → Model → Thymeleaf

Ahora:
H2 → Repository → Model → Thymeleaf
```

---

# 8. Adaptar Create

Antes se generaba el ID manualmente:

```java
long nouId = siguienteId;
siguienteId++;
```

Y se añadía el alumno a la lista:

```java
alumnos.add(nouAlumne);
```

Ahora el método queda así:

```java
@PostMapping("/afegir-alumne")
public String afegirAlumne(
        @RequestParam String nombre,
        @RequestParam String apellido,
        @RequestParam String edad,
        @RequestParam String modalidad,
        @RequestParam String profesor
) {
    Alumno nouAlumne = new Alumno(
            null,
            nombre,
            apellido,
            edad,
            null,
            modalidad,
            profesor
    );

    alumnoRepository.save(nouAlumne);

    return "redirect:/";
}
```

## ID con valor `null`

```java
new Alumno(
        null,
        nombre,
        apellido,
        edad,
        null,
        modalidad,
        profesor
);
```

El primer `null` corresponde al ID.

Como el ID no existe todavía, JPA entiende que debe crear un nuevo registro.

```text
id = null
↓
save()
↓
INSERT
↓
H2 genera el identificador
```

El segundo `null` corresponde a:

```java
fechaNac
```

Todavía no se recoge la fecha de nacimiento desde el formulario.

## Guardar con `save()`

```java
alumnoRepository.save(nouAlumne);
```

Sustituye a:

```java
alumnos.add(nouAlumne);
```

El recorrido completo queda así:

```text
Formulario
↓
POST /afegir-alumne
↓
@RequestParam
↓
new Alumno(...)
↓
alumnoRepository.save(...)
↓
Hibernate genera INSERT
↓
H2 guarda el registro
↓
redirect:/
↓
findAll()
↓
La página muestra el nuevo alumno
```

Después de adaptar Create puede eliminarse:

```java
private long siguienteId = 1;
```

La base de datos ya genera los identificadores.

---

# 9. Adaptar Delete

Antes:

```java
@PostMapping("/eliminar-alumne")
public String eliminarAlumne(@RequestParam long id) {
    alumnos.removeIf(alumne -> alumne.getId() == id);

    return "redirect:/";
}
```

Después:

```java
@PostMapping("/eliminar-alumne")
public String eliminarAlumne(@RequestParam Long id) {

    alumnoRepository.deleteById(id);

    return "redirect:/";
}
```

## Qué hace `deleteById()`

```java
alumnoRepository.deleteById(id);
```

Elimina de la tabla el registro cuyo ID coincide con el valor recibido.

Recorrido:

```text
Botón Eliminar
↓
POST /eliminar-alumne
↓
El formulario envía el ID
↓
@RequestParam Long id
↓
deleteById(id)
↓
H2 elimina el registro
↓
redirect:/
↓
findAll()
↓
La lista y el contador se actualizan
```

El formulario HTML puede seguir igual:

```html
<form action="/eliminar-alumne" method="post">
    <input
        type="hidden"
        name="id"
        data-th-value="${alumne.id}">

    <button type="submit">Eliminar</button>
</form>
```

Deben coincidir:

```text
HTML action="/eliminar-alumne"
Controller @PostMapping("/eliminar-alumne")

HTML method="post"
Controller @PostMapping

HTML name="id"
Controller @RequestParam Long id
```

---

# 10. Adaptar Update: abrir el formulario

Antes el Controller recorría la `ArrayList`:

```java
Alumno alumnoEncontrado = null;

for (Alumno a : alumnos) {
    if (a.getId() == id) {
        alumnoEncontrado = a;
        break;
    }
}
```

Ahora se utiliza:

```java
alumnoRepository.findById(id);
```

El método completo queda así:

```java
@GetMapping("/editar-alumne")
public String mostrarFormularioEditarAlumne(
        @RequestParam Long id,
        Model model
) {
    Alumno alumnoEncontrado = alumnoRepository
            .findById(id)
            .orElse(null);

    model.addAttribute("alumneEditar", alumnoEncontrado);

    return "editar";
}
```

## Qué devuelve `findById()`

```java
alumnoRepository.findById(id)
```

No devuelve directamente un `Alumno`.

Devuelve:

```java
Optional<Alumno>
```

Esto representa dos posibilidades:

```text
El alumno existe
o
El alumno no existe
```

Con:

```java
.orElse(null)
```

se indica:

```text
Si existe → devuelve el alumno
Si no existe → devuelve null
```

Más adelante conviene gestionar el caso de alumno inexistente de una forma más completa.

---

# 11. Adaptar Update: guardar los cambios

Antes se recorría la lista y se modificaba el objeto:

```java
for (Alumno a : alumnos) {
    if (a.getId() == id) {
        a.setNombre(nombre);
        a.setApellido(apellido);
        a.setEdad(edad);
        a.setModalidad(modalidad);
        a.setProfesor(profesor);
        break;
    }
}
```

Ahora el método queda así:

```java
@PostMapping("/actualizar-alumne")
public String actualizarAlumne(
        @RequestParam Long id,
        @RequestParam String nombre,
        @RequestParam String apellido,
        @RequestParam String edad,
        @RequestParam String modalidad,
        @RequestParam String profesor
) {
    Alumno alumnoEncontrado = alumnoRepository
            .findById(id)
            .orElse(null);

    if (alumnoEncontrado != null) {
        alumnoEncontrado.setNombre(nombre);
        alumnoEncontrado.setApellido(apellido);
        alumnoEncontrado.setEdad(edad);
        alumnoEncontrado.setModalidad(modalidad);
        alumnoEncontrado.setProfesor(profesor);

        alumnoRepository.save(alumnoEncontrado);
    }

    return "redirect:/";
}
```

## Qué sucede paso a paso

Primero se busca el alumno:

```java
Alumno alumnoEncontrado = alumnoRepository
        .findById(id)
        .orElse(null);
```

Se comprueba que exista:

```java
if (alumnoEncontrado != null)
```

Se modifican sus propiedades:

```java
alumnoEncontrado.setNombre(nombre);
alumnoEncontrado.setApellido(apellido);
alumnoEncontrado.setEdad(edad);
alumnoEncontrado.setModalidad(modalidad);
alumnoEncontrado.setProfesor(profesor);
```

Finalmente se guarda:

```java
alumnoRepository.save(alumnoEncontrado);
```

## `save()` crea o actualiza

```text
Objeto con id = null
↓
INSERT

Objeto con id existente
↓
UPDATE
```

En Create se guarda un alumno con ID `null`.

En Update se guarda un alumno que ya tiene ID.

Por eso el mismo método:

```java
save()
```

sirve para crear y actualizar.

---

# 12. Eliminar definitivamente la `ArrayList`

Cuando Read, Create, Delete y Update utilizan el Repository, ya no hace falta:

```java
private List<Alumno> alumnos = new ArrayList<>();
```

También se puede borrar el import:

```java
import java.util.ArrayList;
```

Debe conservarse:

```java
import java.util.List;
```

porque Read utiliza:

```java
List<Alumno> alumnosGuardados = alumnoRepository.findAll();
```

La aplicación queda completamente conectada a H2:

```text
Create → save()
Read   → findAll()
Update → findById() + save()
Delete → deleteById()
```

---

# 13. Código completo de `HomeController.java`

```java
package com.cristian.gestoralumnos.controller;

import com.cristian.gestoralumnos.model.Alumno;
import com.cristian.gestoralumnos.repository.AlumnoRepository;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

        List<Alumno> alumnosGuardados = alumnoRepository.findAll();

        model.addAttribute("titolPagina", "Gestor d'alumnes");
        model.addAttribute(
                "missatge",
                "Benvinguda a la meva app de gestió d'alumnes"
        );
        model.addAttribute("totalAlumnes", alumnosGuardados.size());
        model.addAttribute("alumnes", alumnosGuardados);

        return "index";
    }

    @PostMapping("/afegir-alumne")
    public String afegirAlumne(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String edad,
            @RequestParam String modalidad,
            @RequestParam String profesor
    ) {
        Alumno nouAlumne = new Alumno(
                null,
                nombre,
                apellido,
                edad,
                null,
                modalidad,
                profesor
        );

        alumnoRepository.save(nouAlumne);

        return "redirect:/";
    }

    @PostMapping("/eliminar-alumne")
    public String eliminarAlumne(@RequestParam Long id) {

        alumnoRepository.deleteById(id);

        return "redirect:/";
    }

    @GetMapping("/editar-alumne")
    public String mostrarFormularioEditarAlumne(
            @RequestParam Long id,
            Model model
    ) {
        Alumno alumnoEncontrado = alumnoRepository
                .findById(id)
                .orElse(null);

        model.addAttribute("alumneEditar", alumnoEncontrado);

        return "editar";
    }

    @PostMapping("/actualizar-alumne")
    public String actualizarAlumne(
            @RequestParam Long id,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String edad,
            @RequestParam String modalidad,
            @RequestParam String profesor
    ) {
        Alumno alumnoEncontrado = alumnoRepository
                .findById(id)
                .orElse(null);

        if (alumnoEncontrado != null) {
            alumnoEncontrado.setNombre(nombre);
            alumnoEncontrado.setApellido(apellido);
            alumnoEncontrado.setEdad(edad);
            alumnoEncontrado.setModalidad(modalidad);
            alumnoEncontrado.setProfesor(profesor);

            alumnoRepository.save(alumnoEncontrado);
        }

        return "redirect:/";
    }
}
```

---

# 14. Estructura final del proyecto

```text
src
├── main
│   ├── java
│   │   └── com/cristian/gestoralumnos
│   │       ├── GestoralumnosApplication.java
│   │       ├── controller
│   │       │   └── HomeController.java
│   │       ├── model
│   │       │   └── Alumno.java
│   │       └── repository
│   │           └── AlumnoRepository.java
│   └── resources
│       ├── application.properties
│       ├── static
│       └── templates
│           ├── index.html
│           └── editar.html
└── test
    └── java
```

Nueva pieza:

```text
repository/AlumnoRepository.java
```

Nueva relación:

```text
HomeController
↓
AlumnoRepository
↓
Alumno
↓
H2
```

---

# 15. Comprobar el proyecto

## Compilar

```bash
./mvnw clean compile
```

Resultado esperado:

```text
BUILD SUCCESS
```

## Arrancar

```bash
./mvnw spring-boot:run
```

La aplicación queda disponible en:

```text
http://localhost:8080/
```

## Recorrido de prueba

```text
1. Crear un alumno.
2. Comprobar que aparece.
3. Comprobar que aumenta el contador.
4. Editar el alumno.
5. Comprobar que aparecen los cambios.
6. Eliminar el alumno.
7. Comprobar que desaparece.
8. Comprobar que baja el contador.
```

Si se cambia código Java, conviene detener y reiniciar la aplicación:

```text
Ctrl + C
```

Después:

```bash
./mvnw spring-boot:run
```

`clean compile` no sustituye el reinicio de una aplicación que ya está ejecutándose.

---

# 16. SQL generado por Hibernate

Con esta configuración:

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Hibernate muestra las consultas en la terminal.

## Create

```sql
insert
into
    alumno
    (apellido, edad, fecha_nac, modalidad, nombre, profesor, id)
values
    (?, ?, ?, ?, ?, ?, default)
```

## Read

```sql
select
    a1_0.id,
    a1_0.apellido,
    a1_0.edad,
    a1_0.fecha_nac,
    a1_0.modalidad,
    a1_0.nombre,
    a1_0.profesor
from
    alumno a1_0
```

## Update

Hibernate genera una consulta similar a:

```sql
update
    alumno
set
    apellido=?,
    edad=?,
    fecha_nac=?,
    modalidad=?,
    nombre=?,
    profesor=?
where
    id=?
```

## Delete

Hibernate genera una consulta similar a:

```sql
delete
from
    alumno
where
    id=?
```

Los signos:

```text
?
```

representan valores que Hibernate envía de forma segura a la consulta.

---

# 17. Abrir la consola de H2

La aplicación debe estar arrancada:

```bash
./mvnw spring-boot:run
```

Después se abre:

```text
http://localhost:8080/h2-console/
```

La barra final fue necesaria en este proyecto.

## Datos de conexión

```text
Driver Class: org.h2.Driver
JDBC URL:     jdbc:h2:mem:gestoralumnosdb
User Name:    sa
Password:     dejar vacío
```

Primero puede utilizarse:

```text
Test Connection
```

Después:

```text
Connect
```

## Localizar la tabla

En el panel izquierdo aparece:

```text
ALUMNO
```

Sus columnas son:

```text
ID
APELLIDO
EDAD
FECHA_NAC
MODALIDAD
NOMBRE
PROFESOR
```

La columna:

```text
FECHA_NAC
```

aparece con valor:

```text
null
```

porque el formulario todavía no envía ese campo.

## Consultar todos los registros

```sql
SELECT * FROM ALUMNO;
```

Después se pulsa:

```text
Run
```

Esto permite comprobar directamente los datos guardados.

## Prueba completa

Mantener abiertas:

```text
http://localhost:8080/
http://localhost:8080/h2-console/
```

Después:

```text
Crear alumno en la web
↓
Ejecutar SELECT * FROM ALUMNO
↓
El alumno aparece en H2
```

```text
Editar alumno en la web
↓
Ejecutar SELECT * FROM ALUMNO
↓
Los datos aparecen modificados
```

```text
Eliminar alumno en la web
↓
Ejecutar SELECT * FROM ALUMNO
↓
El registro desaparece
```

---

# 18. Errores encontrados durante la migración

## Los alumnos se guardaban pero no aparecían

Create utilizaba:

```java
alumnoRepository.save(nouAlumne);
```

Pero Read seguía enviando:

```java
model.addAttribute("alumnes", alumnos);
```

La aplicación guardaba en H2, pero Thymeleaf mostraba la `ArrayList` vacía.

Solución:

```java
List<Alumno> alumnosGuardados = alumnoRepository.findAll();

model.addAttribute("alumnes", alumnosGuardados);
```

---

## Error `cannot find symbol: variable alumnos`

Se eliminó:

```java
private List<Alumno> alumnos = new ArrayList<>();
```

cuando Delete y Update todavía la utilizaban.

El compilador mostraba:

```text
cannot find symbol
symbol: variable alumnos
```

Durante una migración paso a paso, la lista debe mantenerse hasta que todas las operaciones hayan sido adaptadas.

Solo debe eliminarse cuando:

```text
Read   → Repository
Create → Repository
Delete → Repository
Update → Repository
```

---

## Delete no hacía nada

Delete seguía utilizando:

```java
alumnos.removeIf(...)
```

Pero los alumnos estaban guardados en H2.

Solución:

```java
alumnoRepository.deleteById(id);
```

---

## Update mostraba Whitelabel Error Page

El método de edición seguía buscando en:

```java
for (Alumno a : alumnos)
```

La lista estaba vacía, por lo que el alumno encontrado era:

```java
null
```

Después `editar.html` intentaba acceder a:

```html
${alumneEditar.nombre}
```

pero `alumneEditar` no contenía ningún objeto.

Solución:

```java
Alumno alumnoEncontrado = alumnoRepository
        .findById(id)
        .orElse(null);
```

---

## La consola H2 mostraba Whitelabel

La URL utilizada era:

```text
http://localhost:8080/h2-console
```

En este proyecto funcionó al añadir la barra final:

```text
http://localhost:8080/h2-console/
```

También fue necesario añadir al `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-h2console</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## `BUILD FAILURE` con `${start-class}` en NetBeans

NetBeans intentaba ejecutar literalmente:

```text
${start-class}
```

El error era parecido a:

```text
ClassNotFoundException: ${start-class}
```

Esto no significaba que el proyecto no compilara.

La comprobación correcta se realizó desde terminal:

```bash
./mvnw clean compile
```

Resultado:

```text
BUILD SUCCESS
```

Para ejecutar Spring Boot se utilizó:

```bash
./mvnw spring-boot:run
```

---

# 19. Conceptos aprendidos

## JPA

JPA define una forma estándar de relacionar objetos Java con tablas de bases de datos.

```text
Objeto Java
↕
JPA
↕
Registro de una tabla
```

## Hibernate

Hibernate es la implementación que utiliza Spring para realizar gran parte del trabajo de JPA.

Hibernate:

```text
Analiza entidades
Genera tablas
Genera SQL
Convierte filas en objetos
Convierte objetos en filas
```

## H2

H2 es una base de datos ligera.

En este proyecto se utiliza en memoria:

```properties
jdbc:h2:mem:gestoralumnosdb
```

Es útil para:

```text
Aprender
Desarrollar
Probar
Depurar
```

## Repository

El Repository contiene las operaciones de acceso a datos.

```text
Controller
↓
Repository
↓
Base de datos
```

## Inyección de dependencias

Spring proporciona el Repository al Controller mediante el constructor:

```java
public HomeController(AlumnoRepository alumnoRepository) {
    this.alumnoRepository = alumnoRepository;
}
```

## Entidad

Una entidad es una clase Java relacionada con una tabla.

```java
@Entity
public class Alumno {
}
```

## Clave primaria

```java
@Id
private Long id;
```

Identifica de forma única cada registro.

## ID autogenerado

```java
@GeneratedValue(strategy = GenerationType.IDENTITY)
```

Indica que la base de datos genera el ID.

## `Optional`

```java
findById(id)
```

devuelve:

```java
Optional<Alumno>
```

porque el registro puede existir o no.

## `save()`

```java
alumnoRepository.save(alumno);
```

Tiene dos comportamientos:

```text
Sin ID → INSERT
Con ID → UPDATE
```

---

# 20. Flujo final de cada operación

## Create

```text
Formulario POST
↓
@RequestParam
↓
new Alumno(id = null)
↓
alumnoRepository.save()
↓
Hibernate genera INSERT
↓
H2 asigna el ID
```

## Read

```text
GET /
↓
alumnoRepository.findAll()
↓
List<Alumno>
↓
Model
↓
Thymeleaf
↓
HTML
```

## Update

```text
GET /editar-alumne?id=...
↓
findById(id)
↓
Model
↓
editar.html
↓
POST /actualizar-alumne
↓
findById(id)
↓
setters
↓
save()
↓
Hibernate genera UPDATE
```

## Delete

```text
POST /eliminar-alumne
↓
@RequestParam Long id
↓
deleteById(id)
↓
Hibernate genera DELETE
↓
redirect:/
```

---

# 21. Estado actual del proyecto

```text
Create ✅ conectado a H2
Read   ✅ conectado a H2
Update ✅ conectado a H2
Delete ✅ conectado a H2
```

La `ArrayList` ha sido eliminada.

El Controller utiliza:

```java
AlumnoRepository
```

La entidad utiliza:

```java
@Entity
@Id
@GeneratedValue
```

La base de datos utiliza:

```text
H2 en memoria
```

La consola permite observar:

```sql
SELECT * FROM ALUMNO;
```

---

# 22. Limitación actual de H2 en memoria

La configuración actual utiliza:

```properties
spring.datasource.url=jdbc:h2:mem:gestoralumnosdb
```

Esto significa:

```text
Reiniciar la página
↓
Los datos siguen existiendo

Reiniciar Spring Boot
↓
Los datos desaparecen
```

La persistencia funciona mientras la aplicación está encendida.

Un paso futuro puede ser utilizar H2 en archivo:

```properties
spring.datasource.url=jdbc:h2:file:./data/gestoralumnosdb
```

O migrar a:

```text
MySQL
PostgreSQL
MariaDB
```

No debe realizarse este cambio hasta comprender bien la versión actual.

---

# 23. Próximos pasos recomendados

Orden recomendado:

```text
1. Añadir validación de formularios.
2. Gestionar correctamente alumnos inexistentes.
3. Crear AlumnoService.
4. Mover la lógica fuera del Controller.
5. Cambiar edad de String a un tipo numérico.
6. Añadir fecha de nacimiento al formulario.
7. Mejorar el HTML y mostrar los alumnos en una tabla.
8. Añadir CSS.
9. Añadir búsqueda y filtros.
10. Exportar información a CSV y PDF.
11. Utilizar una base de datos persistente.
12. Preparar el despliegue.
```

Arquitectura futura:

```text
Controller
↓
Service
↓
Repository
↓
Base de datos
```

---

# 24. Resumen final

La aplicación comenzó utilizando:

```java
private List<Alumno> alumnos = new ArrayList<>();
```

Después se añadieron:

```text
Spring Data JPA
Hibernate
H2
AlumnoRepository
@Entity
@Id
@GeneratedValue
```

Las operaciones antiguas:

```java
alumnos.add(...);
alumnos.removeIf(...);
for (Alumno alumno : alumnos) {
}
```

fueron sustituidas por:

```java
alumnoRepository.save(...);
alumnoRepository.findAll();
alumnoRepository.findById(...);
alumnoRepository.deleteById(...);
```

El resultado es un CRUD completo conectado a una base de datos:

```text
Formulario
↓
Controller
↓
Repository
↓
Hibernate
↓
H2
```

La consola de H2 permite comprobar directamente que los datos creados, editados y eliminados desde la aplicación modifican realmente la tabla:

```sql
SELECT * FROM ALUMNO;
```

Con esta migración, el proyecto deja de ser un CRUD basado únicamente en memoria Java y pasa a utilizar una arquitectura de acceso a datos real.

---

# PARTE III. MODELO ACTUAL: `LocalDate` Y `fechaNacimiento`

Después de conectar el CRUD a H2 se mejora el modelo de `Alumno`. Se elimina `edad`, se sustituye `Date` por `LocalDate` y la fecha de nacimiento atraviesa correctamente Create, Read y Update.

Esta guía documenta los cambios realizados en el proyecto **Gestor de
alumnos** durante esta sesión.

El punto de partida era un CRUD completo conectado a H2 mediante Spring
Data JPA.

La aplicación ya permitía:

``` text
Create ✅
Read   ✅
Update ✅
Delete ✅
```

Durante este ejercicio se ha mejorado el modelo de datos de `Alumno`, se
ha eliminado el campo `edad`, se ha sustituido el antiguo tipo `Date`
por `LocalDate`, se ha conectado la fecha de nacimiento con los
formularios de alta y edición y se ha preparado el proyecto para añadir
validación de formularios.

------------------------------------------------------------------------

# 1. Objetivo del ejercicio

Antes, el modelo de `Alumno` contenía información relacionada con la
edad y la fecha de nacimiento.

Se utilizaba:

``` java
private String edad;
```

y anteriormente la fecha estaba representada mediante:

``` java
private Date fechaNac;
```

Se decidió modificar el modelo por dos motivos.

## La edad cambia con el tiempo

La edad no es un dato permanente.

Ejemplo:

``` text
Fecha de nacimiento: 14/06/2000

2026 → 26 años
2027 → 27 años
2028 → 28 años
```

Si almacenamos directamente:

``` java
edad = "26";
```

el dato terminará quedando desactualizado.

La fecha de nacimiento, en cambio, no cambia.

Por tanto, el modelo debe guardar:

``` text
fecha de nacimiento
```

y la edad podrá calcularse más adelante cuando sea necesaria.

## `LocalDate` representa mejor una fecha de nacimiento

Se sustituyó:

``` java
java.util.Date
```

por:

``` java
java.time.LocalDate
```

Una fecha de nacimiento necesita:

``` text
Año
Mes
Día
```

No necesita:

``` text
Hora
Minutos
Segundos
Zona horaria
```

Por este motivo `LocalDate` es un tipo adecuado para este dato.

------------------------------------------------------------------------

# 2. Eliminar el campo `edad`

Se eliminó de la entidad:

``` java
private String edad;
```

También se eliminaron:

``` java
getEdad()
setEdad()
```

El parámetro correspondiente del constructor también fue eliminado.

Antes, el constructor podía contener:

``` java
String edad
```

Después del cambio, el constructor utiliza directamente:

``` java
LocalDate fechaNacimiento
```

La entidad queda conceptualmente así:

``` text
Alumno
├── id
├── nombre
├── apellido
├── fechaNacimiento
├── modalidad
└── profesor
```

------------------------------------------------------------------------

# 3. Cambiar `Date` por `LocalDate`

Se eliminó el import antiguo:

``` java
import java.util.Date;
```

Y se añadió:

``` java
import java.time.LocalDate;
```

El atributo de fecha quedó inicialmente con un nombre abreviado:

``` java
private LocalDate fechaNac;
```

Posteriormente se decidió utilizar un nombre más descriptivo:

``` java
private LocalDate fechaNacimiento;
```

Esta decisión mejora la consistencia del código.

Ahora utilizamos el mismo concepto en toda la aplicación:

``` text
fechaNacimiento
```

------------------------------------------------------------------------

# 4. Importancia de mantener nombres consistentes

El nombre definitivo es:

``` java
fechaNacimiento
```

Por tanto, utilizamos:

``` text
Entidad          fechaNacimiento
Getter           getFechaNacimiento()
Setter           setFechaNacimiento()
HTML             fechaNacimiento
Controller       fechaNacimiento
Thymeleaf        fechaNacimiento
```

La relación completa es:

``` text
private LocalDate fechaNacimiento
                ↓
getFechaNacimiento()
                ↓
${alumne.fechaNacimiento}
```

Thymeleaf sigue la convención JavaBean.

Cuando escribimos:

``` html
${alumne.fechaNacimiento}
```

Thymeleaf busca un método parecido a:

``` java
getFechaNacimiento()
```

Por este motivo no era conveniente mantener:

``` java
getFechaNac()
```

si el atributo y la propiedad utilizada en Thymeleaf se llaman:

``` java
fechaNacimiento
```

------------------------------------------------------------------------

# 5. Código actualizado de `Alumno.java`

La entidad quedó así:

``` java
package com.cristian.gestoralumnos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento;
    private String modalidad;
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
        return "Alumno{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", fechaNacimiento=" + fechaNacimiento +
                ", modalidad='" + modalidad + '\'' +
                ", profesor='" + profesor + '\'' +
                '}';
    }
}
```

------------------------------------------------------------------------

# 6. Error del constructor después de modificar `Alumno`

Después de cambiar el modelo, el Controller creó un alumno así:

``` java
Alumno nouAlumne = new Alumno(
        null,
        nombre,
        apellido,
        fechaNacimiento,
        modalidad,
        profesor
);
```

Los tipos enviados eran:

``` text
Long
String
String
LocalDate
String
String
```

El constructor de `Alumno` también esperaba:

``` text
Long
String
String
LocalDate
String
String
```

Por tanto, el código era correcto.

Sin embargo, NetBeans mostraba un error indicando constructores antiguos
relacionados con:

``` text
String edad
Date
```

El mensaje mostraba firmas antiguas de `Alumno`.

Esto indicaba que el IDE estaba utilizando información de una
compilación anterior.

## Solución

Se realizó:

``` text
Clean and Build
```

Después de limpiar y recompilar el proyecto, NetBeans reconoció
correctamente el constructor nuevo.

La comprobación equivalente desde terminal es:

``` bash
./mvnw clean compile
```

Resultado esperado:

``` text
BUILD SUCCESS
```

## Idea importante

Si:

``` text
El número de argumentos coincide
Los tipos coinciden
El constructor existe
```

pero el IDE muestra firmas antiguas que ya no aparecen en el código,
puede existir información de compilación desactualizada.

En ese caso:

``` text
Guardar archivos
↓
Clean and Build
↓
Recompilar
```

------------------------------------------------------------------------

# 7. Añadir la dependencia de validación

El proyecto se preparó para utilizar validación de formularios.

En `pom.xml` se añadió:

``` xml
<!-- Validación de formularios -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

Esta dependencia permitirá utilizar posteriormente anotaciones como:

``` java
@NotBlank
@NotNull
@Past
```

Todavía no se han aplicado las reglas de validación.

En esta sesión solamente se ha preparado la dependencia.

------------------------------------------------------------------------

# 8. Limpieza del `pom.xml`

Se revisaron las dependencias del proyecto.

Las dependencias principales quedaron organizadas según su función.

``` xml
<dependencies>

    <!-- Aplicación web con Spring MVC y Tomcat -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>

    <!-- Plantillas HTML con Thymeleaf -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>

    <!-- JPA, Hibernate y repositorios -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Validación de formularios -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Base de datos H2 -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Consola web de H2 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-h2console</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Herramientas de desarrollo -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>

    <!-- Herramientas de prueba -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

</dependencies>
```

También se eliminaron bloques vacíos del `pom.xml` que no aportaban
información:

``` xml
<url/>

<licenses>
    <license/>
</licenses>

<developers>
    <developer/>
</developers>

<scm>
    ...
</scm>
```

Se añadieron valores descriptivos:

``` xml
<name>gestoralumnos</name>
<description>Aplicación web para la gestión de alumnos</description>
```

Después de modificar el `pom.xml` se ejecutó:

``` bash
./mvnw clean compile
```

El proyecto terminó con:

``` text
BUILD SUCCESS
```

------------------------------------------------------------------------

# 9. Añadir la fecha de nacimiento al formulario de alta

En `index.html` se eliminó el antiguo campo de edad.

Se añadió:

``` html
<label for="fechaNacimiento">Data de naixement:</label>

<input
    type="date"
    id="fechaNacimiento"
    name="fechaNacimiento">
```

La propiedad:

``` html
type="date"
```

indica al navegador que el campo contiene una fecha.

El navegador envía normalmente la fecha utilizando el formato ISO:

``` text
2026-07-13
```

La parte importante para el Controller es:

``` html
name="fechaNacimiento"
```

La relación es:

``` text
HTML
name="fechaNacimiento"
        ↓
Controller
@RequestParam LocalDate fechaNacimiento
```

El atributo:

``` html
id="fechaNacimiento"
```

se utiliza para relacionar el campo con:

``` html
<label for="fechaNacimiento">
```

------------------------------------------------------------------------

# 10. Recibir `LocalDate` en el Controller

En `HomeController` se añadieron los imports:

``` java
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
```

El método de alta recibe ahora:

``` java
@RequestParam
@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
LocalDate fechaNacimiento
```

El método quedó así:

``` java
@PostMapping("/afegir-alumne")
public String afegirAlumne(
        @RequestParam String nombre,
        @RequestParam String apellido,

        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fechaNacimiento,

        @RequestParam String modalidad,
        @RequestParam String profesor
) {

    Alumno nouAlumne = new Alumno(
            null,
            nombre,
            apellido,
            fechaNacimiento,
            modalidad,
            profesor
    );

    alumnoRepository.save(nouAlumne);

    return "redirect:/";
}
```

## Función de `@DateTimeFormat`

La anotación:

``` java
@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
```

indica que el dato recibido utiliza un formato de fecha ISO.

Ejemplo:

``` text
"2001-04-18"
        ↓
Spring
        ↓
LocalDate
```

El Controller recibe un objeto:

``` java
LocalDate fechaNacimiento
```

en lugar de trabajar directamente con un `String`.

------------------------------------------------------------------------

# 11. Flujo de Create con la fecha de nacimiento

El flujo completo queda así:

``` text
Usuario selecciona una fecha
↓
<input type="date">
↓
name="fechaNacimiento"
↓
POST /afegir-alumne
↓
@RequestParam
↓
@DateTimeFormat
↓
LocalDate fechaNacimiento
↓
new Alumno(...)
↓
alumnoRepository.save(...)
↓
Hibernate
↓
H2
```

La fecha queda almacenada en la base de datos.

------------------------------------------------------------------------

# 12. Aparición de Whitelabel después de guardar

Después de añadir un alumno desde la aplicación apareció:

``` text
Whitelabel Error Page
```

Sin embargo, al consultar H2, el nuevo alumno aparecía correctamente
guardado.

Esto permitió localizar la capa en la que ocurría el error.

El recorrido real era:

``` text
POST /afegir-alumne
↓
@RequestParam                  ✅
↓
new Alumno(...)                ✅
↓
alumnoRepository.save(...)     ✅
↓
H2 guarda el registro          ✅
↓
redirect:/                     ✅
↓
GET /                          ✅
↓
findAll()                      ✅
↓
Thymeleaf renderiza index.html ❌
```

Por tanto, el problema no estaba en:

``` text
Controller
Repository
JPA
Hibernate
H2
```

El problema estaba en la vista HTML.

------------------------------------------------------------------------

# 13. Error encontrado en `index.html`

Aunque `edad` había sido eliminada de `Alumno`, `index.html` todavía
contenía:

``` html
<span data-th-text="${alumne.edad}">Edat</span>
anys -
```

Thymeleaf intentaba acceder a:

``` text
alumne.edad
```

Pero la propiedad ya no existía.

Conceptualmente intentaba encontrar:

``` java
alumne.getEdad()
```

Ese método había sido eliminado.

Por este motivo la vista fallaba durante el renderizado.

## Solución

Se eliminó:

``` html
<span data-th-text="${alumne.edad}">Edat</span>
anys -
```

Y se añadió:

``` html
Data de naixement:

<span data-th-text="${alumne.fechaNacimiento}">
    Data
</span>
```

Ahora Thymeleaf utiliza:

``` html
${alumne.fechaNacimiento}
```

que corresponde a:

``` java
getFechaNacimiento()
```

------------------------------------------------------------------------

# 14. Diagnosticar un error por capas

Este error permitió aplicar una forma más precisa de depuración.

La pregunta clave fue:

``` text
¿El alumno aparece en H2?
```

La respuesta era:

``` text
Sí
```

Por tanto:

``` text
Create funciona.
save() funciona.
Hibernate funciona.
H2 funciona.
```

Después:

``` text
¿El error aparece al volver a la página principal?
```

La respuesta era:

``` text
Sí
```

Por tanto, había que investigar:

``` text
Model
Thymeleaf
HTML
Getters
```

Este razonamiento permite localizar el error antes de modificar código
al azar.

Resumen:

``` text
Dato no llega al Controller
→ revisar formulario y parámetros

Dato llega pero no se guarda
→ revisar Repository, entidad y base de datos

Dato aparece en H2 pero falla la página
→ revisar Model, Thymeleaf y getters

Falla al editar
→ revisar ID, findById y formulario de edición
```

------------------------------------------------------------------------

# 15. Corregir getters y setters de la fecha

Después de renombrar:

``` java
fechaNac
```

a:

``` java
fechaNacimiento
```

los métodos todavía utilizaban nombres antiguos:

``` java
getFechaNac()
setFechaNac()
```

Se sustituyeron por:

``` java
public LocalDate getFechaNacimiento() {
    return fechaNacimiento;
}

public void setFechaNacimiento(LocalDate fechaNacimiento) {
    this.fechaNacimiento = fechaNacimiento;
}
```

Esto mantiene la convención:

``` text
fechaNacimiento
↓
getFechaNacimiento()
↓
setFechaNacimiento()
```

Y permite utilizar en Thymeleaf:

``` html
${alumne.fechaNacimiento}
```

------------------------------------------------------------------------

# 16. Añadir la fecha al formulario de edición

El formulario `editar.html` también fue actualizado.

Se añadió:

``` html
<label for="fechaNacimiento">
    Data de naixement:
</label>

<input
    type="date"
    id="fechaNacimiento"
    name="fechaNacimiento"
    data-th-value="${alumneEditar.fechaNacimiento}">
```

`data-th-value` carga la fecha actual del alumno.

Ejemplo:

``` text
Alumno guardado
fechaNacimiento = 2001-04-18
```

Thymeleaf genera un campo equivalente a:

``` html
<input
    type="date"
    value="2001-04-18">
```

El usuario puede modificar la fecha y enviar el formulario.

------------------------------------------------------------------------

# 17. Error tipográfico en `editar.html`

Durante la revisión se encontró:

``` html
name="fefchaNacimiento"
```

El nombre contenía una letra adicional.

Se corrigió por:

``` html
name="fechaNacimiento"
```

Este tipo de error es importante porque el Controller relaciona los
datos enviados mediante el atributo:

``` html
name
```

La relación correcta es:

``` text
HTML
name="fechaNacimiento"
        ↓
Controller
@RequestParam LocalDate fechaNacimiento
```

Si los nombres no coinciden, Spring no encuentra el parámetro esperado.

------------------------------------------------------------------------

# 18. Actualizar la operación Update

El método:

``` java
actualizarAlumne()
```

todavía no recibía la fecha de nacimiento.

Se añadió:

``` java
@RequestParam
@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
LocalDate fechaNacimiento
```

También se añadió:

``` java
alumnoEncontrado.setFechaNacimiento(fechaNacimiento);
```

El método actualizado queda así:

``` java
@PostMapping("/actualizar-alumne")
public String actualizarAlumne(
        @RequestParam Long id,
        @RequestParam String nombre,
        @RequestParam String apellido,

        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fechaNacimiento,

        @RequestParam String modalidad,
        @RequestParam String profesor
) {

    Alumno alumnoEncontrado = alumnoRepository
            .findById(id)
            .orElse(null);

    if (alumnoEncontrado != null) {
        alumnoEncontrado.setNombre(nombre);
        alumnoEncontrado.setApellido(apellido);
        alumnoEncontrado.setFechaNacimiento(fechaNacimiento);
        alumnoEncontrado.setModalidad(modalidad);
        alumnoEncontrado.setProfesor(profesor);

        alumnoRepository.save(alumnoEncontrado);
    }

    return "redirect:/";
}
```

También se cambió:

``` java
@RequestParam long id
```

por:

``` java
@RequestParam Long id
```

Esto mantiene consistencia con la entidad:

``` java
private Long id;
```

y con:

``` java
JpaRepository<Alumno, Long>
```

------------------------------------------------------------------------

# 19. Flujo completo de Update con `LocalDate`

El flujo queda así:

``` text
Botón Editar
↓
GET /editar-alumne?id=...
↓
findById(id)
↓
Model
↓
alumneEditar
↓
editar.html
↓
data-th-value muestra fechaNacimiento
↓
Usuario modifica la fecha
↓
POST /actualizar-alumne
↓
@RequestParam LocalDate fechaNacimiento
↓
setFechaNacimiento(...)
↓
save(...)
↓
Hibernate genera UPDATE
↓
H2 actualiza el registro
↓
redirect:/
```

------------------------------------------------------------------------

# 20. Relación final entre todas las capas

La fecha de nacimiento atraviesa toda la aplicación.

``` text
index.html
name="fechaNacimiento"
        ↓
HomeController
LocalDate fechaNacimiento
        ↓
Alumno
private LocalDate fechaNacimiento
        ↓
AlumnoRepository
        ↓
JPA / Hibernate
        ↓
H2
        ↓
findAll()
        ↓
Model
        ↓
Thymeleaf
${alumne.fechaNacimiento}
        ↓
index.html
```

En edición:

``` text
Alumno guardado
↓
findById(id)
↓
Model
↓
alumneEditar
↓
${alumneEditar.fechaNacimiento}
↓
input type="date"
↓
Usuario modifica fecha
↓
POST
↓
LocalDate fechaNacimiento
↓
setFechaNacimiento(...)
↓
save(...)
```

------------------------------------------------------------------------

# 21. Errores encontrados durante el ejercicio

## Error 1: import antiguo de `Date`

Existía:

``` java
import java.util.Date;
```

aunque la entidad ya utilizaba:

``` java
LocalDate
```

Solución:

``` java
import java.time.LocalDate;
```

------------------------------------------------------------------------

## Error 2: constructor aparentemente incompatible

NetBeans mostraba constructores antiguos relacionados con:

``` text
edad
Date
```

aunque el constructor actual ya utilizaba:

``` java
LocalDate fechaNacimiento
```

Solución:

``` text
Clean and Build
```

o:

``` bash
./mvnw clean compile
```

------------------------------------------------------------------------

## Error 3: Whitelabel después de guardar

El registro aparecía correctamente en H2.

Por tanto, `save()` funcionaba.

El problema estaba en:

``` html
${alumne.edad}
```

La propiedad `edad` ya no existía.

Solución:

``` html
${alumne.fechaNacimiento}
```

------------------------------------------------------------------------

## Error 4: getter y setter con el nombre antiguo

Existían:

``` java
getFechaNac()
setFechaNac()
```

pero la propiedad se llamaba:

``` java
fechaNacimiento
```

Solución:

``` java
getFechaNacimiento()
setFechaNacimiento()
```

------------------------------------------------------------------------

## Error 5: nombre incorrecto en el formulario de edición

Existía:

``` html
name="fefchaNacimiento"
```

Solución:

``` html
name="fechaNacimiento"
```

------------------------------------------------------------------------

## Error 6: Update no modificaba la fecha

El método:

``` java
actualizarAlumne()
```

no recibía `fechaNacimiento` y tampoco utilizaba su setter.

Solución:

``` java
@RequestParam
@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
LocalDate fechaNacimiento
```

y:

``` java
alumnoEncontrado.setFechaNacimiento(fechaNacimiento);
```

------------------------------------------------------------------------

# 22. Estado final del proyecto después del ejercicio

La entidad `Alumno` utiliza ahora:

``` java
private LocalDate fechaNacimiento;
```

El campo:

``` java
edad
```

ha sido eliminado.

El CRUD continúa funcionando:

``` text
Create ✅
Read   ✅
Update ✅
Delete ✅
```

La fecha de nacimiento funciona en:

``` text
Formulario de alta      ✅
Controller Create        ✅
Entidad Alumno           ✅
JPA / Hibernate          ✅
H2                       ✅
Listado Thymeleaf        ✅
Formulario de edición    ✅
Controller Update        ✅
```

El proyecto también dispone ahora de:

``` xml
spring-boot-starter-validation
```

por lo que está preparado para comenzar la siguiente etapa.

------------------------------------------------------------------------

# 23. Conceptos aprendidos

Durante este ejercicio se han trabajado los siguientes conceptos:

``` text
LocalDate
Date frente a LocalDate
Modelado de datos
Datos permanentes frente a datos calculables
Convención JavaBean
Getters y setters
Propiedades Thymeleaf
input type="date"
@RequestParam
@DateTimeFormat
Formato ISO de fechas
Flujo completo de un dato entre capas
Depuración por capas
Clean and Build
Consistencia de nombres
Actualización de Create y Update
```

Idea importante:

``` text
Cambiar un atributo de una entidad
no significa cambiar solamente una línea Java.
```

Un dato puede aparecer en:

``` text
Entidad
Constructor
Getter
Setter
Controller Create
Controller Update
Formulario de alta
Formulario de edición
Thymeleaf
Base de datos
```

Por tanto, cuando cambia el modelo de datos conviene revisar el
recorrido completo del dato.

------------------------------------------------------------------------

# 24. Próximo paso

El proyecto ya tiene añadida la dependencia:

``` xml
spring-boot-starter-validation
```

El siguiente paso será añadir reglas de validación a `Alumno`.

Las primeras reglas previstas son:

``` java
@NotBlank
private String nombre;

@NotBlank
private String apellido;

@NotNull
@Past
private LocalDate fechaNacimiento;
```

Después se estudiarán:

``` text
@Valid
@ModelAttribute
BindingResult
```

El objetivo será dejar de recibir cada campo del formulario mediante
múltiples `@RequestParam` y comenzar a recibir un objeto `Alumno`
completo.

La evolución será:

``` text
ANTES

@RequestParam nombre
@RequestParam apellido
@RequestParam fechaNacimiento
@RequestParam modalidad
@RequestParam profesor
        ↓
new Alumno(...)
```

``` text
DESPUÉS

Formulario
↓
Spring relaciona los name=""
↓
@ModelAttribute Alumno alumno
↓
@Valid
↓
BindingResult
```

Este será el siguiente paso en la evolución del CRUD.

---

# PARTE IV. PUNTO EXACTO DE REANUDACIÓN DEL PROYECTO

Este es el punto desde el que continúa el desarrollo.

## Estado vigente de `Alumno`

Conceptualmente:

```text
Alumno
├── id : Long
├── nombre : String
├── apellido : String
├── fechaNacimiento : LocalDate
├── modalidad : String
└── profesor : String
```

La clase es una entidad JPA:

```text
@Entity
↓
@Id
↓
@GeneratedValue(strategy = GenerationType.IDENTITY)
```

La edad **no se almacena**. Si se necesita, deberá calcularse a partir de:

```java
fechaNacimiento
```

## Estado vigente del CRUD

```text
Create
Formulario
↓
Controller
↓
new Alumno(id = null)
↓
alumnoRepository.save()
↓
INSERT

Read
GET /
↓
alumnoRepository.findAll()
↓
Model
↓
Thymeleaf

Update
GET /editar-alumne?id=...
↓
findById(id)
↓
editar.html
↓
POST /actualizar-alumne
↓
setters
↓
save()
↓
UPDATE

Delete
POST /eliminar-alumne
↓
deleteById(id)
↓
DELETE
```

## Próxima acción: validación

La dependencia ya está añadida:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

Las primeras reglas previstas son:

```java
@NotBlank
private String nombre;

@NotBlank
private String apellido;

@NotNull
@Past
private LocalDate fechaNacimiento;
```

Después se estudiará este cambio de enfoque:

```text
ANTES

@RequestParam nombre
@RequestParam apellido
@RequestParam fechaNacimiento
@RequestParam modalidad
@RequestParam profesor
↓
new Alumno(...)
```

```text
DESPUÉS

Formulario
↓
Spring relaciona los name=""
↓
@ModelAttribute Alumno alumno
↓
@Valid
↓
BindingResult
↓
Si hay errores → volver al formulario
Si no hay errores → save()
```

## Orden de trabajo recomendado desde aquí

```text
1. Añadir anotaciones de validación a Alumno.
2. Comprender @NotBlank, @NotNull y @Past.
3. Cambiar el alta para recibir @ModelAttribute Alumno.
4. Añadir @Valid.
5. Añadir BindingResult.
6. Mostrar errores de validación con Thymeleaf.
7. Aplicar la validación al formulario de edición.
8. Gestionar correctamente IDs inexistentes.
9. Crear AlumnoService.
10. Mover lógica de negocio fuera del Controller.
11. Mejorar listado y formularios con HTML/CSS.
12. Añadir búsqueda y filtros.
13. Añadir exportación CSV.
14. Añadir exportación PDF.
15. Cambiar H2 en memoria por persistencia real.
16. Preparar despliegue.
```

## Regla mental para depurar esta aplicación

```text
¿El formulario envía el dato?
↓
Revisar name="", action y method

¿El Controller recibe el dato?
↓
Revisar mapping y parámetros

¿El dato se guarda?
↓
Revisar entidad, Repository, save() y H2

¿Está en H2 pero la página falla?
↓
Revisar Model, Thymeleaf y getters

¿Falla al editar?
↓
Revisar ID, findById(), alumneEditar y data-th-value
```

## Idea central de toda la guía

```text
Un dato no vive en una sola línea.
```

Por ejemplo, `fechaNacimiento` puede recorrer:

```text
HTML
↓
Controller
↓
Alumno
↓
Repository
↓
Hibernate
↓
H2
↓
Repository
↓
Model
↓
Thymeleaf
↓
HTML
```

Cuando se cambia el modelo, hay que seguir el dato de extremo a extremo. Esta forma de pensar será especialmente importante al introducir validación y la capa `Service`.

---

# RESUMEN DE REFERENCIA RÁPIDA

```text
@Controller
→ identifica un controlador web

@GetMapping
→ responde a GET

@PostMapping
→ responde a POST

@RequestParam
→ recoge un parámetro individual

@ModelAttribute
→ enlaza los campos del formulario con un objeto

@Valid
→ solicita validar el objeto

BindingResult
→ contiene los errores de validación

Model
→ transporta datos hacia la vista

@Entity
→ relaciona una clase con persistencia JPA

@Id
→ identifica la clave primaria

@GeneratedValue
→ delega la generación del ID

JpaRepository
→ proporciona operaciones de acceso a datos

findAll()
→ obtiene todos los registros

findById()
→ busca por ID y devuelve Optional

save()
→ INSERT si es nuevo; UPDATE si ya existe

deleteById()
→ elimina por ID

LocalDate
→ representa una fecha sin hora ni zona horaria

@DateTimeFormat
→ ayuda a convertir el dato textual recibido en fecha

data-th-each
→ recorre una colección

data-th-text
→ escribe contenido

data-th-value
→ asigna el value de un input

redirect:/
→ termina el POST y provoca un nuevo GET /
```

## Punto de reanudación

```text
CRUD + JPA + H2 + LocalDate funcionando
↓
spring-boot-starter-validation añadido
↓
SIGUIENTE PASO:
@NotBlank + @NotNull + @Past
↓
@Valid + @ModelAttribute + BindingResult
```

