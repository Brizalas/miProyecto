# Gestor de alumnos con Spring Boot

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
