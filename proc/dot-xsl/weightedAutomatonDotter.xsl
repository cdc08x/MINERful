<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:t="https://github.com/cdc08x/MINERful/">
  <xsl:output method="xml" encoding="utf-8" omit-xml-declaration="yes" indent="yes" />

  <xsl:param name="DO_APPLY_TRANSPARENCY_FOR_NOT_TRAVERSED" select="false()" />
  <xsl:param name="DO_WEIGH_LINES" select="false()" />

  <xsl:param name="WEIGHT_FACTOR" select="'5'" />
  <xsl:param name="WEIGHT_PENWIDTH_FOR_STATES" select="'0.75'" />
  <xsl:variable name="maxWeightQuantile" select="//t:state[not(//@weightQuantile > @weightQuantile)][1]/@weightQuantile" />
  <xsl:variable name="minWeightQuantile" select="//t:state[not(//@weightQuantile &lt; @weightQuantile)][1]/@weightQuantile" />
  
  <xsl:template match="/">
    <xsl:apply-templates select="t:weightedAutomaton" />
  </xsl:template>
  
  <xsl:template match="t:weightedAutomaton">
    
    <xsl:text><![CDATA[digraph DeclarativeWeightedAutomaton {]]></xsl:text>
    <xsl:text>&#xa;</xsl:text><!-- newline -->

    <xsl:apply-templates select="t:states" />
  
    <xsl:text><![CDATA[}]]></xsl:text>

  </xsl:template>
  
  <xsl:template match="t:states">
    <xsl:text><![CDATA[node [shape="circle", label=""]]]></xsl:text>
    <xsl:text>&#xa;</xsl:text><!-- newline -->

    <xsl:apply-templates select="t:state" />
  </xsl:template>
  
  <xsl:template match="t:state[not(@illegal='true')]">
    <xsl:if test="@initial = 'true'">
      <xsl:text><![CDATA[initial [shape="plaintext", label=""]]]></xsl:text>
      <xsl:text>&#xa;</xsl:text><!-- newline -->
      <xsl:text><![CDATA[initial -> ]]></xsl:text>
      <xsl:value-of select="@id" />
      <xsl:text>&#xa;</xsl:text><!-- newline -->
    </xsl:if>

    <xsl:value-of select="@id" />
    <xsl:text><![CDATA[ []]></xsl:text>

    <xsl:if test="@accept = 'true'">
      <xsl:value-of select="@id" />
      <xsl:text><![CDATA[label="·", shape="doublecircle", ]]></xsl:text>
    </xsl:if>
    <xsl:if test="@weight = '0' and $DO_APPLY_TRANSPARENCY_FOR_NOT_TRAVERSED">
      <xsl:text><![CDATA[color="#00000044", ]]></xsl:text>
    </xsl:if>

    <xsl:text><![CDATA[penwidth=]]></xsl:text>
    <xsl:apply-templates select="." mode="WEIGHT_PENWIDTH" />
    <xsl:text><![CDATA[]]]></xsl:text>
    <xsl:text>&#xa;</xsl:text><!-- newline -->
    
    <xsl:apply-templates select="t:transitions" />
  </xsl:template>
  
  <xsl:template match="t:transitions">
    <xsl:apply-templates select="t:transitionGroup" />
    <xsl:apply-templates select="t:transition" />
  </xsl:template>
  
  <xsl:template match="t:transitionGroup">
    <xsl:value-of select="@idref" /> <!-- s2a1 -->
    <xsl:text><![CDATA[ [label="]]></xsl:text>
    <xsl:if test="@listingMissingTransitions = 'true'">
      <xsl:choose>
        <xsl:when test="t:transition">
          <xsl:text><![CDATA[All except:\n]]></xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text><![CDATA[*]]></xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
    <xsl:apply-templates select="t:transition[1]" mode="NAME" />
    <xsl:apply-templates select="t:transition[1]/following-sibling::t:transition" mode="NEWLINE_AND_NAME" />
<!--
    <xsl:if test="@listingMissingTransitions = 'true'">
      <xsl:text><![CDATA[\n}]]></xsl:text>
    </xsl:if>
-->
    <!-- SBWL Fachprüfung\n... -->
    <xsl:text><![CDATA[", shape="plaintext"]]></xsl:text>
    
    <xsl:if test="@totalWeight = '0' and $DO_APPLY_TRANSPARENCY_FOR_NOT_TRAVERSED">
      <xsl:text><![CDATA[, fontcolor="#00000044", ]]></xsl:text>
    </xsl:if>
    <xsl:text><![CDATA[]]]></xsl:text>
    
    <xsl:text>&#xa;</xsl:text><!-- newline -->
    
    <xsl:value-of select="ancestor::t:state/@id" /> <!-- s2 --> 
    <xsl:text><![CDATA[ -> ]]></xsl:text>
    <xsl:value-of select="@idref" /> <!-- s2a1 -->
    <xsl:text><![CDATA[ [dir=none, penwidth=]]></xsl:text>
    <xsl:apply-templates select="." mode="WEIGHT_PENWIDTH" /> <!--   5 -->
    <xsl:if test="@totalWeight = '0' and $DO_APPLY_TRANSPARENCY_FOR_NOT_TRAVERSED">
      <xsl:text><![CDATA[, color="#00000044", ]]></xsl:text>
    </xsl:if>
    <xsl:text><![CDATA[]]]></xsl:text>    

    <xsl:text>&#xa;</xsl:text><!-- newline -->
    
    <xsl:value-of select="@idref" /> <!-- s2a1 -->
    <xsl:text><![CDATA[ -> ]]></xsl:text>
    <xsl:value-of select="@to" /> <!-- s2a1 -->    
        
    <xsl:text><![CDATA[ [penwidth=]]></xsl:text>
    <xsl:apply-templates select="." mode="WEIGHT_PENWIDTH" /> <!--   5 -->
    <xsl:if test="@totalWeight = '0' and $DO_APPLY_TRANSPARENCY_FOR_NOT_TRAVERSED">
      <xsl:text><![CDATA[, color="#00000044", ]]></xsl:text>
    </xsl:if>
    <xsl:text><![CDATA[]]]></xsl:text>  
    <xsl:text>&#xa;</xsl:text><!-- newline -->

  </xsl:template>
  
  <xsl:template match="t:transition[not(@illegal='true')]">
    <xsl:value-of select="@id" /> <!-- s2a1 -->
    <xsl:text><![CDATA[ [label="]]></xsl:text>
    <xsl:apply-templates select="@taskName" />
    <!-- SBWL Fachprüfung -->
    <xsl:text><![CDATA[", shape="plaintext"]]></xsl:text>
    <xsl:if test="@weight = '0' and $DO_APPLY_TRANSPARENCY_FOR_NOT_TRAVERSED">
      <xsl:text><![CDATA[, fontcolor="#00000044", ]]></xsl:text>
    </xsl:if>
    <xsl:text><![CDATA[]]]></xsl:text>
    
    <xsl:text>&#xa;</xsl:text><!-- newline -->
    
    <xsl:value-of select="ancestor::t:state/@id" /> <!-- s2 --> 
    <xsl:text><![CDATA[ -> ]]></xsl:text>
    <xsl:value-of select="@id" /> <!-- s2a1 -->
    <xsl:text><![CDATA[ [dir=none, penwidth=]]></xsl:text>
    <xsl:apply-templates select="." mode="WEIGHT_PENWIDTH" /> <!--   5 -->
    <xsl:if test="@weight = '0' and $DO_APPLY_TRANSPARENCY_FOR_NOT_TRAVERSED">
      <xsl:text><![CDATA[, color="#00000044", ]]></xsl:text>
    </xsl:if>
    
    <xsl:text><![CDATA[]]]></xsl:text>    

    <xsl:text>&#xa;</xsl:text><!-- newline -->
    
    <xsl:value-of select="@id" /> <!-- s2a1 -->
    <xsl:text><![CDATA[ -> ]]></xsl:text>
    <xsl:value-of select="@to" /> <!-- s2a1 -->    
        
    <xsl:text><![CDATA[ [penwidth=]]></xsl:text>
    <xsl:apply-templates select="." mode="WEIGHT_PENWIDTH" /> <!--  5 -->
    <xsl:if test="@weight = '0' and $DO_APPLY_TRANSPARENCY_FOR_NOT_TRAVERSED">
      <xsl:text><![CDATA[, color="#00000044", ]]></xsl:text>
    </xsl:if>
    <xsl:text><![CDATA[]]]></xsl:text>  
    <xsl:text>&#xa;</xsl:text><!-- newline -->

  </xsl:template>
  
  <xsl:template match="t:transition" mode="NAME">
    <xsl:value-of select="@taskName"></xsl:value-of>
  </xsl:template>
  
  <xsl:template match="t:transition" mode="NEWLINE_AND_NAME">
    <xsl:value-of select="'\n'"></xsl:value-of>
    <xsl:value-of select="@taskName"></xsl:value-of>
  </xsl:template>
  
  <xsl:template match="t:transition" mode="WEIGHT_PENWIDTH">
    <xsl:choose>
    	<xsl:when test="$DO_WEIGH_LINES">
    		<xsl:value-of select="@weightQuantile * $WEIGHT_FACTOR + 1" />
    	</xsl:when>
    	<xsl:otherwise>
    		<xsl:value-of select="1" />
    	</xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  
  <xsl:template match="t:state" mode="WEIGHT_PENWIDTH">
    <xsl:choose>
    	<xsl:when test="$DO_WEIGH_LINES">
    		<xsl:value-of select="@weightQuantile * $WEIGHT_PENWIDTH_FOR_STATES + 1" />
    	</xsl:when>
    	<xsl:otherwise>
    		<xsl:value-of select="1" />
    	</xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="t:transitionGroup" mode="WEIGHT_PENWIDTH">
    <xsl:choose>
    	<xsl:when test="$DO_WEIGH_LINES">
    		<xsl:value-of select="@maxQuantile * $WEIGHT_FACTOR + 1" />
    	</xsl:when>
    	<xsl:otherwise>
    		<xsl:value-of select="1" />
    	</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
