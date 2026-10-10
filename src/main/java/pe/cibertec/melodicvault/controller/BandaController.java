package pe.cibertec.melodicvault.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import pe.cibertec.melodicvault.interfacesService.IBandaService;
import pe.cibertec.melodicvault.modelo.Banda;

@Controller
@RequestMapping("/bandas")
public class BandaController {

    @Autowired
    private IBandaService service;

    @GetMapping({"", "/"})
    public String listar(@RequestParam(value = "buscar", required = false) String buscar, Model model) {

        if (buscar != null && !buscar.trim().isEmpty()) {
            model.addAttribute("bandas", service.buscar(buscar));
            model.addAttribute("total", service.buscar(buscar).size());
            model.addAttribute("buscar", buscar);
        } else {
            model.addAttribute("bandas", service.listar());
            model.addAttribute("total", service.listar().size());
        }

        return "bandas";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("banda", new Banda());
        model.addAttribute("modo", "registrar");
        return "form-banda";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") int id, Model model, RedirectAttributes flash) {
        Optional<Banda> banda = service.listarId(id);

        if (banda.isEmpty()) {
            flash.addFlashAttribute("error", "La banda solicitada no existe.");
            return "redirect:/bandas";
        }

        model.addAttribute("banda", banda.get());
        model.addAttribute("modo", "editar");
        return "form-banda";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Banda banda, BindingResult result, Model model, RedirectAttributes flash) {

        if (result.hasErrors()) {
            model.addAttribute("modo", banda.getIdBanda() == null ? "registrar" : "editar");
            return "form-banda";
        }

        boolean esNuevo = banda.getIdBanda() == null;

        service.save(banda);

        flash.addFlashAttribute("mensaje",
                esNuevo ? "Banda registrada correctamente." : "Banda actualizada correctamente.");

        return "redirect:/bandas";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") int id, RedirectAttributes flash) {
        service.delete(id);
        flash.addFlashAttribute("mensaje", "Banda eliminada correctamente.");
        return "redirect:/bandas";
    }

    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable("id") int id, Model model, RedirectAttributes flash) {
        Optional<Banda> banda = service.listarId(id);

        if (banda.isEmpty()) {
            flash.addFlashAttribute("error", "La banda solicitada no existe.");
            return "redirect:/bandas";
        }

        model.addAttribute("banda", banda.get());
        return "detalle-banda";
    }

}