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
    public String mostrarInicio(Model model){
        
        List<Alumno> alumnos= new ArrayList<>();
        
        alumnos.add(new Alumno(1L, "Laia", "Martinez", "12",null, "Guitarra", "Cristian"));
        alumnos.add(new Alumno(2L, "Miquel", "Perez", "17",null, "Dansa", "Olga"));
        alumnos.add(new Alumno(3L, "Oscar", "Gomez", "42",null, "Teatre", "Lara"));
        
        model.addAttribute("titolPagina", "Gestor d'alumnes");
        model.addAttribute("missatge", "Benvinguda a la meva aplicació de gestió d'alumnes");
        model.addAttribute("totalAlumnes", alumnos.size()); 
        model.addAttribute("alumnes", alumnos); 
        
        return "index";
    } 
    
}
