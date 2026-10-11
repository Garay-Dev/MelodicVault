package pe.cibertec.melodicvault.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import net.sf.jasperreports.engine.JRException;
import pe.cibertec.melodicvault.service.ReporteService;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    @Autowired private ReporteService reporteService;

    @GetMapping("/album/{id}")
    public ResponseEntity<byte[]> reporteAlbum(@PathVariable int id) throws JRException {
        byte[] pdf = reporteService.exportarCancionesAlbum(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=canciones_album_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}