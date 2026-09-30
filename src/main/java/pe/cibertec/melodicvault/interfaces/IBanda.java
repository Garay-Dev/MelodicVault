package pe.cibertec.melodicvault.interfaces;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import pe.cibertec.melodicvault.modelo.Banda;

public interface IBanda extends CrudRepository<Banda, Integer> {

    List<Banda> findByNombreContainingIgnoreCaseOrPaisContainingIgnoreCaseOrGeneroContainingIgnoreCase(
            String nombre, String pais, String genero
    );
}