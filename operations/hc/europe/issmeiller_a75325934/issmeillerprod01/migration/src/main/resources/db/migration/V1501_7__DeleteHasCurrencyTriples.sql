DELETE FROM TUPLES
WHERE PREDICATE = 'http://www.inmindcomputing.com/application/application-schema.owl#hasCurrency'
AND (SUBJECT_TYPE = 'http://www.inmindcomputing.com/application/application-schema.owl#Opportunity' OR SUBJECT_TYPE='http://www.inmindcomputing.com/application/application-schema.owl#Quote');
