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

//        long nouId = alumnos.size() +1;--> Esto da problemas de duplicado a la larga...
        long nouId = siguienteId; //también se puede hacer 'long nouId = siguienteId ++;'
        siguienteId ++; //jugadón échale un vistazo, busca donde empieza siguienteId.

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
    public String eliminarAlumne(@RequestParam long id){
        alumnos.removeIf(alumne -> alumne.getId()== id);
        
        return "redirect:/";
    }

}
