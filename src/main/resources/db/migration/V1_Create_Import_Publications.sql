CREATE TABLE IF NOT EXISTS import_publications (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completion_date TIMESTAMP NULL,
    dto_type VARCHAR(255) NOT NULL,
    serialized_dto TEXT NOT NULL
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_import_publications_tenant_id ON import_publications(tenant_id);
CREATE INDEX IF NOT EXISTS idx_import_publications_completion_date ON import_publications(completion_date);
CREATE INDEX IF NOT EXISTS idx_import_publications_creation_date ON import_publications(creation_date);
CREATE INDEX IF NOT EXISTS idx_import_publications_unprocessed ON import_publications(tenant_id, creation_date)
    WHERE completion_date IS NULL;

-- Add comments
COMMENT ON TABLE import_publications IS 'Universal table for storing ETL import data from various tenants';
COMMENT ON COLUMN import_publications.tenant_id IS 'Identifier for the tenant that submitted the data';
COMMENT ON COLUMN import_publications.creation_date IS 'Timestamp when the record was created';
COMMENT ON COLUMN import_publications.completion_date IS 'Timestamp when the record was processed (NULL = unprocessed)';
COMMENT ON COLUMN import_publications.dto_type IS 'Type/class name of the serialized DTO';
COMMENT ON COLUMN import_publications.serialized_dto IS 'JSON serialized representation of the DTO';