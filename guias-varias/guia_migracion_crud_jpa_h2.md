# Migración del CRUD de alumnos desde `ArrayList` hacia JPA y H2

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
