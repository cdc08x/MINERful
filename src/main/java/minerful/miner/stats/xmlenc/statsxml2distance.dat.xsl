<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="">
	<xsl:output
		encoding="UTF-8"
		method="text"
		omit-xml-declaration="yes"
		standalone="yes"
		indent="no"
	 />
	<xsl:template match="/">
		<xsl:text>&apos;Searched&apos;;&apos;Pivot&apos;;&apos;Distance&apos;;&apos;Times&apos;
<!-- --></xsl:text>
		<xsl:apply-templates select="/globalStatsTable/statsTable/stats"/>
	</xsl:template>

	<xsl:template match="stats">
		<xsl:apply-templates select="details/interplayStats/interplayStatsWith">
			<xsl:with-param name="searched"><xsl:value-of select="@task"/></xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="interplayStatsWith">
		<xsl:param name="searched" />

		<xsl:apply-templates select="details/distances/distance">
			<xsl:with-param name="searched"><xsl:value-of select="$searched"/></xsl:with-param>
			<xsl:with-param name="pivot"><xsl:value-of select="@task"/></xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template match="distance">
		<xsl:param name="searched" />
		<xsl:param name="pivot" />
		
		<xsl:text>&apos;</xsl:text>
		<xsl:value-of select="$searched"/>
		<xsl:text>&apos;;&apos;</xsl:text>
		<xsl:value-of select="$pivot"/>
		<xsl:text>&apos;;</xsl:text>
		<xsl:value-of select="@at"/>
		<xsl:text>;</xsl:text>
		<xsl:value-of select="counted"/>
		<xsl:text>
<!-- --></xsl:text>
	</xsl:template>
	
	<xsl:template match="text()"></xsl:template>
</xsl:stylesheet>