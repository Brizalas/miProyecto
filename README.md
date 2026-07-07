

HomeController es una clase controladora de Spring Boot.

Su función es recibir peticiones del navegador y decidir qué vista HTML debe devolverse.

@Controller indica a Spring que esta clase es un controlador web.

@GetMapping("/") indica que el método mostrarInicio() responderá cuando el usuario 
acceda a la ruta raíz de la aplicación: http://localhost:8080/

El método devuelve "index", que no es texto literal para mostrar, sino el nombre 
de la plantilla HTML que Spring buscará en src/main/resources/templates/index.html.

En este código no hay sobrescritura ni @Override. Las anotaciones @Controller y 
@GetMapping no sobrescriben métodos; solo dan instrucciones a Spring.

Model es un objeto proporcionado por Spring que sirve para enviar datos desde un Controller hacia una vista HTML.

Funciona de forma parecida a un mapa de claves y valores: se añaden datos con model.addAttribute(nombre, valor).

Ejemplo:
model.addAttribute("titulo", "Gestor d'alumnes");

Esto crea un atributo llamado "titulo" que luego podrá ser leído desde la plantilla
Thymeleaf con ${titulo}.

Model no se crea manualmente con new. Spring lo proporciona automáticamente al 
método del Controller cuando lo declaramos como parámetro.

Thymeleaf permite usar atributos data-th-* como alternativa a th:*.

Ejemplo:
data-th-text="${titolPagina}"

Esto hace lo mismo que:
th:text="${titolPagina}"

La ventaja de data-th-text es que algunos editores como NetBeans lo aceptan mejor porque data-* es una sintaxis válida en HTML5.

El texto que hay dentro de la etiqueta funciona como texto de reserva. Cuando Thymeleaf procesa la plantilla, 
sustituye ese contenido por el valor enviado desde el Controller mediante Model.

En Spring Boot, el Controller recibe peticiones web y devuelve el nombre de una vista.

@GetMapping("/") indica que el método responderá a la ruta raíz de la aplicación.

El método devuelve "index", por lo que Spring busca la plantilla:
src/main/resources/templates/index.html

El objeto Model sirve para pasar datos desde el Controller hacia la vista.

Ejemplo:
model.addAttribute("titolPagina", "Gestor d'alumnes");

En Thymeleaf se leen esos datos con:
${titolPagina}

Para evitar warnings en NetBeans, usamos la sintaxis HTML5:
data-th-text="${titolPagina}"

en lugar de:
th:text="${titolPagina}"

data-th-text sustituye el contenido de una etiqueta por el valor recibido desde el Model.

data-th-each permite repetir una etiqueta por cada elemento de una lista.

Ejemplo:
<li data-th-each="alumne : ${alumnes}">
    <span data-th-text="${alumne.nombre}">Nom alumne</span>
</li>

Esto equivale a un for each de Java:
for (Alumno alumne : alumnos)

Cuando usamos ${alumne.nombre}, Thymeleaf accede al getter getNombre() del objeto Alumno.

Los nombres del Model y del HTML deben coincidir exactamente:
model.addAttribute("alumnes", alumnos);
${alumnes}

La estructura correcta del proyecto es:
src/main/java        → clases Java
src/main/resources   → templates, static, properties
src/test/java        → tests

