//Script a passer pour parametrer la session oracle afin que les instructions to_date ne ramenent pas une erreur ORA-01843: not a valid month.
ALTER SESSION SET NLS_DATE_FORMAT='DD/MM/RR';
