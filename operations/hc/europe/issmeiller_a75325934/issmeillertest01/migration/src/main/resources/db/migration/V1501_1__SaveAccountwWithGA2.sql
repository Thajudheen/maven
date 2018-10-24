UPDATE TUPLES SET GA=-1 WHERE GRAPH_ID IN (SELECT GRAPH_ID FROM TUPLES WHERE SUBJECT_TYPE='http://www.inmindcomputing.com/application/application-schema.owl#Account');
