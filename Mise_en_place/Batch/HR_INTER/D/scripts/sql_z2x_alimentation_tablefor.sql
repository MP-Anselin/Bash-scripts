//-----------------------------------------------
// Z2X_BATCH_DIPLO     FOR_DIPLO       
// Z2X_BATCH_DIPNI     FOR_DIPLO_NIVETU
// Z2X_BATCH_ECOLE     FOR_DIPLO_ECOLE 
// Z2X_BATCH_FILIE     FOR_DIPLO_MODACQ
//-----------------------------------------------
//
insert into FOR_DIPLO 						SELECT DISTINCT CODE as CODDIP,'','','',LIBELLE as LIBDIP 				from Z2X_BATCH_DIPLO 			where Z2X_BATCH_DIPLO.CODE 			not in (select CODDIP 		from FOR_DIPLO) ;
insert into FOR_DIPLO_NIVETU 			SELECT DISTINCT CODE as CODNIVETU,LIBELLE as LIBNIVETU 						from Z2X_BATCH_DIPNI 			where Z2X_BATCH_DIPNI.CODE 			not in (select CODNIVETU 	from FOR_DIPLO_NIVETU) ;
insert into FOR_DIPLO_ECOLE 			SELECT DISTINCT CODE as CODECOLE,LIBELLE as LIBECOLE 							from Z2X_BATCH_ECOLE 			where Z2X_BATCH_ECOLE.CODE 			not in (select CODECOLE 	from FOR_DIPLO_ECOLE) ;
insert into FOR_DIPLO_MODACQ 			SELECT DISTINCT CODE as CODMODACQ,LIBELLE as LIBMODACQ 						from Z2X_BATCH_FILIE 			where Z2X_BATCH_FILIE.CODE 			not in (select CODMODACQ 	from FOR_DIPLO_MODACQ) ;
update FOR_DIPLO set LIBDIP = (select LIBELLE FROM Z2X_BATCH_DIPLO where Z2X_BATCH_DIPLO.CODE = FOR_DIPLO.CODDIP) where FOR_DIPLO.CODDIP  in (select Z2X_BATCH_DIPLO.CODE from Z2X_BATCH_DIPLO);
update FOR_DIPLO_NIVETU set LIBNIVETU = (select LIBELLE FROM Z2X_BATCH_DIPNI where Z2X_BATCH_DIPNI.CODE = FOR_DIPLO_NIVETU.CODNIVETU) where FOR_DIPLO_NIVETU.CODNIVETU  in (select Z2X_BATCH_DIPNI.CODE from Z2X_BATCH_DIPNI);
update FOR_DIPLO_ECOLE set LIBECOLE = (select LIBELLE FROM Z2X_BATCH_ECOLE where Z2X_BATCH_ECOLE.CODE = FOR_DIPLO_ECOLE.CODECOLE) where FOR_DIPLO_ECOLE.CODECOLE  in (select Z2X_BATCH_ECOLE.CODE from Z2X_BATCH_ECOLE);
update FOR_DIPLO_MODACQ	set LIBMODACQ = (select LIBELLE FROM Z2X_BATCH_FILIE where Z2X_BATCH_FILIE.CODE = FOR_DIPLO_MODACQ.CODMODACQ) where FOR_DIPLO_MODACQ.CODMODACQ in (select Z2X_BATCH_FILIE.CODE from Z2X_BATCH_FILIE);
//-----------------------------------------------
// Gestion du referentiel emploi = tables de libelles avec concaténation du code ancienne gestion...
// Z2X_BATCH_REFMETIER1        		 EMP_FAMMETIER  
// Z2X_BATCH_REFMETIER2 			 EMP_METIER 
// Z2X_BATCH_REFMETIER3 			 EMP_EMPLOIT
// Z2X_BATCH_REFMETIER4				 Emploi loc pas gere dans RHI actuellement variable applicative AVECEMPLOILOC dans hrTalent
//-----------------------------------------------
//
//insert into EMP_FAMMETIER  SELECT DISTINCT CODE ,LIBELLE  from Z2X_BATCH_REFMETIER1   where Z2X_BATCH_REFMETIER1.CODE       not in (select CODFMET  from EMP_FAMMETIER) ;
//update EMP_FAMMETIER set libfmet = (select libelle from Z2X_BATCH_refmetier1 where Z2X_BATCH_REFMETIER1.CODE = CODFMET  ) ;
//insert into EMP_METIER         SELECT DISTINCT   CODE,SUBSTR(CODE,0,1) ,LIBELLE from Z2X_BATCH_REFMETIER2      where   Z2X_BATCH_REFMETIER2.CODE    not in (select CODFMET  from EMP_METIER) ;
//update EMP_METIER   set libmetier = (select libelle from Z2X_BATCH_refmetier2 where Z2X_BATCH_REFMETIER2.CODE  = CODFMET  ) ;
//insert into EMP_EMPLOI SELECT DISTINCT SUBSTR(CODE,0,1) , SUBSTR(CODE,0,1)||SUBSTR(CODE,3,2) , CODE ,LIBELLE  from Z2X_BATCH_REFMETIER4     where (Z2X_BATCH_REFMETIER4.CODE )      not in (select CODEMPLOI from EMP_EMPLOI) ;
//update EMP_EMPLOI set libelle = (select libelle from Z2X_BATCH_refmetier4 where CODE=CODEMPLOI;
//Gestion des métiers transverses;
//insert into EMP_METIER      SELECT DISTINCT SUBSTR(CODE,0,1)||SUBSTR(CODE,3,2),SUBSTR(CODE,0,1)  ,''  from Z2X_BATCH_REFMETIER3       where ( SUBSTR(Z2X_BATCH_REFMETIER3.CODE,0,1)||SUBSTR(Z2X_BATCH_REFMETIER3.CODE,3,2), SUBSTR(Z2X_BATCH_REFMETIER3.CODE,0,1)  )      not in (select CODFMET,CODMETIER from EMP_METIER) ;
//insert into EMP_FAMMETIER  (select distinct CODMETIER,'' FROM EMP_METIER  WHERE  EMP_METIER.CODMETIER 			not in (select CODFMET 	from EMP_FAMMETIER)  );
//Mise à jour des libellé des méties transverses;
//update EMP_METIER HE set libmetier = (select libmetier from EMP_METIER ME where  SUBSTR(HE.CODFMET,2,2) = ME.CODFMET ) where HE.LIBMETIER is null  ;
//update EMP_FAMMETIER HE set libfmet = 'Famille transverse' where libfmet is null ;
//-----------------------------------------------
// Gestion du referentiel emploi = pas de concatenation de code mais tables de libelles code/libellé
// Z2X_BATCH_REFMETIER1        		 EMP_FAMMETIER  
// Z2X_BATCH_REFMETIER2 			 EMP_METIER 
// Z2X_BATCH_REFMETIER3 			 EMP_EMPLOI
// Z2X_BATCH_REFMETIER4				 Emploi loc pas gere dans RHI actuellement variable applicative AVECEMPLOILOC dans hrTalent
//-----------------------------------------------
//
insert into EMP_FAMMETIER  select distinct refmetier1 ,'' from z2x_batch_hisemplo where refmetier1 is not null and refmetier2 is not null and refmetier3 is not null and NVL(refmetier4,'*') is not null and refmetier1 not in (select CODFMET from EMP_FAMMETIER);
insert into EMP_METIER   select distinct refmetier1,refmetier2,''  from z2x_batch_hisemplo where refmetier1 is not null and refmetier2 is not null and refmetier3 is not null and NVL(refmetier4,'*') is not null and (refmetier1,refmetier2) not in (select codfmet,codmetier from emp_metier);
insert into EMP_EMPLOI   select distinct refmetier1,refmetier2,refmetier3,''  from z2x_batch_hisemplo where refmetier1 is not null and refmetier2 is not null and refmetier3 is not null and NVL(refmetier4,'*') is not null and (refmetier1,refmetier2,refmetier3) not in (select codfmet,codmetier,codemploi from EMP_EMPLOI);
update EMP_FAMMETIER set LIBFMET = NVL((select NVL(libelle,CODE) from Z2X_BATCH_REFMETIER1 where CODE = CODFMET),CODFMET) ;
update EMP_METIER set LIBMETIER = NVL((select libelle from Z2X_BATCH_REFMETIER2 where CODE = CODMETIER),CODMETIER) ;
update EMP_EMPLOI set LIBEMPLOI = NVL((select NVL(libelle,CODE) from Z2X_BATCH_REFMETIER3 where CODE=CODEMPLOI),CODEMPLOI) ;
