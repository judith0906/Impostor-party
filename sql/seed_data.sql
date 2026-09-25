-- FILE: sql/seed_data.sql
USE impostor_party;

-- Palabras por categoria (se elige una para los jugadores normales y otra distinta para los impostores)
INSERT INTO words (word, challenge_type) VALUES
('Playa', 'normales'), ('Pizza', 'normales'), ('Bicicleta', 'normales'), ('Cine', 'normales'), ('Guitarra', 'normales'),
('Piscina', 'amigos'), ('Barbacoa', 'amigos'), ('Karaoke', 'amigos'), ('Excursion', 'amigos'), ('Videojuego', 'amigos'),
('Masaje', 'picantes'), ('Baile lento', 'picantes'), ('Perfume', 'picantes'), ('Susurro', 'picantes'), ('Cena a la luz de las velas', 'picantes'),
('Cotilleo', 'salseo'), ('Ex pareja', 'salseo'), ('Secreto', 'salseo'), ('Rumor', 'salseo'), ('Indirecta', 'salseo'),
('Salto en paracaidas', 'extremos'), ('Comida picante', 'extremos'), ('Karaoke a gritos', 'extremos'), ('Reto de frio', 'extremos'), ('Bano en agua helada', 'extremos');

-- Tareas para jugadores normales (is_for_impostor = FALSE)
INSERT INTO tasks (description, challenge_type, is_for_impostor) VALUES
('Menciona tu palabra en una frase sin que suene raro', 'normales', FALSE),
('Haz una foto relacionada con tu palabra sin explicar por que', 'normales', FALSE),
('Consigue que otra persona diga tu palabra en voz alta', 'normales', FALSE),
('Dibuja tu palabra en una servilleta y dejala a la vista', 'normales', FALSE),
('Cuenta una anecdota relacionada con tu palabra', 'normales', FALSE),

('Propon un brindis que tenga que ver con tu palabra', 'amigos', FALSE),
('Haz una peticion de cancion relacionada con tu palabra', 'amigos', FALSE),
('Organiza una mini votacion sobre algo relacionado con tu palabra', 'amigos', FALSE),
('Cuenta un plan futuro relacionado con tu palabra', 'amigos', FALSE),
('Ensena algo en tu movil relacionado con tu palabra', 'amigos', FALSE),

('Haz un cumplido que tenga relacion con tu palabra', 'picantes', FALSE),
('Susurra algo al oido de alguien relacionado con tu palabra', 'picantes', FALSE),
('Invita a bailar a alguien de forma relacionada con tu palabra', 'picantes', FALSE),
('Haz contacto visual prolongado mientras mencionas tu palabra', 'picantes', FALSE),
('Cuenta una experiencia relacionada (sutil) con tu palabra', 'picantes', FALSE),

('Cuenta un cotilleo inventado relacionado con tu palabra', 'salseo', FALSE),
('Pregunta a alguien algo indiscreto relacionado con tu palabra', 'salseo', FALSE),
('Suelta una indirecta que tenga que ver con tu palabra', 'salseo', FALSE),
('Haz una prediccion picante relacionada con tu palabra', 'salseo', FALSE),
('Cuenta un secreto (inventado) relacionado con tu palabra', 'salseo', FALSE),

('Reta a alguien a algo relacionado con tu palabra', 'extremos', FALSE),
('Haz un sonido relacionado con tu palabra sin que se note demasiado', 'extremos', FALSE),
('Convence a alguien de hacer algo relacionado con tu palabra', 'extremos', FALSE),
('Actua una mini escena relacionada con tu palabra', 'extremos', FALSE),
('Grita una palabra en clave relacionada con la tuya', 'extremos', FALSE);

-- Tareas especificas para el impostor (is_for_impostor = TRUE) -- mas orientadas a camuflarse
INSERT INTO tasks (description, challenge_type, is_for_impostor) VALUES
('Actua como si supieras la palabra de los demas sin preguntar directamente', 'normales', TRUE),
('Intenta averiguar la palabra real haciendo una pregunta indirecta', 'normales', TRUE),
('Copia el comportamiento de otro jugador para camuflarte', 'normales', TRUE),
('Se el primero en proponer un brindis para desviar la atencion', 'amigos', TRUE),
('Finge reconocer una referencia que en realidad no entiendes', 'amigos', TRUE),
('Da un cumplido ambiguo que podria aplicar a cualquier palabra', 'picantes', TRUE),
('Mira fijamente a alguien como si supieras algo que no sabes', 'picantes', TRUE),
('Inventa un cotilleo vago que podria encajar con cualquier tema', 'salseo', TRUE),
('Pregunta con cara de sospecha a otro jugador sin dar pistas tuyas', 'salseo', TRUE),
('Acepta un reto sin saber muy bien de que va y disimula', 'extremos', TRUE),
('Finge entusiasmo por algo que no entiendes del todo', 'extremos', TRUE);