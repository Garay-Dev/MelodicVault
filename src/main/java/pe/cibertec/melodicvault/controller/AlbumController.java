package pe.cibertec.melodicvault.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import pe.cibertec.melodicvault.interfacesService.IAlbumService;
import pe.cibertec.melodicvault.interfacesService.IBandaService;
import pe.cibertec.melodicvault.modelo.Album;

@Controller
@RequestMapping("/albumes")
public class AlbumController {

    @Autowired
    private IAlbumService service;

    @Autowired
    private IBandaService bandaService;

    @GetMapping({"", "/"})
    public String listar(@RequestParam(value = "buscar", required = false) String buscar, Model model) {

        if (buscar != null && !buscar.trim().isEmpty()) {
            model.addAttribute("albumes", service.buscar(buscar));
            model.addAttribute("total", service.buscar(buscar).size());
            model.addAttribute("buscar", buscar);
        } else {
            model.addAttribute("albumes", service.listar());
            model.addAttribute("total", service.listar().size());
        }

        return "albumes";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("album", new Album());
        model.addAttribute("bandas", bandaService.listar());
        model.addAttribute("modo", "registrar");
        return "form-album";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") int id, Model model, RedirectAttributes flash) {
        Optional<Album> album = service.listarId(id);

        if (album.isEmpty()) {
            flash.addFlashAttribute("error", "El álbum solicitado no existe.");
            return "redirect:/albumes";
        }

        model.addAttribute("album", album.get());
        model.addAttribute("bandas", bandaService.listar());
        model.addAttribute("modo", "editar");

        return "form-album";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Album album, BindingResult result, Model model, RedirectAttributes flash) {

        if (result.hasErrors()) {
            model.addAttribute("bandas", bandaService.listar());
            model.addAttribute("modo", album.getIdAlbum() == null ? "registrar" : "editar");
            return "form-album";
        }

        boolean esNuevo = album.getIdAlbum() == null;

        service.save(album);

        flash.addFlashAttribute("mensaje",
                esNuevo ? "Álbum registrado correctamente." : "Álbum actualizado correctamente.");

        return "redirect:/albumes";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") int id, RedirectAttributes flash) {

        Optional<Album> album = service.listarId(id);

        if (album.isEmpty()) {
            flash.addFlashAttribute("error", "El álbum no existe.");
            return "redirect:/albumes";
        }

        service.delete(id);
        flash.addFlashAttribute("mensaje", "Álbum eliminado correctamente.");

        return "redirect:/albumes";
    }
    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable("id") int id, Model model, RedirectAttributes flash) {

        Optional<Album> album = service.listarId(id);

        if (album.isEmpty()) {
            flash.addFlashAttribute("error", "El álbum solicitado no existe.");
            return "redirect:/albumes";
        }

        model.addAttribute("album", album.get());
        return "detalle-album";
    }
}