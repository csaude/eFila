ALTER TABLE drug DROP CONSTRAINT fk20a3c0633ef5cb;
ALTER TABLE regimendrugs DROP CONSTRAINT fk281dee12633d3b83;
ALTER TABLE clinic DROP CONSTRAINT fk78780108c18d4d76;
ALTER TABLE prescription DROP CONSTRAINT fk253af83a4e29eee7;
ALTER TABLE prescription DROP CONSTRAINT fk253af83a5e6aa99;
ALTER TABLE prescription DROP CONSTRAINT fk253af83a8877f9c1;
ALTER TABLE clinic ADD COLUMN IF NOT EXISTS province character varying(255) COLLATE pg_catalog."default" DEFAULT  '';
ALTER TABLE clinic ADD COLUMN IF NOT EXISTS district character varying(255) COLLATE pg_catalog."default" DEFAULT '';
ALTER TABLE clinic ADD COLUMN IF NOT EXISTS subDistrict character varying(255) COLLATE pg_catalog."default" DEFAULT '';
ALTER TABLE clinic ADD COLUMN IF NOT EXISTS code character varying(255) COLLATE pg_catalog."default" DEFAULT '';
ALTER TABLE clinic ADD COLUMN IF NOT EXISTS facilityType character varying(255) COLLATE pg_catalog."default" DEFAULT '';
ALTER TABLE clinic ADD COLUMN IF NOT EXISTS uuid character varying(255) COLLATE pg_catalog."default" DEFAULT '';
ALTER TABLE users ADD COLUMN IF NOT EXISTS state integer DEFAULT 1;
ALTER TABLE sync_temp_patients ADD COLUMN IF NOT EXISTS syncstatus character(1) COLLATE pg_catalog."default" DEFAULT 'P'::bpchar;
ALTER TABLE sync_temp_patients ADD COLUMN IF NOT EXISTS syncuuid character varying(255) COLLATE pg_catalog."default";
ALTER TABLE sync_temp_patients ADD COLUMN IF NOT EXISTS clinicuuid character varying(255) COLLATE pg_catalog."default";
ALTER TABLE sync_temp_patients ADD COLUMN IF NOT EXISTS mainclinicuuid character varying(255) COLLATE pg_catalog."default";
ALTER TABLE sync_temp_patients RENAME COLUMN uuid TO uuidopenmrs;
ALTER TABLE sync_temp_dispense ADD COLUMN IF NOT EXISTS syncstatus character(1) COLLATE pg_catalog."default" DEFAULT 'P'::bpchar;
ALTER TABLE sync_temp_dispense ADD COLUMN IF NOT EXISTS syncuuid character varying(255) COLLATE pg_catalog."default";
ALTER TABLE sync_temp_dispense ADD COLUMN IF NOT EXISTS uuidopenmrs character varying(255) COLLATE pg_catalog."default";
ALTER TABLE sync_temp_dispense DROP CONSTRAINT sync_temp_dispense_pkey;
ALTER TABLE sync_temp_dispense ADD CONSTRAINT sync_temp_dispense_pkey PRIMARY KEY (id, mainclinicname);
ALTER TABLE sync_temp_dispense RENAME COLUMN linhaid TO linhanome;
ALTER TABLE sync_temp_dispense RENAME COLUMN regimeid TO regimenome;
ALTER TABLE sync_temp_dispense DROP COLUMN IF EXISTS sync_temp_dispenseid;
ALTER TABLE sync_temp_dispense ADD COLUMN IF NOT EXISTS mainclinic integer NOT NULL;
ALTER TABLE sync_temp_dispense ADD COLUMN IF NOT EXISTS mainclinicname character varying(255) COLLATE pg_catalog."default" NOT NULL;
ALTER TABLE sync_temp_dispense ADD COLUMN IF NOT EXISTS mainclinicuuid character varying(255) COLLATE pg_catalog."default";
ALTER TABLE sync_temp_dispense ADD COLUMN IF NOT EXISTS prescriptionid character varying(255) COLLATE pg_catalog."default";
ALTER TABLE sync_temp_dispense ADD COLUMN IF NOT EXISTS tipods character varying(255) COLLATE pg_catalog."default";
ALTER TABLE sync_temp_dispense ADD COLUMN IF NOT EXISTS dispensasemestral integer NOT NULL DEFAULT 0;
ALTER TABLE sync_temp_dispense ADD COLUMN IF NOT EXISTS durationsentence character varying(255) COLLATE pg_catalog."default" DEFAULT NULL::character varying;
ALTER TABLE sync_temp_dispense ADD COLUMN IF NOT EXISTS dc character(1) COLLATE pg_catalog."default" DEFAULT 'F'::bpchar;
ALTER TABLE sync_temp_dispense ADD COLUMN IF NOT EXISTS prep character(1) COLLATE pg_catalog."default" DEFAULT 'F'::bpchar;
ALTER TABLE sync_temp_dispense ADD COLUMN IF NOT EXISTS ce character(1) COLLATE pg_catalog."default" DEFAULT 'F'::bpchar;
ALTER TABLE sync_temp_dispense ADD COLUMN IF NOT EXISTS cpn character(1) COLLATE pg_catalog."default" DEFAULT 'F'::bpchar;
ALTER TABLE sync_temp_dispense ADD COLUMN IF NOT EXISTS prescricaoespecial character(1) COLLATE pg_catalog."default" DEFAULT 'F'::bpchar;
ALTER TABLE sync_temp_dispense ADD COLUMN IF NOT EXISTS motivocriacaoespecial character varying(255) COLLATE pg_catalog."default" DEFAULT ''::character varying;
ALTER TABLE packagedruginfotmp ADD COLUMN IF NOT EXISTS ctzpickup boolean DEFAULT False;
ALTER TABLE packagedruginfotmp ADD COLUMN IF NOT EXISTS inhpickup boolean DEFAULT False;
ALTER TABLE sync_temp_patients ADD COLUMN IF NOT EXISTS prescriptiondate timestamp with time zone NULL;
ALTER TABLE sync_temp_patients ADD COLUMN IF NOT EXISTS duration integer DEFAULT 0;
ALTER TABLE sync_temp_patients ADD COLUMN IF NOT EXISTS prescriptionenddate timestamp with time zone NULL;
ALTER TABLE sync_temp_patients ADD COLUMN IF NOT EXISTS regimenome character varying(255) COLLATE pg_catalog."default";
ALTER TABLE sync_temp_patients ADD COLUMN IF NOT EXISTS linhanome character varying(255) COLLATE pg_catalog."default";
ALTER TABLE sync_temp_patients ADD COLUMN IF NOT EXISTS dispensatrimestral integer DEFAULT 0;
ALTER TABLE sync_temp_patients ADD COLUMN IF NOT EXISTS dispensasemestral integer DEFAULT 0;
ALTER TABLE sync_temp_patients ADD COLUMN IF NOT EXISTS prescriptionid character varying(255) COLLATE pg_catalog."default";
ALTER TABLE sync_temp_patients ADD COLUMN IF NOT EXISTS prescricaoespecial character(1) COLLATE pg_catalog."default" DEFAULT 'F'::bpchar;
ALTER TABLE sync_temp_patients ADD COLUMN IF NOT EXISTS motivocriacaoespecial character varying(255) COLLATE pg_catalog."default" DEFAULT ''::character varying;
UPDATE simpledomain set value = 'Voltou da Referencia' where name = 'activation_reason' and value = 'Desconhecido';
UPDATE clinic set uuid = uuid_generate_v1() where mainclinic = true and (uuid is null or uuid = '');
UPDATE regimeterapeutico set regimeesquema = REPLACE(regimeesquema, '_', '' );
UPDATE regimeterapeutico set regimeesquema = regimeesquema || '_' where codigoregime = null OR codigoregime = '';
UPDATE regimeterapeutico set active = false where codigoregime = null OR codigoregime = '' OR regimeesquema like '%d4T%';
DELETE FROM simpledomain WHERE description  = 'pharmacy_type';
DELETE FROM simpledomain WHERE description  = 'dispense_type';
DELETE FROM simpledomain WHERE description  = 'disease_type';
UPDATE regimeterapeutico SET regimenomeespecificado = 'cf05347e-063c-4896-91a4-097741cf6be6' WHERE regimeesquema LIKE 'ABC+3TC+LPV/r%';

-- UPDATE drug set active = false, name = name || ' (Inactivo)', atccode_id = '[inactivo]' where atccode_id is null or atccode_id = '';
-- update clinic set clinicname = 'Centro de Saude' where mainclinic = true;
-- update nationalclinics set facilityname = 'CS Chabeco' where facilityname = 'Unidade Sanitária';
-- update stockcenter set stockcentername = 'CS Chabeco' where stockcentername = 'Unidade Sanitária';
-- update simpledomain set "value" = 'CS Chabeco' where "value" = 'Unidade Sanitária';

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS country (
    id integer NOT NULL PRIMARY KEY,
    code character varying(50) NOT NULL,
    name character varying(200) NOT NULL,
	uuid character varying(255) NOT NULL DEFAULT uuid_generate_v1()
);

CREATE TABLE IF NOT EXISTS province (
    id integer NOT NULL PRIMARY KEY,
    code character varying(50) NOT NULL,
    name character varying(200) NOT NULL,
    country integer NOT NULL,
	uuid character varying(255) NOT NULL DEFAULT uuid_generate_v1()
);

CREATE TABLE IF NOT EXISTS district (
    id integer NOT NULL PRIMARY KEY,
    code character varying(50) NOT NULL,
    name character varying(200) NOT NULL,
    province integer NOT NULL,
	uuid character varying(255) NOT NULL DEFAULT uuid_generate_v1()
);

CREATE TABLE IF NOT EXISTS subdistrict (
    id integer NOT NULL PRIMARY KEY,
    code character varying(50) NOT NULL,
    name character varying(200) NOT NULL,
    district integer NOT NULL,
	uuid character varying(255) NOT NULL DEFAULT uuid_generate_v1()
);

CREATE TABLE IF NOT EXISTS sync_openmrs_patient (
    id integer NOT NULL PRIMARY KEY,
    cellphone character varying(255) COLLATE pg_catalog."default",
    dateofbirth timestamp with time zone,
    clinic integer NOT NULL,
    firstnames character varying(255) COLLATE pg_catalog."default",
    homephone character varying(255) COLLATE pg_catalog."default",
    lastname character varying(255) COLLATE pg_catalog."default",
    patientid character varying(255) COLLATE pg_catalog."default" NOT NULL,
    province character varying(255) COLLATE pg_catalog."default",
    sex character(1) COLLATE pg_catalog."default",
	syncstatus character(1) COLLATE pg_catalog."default",
    workphone character varying(255) COLLATE pg_catalog."default",
    address1 character varying(255) COLLATE pg_catalog."default",
    address2 character varying(255) COLLATE pg_catalog."default",
    address3 character varying(255) COLLATE pg_catalog."default",
    nextofkinname character varying(255) COLLATE pg_catalog."default",
    nextofkinphone character varying(255) COLLATE pg_catalog."default",
    race character varying(255) COLLATE pg_catalog."default",
    uuidopenmrs character varying(255) COLLATE pg_catalog."default",
    syncuuid character varying(255) NOT NULL DEFAULT uuid_generate_v1()
);

CREATE TABLE IF NOT EXISTS sync_openmrs_dispense (
    id integer NOT NULL PRIMARY KEY,
    strpickup character varying(255) COLLATE pg_catalog."default",
    nid character varying(255) COLLATE pg_catalog."default",
    uuid character varying(255) COLLATE pg_catalog."default",
    encountertype character varying(255) COLLATE pg_catalog."default",
    strfacility character varying(255) COLLATE pg_catalog."default",
    filauuid character varying(255) COLLATE pg_catalog."default",
    provider character varying(255) COLLATE pg_catalog."default",
    regimeuuid character varying(255) COLLATE pg_catalog."default",
	syncstatus character(1) COLLATE pg_catalog."default",
    regimenanswer character varying(255) COLLATE pg_catalog."default",
    dispensedamountuuid character varying(255) COLLATE pg_catalog."default",
    dosageuuid character varying(255) COLLATE pg_catalog."default",
    returnvisituuid character varying(255) COLLATE pg_catalog."default",
    strnextpickup character varying(255) COLLATE pg_catalog."default",
    prescription integer NOT NULL
);

CREATE TABLE IF NOT EXISTS systemfunctionality (
	id int4 NOT NULL,
	description varchar(100) NOT NULL,
	code varchar(100) NOT NULL,
	CONSTRAINT systemfunctionality_pk PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS "role" (
	id int4 NOT NULL,
	description varchar(100) NOT NULL,
	code varchar(100) NOT NULL,
	CONSTRAINT role_pk PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS rolefunction (
	roleid int4 NOT NULL,
	functionid int4 NOT NULL,
	CONSTRAINT rolefunction_fk FOREIGN KEY (roleid) REFERENCES "role"(id) ON DELETE CASCADE,
	CONSTRAINT rolefunction_fk_1 FOREIGN KEY (functionid) REFERENCES systemfunctionality(id) ON DELETE CASCADE,
	CONSTRAINT rolefunction_un UNIQUE (roleid,functionid)
);

CREATE TABLE IF NOT EXISTS user_role (
	roleid int4 NOT NULL,
	userid int4 NOT NULL,
	CONSTRAINT user_role_fk FOREIGN KEY (userid) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT user_role_fk_1 FOREIGN KEY (roleid) REFERENCES public."role"(id) ON DELETE CASCADE ON UPDATE CASCADE
);

INSERT INTO country (id, code, name) VALUES (1, '01', 'Moçambique');
INSERT INTO country (id, code, name) VALUES (2, '02', 'Angola');
INSERT INTO country (id, code, name) VALUES (3, '03', 'Africa do Sul');

INSERT INTO province (id, code, name, country) VALUES (1, '11', 'Maputo Cidade', 1);
INSERT INTO province (id, code, name, country) VALUES (2, '10', 'Maputo Província', 1);
INSERT INTO province (id, code, name, country) VALUES (3, '08', 'Inhambane', 1);
INSERT INTO province (id, code, name, country) VALUES (4, '09', 'Gaza', 1);
INSERT INTO province (id, code, name, country) VALUES (5, '07', 'Sofala', 1);
INSERT INTO province (id, code, name, country) VALUES (6, '06', 'Manica', 1);
INSERT INTO province (id, code, name, country) VALUES (7, '05', 'Tete', 1);
INSERT INTO province (id, code, name, country) VALUES (8, '04', 'Zambézia', 1);
INSERT INTO province (id, code, name, country) VALUES (9, '03', 'Nampula', 1);
INSERT INTO province (id, code, name, country) VALUES (10, '02', 'Cabo Delgado', 1);
INSERT INTO province (id, code, name, country) VALUES (11, '01', 'Niassa', 1);

INSERT INTO district (id, code, name, province ) VALUES (1, '01', 'Distrito Urbano de KaMpfumo', 1);
INSERT INTO district (id, code, name, province ) VALUES (2, '02', 'Distrito Urbano de Nlhamankulu', 1);
INSERT INTO district (id, code, name, province ) VALUES (3, '03', 'Distrito Urbano de KaMaxaquene', 1);
INSERT INTO district (id, code, name, province ) VALUES (4, '04', 'Distrito Urbano de KaMavota', 1);
INSERT INTO district (id, code, name, province ) VALUES (5, '05', 'Distrito Urbano de KaMubukwana', 1);
INSERT INTO district (id, code, name, province ) VALUES (6, '06', 'Distrito Municipal de KaTembe', 1);
INSERT INTO district (id, code, name, province ) VALUES (7, '07', 'Distrito Municipal de KaNyaka', 1);

INSERT INTO district (id, code, name, province ) VALUES (8, '01', 'Boane', 2);
INSERT INTO district (id, code, name, province ) VALUES (9, '02', 'Magude', 2);
INSERT INTO district (id, code, name, province ) VALUES (10, '03', 'Manhiça', 2);
INSERT INTO district (id, code, name, province ) VALUES (11, '04', 'Marracuene', 2);
INSERT INTO district (id, code, name, province ) VALUES (12, '05', 'Matola', 2);
INSERT INTO district (id, code, name, province ) VALUES (13, '06', 'Matutuine', 2);
INSERT INTO district (id, code, name, province ) VALUES (14, '07', 'Moamba', 2);
INSERT INTO district (id, code, name, province ) VALUES (15, '08', 'Namaacha', 2);

INSERT INTO district (id, code, name, province ) VALUES (16, '01', 'Funhalouro', 3);
INSERT INTO district (id, code, name, province ) VALUES (17, '02', 'Govuro', 3);
INSERT INTO district (id, code, name, province ) VALUES (18, '03', 'Homoíne', 3);
INSERT INTO district (id, code, name, province ) VALUES (19, '04', 'Cidade de Inhambane', 3);
INSERT INTO district (id, code, name, province ) VALUES (20, '05', 'Inharrime', 3);
INSERT INTO district (id, code, name, province ) VALUES (21, '06', 'Inhassoro', 3);
INSERT INTO district (id, code, name, province ) VALUES (22, '07', 'Jangamo', 3);
INSERT INTO district (id, code, name, province ) VALUES (23, '08', 'Mabote', 3);
INSERT INTO district (id, code, name, province ) VALUES (24, '09', 'Massinga', 3);
INSERT INTO district (id, code, name, province ) VALUES (25, '10', 'Maxixe', 3);
INSERT INTO district (id, code, name, province ) VALUES (26, '11', 'Morrumbene', 3);
INSERT INTO district (id, code, name, province ) VALUES (27, '12', 'Panda', 3);
INSERT INTO district (id, code, name, province ) VALUES (28, '13', 'Vilanculo', 3);
INSERT INTO district (id, code, name, province ) VALUES (29, '14', 'Zavala', 3);

INSERT INTO district (id, code, name, province ) VALUES (30, '01', 'Bilene', 4);
INSERT INTO district (id, code, name, province ) VALUES (31, '02', 'Chibuto', 4);
INSERT INTO district (id, code, name, province ) VALUES (32, '03', 'Chicualacuala', 4);
INSERT INTO district (id, code, name, province ) VALUES (33, '04', 'Chigubo', 4);
INSERT INTO district (id, code, name, province ) VALUES (34, '05', 'Chókwè', 4);
INSERT INTO district (id, code, name, province ) VALUES (35, '06', 'Chongoene', 4);
INSERT INTO district (id, code, name, province ) VALUES (36, '07', 'Guijá', 4);
INSERT INTO district (id, code, name, province ) VALUES (37, '08', 'Limpopo', 4);
INSERT INTO district (id, code, name, province ) VALUES (38, '09', 'Mabalane', 4);
INSERT INTO district (id, code, name, province ) VALUES (39, '10', 'Manjacaze', 4);
INSERT INTO district (id, code, name, province ) VALUES (40, '11', 'Mapai', 4);
INSERT INTO district (id, code, name, province ) VALUES (41, '12', 'Massangena', 4);
INSERT INTO district (id, code, name, province ) VALUES (42, '13', 'Massingir', 4);
INSERT INTO district (id, code, name, province ) VALUES (43, '14', 'Xai-Xai', 4);

INSERT INTO district (id, code, name, province ) VALUES (44, '01', 'Beira', 5);
INSERT INTO district (id, code, name, province ) VALUES (45, '02', 'Búzi', 5);
INSERT INTO district (id, code, name, province ) VALUES (46, '03', 'Caia', 5);
INSERT INTO district (id, code, name, province ) VALUES (47, '04', 'Chemba', 5);
INSERT INTO district (id, code, name, province ) VALUES (48, '05', 'Cheringoma', 5);
INSERT INTO district (id, code, name, province ) VALUES (49, '06', 'Chibabava', 5);
INSERT INTO district (id, code, name, province ) VALUES (50, '08', 'Dondo', 5);
INSERT INTO district (id, code, name, province ) VALUES (51, '09', 'Gorongosa', 5);
INSERT INTO district (id, code, name, province ) VALUES (52, '10', 'Machanga', 5);
INSERT INTO district (id, code, name, province ) VALUES (53, '11', 'Maringué', 5);
INSERT INTO district (id, code, name, province ) VALUES (54, '12', 'Marromeu', 5);
INSERT INTO district (id, code, name, province ) VALUES (55, '13', 'Muanza', 5);
INSERT INTO district (id, code, name, province ) VALUES (56, '14', 'Nhamatanda', 5);

INSERT INTO district (id, code, name, province ) VALUES (57, '01', 'Bárue', 6);
INSERT INTO district (id, code, name, province ) VALUES (58, '02', 'Chimoio', 6);
INSERT INTO district (id, code, name, province ) VALUES (59, '03', 'Gondola', 6);
INSERT INTO district (id, code, name, province ) VALUES (60, '04', 'Guro', 6);
INSERT INTO district (id, code, name, province ) VALUES (61, '05', 'Macate', 6);
INSERT INTO district (id, code, name, province ) VALUES (62, '06', 'Machaze', 6);
INSERT INTO district (id, code, name, province ) VALUES (63, '07', 'Macossa', 6);
INSERT INTO district (id, code, name, province ) VALUES (64, '08', 'Gorongosa', 6);
INSERT INTO district (id, code, name, province ) VALUES (65, '09', 'Manica', 6);
INSERT INTO district (id, code, name, province ) VALUES (66, '10', 'Mossurize', 6);
INSERT INTO district (id, code, name, province ) VALUES (67, '11', 'Sussundenga', 6);
INSERT INTO district (id, code, name, province ) VALUES (68, '12', 'Tambara', 6);
INSERT INTO district (id, code, name, province ) VALUES (69, '13', 'Vanduzi', 6);

INSERT INTO district (id, code, name, province ) VALUES (70, '01', 'Angónia', 7);
INSERT INTO district (id, code, name, province ) VALUES (71, '02', 'Cahora-Bassa', 7);
INSERT INTO district (id, code, name, province ) VALUES (72, '03', 'Changara', 7);
INSERT INTO district (id, code, name, province ) VALUES (73, '04', 'Chifunde', 7);
INSERT INTO district (id, code, name, province ) VALUES (74, '05', 'Chiuta', 7);
INSERT INTO district (id, code, name, province ) VALUES (75, '06', 'Dôa', 7);
INSERT INTO district (id, code, name, province ) VALUES (76, '08', 'Macanga', 7);
INSERT INTO district (id, code, name, province ) VALUES (77, '09', 'Magoé', 7);
INSERT INTO district (id, code, name, province ) VALUES (78, '10', 'Marara', 7);
INSERT INTO district (id, code, name, province ) VALUES (79, '11', 'Marávia', 7);
INSERT INTO district (id, code, name, province ) VALUES (80, '12', 'Moatize', 7);
INSERT INTO district (id, code, name, province ) VALUES (81, '13', 'Mutarara', 7);
INSERT INTO district (id, code, name, province ) VALUES (82, '14', 'Tete', 7);
INSERT INTO district (id, code, name, province ) VALUES (83, '15', 'Tsangano', 7);
INSERT INTO district (id, code, name, province ) VALUES (84, '16', 'Zumbo', 7);

INSERT INTO district (id, code, name, province ) VALUES (85, '01', 'Alto Molócue', 8);
INSERT INTO district (id, code, name, province ) VALUES (86, '02', 'Chinde', 8);
INSERT INTO district (id, code, name, province ) VALUES (87, '03', 'Derre', 8);
INSERT INTO district (id, code, name, province ) VALUES (88, '04', 'Gilé', 8);
INSERT INTO district (id, code, name, province ) VALUES (89, '05', 'Gurué', 8);
INSERT INTO district (id, code, name, province ) VALUES (90, '06', 'Ile', 8);
INSERT INTO district (id, code, name, province ) VALUES (01, '07', 'Inhassunge', 8);
INSERT INTO district (id, code, name, province ) VALUES (92, '08', 'Luabo', 8);
INSERT INTO district (id, code, name, province ) VALUES (93, '09', 'Lugela', 8);
INSERT INTO district (id, code, name, province ) VALUES (94, '10', 'Maganja da Costa', 8);
INSERT INTO district (id, code, name, province ) VALUES (95, '11', 'Milange', 8);
INSERT INTO district (id, code, name, province ) VALUES (96, '12', 'Mocuba', 8);
INSERT INTO district (id, code, name, province ) VALUES (97, '13', 'Mocubela', 8);
INSERT INTO district (id, code, name, province ) VALUES (98, '14', 'Molumbo', 8);
INSERT INTO district (id, code, name, province ) VALUES (99, '15', 'Mopeia', 8);
INSERT INTO district (id, code, name, province ) VALUES (100, '16', 'Morrumbala', 8);
INSERT INTO district (id, code, name, province ) VALUES (101, '17', 'Mulevala', 8);
INSERT INTO district (id, code, name, province ) VALUES (102, '18', 'Namacurra', 8);
INSERT INTO district (id, code, name, province ) VALUES (103, '19', 'Namarroi', 8);
INSERT INTO district (id, code, name, province ) VALUES (104, '20', 'Nicoadala', 8);
INSERT INTO district (id, code, name, province ) VALUES (105, '21', 'Pebane', 8);
INSERT INTO district (id, code, name, province ) VALUES (106, '22', 'Quelimane', 8);

INSERT INTO district (id, code, name, province ) VALUES (107, '01', 'Angoche', 9);
INSERT INTO district (id, code, name, province ) VALUES (108, '02', 'Eráti', 9);
INSERT INTO district (id, code, name, province ) VALUES (109, '03', 'Ilha de Moçambique', 9);
INSERT INTO district (id, code, name, province ) VALUES (110, '04', 'Lalaua', 9);
INSERT INTO district (id, code, name, province ) VALUES (111, '05', 'Larde', 9);
INSERT INTO district (id, code, name, province ) VALUES (112, '06', 'Liúpo', 9);
INSERT INTO district (id, code, name, province ) VALUES (113, '07', 'Malema', 9);
INSERT INTO district (id, code, name, province ) VALUES (114, '08', 'Meconta', 9);
INSERT INTO district (id, code, name, province ) VALUES (115, '09', 'Mecubúri', 9);
INSERT INTO district (id, code, name, province ) VALUES (116, '10', 'Memba', 9);
INSERT INTO district (id, code, name, province ) VALUES (117, '11', 'Mogincual', 9);
INSERT INTO district (id, code, name, province ) VALUES (118, '12', 'Mogovolas', 9);
INSERT INTO district (id, code, name, province ) VALUES (119, '13', 'Moma', 9);
INSERT INTO district (id, code, name, province ) VALUES (120, '14', 'Monapo', 9);
INSERT INTO district (id, code, name, province ) VALUES (121, '15', 'Mossuril', 9);
INSERT INTO district (id, code, name, province ) VALUES (122, '16', 'Muecate', 9);
INSERT INTO district (id, code, name, province ) VALUES (123, '17', 'Murrupula', 9);
INSERT INTO district (id, code, name, province ) VALUES (124, '18', 'Nacala-a-Velha', 9);
INSERT INTO district (id, code, name, province ) VALUES (125, '19', 'Nacala Porto', 9);
INSERT INTO district (id, code, name, province ) VALUES (126, '20', 'Nacarôa', 9);
INSERT INTO district (id, code, name, province ) VALUES (128, '21', 'Nampula', 9);
INSERT INTO district (id, code, name, province ) VALUES (129, '22', 'Rapale', 9);
INSERT INTO district (id, code, name, province ) VALUES (130, '23', 'Ribaué', 9);

INSERT INTO district (id, code, name, province ) VALUES (131, '01', 'Ancuabe', 10);
INSERT INTO district (id, code, name, province ) VALUES (132, '02', 'Balama', 10);
INSERT INTO district (id, code, name, province ) VALUES (133, '03', 'Chiúre', 10);
INSERT INTO district (id, code, name, province ) VALUES (134, '04', 'Ibo', 10);
INSERT INTO district (id, code, name, province ) VALUES (135, '05', 'Macomia', 10);
INSERT INTO district (id, code, name, province ) VALUES (136, '06', 'Mecúfi', 10);
INSERT INTO district (id, code, name, province ) VALUES (137, '07', 'Meluco', 10);
INSERT INTO district (id, code, name, province ) VALUES (138, '08', 'Metuge', 10);
INSERT INTO district (id, code, name, province ) VALUES (139, '09', 'Mocímboa da Praia', 10);
INSERT INTO district (id, code, name, province ) VALUES (140, '10', 'Montepuez', 10);
INSERT INTO district (id, code, name, province ) VALUES (141, '11', 'Mueda', 10);
INSERT INTO district (id, code, name, province ) VALUES (142, '12', 'Muidumbe', 10);
INSERT INTO district (id, code, name, province ) VALUES (143, '13', 'Namuno', 10);
INSERT INTO district (id, code, name, province ) VALUES (144, '14', 'Nangade', 10);
INSERT INTO district (id, code, name, province ) VALUES (145, '15', 'Palma', 10);
INSERT INTO district (id, code, name, province ) VALUES (146, '16', 'Pemba', 10);
INSERT INTO district (id, code, name, province ) VALUES (147, '17', 'Quissanga', 10);

INSERT INTO district (id, code, name, province ) VALUES (148, '01', 'Chimbonila', 11);
INSERT INTO district (id, code, name, province ) VALUES (149, '02', 'Cuamba', 11);
INSERT INTO district (id, code, name, province ) VALUES (150, '03', 'Lago', 11);
INSERT INTO district (id, code, name, province ) VALUES (151, '04', 'Lichinga', 11);
INSERT INTO district (id, code, name, province ) VALUES (152, '05', 'Majune', 11);
INSERT INTO district (id, code, name, province ) VALUES (153, '06', 'Mandimba', 11);
INSERT INTO district (id, code, name, province ) VALUES (154, '07', 'Marrupa', 11);
INSERT INTO district (id, code, name, province ) VALUES (155, '08', 'Maúa', 11);
INSERT INTO district (id, code, name, province ) VALUES (156, '09', 'Mavago', 11);
INSERT INTO district (id, code, name, province ) VALUES (157, '10', 'Mecanhelas', 11);
INSERT INTO district (id, code, name, province ) VALUES (158, '11', 'Mecula', 11);
INSERT INTO district (id, code, name, province ) VALUES (159, '12', 'Metarica', 11);
INSERT INTO district (id, code, name, province ) VALUES (160, '13', 'Muembe', 11);
INSERT INTO district (id, code, name, province ) VALUES (161, '14', 'Ngauma', 11);
INSERT INTO district (id, code, name, province ) VALUES (162, '15', 'Nipepe', 11);
INSERT INTO district (id, code, name, province ) VALUES (163, '16', 'Sanga', 11);

INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'pharmacy_type','pharmacy_type','Unidade Sanitária');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'pharmacy_type','pharmacy_type','Comunitária');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'pharmacy_type','pharmacy_type','Privada');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'pharmacy_type','pharmacy_type','.Outro');

INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'disease_type','disease_type','ARV');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'disease_type','disease_type','TARV');

INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'dispense_type','dispense_type','Dispensa Mensal (DM)');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'dispense_type','dispense_type','Dispensa Trimestral (DT)');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'dispense_type','dispense_type','Dispensa MensSemestral (DS)');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'dispense_type','dispense_type','Outro');

INSERT INTO "role" (id, description, code) values (1,'Administrador','ADMIN');
INSERT INTO "role" (id, description, code) values (2 ,'Técnico de Farmácia','PHARMACIST');
INSERT INTO "role" (id, description, code) values (3 ,'Administrativo de Farmácia','PHARMACISTADMIN');
INSERT INTO "role" (id, description, code) values (4 ,'Digitador','CLERK');
INSERT INTO "role" (id, description, code) values (5 ,'Estagiários','STUDYWORKER');
INSERT INTO "role" (id, description, code) values (6 ,'Monitoria e Avaliação','MEA');

INSERT INTO user_role (userid, roleid) values ((select users.id
                                               from users
                                               where not exists (select * from user_role where userid = users.id) and users.cl_username not like 'admin'),2);

INSERT INTO user_role (userid, roleid) values ((select users.id
                                               from users
                                               where not exists (select * from user_role where userid = users.id) and users.cl_username like 'admin'),1);

INSERT INTO systemfunctionality (id,description,code) VALUES (1,'Relatorios','REPORTS');
INSERT INTO systemfunctionality (id,description,code) VALUES (2,'Administração geral','ADMINISTRATION');
INSERT INTO systemfunctionality (id,description,code) VALUES (3,'Gestão de stock','STOCK_ADMINISTRATION');
INSERT INTO systemfunctionality (id,description,code) VALUES (4,'Administração de Pacientes','PACIENT_ADMINISTRATION');

INSERT INTO rolefunction (functionid, roleid) (select id, (select id as roleid from "role" r where r.code = 'ADMIN') from systemfunctionality s2);
INSERT INTO rolefunction (functionid, roleid) (select id, (select id as roleid from "role" r where r.code = 'PHARMACIST') from systemfunctionality s2 where s2.code != 'ADMINISTRATION');
INSERT INTO rolefunction (functionid, roleid) (select id, (select id as roleid from "role" r where r.code = 'PHARMACISTADMIN') from systemfunctionality s2 where s2.code != 'ADMINISTRATION');
INSERT INTO rolefunction (functionid, roleid) (select id, (select id as roleid from "role" r where r.code = 'CLERK') from systemfunctionality s2 where s2.code != 'ADMINISTRATION');
INSERT INTO rolefunction (functionid, roleid) (select id, (select id as roleid from "role" r where r.code = 'STUDYWORKER') from systemfunctionality s2 where s2.code != 'ADMINISTRATION');
INSERT INTO rolefunction (functionid, roleid) (select id, (select id as roleid from "role" r where r.code = 'MEA') from systemfunctionality s2 where s2.code != 'ADMINISTRATION');

ALTER TABLE users DROP COLUMN IF EXISTS "role";
ALTER TABLE users DROP COLUMN IF EXISTS "permission";