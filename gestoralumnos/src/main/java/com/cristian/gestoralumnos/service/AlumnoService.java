package com.cristian.gestoralumnos.service;

import com.cristian.gestoralumnos.model.Alumno;
import com.cristian.gestoralumnos.repository.AlumnoRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AlumnoService {

    private final AlumnoRepository alumnoRepository;

    public AlumnoService(AlumnoRepository alumnoRepository) {
        this.alumnoRepository = alumnoRepository;
    }

    public List<Alumno> listarAlumnos() {
        return alumnoRepository.findAll();
    }
    
    public Alumno guardarAlumno(Alumno alumno){
        return alumnoRepository.save(alumno);
    }
    
    public void eliminarAlumno(Long id){
         alumnoRepository.deleteById(id);
    }
    
    public Alumno encontrarAlumno(Long id){
        return alumnoRepository.findById(id).orElse(null);
    }
}