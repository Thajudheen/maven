INSERT INTO TUPLES (GRAPH_ID,SUBJECT,SUBJECT_TYPE,PREDICATE ,OBJECT_URI,OBJECT_VALUE,OBJECT_DATATYPE,STATUS,GA,VERSION)
SELECT
    GRAPH_ID
      ,SUBJECT
      ,SUBJECT_TYPE
      ,'http://www.inmindcomputing.com/application/application-schema.owl#hasBaseCurrency' AS PREDICATE
    , OBJECT_URI
      ,OBJECT_VALUE
      ,OBJECT_DATATYPE
      ,STATUS
      ,GA
      ,VERSION
FROM TUPLES
WHERE PREDICATE = 'http://www.inmindcomputing.com/application/application-schema.owl#hasCurrency'
AND (SUBJECT_TYPE = 'http://www.inmindcomputing.com/application/application-schema.owl#Opportunity' OR SUBJECT_TYPE='http://www.inmindcomputing.com/application/application-schema.owl#Quote');
