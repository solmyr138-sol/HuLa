INSERT INTO tenant_info (kp, tenant_id, tenant_name, tenant_desc, create_source, gmt_create, gmt_modified)
SELECT '1', 'bfa0d426-e281-4da0-b830-c3962ed883d1', 'dev', 'HuLa dev namespace', 'nacos', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM tenant_info WHERE tenant_id = 'bfa0d426-e281-4da0-b830-c3962ed883d1'
);
