package pe.cibertec.melodicvault.modelo;

import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "album")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAlbum;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 100)
    private String titulo;

    @NotNull(message = "El año es obligatorio")
    private Integer anio;

    @NotBlank(message = "El tipo es obligatorio")
    private String tipo;

    @DecimalMin(value = "0.0", message = "El rating mínimo es 0")
    @DecimalMax(value = "10.0", message = "El rating máximo es 10")
    private Double rating;

    private String formato;

    private String portadaUrl;

    @Size(max = 500)
    private String descripcion;

    @NotNull(message = "Debe seleccionar una banda")
    @ManyToOne
    @JoinColumn(name = "id_banda", nullable = false)
    private Banda banda;

    public Integer getIdAlbum() {
        return idAlbum;
    }
    
    
    @OneToMany(mappedBy = "album", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @OrderBy("numeroPista ASC")
    private List<Cancion> canciones;

    public List<Cancion> getCanciones() { return canciones; }
    public void setCanciones(List<Cancion> canciones) { this.canciones = canciones; }
    
    

    public void setIdAlbum(Integer idAlbum) {
        this.idAlbum = idAlbum;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public String getPortadaUrl() {
        return portadaUrl;
    }

    public void setPortadaUrl(String portadaUrl) {
        this.portadaUrl = portadaUrl;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }


    public Banda getBanda() {
        return banda;
    }

    public void setBanda(Banda banda) {
        this.banda = banda;
    }
}