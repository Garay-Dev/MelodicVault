package pe.cibertec.melodicvault.interfaces;


import org.springframework.data.repository.CrudRepository;
import pe.cibertec.melodicvault.modelo.Cancion;

public interface ICancion extends CrudRepository<Cancion, Integer> {
}