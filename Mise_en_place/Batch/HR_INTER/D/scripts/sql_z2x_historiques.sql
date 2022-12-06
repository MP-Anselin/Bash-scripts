//-----------------------------------------------
// Z2X_BATCH_HDIPLO		            	FOR_DIPLO_AFFECTATION
// Z2X_BATCH_DONPAIE		            DONPAIE
//-----------------------------------------------
// Attention dans for_diplo_affectation la date d obtention du diplôme 
// est une date complete (suivant les modèles cela ne peut être que l année)
//-----------------------------------------------
//Diplômes
delete FOR_DIPLO_AFFECTATION where coddip <> '00';
insert into FOR_DIPLO_AFFECTATION (select matric,to_char(to_date(DTOBTENTION,'dd/mm/yyyy'),'yyyymmdd'),code_diplome,'NR','','O',code_acquis,code_ecole from Z2X_BATCH_HDIPLO where code_diplome is not null);
//Données de paie
insert into DONPAIE (AN,MOIS,MATRIC,HLEG,HTOT,MLEG,MTOT) (select annee,mois,matric,rub_10,rub_10,rub_15,rub_15 from Z2X_BATCH_donpaie where annee is not null and annee <> '0' and mois ='A' and (annee,mois,matric) not in (select an,mois,matric from donpaie)  );
insert into DONPAIE (AN,MOIS,MATRIC,HLEG,HTOT,MLEG,MTOT) (select annee,mois,matric,rub_10,rub_10,rub_15,rub_15 from Z2X_BATCH_donpaie where annee is not null and annee <> '0' and mois <> 'A' and (annee,mois,matric) not in (select an,mois,matric from donpaie) );
