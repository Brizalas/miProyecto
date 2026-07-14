# Mejora del modelo `Alumno`: `LocalDate`, fecha de nacimiento y preparación de validaciones

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
