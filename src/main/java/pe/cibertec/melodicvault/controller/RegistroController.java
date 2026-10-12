package pe.cibertec.melodicvault.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import jakarta.validation.Valid;
import pe.cibertec.melodicvault.dto.RegistroForm;
import pe.cibertec.melodicvault.interfaces.IUsuario;
import pe.cibertec.melodicvault.modelo.Usuario;

@Controller
public class RegistroController {
    @Autowired private IUsuario repo;
    @Autowired private PasswordEncoder encoder;

    @GetMapping("/registro")
    public String form(Model model) {
        model.addAttribute("registroForm", new RegistroForm());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registroForm") RegistroForm form,
                            BindingResult result, RedirectAttributes flash) {

        if (!result.hasErrors() && !form.getPassword().equals(form.getConfirmar()))
            result.rejectValue("confirmar", "nocoincide", "Las contraseñas no coinciden");

        if (!result.hasErrors() && repo.existsByUsername(form.getUsername()))
            result.rejectValue("username", "duplicado", "Ese usuario ya existe");

        if (result.hasErrors()) return "registro";

        Usuario u = new Usuario();
        u.setUsername(form.getUsername());
        u.setPassword(encoder.encode(form.getPassword()));
        u.setRol("LECTOR");   

        try {
            repo.save(u);
        } catch (DataIntegrityViolationException e) {   
            result.rejectValue("username", "duplicado", "Ese usuario ya existe");
            return "registro";
        }

        flash.addFlashAttribute("exito", "Cuenta creada. Ya puedes iniciar sesión.");
        return "redirect:/login";
    }
}