package pe.cibertec.melodicvault.interfaces;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import pe.cibertec.melodicvault.modelo.Album;

public interface IAlbum extends CrudRepository<Album, Integer> {

    List<Album> findByTituloContainingIgnoreCaseOrTipoContainingIgnoreCaseOrBandaNombreContainingIgnoreCase(
            String titulo, String tipo, String banda
    );

    @Query("SELECT a.banda.nombre, COUNT(a), AVG(a.rating) FROM Album a GROUP BY a.banda.nombre ORDER BY COUNT(a) DESC")
    List<Object[]> estadisticasPorBanda();
}