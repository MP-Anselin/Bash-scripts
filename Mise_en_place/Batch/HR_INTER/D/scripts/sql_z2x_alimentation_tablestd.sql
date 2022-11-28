//-----------------------------------------------
// Z2X_BATCH_CONTRAT_TYPE       STD_TYPCONTRAT
// Z2X_BATCH_CONTRAT_NATURE     STD_CONTRAT
// Z2X_BATCH_COEF               STD_CLASSIFICATION
// Z2X_BATCH_CATPRO             CATPRO
// Z2X_BATCH_CIVILITE           CIVI
// Z2X_BATCH_PAYS               PAYS
//
// Z2X_BATCH_ETAB				STD_ADRESSE
// Z2X_BATCH_SOCIETE			STD_ADRESSE
//-----------------------------------------------
//
//Suppression du contenu des tables (mettre en commentaires pour ne pas effacer le contenu des tables finales mais juste avoir une mise à jour :
//Uniquement ajout des nouvelles valeurs (pas de suppression de celles qui ne servent plus)et mise à jour des libellés)

delete STD_TYPCONTRAT ;
delete STD_CONTRAT ;
delete STD_CLASSIFICATION ;
// la table catpro n est pas effacee par défaut car ça a une signification de garder les anciennes valeurs de catégorie delete CATPRO ;
delete CIVI ;
delete PAYS ;

insert into STD_TYPCONTRAT        SELECT DISTINCT CODE ,LIBELLE ,999                                from Z2X_BATCH_CONTRAT_TYPE     where Z2X_BATCH_CONTRAT_TYPE.CODE   not in (select CODE       from STD_TYPCONTRAT) ;
insert into STD_CONTRAT           SELECT DISTINCT CODE AS CODCONT ,'fr_fr',LIBELLE as LIBCONT,9999  from Z2X_BATCH_CONTRAT_NATURE   where Z2X_BATCH_CONTRAT_NATURE.CODE   not in (select CODCONT    from STD_CONTRAT) ;
insert into STD_CLASSIFICATION    SELECT DISTINCT CODE ,LIBELLE ,999                                from Z2X_BATCH_COEF             where Z2X_BATCH_COEF.CODE       not in (select CODE       from STD_CLASSIFICATION) ;
insert into CATPRO                SELECT DISTINCT CODE ,LIBELLE,0,0,''                              from Z2X_BATCH_CATPRO           where Z2X_BATCH_CATPRO.CODE     not in (select COD_CAT    from CATPRO) ;
insert into CIVI                  SELECT DISTINCT CODE ,LIBELLE                                     from Z2X_BATCH_CIVILITE         where Z2X_BATCH_CIVILITE.CODE   not in (select LIB        from CIVI) ;
insert into PAYS                  SELECT DISTINCT CODE AS CODP ,LIBELLE as NOMP                     from Z2X_BATCH_PAYS             where Z2X_BATCH_PAYS.CODE       not in (select CODP       from PAYS) ;
update CATPRO                 set LIB_CAT   = (select LIBELLE FROM Z2X_BATCH_CATPRO           where Z2X_BATCH_CATPRO.CODE     = cod_cat);
update CIVI                   set LIBL      = (select LIBELLE FROM Z2X_BATCH_CIVILITE         where Z2X_BATCH_CIVILITE.CODE   = LIB);
update STD_CLASSIFICATION     set LIBELLE   = (select LIBELLE FROM Z2X_BATCH_COEF             where Z2X_BATCH_COEF.CODE       = STD_CLASSIFICATION.CODE);
update STD_CONTRAT            set LIBCONT   = (select LIBELLE FROM Z2X_BATCH_CONTRAT_NATURE   where Z2X_BATCH_CONTRAT_NATURE.CODE   = STD_CONTRAT.CODCONT);
update PAYS                   set NOMP      = (select LIBELLE FROM Z2X_BATCH_PAYS             where Z2X_BATCH_PAYS.CODE       = PAYS.CODP);
update STD_TYPCONTRAT         set LIBELLE   = (select LIBELLE FROM Z2X_BATCH_CONTRAT_TYPE     where Z2X_BATCH_CONTRAT_TYPE.CODE = STD_TYPCONTRAT.CODE);

//alimentation de adresse de établissment et société avec un id correspondant
//delete std_adresse
insert into std_adresse(id_adresse,nomvoi,numvoi,cinsee,SIGLE,BISTER,BOURG,COPOST,BUDIST,NOTELE) (select Z2X_BATCH.nextval,NOMVOIE,NUMVOIE,INSEE,NOM,BTQ,NVL(COMMUNE,BURDIST),CODEPOSTAL,BURDIST,NUMTEL from z2x_batch_etab where (nvl(NOMVOIE,'*'),nvl(NUMVOIE,'*'),nvl(INSEE,'*')) not in (select nvl(nomvoi,'*'),nvl(numvoi,'*'),nvl(cinsee,'*') from std_adresse ) )
insert into std_adresse(id_adresse,nomvoi,numvoi,cinsee,SIGLE,BISTER,BOURG,COPOST,BUDIST,NOTELE) (select Z2X_BATCH.nextval,NOMVOIE,NUMVOIE,INSEE,RAISSOC,BTQ,NVL(COMMUNE,BURDIST),CODEPOSTAL,BURDIST,NUMTEL from Z2X_BATCH_SOCIETE where (nvl(NOMVOIE,'*'),nvl(NUMVOIE,'*'),nvl(INSEE,'*')) not in (select nvl(nomvoi,'*'),nvl(numvoi,'*'),nvl(cinsee,'*') from std_adresse ) )
