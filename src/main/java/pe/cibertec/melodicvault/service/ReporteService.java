package pe.cibertec.melodicvault.service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import pe.cibertec.melodicvault.interfacesService.IAlbumService;
import pe.cibertec.melodicvault.interfacesService.IBandaService;
import pe.cibertec.melodicvault.modelo.Album;
import pe.cibertec.melodicvault.modelo.Banda;
import pe.cibertec.melodicvault.modelo.Cancion;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class ReporteService {

	  @Autowired private IAlbumService albumService;
	  @Autowired private IBandaService bandaService;
	  

	    public byte[] exportarCancionesAlbum(int idAlbum) throws JRException {
	        Album album = albumService.listarId(idAlbum)
	                .orElseThrow(() -> new IllegalArgumentException("El álbum no existe"));

	        List<Cancion> canciones = albumService.listadoDeCanciones(idAlbum);

	        Map<String, Object> params = new HashMap<>();
	        params.put("albumTitulo", album.getTitulo());
	        params.put("bandaNombre", album.getBanda().getNombre());

	        InputStream plantilla = getClass()
	                .getResourceAsStream("/reportes/reporte_canciones.jrxml");
	        JasperReport report = JasperCompileManager.compileReport(plantilla);

	        JasperPrint print = JasperFillManager.fillReport(
	                report, params, new JRBeanCollectionDataSource(canciones));

	        return JasperExportManager.exportReportToPdf(print);
	    }
	    
	    public byte[] exportarAlbumesBanda(int idBanda) throws JRException {
	        Banda banda = bandaService.listarId(idBanda)
	                .orElseThrow(() -> new IllegalArgumentException("La banda no existe"));

	        List<Album> albumes = banda.getAlbumes();

	        Map<String, Object> params = new HashMap<>();
	        params.put("bandaNombre", banda.getNombre());
	        params.put("pais", banda.getPais());
	        params.put("genero", banda.getGenero());
	        params.put("anioFormacion", banda.getAnioFormacion());

	        InputStream plantilla = getClass()
	                .getResourceAsStream("/reportes/reporte_albumes.jrxml");
	        JasperReport report = JasperCompileManager.compileReport(plantilla);

	        JasperPrint print = JasperFillManager.fillReport(
	                report, params, new JRBeanCollectionDataSource(albumes));

	        return JasperExportManager.exportReportToPdf(print);
	    }
	    
}