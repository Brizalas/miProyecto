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
}