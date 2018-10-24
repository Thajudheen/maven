/**
 * Copyright (C) 2010-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package db.migration;

import java.sql.Connection;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.er.platform.tools.db.DatabaseInitializer;
import com.er.platform.tools.db.DbInitOptsLocal;
import com.er.platform.tools.owl.CmisResourceLoader;

/**
 * Example of a Java-based migration.
 */
public class V1501_3__RunDBInit implements JdbcMigration {

  private static final Logger LOGGER = LoggerFactory.getLogger(V1501_3__RunDBInit.class);
  public void migrate(final Connection connection) throws Exception {
    LOGGER.info("Running DBInit");
    // TODO specify ops in here

    final DbInitOptsLocal opts = new DbInitOptsLocal();
    opts.setOntology(true);
    opts.setCmisRoot("C:\\Users\\Falk Brauer\\Documents\\devops\\operations\\hc\\europe\\a8799585d\\test");

    final DatabaseInitializer databaseInitializer = new DatabaseInitializer(opts);
    databaseInitializer.uploadOntologyData(connection, new CmisResourceLoader(opts.getCmisRoot()));

  }
}
