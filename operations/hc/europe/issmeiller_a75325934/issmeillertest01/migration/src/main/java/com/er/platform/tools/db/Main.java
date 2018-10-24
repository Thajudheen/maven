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
package com.er.platform.tools.db;

import java.util.Properties;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;

import com.beust.jcommander.JCommander;

/**
 * Simplest possible sample to demonstrate the usage of Flyway.
 */
public class Main {

  /**
   * Runs the sample.
   *
   * @param args
   *          None supported.
   */
  public static void main(final String[] args) throws Exception {
    opts = new DbInitOptsLocal();
    new JCommander(opts, args);

    final Properties properties = new Properties();
    properties.put(DbAccess.DBCP_URL, opts.url);
    properties.put(DbAccess.DBCP_USERNAME, opts.user);
    properties.put(DbAccess.DBCP_PASSWORD, opts.password);

    final DataSource dataSource = DbAccess.createDataSource(opts);

    final Flyway flyway = new Flyway();
    flyway.setDataSource(dataSource);
    flyway.setBaselineOnMigrate(true);
    flyway.setLocations(LOCATIONS);
    flyway.migrate();
  }

  public static DbInitOptsLocal opts;
  public static final String LOCATIONS = "db.migration";
}
