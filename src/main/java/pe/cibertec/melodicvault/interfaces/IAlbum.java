package pe.cibertec.melodicvault.interfaces;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import pe.cibertec.melodicvault.modelo.Album;
import pe.cibertec.melodicvault.modelo.Cancion;

public interface IAlbum extends CrudRepository<Album, Integer> {

    List<Album> findByTituloContainingIgnoreCaseOrTipoContainingIgnoreCaseOrBandaNombreContainingIgnoreCase(
            String titulo, String tipo, String banda
    );

    @Query("SELECT a.banda.nombre, COUNT(a), AVG(a.rating) FROM Album a GROUP BY a.banda.nombre ORDER BY COUNT(a) DESC")
    List<Object[]> estadisticasPorBanda();
    
    @Query("SELECT c FROM Cancion c WHERE c.album.idAlbum = :id ORDER BY c.numeroPista")
    List<Cancion> listadoDeCanciones(@Param("id") int id);
}