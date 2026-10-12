package pe.cibertec.melodicvault.dto;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.Valid;
import pe.cibertec.melodicvault.modelo.Album;
import pe.cibertec.melodicvault.modelo.Cancion;

public class AlbumCompletoForm {

    public static final int FILAS = 5;

    @Valid
    private Album album = new Album();
    private List<Cancion> canciones = new ArrayList<>();

    public Album getAlbum() { return album; }
    public void setAlbum(Album album) { this.album = album; }
    public List<Cancion> getCanciones() { return canciones; }
    public void setCanciones(List<Cancion> canciones) { this.canciones = canciones; }
}