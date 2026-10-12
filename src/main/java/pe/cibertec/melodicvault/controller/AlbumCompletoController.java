package pe.cibertec.melodicvault.controller;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import pe.cibertec.melodicvault.dto.AlbumCompletoForm;
import pe.cibertec.melodicvault.interfacesService.IBandaService;
import pe.cibertec.melodicvault.modelo.Cancion;
import pe.cibertec.melodicvault.service.AlbumTransaccionalService;
import org.springframework.dao.DataIntegrityViolationException;


@Controller
@RequestMapping("/albumes/completo")
public class AlbumCompletoController {

    @Autowired private AlbumTransaccionalService transaccionalService;
    @Autowired private IBandaService bandaService;

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        AlbumCompletoForm form = new AlbumCompletoForm();
        for (int i = 0; i < AlbumCompletoForm.FILAS; i++) {
            form.getCanciones().add(new Cancion());
        }
        model.addAttribute("form", form);
        model.addAttribute("bandas", bandaService.listar());
        return "form-album-completo";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("form") AlbumCompletoForm form,
                          BindingResult result, Model model, RedirectAttributes flash) {

        // Ignora las filas vacias y numera automáticamente las que no traen pista
        List<Cancion> validas = new ArrayList<>();
        int pista = 1;
        for (Cancion c : form.getCanciones()) {
            if (c.getTitulo() == null || c.getTitulo().isBlank()) continue;
            if (c.getNumeroPista() == null) c.setNumeroPista(pista);
            validas.add(c);
            pista++;
        }

        if (result.hasErrors() || validas.isEmpty()) {
            if (validas.isEmpty()) model.addAttribute("error", "Agrega al menos una canción.");
            model.addAttribute("bandas", bandaService.listar());
            return "form-album-completo";
        }

        try {
            transaccionalService.registrarAlbumConCanciones(form.getAlbum(), validas);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "No se guardó nada: " + e.getMessage());
            model.addAttribute("bandas", bandaService.listar());
            return "form-album-completo";
        } catch (DataAccessException e) {
            model.addAttribute("error", "No se guardó nada: revisa que no haya pistas repetidas en el álbum.");
            model.addAttribute("bandas", bandaService.listar());
            return "form-album-completo";
        }

        flash.addFlashAttribute("mensaje", "Álbum y canciones registrados correctamente.");
        return "redirect:/albumes";
    }
}