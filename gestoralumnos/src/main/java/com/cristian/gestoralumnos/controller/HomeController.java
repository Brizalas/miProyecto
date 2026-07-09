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
        
        public HomeController(){
            alumnos.add(new Alumno(1,"laia","Martinez","12", null, "Guitarra", "Cristian"));
            alumnos.add(new Alumno(2,"Miguel","Garcia","21",null,"Teatre", "Blanca"));
            alumnos.add(new Alumno(3,"Laura","Perez","42",null,"Dansa", "Olga"));
        }
        
        @GetMapping("/")
        public String mostrarInicio(Model model){
            model.addAttribute("titolPagina","Gestor d'alumnes");
            model.addAttribute("missatge", "Benvinguda a la meva app de gestió d'alumnes");
            model.addAttribute("totalAlumnes", alumnos.size());
            model.addAttribute("alumnes",alumnos);
            
            return "index";
        }
        
        @PostMapping("/afegir-alumne")
        public String afegirAlumne(@RequestParam String nombre){
            
            Long nouId = (long) alumnos.size() +1; //id auto incremental, nice!
            
            Alumno nouAlumne = new Alumno(
                    nouId,
                    nombre,
                    "",
                    "",
                    null,
                    "",
                    ""
                    
                    );
            
            alumnos.add(nouAlumne);
            
            return "redirect:/";
            
        }
        
        
    
}
