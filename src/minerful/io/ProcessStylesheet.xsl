<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mnf="http://www.dis.uniroma1.it/minerful" xmlns="http://www.w3.org/1999/xhtml">
	<xsl:output
		indent="yes"
		method="xml"
		omit-xml-declaration="no"
		standalone="yes"
	/>
	<xsl:template match="/">
		<html>
			<head>
				<title>Discovered process model</title>
			</head>
			<body>
				<h1>Constraints of the discovered process model</h1>
				<xsl:apply-templates select="/mnf:processModelConstraints/mnf:bag/mnf:constraints" />
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="mnf:constraints">
		<ul>
			<xsl:apply-templates match="mnf:at" />
		</ul>
	</xsl:template>
	
	<xsl:template match="mnf:at">
		<li>
			<span class="Label"><xsl:text>Activity: </xsl:text></span>
			<span class="Activity Grouping"><xsl:value-of select="@name" /></span>
				<dl>
					<xsl:apply-templates select="mnf:value/mnf:existenceConstraint" />
					<xsl:apply-templates select="mnf:value/mnf:relationConstraint" />
					<xsl:apply-templates select="mnf:value/mnf:negativeRelationConstraint" />
				</dl>
		</li>
	</xsl:template>
	
	<xsl:template match="mnf:existenceConstraint">
		<xsl:apply-templates select="self::node()" mode="NAME"/>
		<xsl:apply-templates select="self::node()" mode="METRICS"/>
	</xsl:template>

	<xsl:template match="mnf:relationConstraint">
		<xsl:apply-templates select="self::node()" mode="NAME"/>
		<xsl:apply-templates select="self::node()" mode="METRICS"/>
	</xsl:template>

	<xsl:template match="mnf:negativeRelationConstraint">
		<xsl:apply-templates select="self::node()" mode="NAME"/>
		<xsl:apply-templates select="self::node()" mode="METRICS"/>
	</xsl:template>
	
	<xsl:template match="mnf:existenceConstraint" mode="NAME">
		<xsl:element name="dt">
			<xsl:attribute name="class"><!--
				-->Constraint Type <!--
				--><xsl:value-of select="@type" /><!--
			 --></xsl:attribute>
			<span class="Constraint Name">
				<xsl:value-of select="@type" />
			</span>
			<xsl:text>(</xsl:text>
			<span class="Constraint Activity Implying"><xsl:value-of select="ancestor::mnf:constraints/mnf:at/@name" /></span>
			<xsl:text>)</xsl:text>
		</xsl:element>		
	</xsl:template>
	
	<xsl:template match="mnf:relationConstraint|mnf:negativeRelationConstraint" mode="NAME">
		<xsl:element name="dt">
			<xsl:attribute name="class"><!--
				-->Constraint Type <!--
				--><xsl:value-of select="@type" /><!--
			 --></xsl:attribute>
			<span class="Constraint Name">
				<xsl:value-of select="@type" />
			</span>
			<xsl:text>(</xsl:text>
			<span class="Constraint Activity Implying"><xsl:value-of select="ancestor::mnf:constraints/mnf:at/@name" /></span>
			<xsl:text>, </xsl:text>
			<span class="Constraint Activity Implied"><xsl:value-of select="mnf:implied/@name" /></span>
			<xsl:text>)</xsl:text>
		</xsl:element>		
	</xsl:template>
	
	<xsl:template match="mnf:existenceConstraint|mnf:relationConstraint|mnf:negativeRelationConstraint" mode="METRICS">
		<dd>
		  <ul>
				<li class="Metric">
					<span class="Metric Label"><xsl:text>Support: </xsl:text></span>
					<span class="Metric Value Support"><xsl:value-of select="mnf:support" /></span>
				</li>
				<li class="Metric">
					<span class="Metric Label"><xsl:text>Confidence Level: </xsl:text></span>
					<span class="Metric Value ConfidenceLevel"><xsl:value-of select="mnf:confidence" /></span>
				</li>
				<li class="Metric">
					<span class="Metric Label"><xsl:text>Interest Factor: </xsl:text></span>
					<span class="Metric Value InterestFactor"><xsl:value-of select="mnf:interestFactor" /></span>
				</li>
      </ul>
		</dd>
	</xsl:template>
	
	<xsl:template match="text()"></xsl:template>
	
</xsl:stylesheet>