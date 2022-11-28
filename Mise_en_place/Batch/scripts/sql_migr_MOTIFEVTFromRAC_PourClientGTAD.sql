// Script permettant de compléter les nouvelles colonnes de paramétrage des motifs d''absence 
// des tables GTA_JOU_MOTIFEVT, GTA_JOU_SITMOTIFEVT, GTA_JOU_MODSAIEVT 
// à partir des tables STD_TABRAC et STD_TABRAP
// Pour un client GTA-D démarré avant 4.09 : les tables GTA_JOU_* ont déjà des lignes, 
// ce script conserve le paramétrage MJ ayant pu être fait avant passage en 4.09 
--------------------------
--GTA_JOU_MOTIFEVT--------
create table GTA_JOU_MOTIFEVT_COPY as (select * from GTA_JOU_MOTIFEVT where TYPE_MOTIF='ABS');
delete GTA_JOU_MOTIFEVT where TYPE_MOTIF = 'ABS';
insert into GTA_JOU_MOTIFEVT (
  select
     o.NOM                  as NOM
    ,o.LIB                  as LIB
    ,STD_FRUBRIQ.LIBRED     as LIBRED
    ,o.TYPE_MOTIF           as TYPE_MOTIF
    ,o.PROPRIETAIRE         as PROPRIETAIRE
    ,o.NEUTRE               as NEUTRE
    , CURRENT_TIMESTAMP     as LASTUPDATE_DATE
    , 'migration 08 -> 09'  as LASTUPDATE_USER
  from
    gta_jou_motifevt_copy o
  LEFT OUTER JOIN STD_FRUBRIQ ON (o.TYPE_MOTIF = 'ABS' and STD_FRUBRIQ.CODRUB = o.NOM)
  where o.TYPE_MOTIF = 'ABS'
);
drop table GTA_JOU_MOTIFEVT_COPY;
--------------------------
--GTA_JOU_SITMOTIFEVT-----
create table GTA_JOU_SITMOTIFEVT_COPY as (select GTA_JOU_SITMOTIFEVT.* from GTA_JOU_SITMOTIFEVT, GTA_JOU_MOTIFEVT where GTA_JOU_MOTIFEVT.NOM = GTA_JOU_SITMOTIFEVT.MOTIF_EVT and GTA_JOU_MOTIFEVT.TYPE_MOTIF='ABS');
delete GTA_JOU_SITMOTIFEVT where MOTIF_EVT in (select NOM from GTA_JOU_MOTIFEVT where TYPE_MOTIF='ABS');
insert into GTA_JOU_SITMOTIFEVT (
  select 
     copy.MOTIF_EVT								  as MOTIF_EVT			
    ,copy.DATE_DEB_VALID            			  as DATE_DEB_VALID
    ,'*'                                          as CODPOP
    ,copy.DATE_FIN_VALID                          as DATE_FIN_VALID
    ,copy.RUB_VAL_JOUR                            as RUB_VAL_JOUR
    ,copy.CALCUL_PERIOD_IMMEDIAT                  as CALCUL_PERIOD_IMMEDIAT
    ,copy.SAISIE_POINTAGE                         as SAISIE_POINTAGE
    ,copy.SAISIE_COLLECTIVE          			  as SAISIE_COLLECTIVE
    ,copy.GENERE_TOLERANCE           			  as GENERE_TOLERANCE
    ,copy.GENERE_ARRONDI             			  as GENERE_ARRONDI
    , STD_TABRAC.CUT                              as ABS_UT
    , 1                                           as ABS_ESTPAIE
    , NVL(STD_TABRAP.NATURE,STD_TABRAC.FAMILL)    as ABS_NAT
    , null                                        as ABS_NAT_GC
    , null                                        as ABS_NAT_IJSS
    , null                                        as ABS_HOSPITALISATION
    , null                                        as ABS_RECHUTE
    , null                                        as ABS_PROLONGATION
    , null                                        as ABS_THCOMPTEURS
    , 0                                           as ABS_SAISIEDESACTIVEE
    , 0                                           as ABS_COURTE
    , decode(STD_TABRAP.CODRUB,  null, 0, 1)      as ABS_AVALIDER_WKF
    , STD_TABRAP.STATUT                           as ABS_STATUT_INIT_WKF
    , decode(STD_TABRAC_COMPLEMENT.DUREE_ALGO,  null, null,'00', 'N', '55', 'S','O')      								as ABS_PRISECPTEPAIE
    , decode(substr(STD_TABRAC_COMPLEMENT.DUREE_ALGO,1,1),  '9', 1,null, null, 0)	  								  	as ABS_PRISECPTEJFERIE
    , decode(STD_TABRAC_COMPLEMENT.DUREE_ALGO,  null, null, '55', '0', substr(STD_TABRAC_COMPLEMENT.DUREE_ALGO,2,1))	as ABS_TABUNITEABS
    , null										  as ABS_CALGRP
    , STD_FRUBRIQ.NATRUB                          as CLASSIF_NATURE
    , STD_FRUBRIQ.GRPRUB                          as CLASSIF_GROUPE
    , STD_FRUBRIQ.DOMRUB                          as CLASSIF_DOMAINE
    , STD_TABRAC.FAMILL                           as CLASSIF_FAMILLE
    , CURRENT_TIMESTAMP                           as LASTUPDATE_DATE
    , 'migration 08 -> 09'                        as LASTUPDATE_USER
  FROM GTA_JOU_SITMOTIFEVT_COPY copy 
  INNER JOIN GTA_JOU_MOTIFEVT ON (GTA_JOU_MOTIFEVT.NOM = copy.MOTIF_EVT AND GTA_JOU_MOTIFEVT.TYPE_MOTIF = 'ABS')
  LEFT OUTER JOIN STD_TABRAC ON (copy.MOTIF_EVT = STD_TABRAC.CODRUB)
  LEFT OUTER JOIN STD_TABRAC_COMPLEMENT ON (copy.MOTIF_EVT = STD_TABRAC_COMPLEMENT.CODRUB)
  LEFT OUTER JOIN STD_TABRAP ON (STD_TABRAP.CODRAC = STD_TABRAC.CODRUB)
  LEFT OUTER JOIN STD_FRUBRIQ ON (STD_FRUBRIQ.CODRUB = STD_TABRAC.CODRUB) 
);
drop table GTA_JOU_SITMOTIFEVT_COPY;