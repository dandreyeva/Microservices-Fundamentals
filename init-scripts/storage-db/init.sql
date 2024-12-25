CREATE TABLE storage ( id SERIAL PRIMARY KEY, storage_type VARCHAR(100), bucket VARCHAR(100), path INTEGER REFERENCES path(id) );
CREATE TABLE path (id SERIAL PRIMARY KEY, path VARCHAR(100))

INSERT INTO storage (storage_type) VALUES ("STAGING"));
INSERT INTO storage (storage_type) VALUES ("PERMANENT"));