-- Hasta ahora idioma_interes era texto libre. Desde aqui debe ser el codigo de un idioma del catalogo.
-- Si existieran filas con otro texto, la migracion falla a proposito: no se descartan datos en silencio.
ALTER TABLE inscripciones
    ADD CONSTRAINT inscripciones_idioma_interes_fk
    FOREIGN KEY (idioma_interes) REFERENCES idiomas (codigo);

COMMENT ON COLUMN inscripciones.idioma_interes IS 'Codigo del idioma del catalogo (idiomas.codigo) que la persona quiere aprender.';
