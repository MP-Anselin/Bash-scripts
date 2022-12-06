DELETE FROM STD_FRUBRIQ WHERE TRAIT='N';
DELETE FROM TMP_GAPLIB_01 WHERE NOT TMP='LUT' OR TMP IS NULL;
DELETE GAP_LIBELLES WHERE (CODLANG, CODGROUP1, CODGROUP2) IN (SELECT DISTINCT CODLANG, CODGROUP1, CODGROUP2 FROM TMP_BATCH_GAP_LIBELLES);
INSERT INTO GAP_LIBELLES SELECT CODLANG, CODGROUP1, CODGROUP2, COD1, COD2, MIN(AFF_ORDER), MIN(LIB1), MIN(LIB2), MIN(LIB3), MIN(LIB4) FROM (SELECT DISTINCT * FROM TMP_BATCH_GAP_LIBELLES) GROUP BY CODLANG, CODGROUP1, CODGROUP2, COD1, COD2;
