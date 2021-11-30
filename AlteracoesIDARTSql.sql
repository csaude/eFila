-- ALTER TABLE drug DROP CONSTRAINT fk20a3c0633ef5cb;
-- ALTER TABLE regimendrugs DROP CONSTRAINT fk281dee12633d3b83;
-- ALTER TABLE clinic DROP CONSTRAINT fk78780108c18d4d76;
-- ALTER TABLE prescription DROP CONSTRAINT fk253af83a4e29eee7;
-- ALTER TABLE prescription DROP CONSTRAINT fk253af83a5e6aa99;
-- ALTER TABLE prescription DROP CONSTRAINT fk253af83a8877f9c1;
-- ALTER TABLE stocklevel DROP CONSTRAINT fk9d728e2e51a7a6d;
-- ALTER TABLE stock DROP CONSTRAINT fk4c806f6633d3b83;
-- ALTER TABLE stock DROP CONSTRAINT fk4c806f66ca88453;
ALTER TABLE stock ALTER COLUMN id SET DEFAULT nextval('hibernate_sequence');
ALTER TABLE stocklevel ALTER COLUMN id SET DEFAULT nextval('hibernate_sequence');
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
ALTER TABLE sync_temp_patients ADD COLUMN IF NOT EXISTS jsonprescribeddrugs TEXT COLLATE pg_catalog."default";
ALTER TABLE sync_temp_patients RENAME COLUMN uuid TO uuidopenmrs;
ALTER TABLE sync_temp_dispense ALTER COLUMN id SET DEFAULT nextval('hibernate_sequence');
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
ALTER TABLE packagedruginfotmp ADD COLUMN IF NOT EXISTS modedispense character varying(255) COLLATE pg_catalog."default" DEFAULT ''::character varying;
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
ALTER TABLE stockcenter ADD COLUMN IF NOT EXISTS clinicuuid character varying(255) COLLATE pg_catalog."default" DEFAULT ''::character varying;
ALTER TABLE clinic ADD CONSTRAINT clinic_un_uuid UNIQUE (uuid);
ALTER TABLE patient ADD CONSTRAINT patient_un_uuid UNIQUE (uuidopenmrs);
ALTER TABLE patient ADD COLUMN IF NOT EXISTS uuidlocationopenmrs character varying(255) COLLATE pg_catalog."default";
ALTER TABLE sync_openmrs_dispense ADD COLUMN IF NOT EXISTS notas character varying(255) COLLATE pg_catalog."default";
ALTER TABLE regimeterapeutico ADD COLUMN IF NOT EXISTS tipodoenca character varying(255) COLLATE pg_catalog."default" DEFAULT 'TARV'::character varying;
ALTER TABLE prescription ADD COLUMN IF NOT EXISTS tipodoenca character varying(255) COLLATE pg_catalog."default" DEFAULT 'TARV'::character varying;
ALTER TABLE drug ADD COLUMN IF NOT EXISTS defaultTakePeriod character varying(255) COLLATE pg_catalog."default" DEFAULT 'Dia'::character varying;
ALTER TABLE prescribeddrugs ADD COLUMN IF NOT EXISTS takeperiod character varying(255) COLLATE pg_catalog."default" DEFAULT 'Dia'::character varying;
ALTER TABLE sync_openmrs_dispense ADD COLUMN IF NOT EXISTS dispensemodeanswer character varying(255) COLLATE pg_catalog."default" DEFAULT ''::character varying;
ALTER TABLE sync_openmrs_dispense ADD COLUMN IF NOT EXISTS dispennsedQty character varying(255) COLLATE pg_catalog."default" DEFAULT '30'::character varying;
UPDATE simpledomain set value = 'Voltou da Referencia' where name = 'activation_reason' and value = 'Desconhecido';
UPDATE clinic set uuid = uuid_generate_v1() where mainclinic = true and (uuid is null or uuid = '');
UPDATE stockcenter set clinicuuid = (select uuid from clinic where mainclinic = true) where preferred = true;
UPDATE regimeterapeutico set regimeesquema = REPLACE(regimeesquema, '_', '' );
UPDATE regimeterapeutico set regimeesquema = regimeesquema || '_' where codigoregime = null OR codigoregime = '';
UPDATE regimeterapeutico set active = false where codigoregime = null OR codigoregime = '' OR regimeesquema like '%d4T%';
UPDATE regimeterapeutico SET regimenomeespecificado = 'cf05347e-063c-4896-91a4-097741cf6be6' WHERE regimeesquema LIKE 'ABC+3TC+LPV/r%';
UPDATE sync_openmrs_dispense SET notas='Removido do iDART', syncstatus='W' where syncstatus='P' AND prescription NOT IN (select id from prescription);
UPDATE drug SET defaultTakePeriod = 'Dia' WHERE defaultTakePeriod is null;
UPDATE prescribeddrugs SET takeperiod = 'Dia' WHERE takeperiod is null;
UPDATE identifiertype set name = 'NID CCR' where index = 4;
UPDATE prescription set tipodoenca = 'TARV' where tipodoenca is null ;
UPDATE patient set uuidlocationopenmrs = (select uuid from clinic where mainclinic = true) where uuidlocationopenmrs is null;
DELETE FROM simpledomain WHERE description  = 'pharmacy_type';
DELETE FROM simpledomain WHERE description  = 'dispense_type';
DELETE FROM simpledomain WHERE description  = 'dispense_mode';
DELETE FROM simpledomain WHERE description  = 'disease_type';
DELETE FROM simpledomain WHERE description  = 'Disease';
DELETE FROM simpledomain WHERE description  = 'Period';
DELETE FROM simpledomain WHERE description  = 'inh_prophylaxis';
DELETE FROM simpledomain WHERE value  = 'Referrido para P.U';
DELETE FROM simpledomain WHERE value  = 'Inicio CCR';

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
    dispensemodeanswer character varying(255) COLLATE pg_catalog."default",
    dispensedamountuuid character varying(255) COLLATE pg_catalog."default",
    dosageuuid character varying(255) COLLATE pg_catalog."default",
    returnvisituuid character varying(255) COLLATE pg_catalog."default",
    strnextpickup character varying(255) COLLATE pg_catalog."default",
    prescription integer NOT NULL,
    notas character varying(255) COLLATE pg_catalog."default"
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

CREATE TABLE IF NOT EXISTS sync_temp_episode (
	id int4 NOT NULL,
	startdate timestamptz NULL,
	stopdate timestamptz NULL,
	startreason varchar(255) NULL,
	stopreason varchar(255) NULL,
	startnotes varchar(255) NULL,
	stopnotes varchar(255) NULL,
	patientuuid varchar(255) NULL,
	syncstatus bpchar(1) NULL,
	usuuid varchar(255) NULL,
	clinicuuid varchar(255) NULL,
	CONSTRAINT sync_episode_pkey PRIMARY KEY (id)
);

CREATE OR REPLACE FUNCTION user_role_pharmacist_loop_update()
    RETURNS void AS $$ DECLARE idartusersid int!
    BEGIN FOR idartusersid IN (SELECT * FROM users WHERE NOT EXISTS (SELECT * FROM user_role WHERE userid = users.id) and users.cl_username not like 'admin')
            LOOP INSERT INTO user_role (userid, roleid) values (idartusersid,2)! END LOOP! END!
     $$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS clinicsector (
	id int4 NOT NULL,
	code varchar(255) NULL,
	sectorname varchar(255) NOT NULL,
	telephone varchar(255) NULL,
	uuid varchar(255) NULL,
	clinic integer NOT NULL,
	clinicuuid varchar(255) NULL,
	CONSTRAINT clinic_sector_pkey PRIMARY KEY (id),
	CONSTRAINT clinic_secto_clinic_fk FOREIGN KEY (clinic) REFERENCES clinic(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS patient_sector (
	id int4 NOT NULL,
	startdate timestamptz NULL,
	enddate timestamptz NULL,
	endnotes varchar(255) NULL,
    clinicsector integer NOT NULL,
    patient integer NOT NULL,
	CONSTRAINT patient_sector_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS sync_mobile_patient (
    id integer NOT NULL PRIMARY KEY,
    cellphone character varying(255) COLLATE pg_catalog."default",
    dateofbirth timestamp with time zone,
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
    syncuuid character varying(255) NOT NULL DEFAULT uuid_generate_v1(),
    clinicsectoruuid character varying(255) COLLATE pg_catalog."default",
    clinicuuid character varying(255) COLLATE pg_catalog."default" NOT NULL,
    arvstartdate timestamp with time zone,
    enrolldate timestamp with time zone
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
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer, 'disease_type', 'disease_type', 'PREP');

INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'inh_prophylaxis','inh_prophylaxis','Inicio (I)');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'inh_prophylaxis','inh_prophylaxis','Continua (C)');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'inh_prophylaxis','inh_prophylaxis','Re-Inicio');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'inh_prophylaxis','inh_prophylaxis','Fim (F)');

INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'Disease','TARV','TARV');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'Disease','TB','TB');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer, 'Disease','PREP','PREP');

INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'Period','Dia','Dia');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'Period','Semana','Semana');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'Period','Mes','Mes');

INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'dispense_type','dispense_type','Dispensa Mensal (DM)');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'dispense_type','dispense_type','Dispensa Trimestral (DT)');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'dispense_type','dispense_type','Dispensa MensSemestral (DS)');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'dispense_type','dispense_type','Outro');

INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'','activation_reason','Referrido para P.U');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'','activation_reason','Inicio CCR');

INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'dispense_mode','4b51ace2-f778-4f54-bdaa-be2b350b7499','Farmácia Pública - Hora Normal');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'dispense_mode','1309d08a-5c73-4429-8f4b-43a551952858','Farmácia Pública - Fora Hora Normal');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'dispense_mode','d2eaec39-9c48-443b-a8d5-b2b163d42c53','FARMAC/Farmácia Privada');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'dispense_mode','870e2d25-c5ef-4e36-89db-0a4a37af214e','Distribuição Comunitária pelo Provedor de Saúde');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'dispense_mode','0843c71b-be47-4de2-ba16-a08db52c1136','Distribuição Comunitária pelos APEs');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'dispense_mode','3ab58d0e-f831-4966-97bd-209738f5e4df','Brigadas Móveis - Distribuição durante o dia');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'dispense_mode','d6ad74a1-ff67-4b81-afa1-a0d906462623','Brigadas Móveis - Distribuição durante o final do dia');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'dispense_mode','467718bc-1756-4b3f-b1ee-98d01910153a','Clínicas Móveis - Distribuição durante o dia');
INSERT INTO simpledomain VALUES (NEXTVAL('hibernate_sequence')::integer,'dispense_mode','091737af-d6bf-4830-8e87-82572ffac9ea','Clínicas Móveis - Distribuição durante o final do dia');


INSERT INTO "role" (id, description, code) values (1,'Administrador','ADMIN');
INSERT INTO "role" (id, description, code) values (2 ,'Técnico de Farmácia','PHARMACIST');
INSERT INTO "role" (id, description, code) values (3 ,'Administrativo de Farmácia','PHARMACISTADMIN');
INSERT INTO "role" (id, description, code) values (4 ,'Digitador','CLERK');
INSERT INTO "role" (id, description, code) values (5 ,'Estagiários','STUDYWORKER');
INSERT INTO "role" (id, description, code) values (6 ,'Monitoria e Avaliação','MEA');

select user_role_pharmacist_loop_update();

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

INSERT INTO public.drug (id, form, dispensinginstructions1, dispensinginstructions2, modified, name, packsize, sidetreatment, defaultamnt, defaulttimes, stockcode, pediatric, active, tipodoenca, atccode_id, defaulttakeperiod, uuidopenmrs) VALUES (6117470, 14, '', '', 'T', '[INH 100 cp] Isoniazida 100mg', 1, 'F', 1, 1, NULL, NULL, true, 'TB', '08L04', 'Dia', '');
INSERT INTO public.drug (id, form, dispensinginstructions1, dispensinginstructions2, modified, name, packsize, sidetreatment, defaultamnt, defaulttimes, stockcode, pediatric, active, tipodoenca, atccode_id, defaulttakeperiod, uuidopenmrs) VALUES (6117520, 14, '', '', 'T', '[Vit B6 25mg cp] Piridoxina (Vit B6) 25mg', 1, 'F', 1, 1, NULL, NULL, true, 'TB', '12D14Z', 'Dia', '');
INSERT INTO public.drug (id, form, dispensinginstructions1, dispensinginstructions2, modified, name, packsize, sidetreatment, defaultamnt, defaulttimes, stockcode, pediatric, active, tipodoenca, atccode_id, defaulttakeperiod, uuidopenmrs) VALUES (6117510, 14, '', '', 'T', '[Vit B6 50mg cp] Piridoxina (Vitamina B6) 50mg', 1, 'F', 1, 1, NULL, NULL, true, 'TB', '12D14', 'Dia', '');
INSERT INTO public.drug (id, form, dispensinginstructions1, dispensinginstructions2, modified, name, packsize, sidetreatment, defaultamnt, defaulttimes, stockcode, pediatric, active, tipodoenca, atccode_id, defaulttakeperiod, uuidopenmrs) VALUES (6117480, 14, '', '', 'T', '[INH 300mg cp] Isoniazida 300mg', 1, 'F', 1, 1, NULL, NULL, true, 'TB', '08L03', 'Dia', '');
INSERT INTO public.drug (id, form, dispensinginstructions1, dispensinginstructions2, modified, name, packsize, sidetreatment, defaultamnt, defaulttimes, stockcode, pediatric, active, tipodoenca, atccode_id, defaulttakeperiod, uuidopenmrs) VALUES (6162450, 14, '', '', 'T', '[RPT/INH 300/300mg cp] Rifapentina 300mg/ Isoniazida 300mg', 1, 'F', 3, 1, NULL, NULL, true, 'TB', '08L06X', 'Semana', '');
INSERT INTO public.drug (id, form, dispensinginstructions1, dispensinginstructions2, modified, name, packsize, sidetreatment, defaultamnt, defaulttimes, stockcode, pediatric, active, tipodoenca, atccode_id, defaulttakeperiod, uuidopenmrs) VALUES (6162460, 14, '', '', 'T', '[RPT 150mg cp] Rifapentina 150mg', 1, 'F', 1, 1, NULL, NULL, true, 'TB', '08L06XZ', 'Semana', '');
INSERT INTO public.drug (id, form, dispensinginstructions1, dispensinginstructions2, modified, name, packsize, sidetreatment, defaultamnt, defaulttimes, stockcode, pediatric, active, tipodoenca, atccode_id, defaulttakeperiod, uuidopenmrs) VALUES (6117500, 14, '', '', 'T', '[LFX 250mg cp] Levofloxacina 250mg ', 1, 'F', 1, 1, NULL, NULL, true, 'TB', '08H07', 'Dia', '');
INSERT INTO public.drug (id, form, dispensinginstructions1, dispensinginstructions2, modified, name, packsize, sidetreatment, defaultamnt, defaulttimes, stockcode, pediatric, active, tipodoenca, atccode_id, defaulttakeperiod, uuidopenmrs) VALUES (6117490, 14, '', '', 'T', '[LFX 100mg cp] Levofloxacina 100 mg Disp', 1, 'F', 1, 1, NULL, NULL, true, 'TB', '08H07Y', 'Dia', '');

INSERT INTO public.regimeterapeutico (regimeid, regimeesquema, active, regimenomeespecificado, regimeesquemaidart, codigoregime, tipodoenca) VALUES (6117710, 'TPT - 3HP Adultos (>30kg)', true, 'bce7d965-3396-4667-8294-aa5260c26334', 'TPT - 3HP Adultos (>30kg)', 'TPT - 3HP Adultos (>30kg)', 'TB');
INSERT INTO public.regimeterapeutico (regimeid, regimeesquema, active, regimenomeespecificado, regimeesquemaidart, codigoregime, tipodoenca) VALUES (6117780, 'TPT - 3HP Crianças (< 30kg)', true, 'bce7d965-3396-4667-8294-aa5260c26334', 'TPT - 3HP Crianças (< 30kg)', 'TPT - 3HP Crianças (< 30kg)', 'TB');
INSERT INTO public.regimeterapeutico (regimeid, regimeesquema, active, regimenomeespecificado, regimeesquemaidart, codigoregime, tipodoenca) VALUES (6117580, 'TPT - INH300', true, 'e1d43e52-1d5f-11e0-b929-000c29ad1d07', 'TPT - INH300', 'TPT - INH300', 'TB');
INSERT INTO public.regimeterapeutico (regimeid, regimeesquema, active, regimenomeespecificado, regimeesquemaidart, codigoregime, tipodoenca) VALUES (6162580, 'TPT - MR- LFX100', true, 'e1d4b30a-1d5f-11e0-b929-000c29ad1d07', 'TPT - MR- LFX100', 'TPT - MR- LFX100', 'TB');
INSERT INTO public.regimeterapeutico (regimeid, regimeesquema, active, regimenomeespecificado, regimeesquemaidart, codigoregime, tipodoenca) VALUES (6117630, 'TPT - MR- LFX250', true, 'e1d4b30a-1d5f-11e0-b929-000c29ad1d07', 'TPT - MR- LFX250', 'TPT - MR- LFX250', 'TB');
INSERT INTO public.regimeterapeutico (regimeid, regimeesquema, active, regimenomeespecificado, regimeesquemaidart, codigoregime, tipodoenca) VALUES (6162530, 'TPT - 1HP Adultos (>30kg)', false, 'bce7d965-3396-4667-8294-aa5260c26334', 'TPT - 1HP Adultos (>30kg)', 'TPT - 1HP Adultos (>30kg) ', 'TB');
INSERT INTO public.regimeterapeutico (regimeid, regimeesquema, active, regimenomeespecificado, regimeesquemaidart, codigoregime, tipodoenca) VALUES (6162660, 'TPT - INH100', true, 'e1d43e52-1d5f-11e0-b929-000c29ad1d07', 'TPT - INH100', 'TPT - INH100', 'TB');

INSERT INTO public.regimeterapeutico (regimeid, regimeesquema, active, regimenomeespecificado, regimeesquemaidart, codigoregime, tipodoenca) VALUES (6158800, 'TDF+3TC PrEP', true, 'e1e59e0e-1d5f-11e0-b929-000c29ad1d07', 'TDF+3TC PrEP', 'TDF+3TC PrEP', 'PREP');

INSERT INTO public.regimendrugs (id, amtpertime, drug, modified, regimen, timesperday, notes, regimendrugsindex) VALUES (6162470, 3, 6162450, 'T', 6117710, 1, NULL, 0);
INSERT INTO public.regimendrugs (id, amtpertime, drug, modified, regimen, timesperday, notes, regimendrugsindex) VALUES (6162480, 1, 6117510, 'T', 6117710, 1, NULL, 1);

INSERT INTO public.regimendrugs (id, amtpertime, drug, modified, regimen, timesperday, notes, regimendrugsindex) VALUES (6162770, 1, 6117520, 'T', 6117780, 1, NULL, 0);
INSERT INTO public.regimendrugs (id, amtpertime, drug, modified, regimen, timesperday, notes, regimendrugsindex) VALUES (6162780, 1, 6117510, 'T', 6117780, 1, NULL, 1);
INSERT INTO public.regimendrugs (id, amtpertime, drug, modified, regimen, timesperday, notes, regimendrugsindex) VALUES (6162790, 1, 6162460, 'T', 6117780, 1, NULL, 2);
INSERT INTO public.regimendrugs (id, amtpertime, drug, modified, regimen, timesperday, notes, regimendrugsindex) VALUES (6162800, 1, 6117470, 'T', 6117780, 1, NULL, 3);

INSERT INTO public.regimendrugs (id, amtpertime, drug, modified, regimen, timesperday, notes, regimendrugsindex) VALUES (6162860, 1, 6117510, 'T', 6117580, 1, NULL, 0);
INSERT INTO public.regimendrugs (id, amtpertime, drug, modified, regimen, timesperday, notes, regimendrugsindex) VALUES (6162870, 1, 6117480, 'T', 6117580, 1, NULL, 1);

INSERT INTO public.regimendrugs (id, amtpertime, drug, modified, regimen, timesperday, notes, regimendrugsindex) VALUES (6162920, 1, 6117490, 'T', 6162580, 1, NULL, 0);

INSERT INTO public.regimendrugs (id, amtpertime, drug, modified, regimen, timesperday, notes, regimendrugsindex) VALUES (6162930, 1, 6117500, 'T', 6117630, 1, NULL, 0);

INSERT INTO public.regimendrugs (id, amtpertime, drug, modified, regimen, timesperday, notes, regimendrugsindex) VALUES (6162940, 1, 6117510, 'T', 6162530, 1, NULL, 0);
INSERT INTO public.regimendrugs (id, amtpertime, drug, modified, regimen, timesperday, notes, regimendrugsindex) VALUES (6162950, 1, 6162450, 'T', 6162530, 1, NULL, 1);
INSERT INTO public.regimendrugs (id, amtpertime, drug, modified, regimen, timesperday, notes, regimendrugsindex) VALUES (6162960, 1, 6162460, 'T', 6162530, 1, NULL, 2);

INSERT INTO public.regimendrugs (id, amtpertime, drug, modified, regimen, timesperday, notes, regimendrugsindex) VALUES (6162830, 1, 6117470, 'T', 6162660, 1, NULL, 0);
INSERT INTO public.regimendrugs (id, amtpertime, drug, modified, regimen, timesperday, notes, regimendrugsindex) VALUES (6162840, 1, 6117520, 'T', 6162660, 1, NULL, 1);
INSERT INTO public.regimendrugs (id, amtpertime, drug, modified, regimen, timesperday, notes, regimendrugsindex) VALUES (6162850, 1, 6117510, 'T', 6162660, 1, NULL, 2);

INSERT INTO public.regimendrugs (id, amtpertime, drug, modified, regimen, timesperday, notes, regimendrugsindex) VALUES (6162730, 1, 41188, 'T', 6158800, 1, NULL, 0);


ALTER TABLE users DROP COLUMN IF EXISTS "role";
ALTER TABLE users DROP COLUMN IF EXISTS "permission";

 alter table drug add column uuidopenmrs varchar(255);

update drug set uuidopenmrs='08S18W-7f-c2a7-4d27-95dc-564791951b5f' where atccode_id='08S18W';
update drug set uuidopenmrs='08S18WI-4-70b1-4732-af8b-be24cb04aaa6' where atccode_id='08S18WI';
update drug set uuidopenmrs='08S18WII-5863-4423-9e97-bc1b480df134' where atccode_id='08S18WII';
update drug set uuidopenmrs='08S38Z-99-3fe6-48b7-9b25-3052660f3d8b' where atccode_id='08S38Z';
update drug set uuidopenmrs='08S01ZY-d7-4218-4032-aa8c-615aec71a218' where atccode_id='08S01ZY';
update drug set uuidopenmrs='08S01ZZ-2e-29dd-40aa-94b4-0d4fe65e081c' where atccode_id='08S01ZZ';
update drug set uuidopenmrs='08S40-833-b26a-4996-8066-48847431404a' where atccode_id='08S40';
update drug set uuidopenmrs='08S18Z-da-b787-4fa1-a2d6-2fda22da6564' where atccode_id='08S18Z';
update drug set uuidopenmrs='08S30ZZ-b2-edb5-4815-a4bf-8f0618f029be' where atccode_id='08S30ZZ';
update drug set uuidopenmrs='08S39-054-ad23-4d26-8ca1-88308070d08e' where atccode_id='08S39';
update drug set uuidopenmrs='08S39B-b2-e392-425a-b824-f33f745733e6' where atccode_id='08S39B';
update drug set uuidopenmrs='08S39Z-7a-93f6-4e05-baa3-8c4684c8013e' where atccode_id='08S39Z';
update drug set uuidopenmrs='08S38Y-0c-0932-4b37-ab53-4aae60820544' where atccode_id='08S38Y';
update drug set uuidopenmrs='08S30WZ-48-cf6b-47e2-a101-0cc9faa8fbce' where atccode_id='08S30WZ';
update drug set uuidopenmrs='08S22-d18-2655-4426-868a-b291a5adff38' where atccode_id='08S22';
update drug set uuidopenmrs='08S23Z-cd-22ff-4993-9378-335cf43bd5c8' where atccode_id='08S23Z';
update drug set uuidopenmrs='08S23-316-d7e9-4757-9466-d0e6c1d8b1db' where atccode_id='08S23';
update drug set uuidopenmrs='08S17Y-b6-6e76-451d-8892-a6d407e0e0d6' where atccode_id='08S17Y';
update drug set uuidopenmrs='08S15-f3b-d54c-420d-ad96-698f71c003c5' where atccode_id='08S15';
update drug set uuidopenmrs='08S01-ab-d6e9-4de6-beb8-441daf8d00a3' where atccode_id='08S01';
update drug set uuidopenmrs='08S01Z-02-843e-4dd0-af6f-2eff173d4b30' where atccode_id='08S01Z';
update drug set uuidopenmrs='08S21-918-6273-4f5c-be72-755da371e731' where atccode_id='08S21';
update drug set uuidopenmrs='08S20-9db-3bef-4de8-af61-4610d4ead1ba' where atccode_id='08S20';
update drug set uuidopenmrs='08S13-ddb-e5bd-4b96-ae5d-11ada78c8a35' where atccode_id='08S13';
update drug set uuidopenmrs='08S18-6cb-4b62-4cc0-98af-8053e1396419' where atccode_id='08S18';
update drug set uuidopenmrs='08S18XI-6-0715-4389-a4b2-d20de330e26c' where atccode_id='08S18XI';
update drug set uuidopenmrs='08S18X-b0-de4a-4233-86fa-c91f9e606be5' where atccode_id='08S18X';
update drug set uuidopenmrs='08S18XII-3a07-4e5f-9d00-0016dc840a84' where atccode_id='08S18XII';
update drug set uuidopenmrs='08S18Y-5e-9493-41e4-a5e0-5671be3fd66b' where atccode_id='08S18Y';
update drug set uuidopenmrs='08S42-d98-6b3e-485f-b969-376fca8b7789' where atccode_id='08S42';
update drug set uuidopenmrs='08S42B-d89-7e03-4f25-975e-b66a5e28e063' where atccode_id='08S42B';
update drug set uuidopenmrs='08S41-ec0-c1cf-42e1-9a37-9f23b4f07bba' where atccode_id='08S41';
update drug set uuidopenmrs='08S31-9e9-49f9-4cd6-8268-8df7315e3d09' where atccode_id='08S31';
update drug set uuidopenmrs='08S39Y-3a-20c8-4a16-aaea-f2d4537202e4' where atccode_id='08S39Y';
update drug set uuidopenmrs='08S01ZW-ec-ec31-45aa-a74e-7238872483e8' where atccode_id='08S01ZW';
update drug set uuidopenmrs='08S40Z-fc-6563-49e4-bf81-a456bf79ec88' where atccode_id='08S40Z';
update drug set uuidopenmrs='08S30ZY-ae-3c79-46bd-9970-2d02b8788fdf' where atccode_id='08S30ZY';
CREATE VIEW sync_temp_dispense_vw AS
select sync_temp_dispense.*, sync_temp_patients.clinicuuid clinicuuid
from sync_temp_dispense inner join sync_temp_patients on sync_temp_patients.uuidopenmrs = sync_temp_dispense.uuidopenmrs;
