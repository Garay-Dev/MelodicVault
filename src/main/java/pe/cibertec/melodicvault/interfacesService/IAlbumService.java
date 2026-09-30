package pe.cibertec.melodicvault.interfacesService;

import java.util.List;
import java.util.Optional;

import pe.cibertec.melodicvault.modelo.Album;

public interface IAlbumService {

    List<Album> listar();

    Optional<Album> listarId(int id);

    int save(Album a);

    void delete(int id);
    
    List<Album> buscar(String texto);
}
