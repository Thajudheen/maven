import java.math.BigDecimal

import com.er.platform.PlatformContext
import com.er.platform.PlatformException
import com.er.platform.core.Command
import com.er.platform.vocabulary.Application
import com.google.common.base.Optional
import com.imc.iss.Utils
import com.er.platform.commands.sql.SimpleSqlCommandImpl
import java.sql.Connection;
import com.imc.iss.external.integration.ecc.ProductDataSync
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.SQLException


Logger LOGGER = LoggerFactory.getLogger(ProductDataSync.class);

	  
String sql = "UPDATE objectName SET objectName.OBJECT_VALUE = CONCAT(CONCAT(SUBSTRING (objectName.OBJECT_VALUE, 0,LENGTH(objectName.OBJECT_VALUE)-3),'-'), RIGHT(objectName.OBJECT_VALUE,3)) FROM TUPLES relevantProductTypes JOIN TUPLES objectName ON " + 
"    ( \r\n" + 
"      relevantProductTypes.PREDICATE =  'http://www.inmindcomputing.com/application/application-schema.owl#hasProductType' \r\n" + 
"      AND relevantProductTypes.OBJECT_URI IN (\r\n" + 
"          'http://sales-system-megger-qa.cloud/700/ProductType#ZACM',\r\n" + 
"          'http://sales-system-megger-qa.cloud/700/ProductType#ZDCM',\r\n" + 
"          'http://sales-system-megger-qa.cloud/700/ProductType#ZCBM',\r\n" + 
"          'http://sales-system-megger-qa.cloud/700/ProductType#ZSCM')\r\n" + 
"     AND relevantProductTypes.SUBJECT NOT IN (SELECT SUBJECT FROM TUPLES WHERE PREDICATE = 'http://www.inmindcomputing.com/application/application-schema.owl#productOldMaterialID')\r\n" + 
"      AND relevantProductTypes.SUBJECT = objectName.SUBJECT\r\n" + 
"      AND objectName.PREDICATE = 'http://www.inmindcomputing.com/platform/platform-schema.owl#objectName'\r\n" + 
"	 AND  objectName.OBJECT_VALUE NOT LIKE '%-%'\r\n " +
"  )";

Command<Void> cmd = new SimpleSqlCommandImpl(){
  @Override
  public Void doRun(PlatformContext ctx, Connection conn) throws SQLException, PlatformException {
	conn.createStatement().executeUpdate(sql);
	LOGGER.info("Executed display id")
	return null;
  }
};
commandExecutor.executeUpdate(cmd,queryParamBuilderFactory.get().attribution().build())
