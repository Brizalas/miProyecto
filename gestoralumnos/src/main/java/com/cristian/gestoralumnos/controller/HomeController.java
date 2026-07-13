package com.cristian.gestoralumnos.controller;

import com.cristian.gestoralumnos.repository.AlumnoRepository;
import com.cristian.gestoralumnos.model.Alumno;
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
        List<Alumno> alumnosGuardados = alumnoRepository.findAll();
        model.addAttribute("titolPagina", "Gestor d'alumnes");
        model.addAttribute("missatge", "Benvinguda a la meva app de gestió d'alumnes");
        model.addAttribute("totalAlumnes", alumnosGuardados.size());
        model.addAttribute("alumnes", alumnosGuardados);

        model.addAttribute("nuevoAlumno", new Alumno());

        return "index";
    }

    @PostMapping("/afegir-alumne")
    public String afegirAlumne(
            @Valid
            @ModelAttribute("nuevoAlumno") Alumno nuevoAlumno,
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

    @PostMapping("/eliminar-alumne")
    public String eliminarAlumne(@RequestParam Long id) {
        alumnoRepository.deleteById(id);

        return "redirect:/";
    }

    @GetMapping("/editar-alumne")
    public String MostrarFormularioEditarAlumne(@RequestParam Long id,
            Model model) {

        Alumno alumnoEncontrado = alumnoRepository.findById(id).orElse(null);

        model.addAttribute("alumneEditar", alumnoEncontrado);

        return "editar";
    }

    @PostMapping("/actualizar-alumne")
    public String actualizarAlumne(
            @RequestParam Long id,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String modalidad,
            @RequestParam String profesor
    ) {

        Alumno alumnoEncontrado = alumnoRepository.findById(id).orElse(null);

        if (alumnoEncontrado != null) {
            alumnoEncontrado.setNombre(nombre);
            alumnoEncontrado.setApellido(apellido);

            alumnoEncontrado.setModalidad(modalidad);
            alumnoEncontrado.setProfesor(profesor);

            alumnoRepository.save(alumnoEncontrado);
        }

        return "redirect:/";

    }

}
