package pe.cibertec.melodicvault.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pe.cibertec.melodicvault.interfaces.IAlbum;
import pe.cibertec.melodicvault.interfacesService.IAlbumService;
import pe.cibertec.melodicvault.modelo.Album;
import pe.cibertec.melodicvault.modelo.Cancion;

@Service
public class AlbumService implements IAlbumService {

    @Autowired
    private IAlbum data;

    @Override
    public List<Album> listar() {
        return (List<Album>) data.findAll();
    }

    @Override
    public Optional<Album> listarId(int id) {
        return data.findById(id);
    }

    @Override
    public int save(Album a) {
    	
        int res = 0;
        Album album = data.save(a);

        if (album != null) {
            res = 1;
        }

        return res;
    }

    @Override
    public void delete(int id) {
        data.deleteById(id);
    }
    
    @Override
    public List<Album> buscar(String texto) {
        return data.findByTituloContainingIgnoreCaseOrTipoContainingIgnoreCaseOrBandaNombreContainingIgnoreCase(
                texto, texto, texto
        );
    }
    
    @Override
    public List<Object[]> estadisticas() {
        return data.estadisticasPorBanda();
    }

    @Override
    public List<Cancion> listadoDeCanciones(int id) {
        return data.listadoDeCanciones(id);
    }
    
}