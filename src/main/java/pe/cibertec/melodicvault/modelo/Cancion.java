package pe.cibertec.melodicvault.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.persistence.UniqueConstraint;


@Entity
@Table(name = "cancion",
       uniqueConstraints = @UniqueConstraint(columnNames = {"id_album", "numero_pista"}))
public class Cancion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCancion;
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 100)
    private String titulo;

    @NotNull(message = "El número de pista es obligatorio")
    @Min(value = 1, message = "La pista debe ser 1 o mayor")
    private Integer numeroPista;

    @Min(0)
    private Integer duracionSegundos;

    @NotNull(message = "Debe seleccionar un álbum")
    @ManyToOne
    @JoinColumn(name = "id_album", nullable = false)
    private Album album;
	public Integer getIdCancion() {
		return idCancion;
	}
	public void setIdCancion(Integer idCancion) {
		this.idCancion = idCancion;
	}
	public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	public Integer getNumeroPista() {
		return numeroPista;
	}
	public void setNumeroPista(Integer numeroPista) {
		this.numeroPista = numeroPista;
	}
	public Integer getDuracionSegundos() {
		return duracionSegundos;
	}
	public void setDuracionSegundos(Integer duracionSegundos) {
		this.duracionSegundos = duracionSegundos;
	}
	public Album getAlbum() {
		return album;
	}
	public void setAlbum(Album album) {
		this.album = album;
	}
   
}
