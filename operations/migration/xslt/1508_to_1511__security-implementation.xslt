<?xml version="1.0"?>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:owl="http://www.w3.org/2002/07/owl#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:as="http://www.inmindcomputing.com/application/application-schema.owl#"
	xmlns:ps="http://www.inmindcomputing.com/platform/platform-schema.owl#"
	version="1.0">
	
	<xsl:output omit-xml-declaration="no" indent="yes" encoding="utf-8"/>
	
	<!-- main entry template only handling nodes -->
	
	<xsl:template match="node()" name="main">
	
		<xsl:choose>
		
			<!-- migrate user -->
			
			<xsl:when test="rdf:type[@rdf:resource='http://www.inmindcomputing.com/application/application-schema.owl#User']">
			
				<xsl:variable name="person" select="concat(@rdf:about, '-Person')" /> 
				<xsl:variable name="password" select="concat(@rdf:about, '-Password')" /> 
				<xsl:copy>
					<!-- copy all attributes -->
					<xsl:copy-of select="@*"/>
					
					<xsl:apply-templates select="node()"/>
					
					<as:hasUserStatus rdf:resource='http://www.inmindcomputing.com/application/application-schema.owl#USERACTIVE' />
					
					<as:includesPerson>
						<xsl:attribute name="rdf:resource">
							<xsl:value-of select="$person" />
						</xsl:attribute>
					</as:includesPerson>
					
					<as:includesUserPassword>
						<xsl:attribute name="rdf:resource">
							<xsl:value-of select="$password" />
						</xsl:attribute>
					</as:includesUserPassword>
				</xsl:copy>
				
				<owl:NamedIndividual>
					<xsl:attribute name="rdf:about">
						<xsl:value-of select="$person" />
					</xsl:attribute>
					<rdf:type rdf:resource='http://www.inmindcomputing.com/application/application-schema.owl#Person'/>
					<xsl:copy-of select="as:personPhone"/>
					<xsl:copy-of select="as:personPosition"/>
					<xsl:copy-of select="as:personFirstName"/>
					<xsl:copy-of select="as:personLastName"/>
					<xsl:copy-of select="as:personEmail"/>
					<xsl:copy-of select="as:hasGender"/>
				</owl:NamedIndividual>
				
				<owl:NamedIndividual>
					<xsl:attribute name="rdf:about">
						<xsl:value-of select="$password" />
					</xsl:attribute>
					<rdf:type rdf:resource='http://www.inmindcomputing.com/application/application-schema.owl#UserPassword'/>
					<ps:objectName>
						<xsl:copy-of select="as:userPassword/@*"/>
						<xsl:copy-of select="as:userPassword/text()"/>
					</ps:objectName>
					<as:hasUserPasswordStatus rdf:resource="http://www.inmindcomputing.com/application/application-schema.owl#PasswordActive" />
				</owl:NamedIndividual>

			</xsl:when>
			
			<xsl:otherwise>
				<xsl:call-template name="copy"/>
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>
	
	<!-- recursive copy of elements and attributes -->
	
	<xsl:template name="copy">
		<xsl:copy>
			<!-- copy all attributes -->
			<xsl:copy-of select="@*"/>
			<!-- dispatch to main template -->
			<xsl:apply-templates select="node()"/>
		</xsl:copy>
	</xsl:template>
	
	<!-- ignore during copy -->
	<xsl:template match="as:userPassword"/>
	<xsl:template match="as:personPhone"/>
	<xsl:template match="as:personPosition"/>
	<xsl:template match="as:personFirstName"/>
	<xsl:template match="as:personLastName"/>
	<xsl:template match="as:personEmail"/>
	<xsl:template match="as:hasGender"/>
		
	<xsl:template match="ps:businessTypeShortText">
		<ps:objectName>
			<xsl:copy-of select="@*|text()"/>
		</ps:objectName>
	</xsl:template>
	<xsl:template match="ps:businessTypeExternalId">
		<ps:objectExternalId>
			<xsl:copy-of select="@*|text()"/>
		</ps:objectExternalId>
	</xsl:template>
	<xsl:template match="ps:businessTypeId">
		<ps:objectId>
			<xsl:copy-of select="@*|text()"/>
		</ps:objectId>
	</xsl:template>
	
	<xsl:template match="as:hasCompany">
		<as:containsCompany>
			<xsl:copy-of select="@*|text()"/>
		</as:containsCompany>
	</xsl:template>

	<xsl:template match="as:hasRoleDirect">
		<as:hasRole>
			<xsl:copy-of select="@*|text()"/>
		</as:hasRole>
	</xsl:template>

	<xsl:template match="as:hasRoleRead">
		<as:hasAccessToDataOf>
			<xsl:copy-of select="@*|text()"/>
		</as:hasAccessToDataOf>
	</xsl:template>
	
	<xsl:template match="as:hasRoleWrite">
		<as:createsObjectsVisibleFor>
			<xsl:copy-of select="@*|text()"/>
		</as:createsObjectsVisibleFor>
	</xsl:template>
	
</xsl:stylesheet>
