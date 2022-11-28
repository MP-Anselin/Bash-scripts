DROP INDEX IDX_STD_EPS_1;
DROP INDEX IDX_STD_EPS_2;
CREATE INDEX IDX_STD_EPS_1 ON STD_EPS(ANNEE,PERIODE,RUBRIQUE) LOCAL NOLOGGING COMPRESS 3;
CREATE INDEX IDX_STD_EPS_2 ON STD_EPS(ANNEE,PERIODE,MATRIC,SSPERIODE,RUPED,RUBRIQUE,TYPENR) LOCAL NOLOGGING COMPRESS 5;
