package pe.cibertec.melodicvault.interfaces;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import pe.cibertec.melodicvault.modelo.Banda;

public interface IBanda extends CrudRepository<Banda, Integer> {

    @EntityGraph(attributePaths = "albumes")
    Optional<Banda> findWithAlbumesByIdBanda(Integer idBanda);

    List<Banda> findByNombreContainingIgnoreCaseOrPaisContainingIgnoreCaseOrGeneroContainingIgnoreCase(
            String nombre, String pais, String genero
    );
}