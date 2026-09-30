package pe.cibertec.melodicvault.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import pe.cibertec.melodicvault.modelo.Banda;
import pe.cibertec.melodicvault.modelo.Album;
import pe.cibertec.melodicvault.interfacesService.IAlbumService;
import pe.cibertec.melodicvault.interfacesService.IBandaService;

@Controller
public class HomeController {

    @Autowired
    private IBandaService bandaService;

    @Autowired
    private IAlbumService albumService;

    @GetMapping("/")
    public String home(Model model) {

        List<Banda> bandas = bandaService.listar();
        List<Album> albumes = albumService.listar();

        model.addAttribute("bandas", bandas.stream().limit(3).toList());
        model.addAttribute("albumes", albumes.stream().limit(3).toList());

        model.addAttribute("totalBandas", bandas.size());
        model.addAttribute("totalAlbumes", albumes.size());

        model.addAttribute("totalPaises",
                bandas.stream()
                        .map(Banda::getPais)
                        .distinct()
                        .count());

        model.addAttribute("totalGeneros",
                bandas.stream()
                        .map(Banda::getGenero)
                        .distinct()
                        .count());

        return "index";
    }

    @GetMapping("/acerca")
    public String acerca() {
        return "acerca";
    }
    
}