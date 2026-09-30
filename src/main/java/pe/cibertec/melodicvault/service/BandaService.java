package pe.cibertec.melodicvault.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pe.cibertec.melodicvault.interfaces.IBanda;
import pe.cibertec.melodicvault.interfacesService.IBandaService;
import pe.cibertec.melodicvault.modelo.Banda;

@Service
public class BandaService implements IBandaService {

    @Autowired
    private IBanda data;

    @Override
    public List<Banda> listar() {
        return (List<Banda>) data.findAll();
    }

    @Override
    public Optional<Banda> listarId(int id) {
        return data.findById(id);
    }

    @Override
    public int save(Banda b) {
        int res = 0;
        Banda banda = data.save(b);

        if (banda != null) {
            res = 1;
        }

        return res;
    }

    @Override
    public void delete(int id) {
        data.deleteById(id);
    }

    @Override
    public List<Banda> buscar(String texto) {
        return data.findByNombreContainingIgnoreCaseOrPaisContainingIgnoreCaseOrGeneroContainingIgnoreCase(
                texto, texto, texto
        );
    }
}