// Script permettant d''initialiser 
// les tables GTA_JOU_MOTIFEVT, GTA_JOU_SITMOTIFEVT, GTA_JOU_MODSAIEVT 
// à partir des tables STD_TABRAC et STD_TABRAP
// Pour un client GTA-C : les tables GTA_JOU_* sont vides
--------------------------
--GTA_JOU_MOTIFEVT--------
insert into GTA_JOU_MOTIFEVT (
  select
     STD_TABRAC.CODRUB                             as NOM
    ,NVL(STD_FRUBRIQ.LIBTOT,STD_TABRAC.CODRUB)     as LIB
    ,STD_FRUBRIQ.LIBRED                            as LIBRED
    ,'ABS'                                         as TYPE_MOTIF
    ,1                                             as PROPRIETAIRE
    ,0                                             as NEUTRE
    , CURRENT_TIMESTAMP                            as LASTUPDATE_DATE
    , 'migration 08 -> 09'                         as LASTUPDATE_USER
  
  from STD_TABRAC
  left outer join STD_FRUBRIQ on (STD_FRUBRIQ.CODRUB = STD_TABRAC.CODRUB)
);

--------------------------
--GTA_JOU_SITMOTIFEVT-----
insert into GTA_JOU_SITMOTIFEVT (
  select 
      STD_TABRAC.CODRUB                         as MOTIF_EVT
    , TO_DATE('01/01/2008','dd/mm/yyyy')        as DATE_DEB_VALID
    , '*'                                       as CODPOP
    , null                                      as DATE_FIN_VALID
    , 't_Abs'||STD_TABRAC.CODRUB                as RUB_VAL_JOUR
    , 0                                         as CALCUL_PERIOD_IMMEDIAT
    , 0                                         as SAISIE_POINTAGE
    , 0                                         as SAISIE_COLLECTIVE
    , 0                                         as GENERE_TOLERANCE
    , 0                                         as GENERE_ARRONDI
    , STD_TABRAC.CUT                            as ABS_UT
    , 1                                         as ABS_ESTPAIE
    , NVL(STD_TABRAP.NATURE,STD_TABRAC.FAMILL)  as ABS_NAT
    , null                                      as ABS_NAT_GC
    , null                                      as ABS_NAT_IJSS
    , null                                      as ABS_HOSPITALISATION
    , null                                      as ABS_RECHUTE
    , null                                      as ABS_PROLONGATION
    , null                                      as ABS_THCOMPTEURS
    , 0                                         as ABS_SAISIEDESACTIVEE
    , 0                                         as ABS_COURTE
    , decode(STD_TABRAP.CODRUB,  null, 0, 1)    as ABS_AVALIDER_WKF
    , STD_TABRAP.STATUT                         as ABS_STATUT_INIT_WKF
    , decode(STD_TABRAC_COMPLEMENT.DUREE_ALGO,  null, null,'00', 'N', '55', 'S','O')      								as ABS_PRISECPTEPAIE
    , decode(substr(STD_TABRAC_COMPLEMENT.DUREE_ALGO,1,1),  '9', 1,null, null, 0)	  								  	as ABS_PRISECPTEJFERIE
    , decode(STD_TABRAC_COMPLEMENT.DUREE_ALGO,  null, null, '55', '0', substr(STD_TABRAC_COMPLEMENT.DUREE_ALGO,2,1))	as ABS_TABUNITEABS
    , null										as ABS_CALGRP
    , STD_FRUBRIQ.NATRUB                        as CLASSIF_NATURE
    , STD_FRUBRIQ.GRPRUB                        as CLASSIF_GROUPE
    , STD_FRUBRIQ.DOMRUB                        as CLASSIF_DOMAINE
    , STD_TABRAC.FAMILL                         as CLASSIF_FAMILLE
    , CURRENT_TIMESTAMP                         as LASTUPDATE_DATE
    , 'migration 08 -> 09'                      as LASTUPDATE_USER
  from STD_TABRAC
  LEFT OUTER JOIN STD_TABRAP ON (STD_TABRAP.CODRAC = STD_TABRAC.CODRUB)
  LEFT OUTER JOIN STD_TABRAC_COMPLEMENT ON (STD_TABRAC.CODRUB = STD_TABRAC_COMPLEMENT.CODRUB)
  LEFT OUTER JOIN STD_FRUBRIQ ON (STD_FRUBRIQ.CODRUB = STD_TABRAC.CODRUB)
);
-----------------------------
--GTA_JOU_MODSAIMOTIFEVT-----
insert into GTA_JOU_MODSAIMOTIFEVT (
  select
    STD_TABRAC.CODRUB                                       as MOTIF_EVT
    ,TO_DATE('01/01/2008','dd/mm/yyyy')                     as DATE_DEB_VALID
    ,99                                                     as RANG
    ,'*'                                                    as POP_COD
    ,decode(STD_TABRAC.HEURES,  'O', 'H', '') 
    || decode(STD_TABRAC.DEMI_J,  'O'
        , decode(STD_TABRAC.HEURES,  'O', ',', '')||'P', '') as EXP_MODSAISI
    ,decode(STD_TABRAC.HEURES,  'O', 'H', '') 
    || decode(STD_TABRAC.DEMI_J,  'O'
        , decode(STD_TABRAC.HEURES,  'O', ',', '')||'P', '') as RRH_MODSAISI
    ,decode(STD_TABRAC.HEURES,  'O', 'H', '') 
    || decode(STD_TABRAC.DEMI_J,  'O'
        , decode(STD_TABRAC.HEURES,  'O', ',', '')||'P', '') as MAN_MODSAISI
   ,decode(STD_TABRAC.HEURES,  'O', 'H', '') 
    || decode(STD_TABRAC.DEMI_J,  'O'
        , decode(STD_TABRAC.HEURES,  'O', ',', '')||'P', '') as COLLAB_MODSAISI
    , CURRENT_TIMESTAMP                         as LASTUPDATE_DATE
    , 'migration 08 -> 09'                      as LASTUPDATE_USER
  from
    STD_TABRAC
  where
    (STD_TABRAC.HEURES = 'O' or STD_TABRAC.DEMI_J = 'O')
);
-----------------------------