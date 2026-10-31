CREATE TABLE inscripciones (
    id                   uuid        PRIMARY KEY,
    nombres              text        NOT NULL,
    apellidos            text        NOT NULL,
    correo               text        NOT NULL,
    fecha_nacimiento     date        NOT NULL,
    idioma_interes       text        NOT NULL,
    representante_nombre text,
    representante_correo text,
    consentimiento_en    timestamptz NOT NULL,
    creada_en            timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT inscripciones_correo_unico UNIQUE (correo),
    CONSTRAINT inscripciones_representante_completo
        CHECK ((representante_nombre IS NULL) = (representante_correo IS NULL))
);

COMMENT ON COLUMN inscripciones.consentimiento_en IS
    'Momento en que se recibio la aceptacion del tratamiento de datos (Ley 1581 de 2012).';
