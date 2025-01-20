CREATE TABLE storage (storage_type VARCHAR(100) PRIMARY KEY, bucket VARCHAR(100), path VARCHAR(100) );
INSERT INTO storage (storage_type, bucket) VALUES ('STAGING', 'song-bucket-staging');
INSERT INTO storage (storage_type, bucket) VALUES ('PERMANENT', 'song-bucket-permanent');