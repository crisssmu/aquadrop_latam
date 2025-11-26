-- Insert initial priority tags data
INSERT INTO priority_tag (id, type, score) VALUES (1, 'DEFAULT', 1) ON CONFLICT (id) DO NOTHING;
INSERT INTO priority_tag (id, type, score) VALUES (2, 'HOSPITAL', 3) ON CONFLICT (id) DO NOTHING;
INSERT INTO priority_tag (id, type, score) VALUES (3, 'ESCUELA', 2) ON CONFLICT (id) DO NOTHING;
INSERT INTO priority_tag (id, type, score) VALUES (4, 'VULNERABLE', 4) ON CONFLICT (id) DO NOTHING;
