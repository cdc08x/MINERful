<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
		<xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
	<xsl:template match="/">
	<xsl:apply-templates select="descendant::constraint"/>
	</xsl:template>
	<xsl:template match="constraint">
		<xsl:text><![CDATA[']]></xsl:text>
		<xsl:value-of select="name"/>
		<xsl:text><![CDATA[';']]></xsl:text>
		<xsl:value-of select="constraintparameters/parameter[1]/branches/branch/@name"/>
		<xsl:text><![CDATA[';']]></xsl:text>
		<xsl:value-of select="constraintparameters/parameter[2]/branches/branch/@name"/>
		<xsl:text><![CDATA['
]]></xsl:text>
	</xsl:template>
</xsl:stylesheet>