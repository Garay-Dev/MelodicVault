package pe.cibertec.melodicvault.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.cibertec.melodicvault.interfaces.IAlbum;
import pe.cibertec.melodicvault.interfaces.ICancion;
import pe.cibertec.melodicvault.modelo.Album;
import pe.cibertec.melodicvault.modelo.Cancion;

@Service
public class AlbumTransaccionalService {
    @Autowired private IAlbum albumRepo;
    @Autowired private ICancion cancionRepo;

    @Transactional
    public void registrarAlbumConCanciones(Album album, List<Cancion> canciones) {
        Album guardado = albumRepo.save(album);
        for (Cancion c : canciones) {
            if (c.getNumeroPista() == null || c.getNumeroPista() <= 0)
                throw new IllegalArgumentException("Pista inválida: " + c.getTitulo());
            c.setAlbum(guardado);
            cancionRepo.save(c);
        }
    }
}