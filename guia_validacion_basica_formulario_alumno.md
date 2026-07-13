# Validación básica de formularios con Spring Boot, Thymeleaf y Jakarta Validation

Esta guía documenta el último paso realizado en el proyecto **Gestor de alumnos**.

El objetivo ha sido impedir que se guarde un alumno con el campo `nombre` vacío y comprender cómo colaboran:

```text
@NotBlank
@ModelAttribute
@Valid
BindingResult
Thymeleaf
```

---

# 1. Punto de partida

El CRUD ya funciona con:

```text
Spring Boot
Thymeleaf
JPA / Hibernate
H2
AlumnoRepository
LocalDate
```

En `pom.xml` ya está añadida la dependencia:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

Esta dependencia permite utilizar anotaciones como:

```java
@NotBlank
@NotNull
@Past
```

---

# 2. Primera regla: `@NotBlank`

En `Alumno.java` se añadió:

```java
import jakarta.validation.constraints.NotBlank;
```

Y el campo quedó así:

```java
@NotBlank
private String nombre;
```

`@NotBlank` rechaza:

```text
null
""
"     "
```

Y acepta:

```text
"Cristian"
"Laura"
```

Por tanto, sirve para comprobar que un texto contiene contenido real y no solamente espacios.

---

# 3. Por qué apareció Whitelabel al principio

La primera prueba consistió en añadir solamente:

```java
@NotBlank
private String nombre;
```

Después se envió el formulario con el nombre vacío.

El flujo fue:

```text
Formulario vacío
↓
Controller crea un Alumno
↓
alumnoRepository.save(alumno)
↓
Hibernate intenta guardar
↓
@NotBlank detecta el nombre vacío
↓
Se lanza una excepción
↓
Nadie recoge el error
↓
Whitelabel Error Page
```

La comparación con `try/catch` es razonable:

```java
try {
    alumnoRepository.save(alumno);
} catch (Exception e) {
    // gestionar el fallo
}
```

Sin embargo, para errores normales de formularios Spring MVC dispone de un mecanismo específico:

```text
@Valid
+
BindingResult
```

Un campo vacío no debería tratarse como un accidente grave. Es una situación esperable que debe devolverse al formulario con un mensaje comprensible.

---

# 4. Preparar un objeto para el formulario

En el método `GET /` se añadió:

```java
model.addAttribute("nuevoAlumno", new Alumno());
```

Ejemplo:

```java
@GetMapping("/")
public String mostrarInicio(Model model) {

    List<Alumno> alumnosGuardados = alumnoRepository.findAll();

    model.addAttribute("titolPagina", "Gestor d'alumnes");
    model.addAttribute("missatge", "Benvinguda a la meva app de gestió d'alumnes");
    model.addAttribute("totalAlumnes", alumnosGuardados.size());
    model.addAttribute("alumnes", alumnosGuardados);
    model.addAttribute("nuevoAlumno", new Alumno());

    return "index";
}
```

Esta línea crea un objeto vacío:

```java
new Alumno()
```

que será rellenado con los datos del formulario.

---

# 5. Relacionar el formulario con `nuevoAlumno`

El formulario original:

```html
<form action="/afegir-alumne" method="post">
```

se cambió por:

```html
<form
    action="/afegir-alumne"
    method="post"
    data-th-object="${nuevoAlumno}">
```

`data-th-object` indica que el formulario trabaja con el objeto:

```text
nuevoAlumno
```

Por ejemplo:

```html
<input name="nombre">
```

se relaciona con:

```java
nuevoAlumno.setNombre(...)
```

Y:

```html
<input name="fechaNacimiento">
```

se relaciona con:

```java
nuevoAlumno.setFechaNacimiento(...)
```

---

# 6. Sustituir varios `@RequestParam`

Antes, el método recibía los campos uno por uno:

```java
@RequestParam String nombre,
@RequestParam String apellido,
@RequestParam LocalDate fechaNacimiento,
@RequestParam String modalidad,
@RequestParam String profesor
```

Ahora recibe un objeto completo:

```java
@ModelAttribute("nuevoAlumno")
Alumno nuevoAlumno
```

Import:

```java
import org.springframework.web.bind.annotation.ModelAttribute;
```

`@ModelAttribute` hace este trabajo:

```text
Spring lee los name="" del formulario
↓
Busca propiedades con esos nombres
↓
Crea y rellena un objeto Alumno
↓
Entrega ese objeto al Controller
```

---

# 7. Activar la validación con `@Valid`

Se añadió:

```java
import jakarta.validation.Valid;
```

Y el parámetro quedó así:

```java
@Valid
@ModelAttribute("nuevoAlumno")
Alumno nuevoAlumno
```

La diferencia entre las piezas es:

```text
@NotBlank
→ define la regla

@Valid
→ ordena comprobar la regla
```

Sin `@Valid`, la regla no se procesa en el momento adecuado del formulario.

---

# 8. Recoger los errores con `BindingResult`

Se añadió:

```java
import org.springframework.validation.BindingResult;
```

Y el método recibe:

```java
BindingResult resultado
```

Debe colocarse inmediatamente después del objeto validado:

```java
@Valid
@ModelAttribute("nuevoAlumno")
Alumno nuevoAlumno,

BindingResult resultado,

Model model
```

Después se comprueba:

```java
if (resultado.hasErrors()) {
    // volver al formulario
}
```

`resultado.hasErrors()` significa:

```text
¿Se ha incumplido alguna regla de validación?
```

---

# 9. Método de alta actualizado

```java
@PostMapping("/afegir-alumne")
public String afegirAlumne(
        @Valid
        @ModelAttribute("nuevoAlumno")
        Alumno nuevoAlumno,

        BindingResult resultado,

        Model model
) {

    if (resultado.hasErrors()) {

        List<Alumno> alumnosGuardados = alumnoRepository.findAll();

        model.addAttribute("titolPagina", "Gestor d'alumnes");
        model.addAttribute("missatge", "Benvinguda a la meva app de gestió d'alumnes");
        model.addAttribute("totalAlumnes", alumnosGuardados.size());
        model.addAttribute("alumnes", alumnosGuardados);

        return "index";
    }

    alumnoRepository.save(nuevoAlumno);

    return "redirect:/";
}
```

Cuando existen errores no se ejecuta:

```java
alumnoRepository.save(nuevoAlumno);
```

---

# 10. Flujo cuando el nombre está vacío

```text
Usuario envía el formulario
↓
Spring crea nuevoAlumno
↓
@Valid ejecuta las validaciones
↓
@NotBlank detecta el nombre vacío
↓
BindingResult recoge el error
↓
resultado.hasErrors() devuelve true
↓
No se ejecuta save()
↓
return "index"
↓
No aparece Whitelabel
```

Desde fuera puede parecer que el botón no hace nada, pero sí está trabajando.

El formulario se procesa, el error se detecta y el guardado se cancela.

---

# 11. Flujo cuando el nombre contiene texto

```text
Usuario envía el formulario
↓
Spring crea nuevoAlumno
↓
@Valid comprueba las reglas
↓
@NotBlank se cumple
↓
BindingResult no contiene errores
↓
resultado.hasErrors() devuelve false
↓
alumnoRepository.save(nuevoAlumno)
↓
redirect:/
```

---

# 12. Por qué se guarda aunque otros campos estén vacíos

Actualmente solo existe:

```java
@NotBlank
private String nombre;
```

Por tanto:

```text
nombre vacío          ❌
nombre con texto      ✅

apellido vacío        ✅ permitido
fecha vacía           ✅ permitido
modalidad vacía       ✅ permitido
profesor vacío        ✅ permitido
```

Spring solo controla las reglas que se han declarado.

---

# 13. De dónde sale el mensaje «no debe estar vacío»

La clase actual contiene:

```java
@NotBlank
private String nombre;
```

No contiene ningún mensaje escrito manualmente.

El mensaje:

```text
no debe estar vacío
```

proviene de la propia librería Jakarta Validation.

Cuando no se indica `message`, la anotación utiliza un mensaje predeterminado interno.

Conceptualmente:

```java
@NotBlank(
    message = "{jakarta.validation.constraints.NotBlank.message}"
)
```

La librería busca esa clave en sus archivos internos y muestra el texto correspondiente al idioma configurado.

---

# 14. Personalizar el mensaje

Para decidir exactamente qué texto mostrar:

```java
@NotBlank(message = "El nombre es obligatorio")
private String nombre;
```

O en catalán:

```java
@NotBlank(message = "El nom és obligatori")
private String nombre;
```

Resumen:

```text
@NotBlank
→ mensaje predeterminado de la librería

@NotBlank(message = "...")
→ mensaje escrito por nosotros
```

---

# 15. Mostrar el mensaje en Thymeleaf

Debajo del input de nombre se puede añadir:

```html
<p
    data-th-if="${#fields.hasErrors('nombre')}"
    data-th-errors="*{nombre}">
    Error en el nombre
</p>
```

Bloque completo:

```html
<label for="nombre">Nom:</label>

<input
    type="text"
    id="nombre"
    name="nombre">

<p
    data-th-if="${#fields.hasErrors('nombre')}"
    data-th-errors="*{nombre}">
    Error en el nombre
</p>
```

## `data-th-if`

```html
data-th-if="${#fields.hasErrors('nombre')}"
```

Muestra el párrafo solamente si `nombre` tiene errores.

## `data-th-errors`

```html
data-th-errors="*{nombre}"
```

Muestra el mensaje asociado al error del campo `nombre`.

## `*{nombre}`

Como el formulario tiene:

```html
data-th-object="${nuevoAlumno}"
```

entonces:

```html
*{nombre}
```

significa:

```text
la propiedad nombre del objeto nuevoAlumno
```

---

# 16. Responsabilidad de cada pieza

## `@NotBlank`

```text
Define una regla para un String.
```

## `@ModelAttribute`

```text
Construye un objeto utilizando los datos del formulario.
```

## `@Valid`

```text
Ejecuta las reglas de validación.
```

## `BindingResult`

```text
Recoge los errores encontrados.
```

## `resultado.hasErrors()`

```text
Permite decidir si guardar o volver al formulario.
```

## `data-th-object`

```text
Relaciona el formulario con un objeto.
```

## `data-th-errors`

```text
Muestra el mensaje de validación.
```

---

# 17. Esquema mental completo

```text
Formulario HTML
↓
data-th-object="${nuevoAlumno}"
↓
name="nombre"
↓
@ModelAttribute Alumno nuevoAlumno
↓
@Valid
↓
@NotBlank
↓
BindingResult
↓
resultado.hasErrors()
```

Si hay errores:

```java
return "index";
```

Si no hay errores:

```java
alumnoRepository.save(nuevoAlumno);
return "redirect:/";
```

---

# 18. Errores frecuentes

## Colocar `BindingResult` en una posición incorrecta

Incorrecto:

```java
@Valid Alumno nuevoAlumno,
Model model,
BindingResult resultado
```

Correcto:

```java
@Valid Alumno nuevoAlumno,
BindingResult resultado,
Model model
```

## No volver a cargar los datos del Model

Cuando se devuelve directamente:

```java
return "index";
```

no se ejecuta el método `GET /`.

Por tanto, hay que volver a añadir:

```java
alumnos
totalAlumnes
titolPagina
missatge
```

## Pensar que el botón no funciona

Si no se muestra el error en HTML, parece que no sucede nada.

En realidad:

```text
El error está guardado en BindingResult,
pero todavía no se ha mostrado.
```

---

# 19. Estado actual

```text
@NotBlank          ✅
@ModelAttribute    ✅
@Valid             ✅
BindingResult      ✅
Evitar Whitelabel  ✅
Evitar guardado    ✅
Mostrar mensaje    ⏳ siguiente paso
```

El comportamiento actual es:

```text
Nombre vacío
↓
No se guarda
↓
No aparece Whitelabel
↓
Se vuelve a mostrar el formulario
```

---

# 20. Próximos pasos

Orden recomendado:

```text
1. Personalizar el mensaje de @NotBlank.
2. Mostrar el error debajo del input nombre.
3. Añadir @NotBlank a apellido.
4. Añadir @NotNull a fechaNacimiento.
5. Añadir @Past a fechaNacimiento.
6. Mostrar los errores de todos los campos.
7. Aplicar validación al formulario de edición.
```

Reglas previstas:

```java
@NotBlank(message = "El nom és obligatori")
private String nombre;

@NotBlank(message = "El cognom és obligatori")
private String apellido;

@NotNull(message = "La data de naixement és obligatòria")
@Past(message = "La data de naixement ha de ser anterior a avui")
private LocalDate fechaNacimiento;
```

---

# 21. Idea final

La validación no consiste solamente en colocar:

```java
@NotBlank
```

La cadena completa es:

```text
Regla en Alumno
↓
@Valid ejecuta la regla
↓
BindingResult recoge el error
↓
Controller evita guardar
↓
Thymeleaf muestra el mensaje
```

Resumen:

```text
@NotBlank define el problema.
@Valid lo busca.
BindingResult lo recoge.
El Controller evita guardar.
Thymeleaf se lo explica al usuario.
```
