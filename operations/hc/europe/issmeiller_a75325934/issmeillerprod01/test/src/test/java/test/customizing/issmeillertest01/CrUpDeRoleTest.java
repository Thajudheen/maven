/*
 * Copyright (c) 2014, In Mind Computing AG. All rights reserved.
 * IMC PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package test.customizing.issmeillertest01;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.er.platform.PlatformException;
import com.er.platform.jena.vocabulary.Application;
import com.er.platform.security.BaseRoleTest;
import com.er.platform.security.Permissions;
import com.er.test.util.jena.RoleTestVocabulary;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Test for PermissionEvaluatorImpl.
 *
 */
public class CrUpDeRoleTest extends BaseRoleTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(CrUpDeRoleTest.class);

  private static final List<Resource> DOCUMENT_CREATING_TEST_USERS = Arrays.asList(new Resource[]{
      RoleTestVocabulary.U_GB_NORD,
      RoleTestVocabulary.U_GB_SUED,
      RoleTestVocabulary.U_GB_WEST,
      RoleTestVocabulary.U_GB_MITTE,
      RoleTestVocabulary.U_RL_MITTE,
      RoleTestVocabulary.U_RL_NORD,
      RoleTestVocabulary.U_RL_SUED,
      RoleTestVocabulary.U_RL_WEST,
      RoleTestVocabulary.U_VL,
      RoleTestVocabulary.U_KAM,
      RoleTestVocabulary.U_GK,
  });

  private static final List<Resource> SUPPORTING_TEST_USERS = Arrays.asList(new Resource[]{
    RoleTestVocabulary.U_AL
  });

  /**
   * @throws PlatformException
   */
  @Test
  public void testAdmin() throws PlatformException {

    LOGGER.info("Testing admin role");

    createOrResetTestDocument(DOCUMENT_TYPE.QUOTE);

    assertTrue("Admin has no ADMIN rigths.", hasOperationPermission(RoleTestVocabulary.U_ADMIN, Permissions.ADMIN));
    assertFalse("Admin should not have CREATE rights.",
        hasOperationPermission(RoleTestVocabulary.U_ADMIN, Permissions.CREATE));
    assertFalse("Admin should not have UPDATE rights.",
        hasOperationPermission(RoleTestVocabulary.U_ADMIN, Permissions.UPDATE));
    assertFalse("Admin should not have DELETE rights.",
        hasOperationPermission(RoleTestVocabulary.U_ADMIN, Permissions.DELETE));
  }
  /**
   * @throws PlatformException
   */
  @Test
  public void testDocumentCreatingUsers() throws PlatformException {

    LOGGER.info("Testing main users");

    for (final Resource docCreator : DOCUMENT_CREATING_TEST_USERS) {
      createOrResetTestDocument(DOCUMENT_TYPE.QUOTE);

      assertFalse("User " + docCreator.getLocalName() + " has ADMIN rigths.",
          hasOperationPermission(docCreator, Permissions.ADMIN));
      assertTrue("User " + docCreator.getLocalName() + " has no CREATE rigths.",
          hasOperationPermission(docCreator, Permissions.CREATE));
      assertTrue("User " + docCreator.getLocalName() + " has no UPDATE rigths.",
          hasOperationPermission(docCreator, Permissions.UPDATE));
      assertFalse("User " + docCreator.getLocalName() + " has general DELETE rigths.",
          hasOperationPermission(docCreator, Permissions.DELETE));

      setTestDocumentBDA(Application.includesCreator, docCreator);
      assertTrue("User " + docCreator.getLocalName() + " has no DELETE rigths for own documents.",
          hasOperationPermission(docCreator, Permissions.DELETE));

      final Collection<Resource> allUsers = getAllUsers();
      allUsers.remove(docCreator);

      for (final Resource user : allUsers) {
        setTestDocumentBDA(Application.includesCreator, user);
        assertFalse("User " + docCreator.getLocalName() + " has DELETE rigths.",
            hasOperationPermission(docCreator, Permissions.DELETE));
      }
    }
  }

  /**
   * @throws PlatformException
   */
  @Test
  public void testSupportingUsers() throws PlatformException {

    LOGGER.info("Testing supporting users");

    for (final Resource supportingUser : SUPPORTING_TEST_USERS) {
      createOrResetTestDocument(DOCUMENT_TYPE.QUOTE);

      assertFalse("User " + supportingUser.getLocalName() + " has ADMIN rigths.",
          hasOperationPermission(supportingUser, Permissions.ADMIN));
      assertFalse("User " + supportingUser.getLocalName() + " has CREATE rigths.",
          hasOperationPermission(supportingUser, Permissions.CREATE));
      assertFalse("User " + supportingUser.getLocalName() + " has UPDATE rigths.",
          hasOperationPermission(supportingUser, Permissions.UPDATE));
      assertFalse("User " + supportingUser.getLocalName() + " has DELETE rigths.",
          hasOperationPermission(supportingUser, Permissions.DELETE));
    }
  }
}
