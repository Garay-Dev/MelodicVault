package pe.cibertec.melodicvault.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import pe.cibertec.melodicvault.interfacesService.IAlbumService;
import pe.cibertec.melodicvault.interfacesService.ICancionService;
import pe.cibertec.melodicvault.modelo.Cancion;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;


@Controller
@RequestMapping("/canciones")
public class CancionController {

    @Autowired private ICancionService service;
    @Autowired private IAlbumService albumService;

    @GetMapping({"", "/"})
    public String listar(Model model) {
        List<Cancion> canciones = service.listar();
        model.addAttribute("canciones", canciones);
        model.addAttribute("total", canciones.size());
        return "canciones";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("cancion", new Cancion());
        model.addAttribute("albumes", albumService.listar());
        model.addAttribute("modo", "registrar");
        return "form-cancion";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") int id, Model model, RedirectAttributes flash) {
        Optional<Cancion> c = service.listarId(id);
        if (c.isEmpty()) {
            flash.addFlashAttribute("error", "La canción solicitada no existe.");
            return "redirect:/canciones";
        }
        model.addAttribute("cancion", c.get());
        model.addAttribute("albumes", albumService.listar());
        model.addAttribute("modo", "editar");
        return "form-cancion";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Cancion cancion, BindingResult result, Model model, RedirectAttributes flash) {
    	if (cancion.getAlbum() == null || cancion.getAlbum().getIdAlbum() == null) {
    	    result.rejectValue("album", "requerido", "Debe seleccionar un álbum");
    	}
    	if (result.hasErrors()) {
            model.addAttribute("albumes", albumService.listar());
            model.addAttribute("modo", cancion.getIdCancion() == null ? "registrar" : "editar");
            return "form-cancion";
        }
        boolean esNueva = cancion.getIdCancion() == null;
        try {
            service.save(cancion);
        } catch (DataAccessException e) {
            result.rejectValue("numeroPista", "duplicado", "No se pudo guardar. Revisa que la pista no esté repetida en este álbum.");
            model.addAttribute("albumes", albumService.listar());
            model.addAttribute("modo", cancion.getIdCancion() == null ? "registrar" : "editar");
            return "form-cancion";
        }
        flash.addFlashAttribute("mensaje",
                esNueva ? "Canción registrada correctamente." : "Canción actualizada correctamente.");
        return "redirect:/canciones";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") int id, RedirectAttributes flash) {
        if (service.listarId(id).isEmpty()) {
            flash.addFlashAttribute("error", "La canción no existe.");
            return "redirect:/canciones";
        }
        service.delete(id);
        flash.addFlashAttribute("mensaje", "Canción eliminada correctamente.");
        return "redirect:/canciones";
    }
}