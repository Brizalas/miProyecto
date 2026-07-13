package com.cristian.gestoralumnos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

@Entity
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "El nom es obligatori")
    private String nombre;
    @NotBlank(message = "El cognom es obligatori")
    private String apellido;
    @NotNull(message = "Data de naixament obligatoria")
    @Past(message = "La data de naixament ha de ser anterior a avui")
    private LocalDate fechaNacimiento;
    @NotBlank(message = "La disciplina es obligatoria")
    private String modalidad;
    @NotBlank(message = "El professor es obligatori")
    private String profesor;

    public Alumno() {

    }

    public Alumno(Long id, String nombre, String apellido, LocalDate fechaNacimiento, String modalidad, String profesor) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;

        this.fechaNacimiento = fechaNacimiento;
        this.modalidad = modalidad;
        this.profesor = profesor;

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getModalidad() {
        return modalidad;
    }

    public void setModalidad(String modalidad) {
        this.modalidad = modalidad;
    }

    public String getProfesor() {
        return profesor;
    }

    public void setProfesor(String profesor) {
        this.profesor = profesor;
    }

    @Override
    public String toString() {
        return "Alumno{" + "id= " + id + ", nombre= " + nombre + ", apellido= " + apellido + ", fechaNacimiento= " + fechaNacimiento + ", modalidad= " + modalidad + ", profesor= " + profesor + '}';
    }

}
