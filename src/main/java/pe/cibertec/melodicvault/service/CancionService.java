package pe.cibertec.melodicvault.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.cibertec.melodicvault.interfaces.ICancion;
import pe.cibertec.melodicvault.interfacesService.ICancionService;
import pe.cibertec.melodicvault.modelo.Cancion;

@Service
public class CancionService implements ICancionService {

    @Autowired
    private ICancion data;

    @Override
    public List<Cancion> listar() { return (List<Cancion>) data.findAll(); }

    @Override
    public Optional<Cancion> listarId(int id) { return data.findById(id); }

    @Override
    public int save(Cancion c) { return data.save(c) != null ? 1 : 0; }

    @Override
    public void delete(int id) { data.deleteById(id); }
}