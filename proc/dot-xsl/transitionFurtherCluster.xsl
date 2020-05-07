<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:t="https://github.com/cdc08x/MINERful/" xmlns="https://github.com/cdc08x/MINERful/">
  <xsl:output method="xml" encoding="utf-8" indent="yes"/>
  <xsl:strip-space elements="*"/>
  
  <xsl:key name="pkTransitionName" match="t:transition" use="@name" />
  <xsl:variable name="numberOfInvolvedActivities"><xsl:value-of select="count(//t:transition[generate-id()=generate-id(key('pkTransitionName',@name)[1])])" /></xsl:variable>
  <xsl:variable name="involvedActivities" select="//t:transition[generate-id()=generate-id(key('pkTransitionName',@name)[1])]" />

<!--
	<xsl:template match="/">
		<xsl:for-each
			select="//t:transition[generate-id()=generate-id(key('pkTransitionName',@name)[1])]">
			<xsl:value-of select="@name" />
			<xsl:text>
</xsl:text>
		</xsl:for-each>
    <xsl:value-of select="$numberOfInvolvedActivities" />
	</xsl:template>
-->
    <xsl:template match="@*|node()">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
    </xsl:template>

    <xsl:template match="t:transitionGroup">
      <xsl:choose>
        <xsl:when test="count(child::t:transition)*2 >= $numberOfInvolvedActivities">
		      <xsl:copy>
		        <xsl:apply-templates select="@*" />
<!--
		          <xsl:attribute name="allIncludedBut">
		            <xsl:apply-templates select="." mode="LIST_MISSING_TRANSITION_NAMES" />
		          </xsl:attribute>
-->
            <xsl:attribute name="listingMissingTransitions">
              <xsl:value-of select="'true'" />
            </xsl:attribute>
		        <xsl:apply-templates select="." mode="MISSING_TRANSITION_NODES" />
		      </xsl:copy>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
          </xsl:copy>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <xsl:template mode="LIST_MISSING_TRANSITION_NAMES" match="t:transitionGroup">
      <xsl:variable name="missingOnesWithTrailingSpace">
	      <xsl:for-each select="$involvedActivities[not(./@name = current()/child::t:transition/@name)]">
          <xsl:value-of select="concat(@name, ' ')" />
	      </xsl:for-each>
	    </xsl:variable>
	    <xsl:value-of select="normalize-space($missingOnesWithTrailingSpace)" />
    </xsl:template>

    <xsl:template mode="MISSING_TRANSITION_NODES" match="t:transitionGroup">
       <xsl:for-each select="$involvedActivities[not(./@name = current()/child::t:transition/@name)]">
         <xsl:element name="transition">
           <xsl:apply-templates select="@name"/>
           <xsl:apply-templates select="@taskName"/>
         </xsl:element>
       </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>