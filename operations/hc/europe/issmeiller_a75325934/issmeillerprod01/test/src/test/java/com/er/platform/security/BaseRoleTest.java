/*
 * Copyright (c) 2015, In Mind Computing AG. All rights reserved.
 * IMC PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.er.platform.security;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.junit.Before;
import org.mindswap.pellet.PelletOptions;
import org.mindswap.pellet.PelletOptions.MonitorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.er.platform.PlatformException;
import com.er.platform.context.ContextModelFactory;
import com.er.platform.jena.util.Utils;
import com.er.platform.jena.vocabulary.Application;
import com.er.platform.jena.vocabulary.Platform;
import com.er.platform.tools.owl.CmisResourceLoader;
import com.er.test.util.jena.RoleTestVocabulary;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 *
 *
 */
public class BaseRoleTest {

  public static enum DOCUMENT_TYPE {
    OPPORTUNITY, QUOTE,
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(BaseRoleTest.class);
  private PermissionEvaluatorImpl evaluator;

  private ACLUserDetailsServiceImpl userService;

  private OntModel reasonedSecurityModel;

  private static final Resource TEST_DOCUMENT = ResourceFactory
      .createResource("http://www.inmindcomputing.com/application/application-implementation.owl#TestDocument");

  public void createOrResetTestDocument(final DOCUMENT_TYPE type) {
    deleteTestDocument();
    if (type.equals(DOCUMENT_TYPE.QUOTE)) {
      reasonedSecurityModel.add(TEST_DOCUMENT, RDF.type, Application.Quote);
      LOGGER.info("Test quote created.");
    } else {
      reasonedSecurityModel.add(TEST_DOCUMENT, RDF.type, Application.Opportunity);
      LOGGER.info("Test opportunity created.");
    }
    reasonedSecurityModel.rebind();
  }

  private OntModel createReasonedSecurityModel() throws PlatformException {
    // only reasoned model will contain all inferences
    final OntModel staticSecurityModel = getStaticSecurityModel();
    reasonedSecurityModel = ContextModelFactory.createReasonedModel(staticSecurityModel); // private mod
    return reasonedSecurityModel;
  }

  private void deleteTestDocument() {
    reasonedSecurityModel.removeAll(TEST_DOCUMENT, (Property) null, (RDFNode) null);
  }

  public Collection<Resource> getAllUsers() {
    final Collection<Resource> allUsers =
        reasonedSecurityModel.listResourcesWithProperty(RDF.type, Application.User).toSet();
    final Collection<Resource> testObjects =
        reasonedSecurityModel.listResourcesWithProperty(RDF.type, RoleTestVocabulary.TestObject).toSet();
    allUsers.retainAll(testObjects);
    return new HashSet<Resource>(allUsers);
  }

  private OntModel getStaticSecurityModel() throws PlatformException {
    final String schemaUri = "http://www.inmindcomputing.com/security/roleTest.owl";
    final ResourceLoader resourceLoader = new CmisResourceLoader(System.getProperty("user.dir") + "/../"); // C:\Users\Falk
    // Brauer\Documents\devops\operations\hc\australia\In
    // Mind Computing
    // OEM\issamtektest01

    final OntDocumentManager documentManager =
        ContextModelFactory.createDocumentManager(resourceLoader,
            ContextModelFactory.createLocationMapper(resourceLoader, "location-mapping.n3"));

    final OntModel staticSecurityModel = ContextModelFactory.createRawModel(documentManager, schemaUri);
    staticSecurityModel.prepare();

    return staticSecurityModel;
  }

  public boolean hasOperationPermission(final Resource accessor, final Permissions operation) throws PlatformException {
    LOGGER.info("Testing operation {} for user {}", operation.name(), accessor.getLocalName());
    final Iterator<Statement> it =
        reasonedSecurityModel.listStatements(accessor, Platform.businessTypeShortText, (RDFNode) null);
    if (!it.hasNext()) {
      throw new PlatformException("Could find user or user's 'businessTypeShortText' attribute");
    }
    final TestingAuthenticationToken token = new TestingAuthenticationToken(it.next().getString(), "password");

    final boolean permission = hasPermission(token, reasonedSecurityModel, operation);

    return permission;

  }
  /**
   * Method used for testing
   *
   * @param authentication
   * @param securityModel
   * @param permission
   * @return whether permission has been granted
   *
   */
  private boolean hasPermission(final Authentication authentication, final OntModel securityModel,
      final Permissions permission) {
    return evaluator.hasPermissionInternal(authentication, securityModel, permission.toString());
  }

  public boolean hasReadPermission(final Resource artifactAccessor, final Resource artifactCreator)
      throws PlatformException {
    LOGGER.info("Testing read access for user {} on data created by {}", artifactAccessor.getLocalName(),
        artifactCreator.getLocalName());
    boolean result = false;
    final Collection<GrantedAuthority> creatorAuthorities =
        userService.getAuthorities(reasonedSecurityModel, artifactCreator);
    final Collection<GrantedAuthority> accessorAuthorities =
        userService.getAuthorities(reasonedSecurityModel, artifactAccessor);

    final BigInteger creatorAuthority = BigInteger.valueOf(userService.getWriteAuthorityInternal(creatorAuthorities));
    final BigInteger accessorAuthority = userService.getReadAuthorityInternal(accessorAuthorities);

    if (accessorAuthority.mod(creatorAuthority) == BigInteger.ZERO) {
      result = true;
    }
    return result;
  }

  public void setTestDocumentBDA(final Property bda, final Resource bo) {
    LOGGER.info("Setting BDA {} of current test document to {} ", bda.getLocalName(), bo.getLocalName());
    reasonedSecurityModel.removeAll(TEST_DOCUMENT, bda, (RDFNode) null);
    reasonedSecurityModel.add(TEST_DOCUMENT, bda, bo);
    reasonedSecurityModel.rebind();
  }

  @Before
  public void setUp() throws Exception {

    PelletOptions.USE_CLASSIFICATION_MONITOR = MonitorType.NONE;
    Utils.configurePelletOptions();

    createReasonedSecurityModel();

    evaluator = new PermissionEvaluatorImpl();
    userService = new ACLUserDetailsServiceImpl();

  }
}
