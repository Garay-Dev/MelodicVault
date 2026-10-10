package pe.cibertec.melodicvault.service;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User;

import pe.cibertec.melodicvault.interfaces.IUsuario;
import pe.cibertec.melodicvault.modelo.Usuario;

@Service
public class UsuarioDetailsService implements UserDetailsService {
    @Autowired private IUsuario repo;
    @Override
    public UserDetails loadUserByUsername(String username) {
        Usuario u = repo.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("No existe"));
        return User.withUsername(u.getUsername())
            .password(u.getPassword()).roles(u.getRol()).build();
    }
}