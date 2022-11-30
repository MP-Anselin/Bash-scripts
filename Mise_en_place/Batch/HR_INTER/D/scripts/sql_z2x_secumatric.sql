//-----------------------------------------------
// Z2X_BATCH_DOSSIER            SECUMATRIC
//-----------------------------------------------
//
delete from SECUMATRIC;
insert into secumatric select * from ETAT_CIV;
