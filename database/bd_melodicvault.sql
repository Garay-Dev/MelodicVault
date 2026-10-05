DROP DATABASE IF EXISTS bd_melodicvault;
CREATE DATABASE bd_melodicvault;
USE bd_melodicvault;

-- ============ TABLAS ============

CREATE TABLE banda (
    id_banda        INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(80)  NOT NULL,
    pais            VARCHAR(60)  NOT NULL,
    genero          VARCHAR(60)  NOT NULL,
    anio_formacion  INT          NOT NULL,
    estado          VARCHAR(20)  NOT NULL,
    descripcion     VARCHAR(500),
    imagen_url      VARCHAR(255)
);

CREATE TABLE album (
    id_album     INT AUTO_INCREMENT PRIMARY KEY,
    titulo       VARCHAR(100) NOT NULL,
    anio         INT          NOT NULL,
    tipo         VARCHAR(45)  NOT NULL,
    rating       DECIMAL(3,1),
    formato      VARCHAR(45),
    portada_url  VARCHAR(255),
    descripcion  VARCHAR(500),
    id_banda     INT          NOT NULL,
    CONSTRAINT fk_album_banda FOREIGN KEY (id_banda) REFERENCES banda(id_banda)
        ON DELETE CASCADE
);

CREATE TABLE usuario (
    id_usuario  INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(100) NOT NULL,
    rol         VARCHAR(20)  NOT NULL
);

CREATE TABLE cancion (
    id_cancion         INT AUTO_INCREMENT PRIMARY KEY,
    titulo             VARCHAR(100) NOT NULL,
    numero_pista       INT          NOT NULL,
    duracion_segundos  INT,
    id_album           INT          NOT NULL,
    CONSTRAINT fk_cancion_album FOREIGN KEY (id_album) REFERENCES album(id_album)
        ON DELETE CASCADE,
    CONSTRAINT uq_cancion_album_pista UNIQUE (id_album, numero_pista)
);

-- ============ DATOS ============

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

INSERT INTO usuario (username, password, rol) VALUES
('admin',  '$2b$10$aGsUNkvtNDwhTgm29zPs8eqWjD9I8rYghRx0p4ntzot/T.iCesOAq', 'ADMIN'),  

-- clave del admin : admin123

('lector', '$2b$10$DQuGrGBMFRwENahhD4sQ.uEe4hfvlGi/RHImkPataPw.MNhm6iFBm', 'USER');

-- clave del lector : lector123

INSERT INTO cancion (titulo, numero_pista, duracion_segundos, id_album) VALUES
('Into The Fire',       1, 245, (SELECT id_album FROM album WHERE titulo = 'Tooth And Nail')),
('Just Got Lucky',      2, 210, (SELECT id_album FROM album WHERE titulo = 'Tooth And Nail')),
('The Final Countdown', 1, 296, (SELECT id_album FROM album WHERE titulo = 'The Final Countdown'));

SELECT * FROM banda;
SELECT * FROM album;
SELECT * FROM usuario;
SELECT * FROM cancion;