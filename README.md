# Gestor de alumnos con Spring Boot

Este proyecto es una primera plantilla web construida con **Spring Boot**, **Thymeleaf** y **Maven**.

El objetivo inicial es crear un CRUD para gestionar alumnos desde una aplicación web. De momento estamos trabajando con datos temporales en memoria, pero más adelante el proyecto podrá ampliarse con formularios, informes, exportación a PDF/CSV, base de datos y despliegue en producción.

---

## 1. Estructura básica del proyecto

La estructura correcta del proyecto es:

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
│           └── index.html
└── test
    └── java
```

Resumen:

```text
src/main/java        → clases Java principales de la aplicación
src/main/resources   → plantillas HTML, CSS, JS y configuración
src/test/java        → clases de test
```

La carpeta `templates` contiene las vistas HTML que procesa Thymeleaf.

La carpeta `static` se usará más adelante para archivos estáticos como CSS, JavaScript o imágenes.

---

## 2. Clase principal de Spring Boot

La clase principal del proyecto es:

```java
GestoralumnosApplication.java
```

Esta clase contiene el método `main`, pero en Spring Boot el `main` ya no contiene toda la lógica del programa.

Su función principal es arrancar la aplicación:

```text
main()
↓
Spring Boot arranca
↓
Se levanta un servidor web
↓
La aplicación queda disponible en localhost:8080
```

En una aplicación Java de consola, el `main` suele contener la lógica principal del programa.

En Spring Boot, el `main` solo enciende la aplicación. Después, Spring se encarga de gestionar los controladores, las rutas, las vistas y el flujo web.

---

## 3. HomeController

`HomeController` es una clase controladora de Spring Boot.

Su función es recibir peticiones del navegador y decidir qué vista HTML debe devolverse.

Ejemplo:

```java
package com.cristian.gestoralumnos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String mostrarInicio(Model model) {
        return "index";
    }
}
```

---

## 4. `@Controller`

La anotación:

```java
@Controller
```

indica a Spring que esta clase es un controlador web.

Es decir, Spring debe tenerla en cuenta cuando lleguen peticiones desde el navegador.

Sin esta anotación, Spring no sabría que esa clase debe actuar como intermediaria entre una URL y una vista HTML.

---

## 5. `@GetMapping("/")`

La anotación:

```java
@GetMapping("/")
```

indica que el método responderá cuando el usuario acceda a la ruta raíz de la aplicación:

```text
http://localhost:8080/
```

Por ejemplo:

```java
@GetMapping("/")
public String mostrarInicio(Model model) {
    return "index";
}
```

Esto significa:

```text
Cuando el navegador pida la ruta /
↓
Spring ejecuta el método mostrarInicio()
```

---

## 6. `return "index"`

Cuando el método devuelve:

```java
return "index";
```

no está devolviendo el texto `"index"` al navegador.

Está devolviendo el nombre de una plantilla HTML.

Spring buscará esta plantilla en:

```text
src/main/resources/templates/index.html
```

Por tanto, el flujo completo es:

```text
Usuario entra en localhost:8080
↓
Spring recibe la petición GET /
↓
HomeController ejecuta mostrarInicio()
↓
El método devuelve "index"
↓
Spring busca templates/index.html
↓
Thymeleaf procesa la plantilla
↓
El navegador muestra el HTML final
```

---

## 7. Anotaciones y `@Override`

En este código no hay sobrescritura ni usamos `@Override`.

Las anotaciones como:

```java
@Controller
@GetMapping("/")
```

no sobrescriben métodos.

Simplemente dan instrucciones a Spring.

Una sobrescritura real sería, por ejemplo:

```java
@Override
public String toString() {
    return "Alumno...";
}
```

Ahí sí se sobrescribe un método heredado de la clase `Object`.

---

## 8. El objeto `Model`

`Model` es un objeto proporcionado por Spring que sirve para enviar datos desde un Controller hacia una vista HTML.

Funciona de forma parecida a un mapa de claves y valores.

Ejemplo:

```java
model.addAttribute("titolPagina", "Gestor d'alumnes");
model.addAttribute("missatge", "Benvinguda a la meva aplicació de gestió d'alumnes");
model.addAttribute("totalAlumnes", alumnos.size());
model.addAttribute("alumnes", alumnos);
```

Esto crea atributos que la plantilla HTML podrá leer:

```text
"titolPagina"  → "Gestor d'alumnes"
"missatge"     → "Benvinguda..."
"totalAlumnes" → número total de alumnos
"alumnes"      → lista de objetos Alumno
```

El `Model` no se crea manualmente con `new`.

No hacemos esto:

```java
Model model = new Model();
```

Spring lo proporciona automáticamente cuando lo declaramos como parámetro del método:

```java
public String mostrarInicio(Model model)
```

Resumen:

```text
Controller prepara datos
↓
Model transporta esos datos
↓
Thymeleaf los lee
↓
HTML los muestra
```

---

## 9. Thymeleaf

Thymeleaf es el motor de plantillas que permite mezclar HTML con datos enviados desde Java.

En el Controller añadimos datos al `Model`:

```java
model.addAttribute("titolPagina", "Gestor d'alumnes");
```

Y en el HTML los leemos así:

```html
<h1 data-th-text="${titolPagina}">Gestor d'alumnes</h1>
```

La expresión:

```html
${titolPagina}
```

busca en el `Model` un atributo llamado `titolPagina`.

---

## 10. Uso de `data-th-text`

Thymeleaf permite usar dos sintaxis:

```html
th:text="${titolPagina}"
```

o:

```html
data-th-text="${titolPagina}"
```

Ambas hacen lo mismo.

En este proyecto usamos:

```html
data-th-text
```

porque NetBeans lo acepta mejor. La sintaxis `data-*` es válida en HTML5, así que evita muchos warnings visuales del editor.

Ejemplo:

```html
<h1 data-th-text="${titolPagina}">Gestor d'alumnes</h1>
```

Esto significa:

```text
Busca el atributo titolPagina en el Model
↓
Sustituye el contenido del h1 por ese valor
```

El texto dentro de la etiqueta funciona como texto de reserva:

```html
Gestor d'alumnes
```

Si el HTML se abre sin pasar por Spring, se verá ese texto.

Si lo procesa Thymeleaf, será sustituido por el valor real enviado desde Java.

---

## 11. Nombres entre Java y HTML

Los nombres usados en el `Model` y en Thymeleaf deben coincidir exactamente.

Correcto:

```java
model.addAttribute("alumnes", alumnos);
```

```html
${alumnes}
```

Incorrecto:

```java
model.addAttribute("Alumnes", alumnos);
```

```html
${alumnes}
```

`Alumnes` y `alumnes` no son lo mismo.

Thymeleaf distingue mayúsculas y minúsculas.

---

## 12. Clase `Alumno`

La clase `Alumno` representa los datos de un alumno.

Ejemplo de atributos:

```java
private long id;
private String nombre;
private String apellido;
private String edad;
private Date fechaNac;
private String modalidad;
private String profesor;
```

De momento `Alumno` es una clase Java normal, con:

```text
atributos
constructores
getters y setters
toString()
```

Más adelante, cuando usemos base de datos con JPA, esta clase podrá convertirse en una entidad usando:

```java
@Entity
```

Pero todavía no estamos en esa fase.

---

## 13. Ejemplo de clase `Alumno`

Ejemplo simplificado de la clase `Alumno`:

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

    public Alumno(long id, String nombre, String apellido, String edad, Date fechaNac, String modalidad, String profesor) {
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

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getEdad() {
        return edad;
    }

    public Date getFechaNac() {
        return fechaNac;
    }

    public String getModalidad() {
        return modalidad;
    }

    public String getProfesor() {
        return profesor;
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

## 14. Lista temporal de alumnos

Actualmente estamos usando una lista falsa de alumnos dentro del Controller.

Ejemplo:

```java
List<Alumno> alumnos = new ArrayList<>();

alumnos.add(new Alumno(1L, "Laia", "Martinez", "12", null, "Guitarra", "Cristian"));
alumnos.add(new Alumno(2L, "Miquel", "Perez", "17", null, "Dansa", "Olga"));
alumnos.add(new Alumno(3L, "Oscar", "Gomez", "42", null, "Teatre", "Lara"));
```

Esta lista es temporal.

Sirve para aprender a enviar objetos Java al HTML y mostrarlos con Thymeleaf.

Más adelante, los alumnos serán introducidos por el profesor mediante un formulario y se guardarán en una base de datos.

---

## 15. Enviar la lista al HTML

Para enviar la lista al HTML usamos:

```java
model.addAttribute("alumnes", alumnos);
```

También podemos enviar el total:

```java
model.addAttribute("totalAlumnes", alumnos.size());
```

Entonces en el HTML podemos mostrar el total:

```html
<p>
    Total d'alumnes registrats:
    <strong data-th-text="${totalAlumnes}">0</strong>
</p>
```

---

## 16. `data-th-each`

`data-th-each` permite repetir una etiqueta HTML por cada elemento de una lista.

Ejemplo:

```html
<ul>
    <li data-th-each="alumne : ${alumnes}">
        <span data-th-text="${alumne.nombre}">Nom alumne</span>
    </li>
</ul>
```

Esto equivale a un `for each` de Java:

```java
for (Alumno alumne : alumnos) {
    System.out.println(alumne.getNombre());
}
```

La parte izquierda representa el elemento individual:

```html
alumne
```

La parte derecha representa la lista completa recibida desde el `Model`:

```html
${alumnes}
```

Por tanto:

```html
data-th-each="alumne : ${alumnes}"
```

significa:

```text
Por cada elemento de la lista alumnes,
crea un elemento HTML,
y llama alumne al elemento actual.
```

---

## 17. Nombre temporal del `for each`

El nombre temporal puede cambiar.

Esto funciona:

```html
<li data-th-each="alumne : ${alumnes}">
    <span data-th-text="${alumne.nombre}">Nom alumne</span>
</li>
```

Y esto también funciona:

```html
<li data-th-each="a : ${alumnes}">
    <span data-th-text="${a.nombre}">Nom alumne</span>
</li>
```

La variable `a` representa cada alumno individual durante cada vuelta del bucle.

Aun así, es mejor usar nombres claros:

```html
alumne
```

en lugar de nombres demasiado cortos como:

```html
a
x
item
```

porque el código resulta más fácil de leer.

---

## 18. Acceso a propiedades del objeto

Cuando escribimos:

```html
${alumne.nombre}
```

Thymeleaf accede al getter:

```java
getNombre()
```

Es decir, aunque en el HTML escribamos:

```html
${alumne.nombre}
```

por detrás Thymeleaf está usando el método Java correspondiente.

Otros ejemplos:

```html
${alumne.id}
${alumne.apellido}
${alumne.edad}
${alumne.modalidad}
${alumne.profesor}
```

Estos valores dependen de que existan los getters correspondientes:

```java
getId()
getApellido()
getEdad()
getModalidad()
getProfesor()
```

Por eso los getters son importantes.

---

## 19. Ejemplo completo de `HomeController`

Ejemplo del Controller con lista temporal de alumnos:

```java
package com.cristian.gestoralumnos.controller;

import com.cristian.gestoralumnos.model.Alumno;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String mostrarInicio(Model model) {
        
        List<Alumno> alumnos = new ArrayList<>();
        
        alumnos.add(new Alumno(1L, "Laia", "Martinez", "12", null, "Guitarra", "Cristian"));
        alumnos.add(new Alumno(2L, "Miquel", "Perez", "17", null, "Dansa", "Olga"));
        alumnos.add(new Alumno(3L, "Oscar", "Gomez", "42", null, "Teatre", "Lara"));
        
        model.addAttribute("titolPagina", "Gestor d'alumnes");
        model.addAttribute("missatge", "Benvinguda a la meva aplicació de gestió d'alumnes");
        model.addAttribute("totalAlumnes", alumnos.size()); 
        model.addAttribute("alumnes", alumnos); 
        
        return "index";
    } 
}
```

---

## 20. Ejemplo completo de `index.html`

Ejemplo de plantilla HTML usando Thymeleaf con sintaxis `data-th-*`:

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

    <p>Alumnes:</p>

    <ul>
        <li data-th-each="alumne : ${alumnes}">
            <span data-th-text="${alumne.nombre}">Nom alumne</span>
        </li>
    </ul>

    <p>Aquesta pàgina es carrega desde <strong>templates/index.html</strong></p>

</body>
</html>
```

---

## 21. Flujo completo actual de la aplicación

El flujo actual del proyecto es:

```text
Usuario entra en localhost:8080
↓
Spring recibe la petición GET /
↓
HomeController.mostrarInicio()
↓
Se crea una lista temporal de alumnos
↓
Los datos se añaden al Model
↓
El método devuelve "index"
↓
Spring busca templates/index.html
↓
Thymeleaf procesa data-th-text y data-th-each
↓
El navegador muestra el HTML final
```

Versión resumida:

```text
URL → Controller → Model → Thymeleaf → HTML renderizado
```

---

## 22. Errores encontrados y soluciones

### Error: Whitelabel Error Page

Apareció una página de error de Spring Boot porque había una expresión Thymeleaf mal escrita.

Ejemplo incorrecto:

```html
${alumnes]
```

La expresión estaba cerrada con `]` en lugar de `}`.

Correcto:

```html
${alumnes}
```

Cuando Thymeleaf encuentra una expresión mal escrita, puede fallar al procesar la plantilla y mostrar una Whitelabel Error Page.

---

### Error: mayúsculas y minúsculas

También hubo un problema con este atributo:

```java
model.addAttribute("Alumnes", alumnos);
```

Pero el HTML buscaba:

```html
${alumnes}
```

La solución fue usar el mismo nombre:

```java
model.addAttribute("alumnes", alumnos);
```

```html
${alumnes}
```

Regla:

```text
Los nombres del Model y del HTML deben coincidir exactamente.
```

---

### Error: clase `Alumno` en carpeta incorrecta

La clase `Alumno.java` debe estar en:

```text
src/main/java/com/cristian/gestoralumnos/model/Alumno.java
```

No debe estar en:

```text
src/test/java
```

ni en:

```text
src/main/resources
```

`src/main/java` contiene las clases reales de la aplicación.

`src/test/java` contiene pruebas.

`src/main/resources` contiene recursos como HTML, CSS, configuración y archivos estáticos.

---

## 23. Configuración de Thymeleaf durante el desarrollo

Para evitar problemas de caché mientras modificamos HTML, añadimos en:

```text
src/main/resources/application.properties
```

esta línea:

```properties
spring.thymeleaf.cache=false
```

Esto hace que Thymeleaf no reutilice versiones antiguas de las plantillas durante el desarrollo.

Así los cambios en `index.html` se reflejan más fácilmente al refrescar o ejecutar el archivo desde NetBeans.

---

## 24. Flujo de trabajo con NetBeans

Durante el desarrollo con NetBeans:

```text
Cambios en HTML
↓
Guardar
↓
Run File o refrescar navegador
```

Para cambios en Java:

```text
Cambios en HomeController.java o Alumno.java
↓
Guardar
↓
Run Project o reiniciar la aplicación si es necesario
```

Si se modifican dependencias, el `pom.xml`, paquetes o estructura de carpetas, conviene reiniciar la aplicación.

---

## 25. Estado actual del proyecto

Actualmente el proyecto ya consigue:

```text
Arrancar Spring Boot
Responder en localhost:8080
Cargar una plantilla HTML desde templates/index.html
Enviar datos desde el Controller al HTML usando Model
Mostrar texto dinámico con data-th-text
Recorrer una lista de alumnos con data-th-each
Mostrar alumnos temporales en la vista
```

Todavía no tenemos:

```text
Formulario para crear alumnos
Guardar alumnos introducidos por el profesor
Editar alumnos
Eliminar alumnos
Base de datos
Service
Repository
Login
Exportación PDF
Exportación CSV
Producción
```

---

## 26. Próximos pasos

Los próximos objetivos del proyecto serán:

```text
1. Mostrar los alumnos en una tabla HTML.
2. Crear un formulario para añadir alumnos.
3. Recibir los datos del formulario en el Controller.
4. Guardar alumnos temporalmente en memoria.
5. Crear funciones de editar y eliminar.
6. Añadir capa Service.
7. Añadir base de datos con Spring Data JPA.
8. Generar informes de alumnos.
9. Exportar datos a CSV.
10. Exportar informes a PDF.
11. Preparar el despliegue en producción.
```

---

## 27. Idea principal aprendida

La idea central aprendida hasta ahora es:

```text
Java prepara datos
↓
Model los transporta
↓
Thymeleaf los incrusta en HTML
↓
El navegador muestra una página dinámica
```

Ya no estamos abriendo un HTML estático directamente.

Ahora la página es servida por una aplicación Java con Spring Boot.

Esto convierte el proyecto en una aplicación web real.