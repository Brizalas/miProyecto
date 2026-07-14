package com.cristian.gestoralumnos.controller;

import com.cristian.gestoralumnos.repository.AlumnoRepository;
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
    private final AlumnoRepository alumnoRepository;

    public HomeController(AlumnoService alumnoService, AlumnoRepository alumnoRepository) {
        this.alumnoService = alumnoService;
        this.alumnoRepository=alumnoRepository;
    }

    @GetMapping("/")
    public String mostrarInicio(Model model) {
        List<Alumno> alumnosGuardados = alumnoService.listarAlumnos();
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

            List<Alumno> alumnosGuardados = alumnoService.listarAlumnos();

            model.addAttribute("titolPagina", "Gestor d'alumnes");
            model.addAttribute("missatge", "Benvinguda a la meva app de gestió d'alumnes");
            model.addAttribute("totalAlumnes", alumnosGuardados.size());
            model.addAttribute("alumnes", alumnosGuardados);

            return "index";
        }

        alumnoService.guardarAlumno(nuevoAlumno);

        return "redirect:/";

    }

    @PostMapping("/eliminar-alumne")
    public String eliminarAlumne(@RequestParam Long id) {
        alumnoService.eliminarAlumno(id);

        return "redirect:/";
    }

    @GetMapping("/editar-alumne")
    public String mostrarFormularioEditarAlumne(@RequestParam Long id,
            Model model) {

        Alumno alumnoEncontrado = alumnoService.encontrarAlumno(id);
        if(alumnoEncontrado==null){
            return "redirect:/";
        }
        model.addAttribute("alumneEditar", alumnoEncontrado);

        return "editar";
    }

    @PostMapping("/actualizar-alumne")
    public String actualizarAlumne(
            @Valid
            @ModelAttribute("alumneEditar") Alumno alumneEditar,
            BindingResult resultado
    ) {

        if (resultado.hasErrors()) {
            return "editar";
        }

         Alumno alumnoEncontrado = alumnoService.encontrarAlumno(alumneEditar.getId());

        if (alumnoEncontrado == null) {
            return "redirect:/";
        }

        alumnoEncontrado.setNombre(alumneEditar.getNombre());
        alumnoEncontrado.setApellido(alumneEditar.getApellido());
        alumnoEncontrado.setFechaNacimiento(
                alumneEditar.getFechaNacimiento()
        );
        alumnoEncontrado.setModalidad(alumneEditar.getModalidad());
        alumnoEncontrado.setProfesor(alumneEditar.getProfesor());

        alumnoRepository.save(alumnoEncontrado);

        return "redirect:/";
    }

}
