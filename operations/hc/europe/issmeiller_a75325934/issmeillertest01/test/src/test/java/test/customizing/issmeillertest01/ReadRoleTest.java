/*
 * Copyright (c) 2015, In Mind Computing AG. All rights reserved.
 * IMC PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package test.customizing.issmeillertest01;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.er.platform.PlatformException;
import com.er.platform.security.BaseRoleTest;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Test for UserService
 *
 */
public class ReadRoleTest extends BaseRoleTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReadRoleTest.class);

  @Test
  public void testVisibilityMatrix() throws PlatformException {
    final Resource[] roleRefernce = ReadRoleMatrix.roleArray;
    final boolean[][] accessMatrix = ReadRoleMatrix.accessMatrix;

    for (int accRow = 0; accRow < accessMatrix.length; accRow++) {
      final Resource accesor = roleRefernce[accRow];
      LOGGER.info("Testing access rights for {}", accesor.getLocalName());
      for (int creatCol = 0; creatCol < accessMatrix[accRow].length; creatCol++) {
        final boolean hasAccess = accessMatrix[accRow][creatCol];

        final Resource creator = roleRefernce[creatCol];
        if (hasAccess) {
          assertTrue("Matrix position [" + accRow + "][" + creatCol + "]: " + accesor.getLocalName()
              + " should not be able to access data of " + creator.getLocalName(), hasReadPermission(accesor, creator));
        } else {
          assertFalse("Matrix position [" + accRow + "][" + creatCol + "]: " + accesor.getLocalName()
              + " should be able to access data of " + creator.getLocalName(), hasReadPermission(accesor, creator));
        }
      }
    }
  }
 }
