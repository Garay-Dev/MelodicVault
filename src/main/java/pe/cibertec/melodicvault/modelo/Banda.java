package pe.cibertec.melodicvault.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.List;

@Entity
@Table(name = "banda")
public class Banda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idBanda;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80)
    private String nombre;

    @NotBlank(message = "El país es obligatorio")
    @Size(max = 60)
    private String pais;

    @NotBlank(message = "El género es obligatorio")
    @Size(max = 60)
    private String genero;

    @NotNull(message = "El año de formación es obligatorio")
    private Integer anioFormacion;

    @NotBlank(message = "El estado es obligatorio")
    @Size(max = 20)
    private String estado;

    @Size(max = 500)
    private String descripcion;

    private String imagenUrl;

    @OneToMany(mappedBy = "banda", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Album> albumes;

    public Integer getIdBanda() {
        return idBanda;
    }

    public void setIdBanda(Integer idBanda) {
        this.idBanda = idBanda;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public Integer getAnioFormacion() {
        return anioFormacion;
    }

    public void setAnioFormacion(Integer anioFormacion) {
        this.anioFormacion = anioFormacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }


    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public List<Album> getAlbumes() {
        return albumes;
    }

    public void setAlbumes(List<Album> albumes) {
        this.albumes = albumes;
    }
}