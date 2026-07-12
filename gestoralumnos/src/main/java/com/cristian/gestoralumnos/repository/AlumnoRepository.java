
package com.cristian.gestoralumnos.repository;

import com.cristian.gestoralumnos.model.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> {
    
}
