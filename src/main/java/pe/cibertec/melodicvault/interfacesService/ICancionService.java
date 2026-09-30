package pe.cibertec.melodicvault.interfacesService;
import java.util.List;
import java.util.Optional;
import pe.cibertec.melodicvault.modelo.Cancion;

public interface ICancionService {
    List<Cancion> listar();
    Optional<Cancion> listarId(int id);
    int save(Cancion c);
    void delete(int id);
}
