CREATE TABLE idiomas (
    codigo       text PRIMARY KEY,
    nombre       text NOT NULL,
    nivel_maximo text NOT NULL,
    CONSTRAINT idiomas_codigo_iso CHECK (codigo ~ '^[a-z]{2}$'),
    CONSTRAINT idiomas_nombre_unico UNIQUE (nombre),
    CONSTRAINT idiomas_nivel_maximo_mcer CHECK (nivel_maximo IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2'))
);

COMMENT ON TABLE idiomas IS
    'Idiomas que ensena la academia. Agregar uno es agregar una fila, no programar (ADR-0004).';
COMMENT ON COLUMN idiomas.codigo IS 'Codigo ISO 639-1. Identifica al idioma en toda la aplicacion.';
COMMENT ON COLUMN idiomas.nivel_maximo IS 'Ultimo nivel del Marco Comun Europeo que la academia ensena en este idioma.';

CREATE TABLE certificaciones (
    idioma_codigo text NOT NULL REFERENCES idiomas (codigo),
    nombre        text NOT NULL,
    PRIMARY KEY (idioma_codigo, nombre)
);

COMMENT ON TABLE certificaciones IS 'Examenes y enfoques para los que la academia prepara, por idioma.';

-- Programas publicados en https://polyglotcommonwealth.com/ (seccion Programas).
INSERT INTO idiomas (codigo, nombre, nivel_maximo) VALUES
    ('en', 'English', 'C2'),
    ('fr', 'Français', 'C1'),
    ('it', 'Italiano', 'B2'),
    ('pt', 'Português', 'B2'),
    ('es', 'Español para extranjeros (ELE)', 'C2'),
    ('de', 'Alemán', 'C1');

INSERT INTO certificaciones (idioma_codigo, nombre) VALUES
    ('en', 'Cambridge'),
    ('en', 'IELTS'),
    ('en', 'TOEFL'),
    ('en', 'PTE'),
    ('en', 'Duolingo'),
    ('en', 'Business'),
    ('fr', 'DELF'),
    ('fr', 'DALF'),
    ('it', 'CILS'),
    ('pt', 'CELPE-Bras'),
    ('es', 'DELE'),
    ('de', 'TestDaF'),
    ('de', 'Goethe');
