OPTIONS (DIRECT=TRUE,PARALLEL=TRUE,SKIP_INDEX_MAINTENANCE=TRUE,ERRORS=500)
UNRECOVERABLE
LOAD DATA
APPEND
INTO TABLE STD_EPS
(
ANNEE POSITION(1:4) INTEGER EXTERNAL
,PERIODE POSITION(5:6) INTEGER EXTERNAL
,MATRIC POSITION(7:13) CHAR
,SSPERIODE POSITION(14:16) CHAR NULLIF SSPERIODE=BLANKS
,RUPED POSITION(17:20) INTEGER EXTERNAL
,RUBRIQUE POSITION(21:26) CHAR TERMINATED BY WHITESPACE
,TYPENR POSITION(27:27) CHAR NULLIF TYPENR=BLANKS
,NUMERO_SEQUENCE POSITION(28:32) INTEGER EXTERNAL
,SENS_RUBRIQUE POSITION(33:33) CHAR NULLIF SENS_RUBRIQUE=BLANKS
,NOMBRE POSITION(34:46) DECIMAL EXTERNAL
,TAUX POSITION(47:57) DECIMAL EXTERNAL
,MONTANT POSITION(58:70) DECIMAL EXTERNAL
,PRESENCE_LIBELLE POSITION(71:71) CHAR NULLIF PRESENCE_LIBELLE=BLANKS
,PRESENCE_IMPUTATION POSITION(72:72) CHAR NULLIF PRESENCE_IMPUTATION=BLANKS
,NUMERO_ORDRE POSITION(73:73) CHAR NULLIF NUMERO_ORDRE=BLANKS
,DATE_EPV POSITION(74:77) CHAR NULLIF DATE_EPV=BLANKS
,NUMERO_EPV POSITION(78:78) CHAR NULLIF NUMERO_EPV=BLANKS
)