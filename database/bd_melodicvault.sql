USE bd_melodicvault;

CREATE DATABASE IF NOT EXISTS BD_MelodicVault;
USE BD_MelodicVault;

INSERT INTO banda (nombre, pais, genero, anio_formacion, estado, descripcion, imagen_url)
VALUES
('Dokken', 'Estados Unidos', 'Melodic Hard Rock', 1978, 'Activa',
'Banda clásica del hard rock melódico ochentero.',
'https://upload.wikimedia.org/wikipedia/commons/8/89/Dokken_2008.jpg'),

('Europe', 'Suecia', 'Hard Rock', 1979, 'Activa',
'Agrupación sueca reconocida por su sonido melódico y teclados memorables.',
'https://upload.wikimedia.org/wikipedia/commons/9/99/Europe_the_band_in_2016.jpg'),

('Journey', 'Estados Unidos', 'AOR', 1973, 'Activa',
'Referente del AOR y rock melódico estadounidense.',
'https://upload.wikimedia.org/wikipedia/commons/f/fd/Journey_band.jpg');
select * from banda;

CREATE DATABASE IF NOT EXISTS BD_MelodicVault;
USE BD_MelodicVault;

INSERT INTO album (titulo, anio, tipo, rating, formato, portada_url, descripcion, id_banda)
VALUES
('Tooth And Nail', 1984, 'Studio Album', 9.2, 'Vinilo',
'https://heavyharmonies.com/cdcovers/D/DOKKEN6.JPG',
'Uno de los discos más representativos de Dokken.',
(SELECT id_banda FROM banda WHERE nombre = 'Dokken' LIMIT 1)),

('Back for Attack', 1987, 'Studio Album', 9.6, 'Vinilo',
'https://heavyharmonies.com/cdcovers/D/DOKKEN7.JPG',
'Álbum clásico con sonido potente y guitarras memorables.',
(SELECT id_banda FROM banda WHERE nombre = 'Dokken' LIMIT 1)),

('The Final Countdown', 1986, 'Studio Album', 9.0, 'CD',
'https://heavyharmonies.com/cdcovers/E/EUROPE1.JPG',
'Disco emblemático del hard rock europeo.',
(SELECT id_banda FROM banda WHERE nombre = 'Europe' LIMIT 1)),

('Escape', 1981, 'Studio Album', 9.5, 'CD',
'https://heavyharmonies.com/cdcovers/J/JOURNEY4.JPG',
'Uno de los álbumes más importantes del AOR.',
(SELECT id_banda FROM banda WHERE nombre = 'Journey' LIMIT 1));