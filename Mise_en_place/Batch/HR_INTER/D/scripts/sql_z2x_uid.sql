//-----------------------------------------------
// Z2X_BATCH_UID				            ETAT_CIV
// Z2X_BATCH_UID		            		SECUMATRIC
// Z2X_BATCH_MEMBERS				        ETAT_CIV
// Z2X_BATCH_LEADERS		            SECUMATRIC
//-----------------------------------------------
//Mise à jour des code uid pour les extration avec les tables members et leaders
//update secumatric set et_code_uid= (select distinct upper(code_uid) from Z2X_BATCH_members where matricule = matric )  where matric in (select matricule from Z2X_BATCH_members) ;
//update secumatric set et_code_uid= (select distinct upper(code_uid_pere) from Z2X_BATCH_members where matricule_pere = matric )  where matric in (select matricule_pere from Z2X_BATCH_members) ;
//update secumatric set et_code_uid= (select distinct upper(code_uid) from Z2X_BATCH_leaders where matricule = matric ) where matric in (select matricule from Z2X_BATCH_leaders) ;
//update etat_civ set et_code_uid= (select distinct upper(code_uid) from Z2X_BATCH_members where matricule = matric )  where matric in (select matricule from Z2X_BATCH_members) ;
//update etat_civ set et_code_uid= (select distinct upper(code_uid_pere) from Z2X_BATCH_members where matricule_pere = matric )  where matric in (select matricule_pere from Z2X_BATCH_members) ;
//update etat_civ set et_code_uid= (select distinct upper(code_uid) from Z2X_BATCH_leaders where matricule = matric ) where matric in (select matricule from Z2X_BATCH_leaders) ;
//Mise à jour à parti des tables uid 
update secumatric set et_code_uid = '' ;
update etat_civ set et_code_uid = '' ;
update secumatric set et_code_uid = (select upper(code_uid) from Z2X_BATCH_UID where Z2X_BATCH_UID.MATRICULE = MATRIC );
update etat_civ  set et_code_uid = (select upper(code_uid) from Z2X_BATCH_UID where Z2X_BATCH_UID.MATRICULE = MATRIC );
//
update secumatric set et_code_uid = null where et_code_uid= '(n)' ;
update etat_civ set et_code_uid = null where et_code_uid= '(n)' ;
