package pe.cibertec.melodicvault.interfaces;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import pe.cibertec.melodicvault.modelo.Usuario;

public interface IUsuario extends CrudRepository<Usuario, Integer> {
    Optional<Usuario> findByUsername(String username);
    boolean existsByUsername(String username);
}