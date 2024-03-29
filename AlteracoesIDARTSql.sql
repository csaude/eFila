
-- Alter database structure for iDARt v3.7.0
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'jan','Jan') where substr(dateexpectedstring,4,3)='jan';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'fev','Feb') where substr(dateexpectedstring,4,3)='fev';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'mar','Mar') where substr(dateexpectedstring,4,3)='mar';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'abr','Apr') where substr(dateexpectedstring,4,3)='abr';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'mai','May') where substr(dateexpectedstring,4,3)='mai';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'jun','Jun') where substr(dateexpectedstring,4,3)='jun';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'jul','Jul') where substr(dateexpectedstring,4,3)='jul';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'ago','Aug') where substr(dateexpectedstring,4,3)='ago';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'set','Sep') where substr(dateexpectedstring,4,3)='set';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'out','Oct') where substr(dateexpectedstring,4,3)='out';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'nov','Nov') where substr(dateexpectedstring,4,3)='nov';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'dez','Dec') where substr(dateexpectedstring,4,3)='dez';

update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'Jan','Jan') where substr(dateexpectedstring,4,3)='Jan';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'Fev','Feb') where substr(dateexpectedstring,4,3)='Fev';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'Mar','Mar') where substr(dateexpectedstring,4,3)='Mar';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'Abr','Apr') where substr(dateexpectedstring,4,3)='Abr';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'Mai','May') where substr(dateexpectedstring,4,3)='Mai';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'Jun','Jun') where substr(dateexpectedstring,4,3)='Jun';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'Jul','Jul') where substr(dateexpectedstring,4,3)='Jul';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'Ago','Aug') where substr(dateexpectedstring,4,3)='Ago';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'Set','Sep') where substr(dateexpectedstring,4,3)='Set';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'Out','Oct') where substr(dateexpectedstring,4,3)='Out';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'Nov','Nov') where substr(dateexpectedstring,4,3)='Nov';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'Dez','Dec') where substr(dateexpectedstring,4,3)='Dez';

update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M01','Jan') where substr(dateexpectedstring,4,3)='M01';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M02','Feb') where substr(dateexpectedstring,4,3)='M02';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M03','Mar') where substr(dateexpectedstring,4,3)='M03';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M04','Apr') where substr(dateexpectedstring,4,3)='M04';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M05','May') where substr(dateexpectedstring,4,3)='M05';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M06','Jun') where substr(dateexpectedstring,4,3)='M06';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M07','Jul') where substr(dateexpectedstring,4,3)='M07';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M08','Aug') where substr(dateexpectedstring,4,3)='M08';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M09','Sep') where substr(dateexpectedstring,4,3)='M09';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M10','Oct') where substr(dateexpectedstring,4,3)='M10';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M11','Nov') where substr(dateexpectedstring,4,3)='M11';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M12','Dec') where substr(dateexpectedstring,4,3)='M12';


update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M01','Jan') where substr(dateexpectedstring,4,3)='M01';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M02','Feb') where substr(dateexpectedstring,4,3)='M02';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M03','Mar') where substr(dateexpectedstring,4,3)='M03';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M04','Apr') where substr(dateexpectedstring,4,3)='M04';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M05','May') where substr(dateexpectedstring,4,3)='M05';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M06','Jun') where substr(dateexpectedstring,4,3)='M06';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M07','Jul') where substr(dateexpectedstring,4,3)='M07';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M08','Aug') where substr(dateexpectedstring,4,3)='M08';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M09','Sep') where substr(dateexpectedstring,4,3)='M09';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M10','Oct') where substr(dateexpectedstring,4,3)='M10';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M11','Nov') where substr(dateexpectedstring,4,3)='M11';
update packagedruginfotmp set dateexpectedstring=replace(dateexpectedstring,'M12','Dec') where substr(dateexpectedstring,4,3)='M12';


update sync_temp_dispense set dateexpectedstring=replace(dateexpectedstring,'M01','Jan') where substr(dateexpectedstring,4,3)='M01';
update sync_temp_dispense set dateexpectedstring=replace(dateexpectedstring,'M02','Feb') where substr(dateexpectedstring,4,3)='M02';
update sync_temp_dispense set dateexpectedstring=replace(dateexpectedstring,'M03','Mar') where substr(dateexpectedstring,4,3)='M03';
update sync_temp_dispense set dateexpectedstring=replace(dateexpectedstring,'M04','Apr') where substr(dateexpectedstring,4,3)='M04';
update sync_temp_dispense set dateexpectedstring=replace(dateexpectedstring,'M05','May') where substr(dateexpectedstring,4,3)='M05';
update sync_temp_dispense set dateexpectedstring=replace(dateexpectedstring,'M06','Jun') where substr(dateexpectedstring,4,3)='M06';
update sync_temp_dispense set dateexpectedstring=replace(dateexpectedstring,'M07','Jul') where substr(dateexpectedstring,4,3)='M07';
update sync_temp_dispense set dateexpectedstring=replace(dateexpectedstring,'M08','Aug') where substr(dateexpectedstring,4,3)='M08';
update sync_temp_dispense set dateexpectedstring=replace(dateexpectedstring,'M09','Sep') where substr(dateexpectedstring,4,3)='M09';
update sync_temp_dispense set dateexpectedstring=replace(dateexpectedstring,'M10','Oct') where substr(dateexpectedstring,4,3)='M10';
update sync_temp_dispense set dateexpectedstring=replace(dateexpectedstring,'M11','Nov') where substr(dateexpectedstring,4,3)='M11';
update sync_temp_dispense set dateexpectedstring=replace(dateexpectedstring,'M12','Dec') where substr(dateexpectedstring,4,3)='M12';


ALTER TABLE drug ADD COLUMN IF NOT EXISTS report_order integer NOT NULL DEFAULT 300;
ALTER TABLE regimeterapeutico ADD COLUMN IF NOT EXISTS report_order integer NOT NULL DEFAULT 300;

UPDATE regimeterapeutico SET report_order = 1 where codigoregime like '1aLTLD%';
UPDATE regimeterapeutico SET report_order = 2 where codigoregime like 'A4A%';
UPDATE regimeterapeutico SET report_order = 3 where codigoregime like '1alt1%';
UPDATE regimeterapeutico SET report_order = 4 where codigoregime like '1alt2%';
UPDATE regimeterapeutico SET report_order = 5 where codigoregime like '2alt3%';
UPDATE regimeterapeutico SET report_order = 6 where codigoregime like '2alt1%';
UPDATE regimeterapeutico SET report_order = 7 where codigoregime like '2alt2%';
UPDATE regimeterapeutico SET report_order = 8 where codigoregime like 'A2F%';
UPDATE regimeterapeutico SET report_order = 9 where codigoregime like 'C1A%';
UPDATE regimeterapeutico SET report_order = 10 where codigoregime like 'ABC12%';
UPDATE regimeterapeutico SET report_order = 11 where codigoregime like 'TDF+3TC PrEP%';
UPDATE regimeterapeutico SET report_order = 12 where codigoregime like 'X6APed%';
UPDATE regimeterapeutico SET report_order = 13 where codigoregime like 'X7APed%';
UPDATE regimeterapeutico SET report_order = 14 where codigoregime like 'ABCPedGranulos%';
UPDATE regimeterapeutico SET report_order = 15 where codigoregime like 'ABCPedCpts%';
UPDATE regimeterapeutico SET report_order = 16 where codigoregime like 'ABCPedXarope%';
UPDATE regimeterapeutico SET report_order = 17 where codigoregime like 'ABCPedNew%';
UPDATE regimeterapeutico SET report_order = 18 where codigoregime like 'A2Fped Cpts%';
UPDATE regimeterapeutico SET report_order = 19 where codigoregime like 'A2Fped Granulos%';
UPDATE regimeterapeutico SET report_order = 20 where codigoregime like 'A2Fped Xarope%';
UPDATE regimeterapeutico SET report_order = 21 where codigoregime like 'A2Fped Cpts%';
UPDATE regimeterapeutico SET report_order = 22 where codigoregime like 'X5APed%';
UPDATE regimeterapeutico SET report_order = 23 where codigoregime like 'AX3N1%';
UPDATE regimeterapeutico SET report_order = 24 where codigoregime like 'X3N1%';
UPDATE regimeterapeutico SET report_order = 25 where codigoregime like 'X5A%';
UPDATE regimeterapeutico SET report_order = 26 where codigoregime like '2Op4%';

update drug set report_order = 1 where atccode_id like '%08S18WI';
update drug set report_order = 2 where atccode_id like '%08S18W';
update drug set report_order = 3 where atccode_id like '%08S18WII';
update drug set report_order = 4 where atccode_id like '%08S18XI';
update drug set report_order = 5 where atccode_id like '%08S18X';
update drug set report_order = 6 where atccode_id like '%08S18XII';
update drug set report_order = 7 where atccode_id like '%08S40';
update drug set report_order = 8 where atccode_id like '%08S18Z';
update drug set report_order = 9 where atccode_id like '%08S01ZY';
update drug set report_order = 10 where atccode_id like '%08S01ZZ';
update drug set report_order = 11 where atccode_id like '%08S30WZ';
update drug set report_order = 12 where atccode_id like '%08S30ZY';
update drug set report_order = 13 where atccode_id like '%08S39Z';
update drug set report_order = 15 where atccode_id like '%08S30ZX';
update drug set report_order = 17 where atccode_id like '%08S30ZXi';
update drug set report_order = 18 where atccode_id like '%08S38Y';
update drug set report_order = 19 where atccode_id like '%08S39B';
update drug set report_order = 20 where atccode_id like '%08S01ZW';
update drug set report_order = 21 where atccode_id like '%08S01ZWi';
update drug set report_order = 22 where atccode_id like '%08S40Z';
update drug set report_order = 23 where atccode_id like '%08S23';
update drug set report_order = 24 where atccode_id like '%08S23Z';
update drug set report_order = 25 where atccode_id like '%08S17Y';
update drug set report_order = 26 where atccode_id like '%08S15';
update drug set report_order = 27 where atccode_id like '%08S01';
update drug set report_order = 28 where atccode_id like '%08S01Z';
update drug set report_order = 29 where atccode_id like '%08S21';
update drug set report_order = 30 where atccode_id like '%08S20';
update drug set report_order = 31 where atccode_id like '%08S13';
update drug set report_order = 32 where atccode_id like '%08S18';
update drug set report_order = 33 where atccode_id like '%08S18Y';
update drug set report_order = 34 where atccode_id like '%08S42';
update drug set report_order = 35 where atccode_id like '%08S42B';
update drug set report_order = 36 where atccode_id like '%08S41';
update drug set report_order = 37 where atccode_id like '%08S31';
update drug set report_order = 38 where atccode_id like '%08S39Y';
update drug set report_order = 39 where atccode_id like '%08S38Z';
update drug set report_order = 40 where atccode_id like '%08S30ZZ';
update drug set report_order = 41 where atccode_id like '%08S39';
update drug set report_order = 42 where atccode_id like '%08S22';

alter table sync_temp_dispense add column username text default '' ;
alter table sync_temp_dispense add column clinicuuid text default '';

