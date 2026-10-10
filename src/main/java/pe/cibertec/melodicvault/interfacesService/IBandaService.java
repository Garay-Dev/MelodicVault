package pe.cibertec.melodicvault.interfacesService;

import java.util.List;
import java.util.Optional;

import pe.cibertec.melodicvault.modelo.Banda;

public interface IBandaService {

    List<Banda> listar();

    Optional<Banda> listarId(int id);

    int save(Banda b);

    void delete(int id);
    
    List<Banda> buscar(String texto);
}