package pe.cibertec.melodicvault.interfaces;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import pe.cibertec.melodicvault.modelo.Album;

public interface IAlbum extends CrudRepository<Album, Integer> {

    List<Album> findByTituloContainingIgnoreCaseOrTipoContainingIgnoreCaseOrBandaNombreContainingIgnoreCase(
            String titulo, String tipo, String banda
    );
}