<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:t="https://github.com/cdc08x/MINERful/" xmlns="https://github.com/cdc08x/MINERful/">
  <xsl:output method="xml" encoding="utf-8" indent="yes"/>
  <xsl:strip-space elements="*"/>

    <xsl:template match="@*|node()">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
    </xsl:template>

    <!-- When matching initialState: do nothing -->
    <xsl:template match="t:initialState" />
    
    <!-- When matching state: if it is initial, print the attribute -->
    <xsl:template match="t:state">
      <xsl:copy>
        <xsl:apply-templates select="@*[name()!='id']"/>
        <xsl:if test="@id = /t:weightedAutomaton/t:initialState/@id">
          <xsl:attribute name="initial">true</xsl:attribute>
        </xsl:if>
        <xsl:attribute name="id">
          <xsl:value-of select="@id"></xsl:value-of>
        </xsl:attribute>
        <xsl:apply-templates select="node()"/>
      </xsl:copy>
    </xsl:template>
        
    <xsl:template match="t:transition[not(@illegal='true')]">
	      <xsl:choose>
	        <xsl:when test="not (@to=preceding-sibling::t:transition/@to)">
	          <xsl:choose>
	           <xsl:when test="following-sibling::t:transition[@to=current()/@to]">
	            <xsl:element name="transitionGroup">
                <xsl:attribute name="idref">
                  <xsl:value-of select="@id"></xsl:value-of>
                </xsl:attribute>
                <xsl:attribute name="to">
                  <xsl:value-of select="@to"></xsl:value-of>
                </xsl:attribute>
	              <xsl:attribute name="totalWeight">
	                <xsl:value-of select="sum(parent::t:transitions/child::t:transition[@to=current()/@to]/@weight)" />
	              </xsl:attribute>
                <xsl:attribute name="maxQuantile">
                  <xsl:value-of select="parent::t:transitions/child::t:transition[(@to=current()/@to) and (not(parent::t:transitions/child::t:transition[@to=current()/@to]/@weightQuantile > @weightQuantile))][1]/@weightQuantile" />
                </xsl:attribute>
	              <xsl:apply-templates select="." mode="COPYTHIS" />
	              <xsl:apply-templates select="following-sibling::t:transition[@to=current()/@to]" mode="COPYTHIS" />
	            </xsl:element>
             </xsl:when>
             <xsl:otherwise>
              <xsl:apply-templates select="." mode="COPYTHISWITHTO" />
             </xsl:otherwise>
	          </xsl:choose>
	        </xsl:when>
	        <xsl:otherwise />
	      </xsl:choose>
    </xsl:template>

    <xsl:template match="t:transition" mode="COPYTHIS">
      <xsl:copy>
        <xsl:apply-templates select="@*[name()!='to']|node()"/>
      </xsl:copy>
    </xsl:template>
    
    <xsl:template match="t:transition" mode="COPYTHISWITHTO">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
    </xsl:template>
</xsl:stylesheet>