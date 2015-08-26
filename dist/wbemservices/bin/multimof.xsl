<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0"
    xmlns:lxslt="http://xml.apache.org/xslt"
    xmlns:redirect="org.apache.xalan.xslt.extensions.Redirect"
    extension-element-prefixes="redirect">
<xsl:output method="html" indent="yes"/>

<!-- KEY DEFINITIONS -->
<xsl:key name="classes" match="CLASS" use="@NAME"/>
<xsl:key name="classsuper" match="CLASS" use="@SUPERCLASS"/>

<!-- VARIABLE DEFINITIONS -->
<!-- The root hierarchy of each xml file listed in k_filelist.xml (all CIM classes). -->
<!-- <xsl:variable name="documents" 
  select="document(document('k_filelist.xml')/documents/doc/@href)" /> -->

<!-- The root hierarchy of mof.xml, which contains all CIM classes. -->
 <xsl:variable name="documents" select="document('mof.xml')" />

<!-- don't do anything for elements not explicitly matched -->
<xsl:template match="*"/>

<!-- apply templates to reach CLASS elements -->
<xsl:template match="/|CIM|DECLARATION|DECLGROUP|VALUE.OBJECT">
 <xsl:apply-templates/>
</xsl:template>

<!-- INCLUDED MODULES -->
<!-- Rules for CIM Class -->
<xsl:include href="multiclass.xsl"/>

<xsl:template match="CLASS" mode="hierarchy">
<xsl:param name="bottomNode">false</xsl:param>
<xsl:param name="parentname" select="@SUPERCLASS"/>
  <!--<xsl:variable name="parentname" select="@SUPERCLASS"/>-->
    <xsl:for-each select="$documents">
 <!-- applies hierarchy template to the parent of the each class -->
        <xsl:apply-templates select="key('classes', $parentname)" mode="hierarchy"/>
    </xsl:for-each>
 <!-- outputs details about the current class -->
    <xsl:choose>
    <xsl:when test="$bottomNode = 'false'">
        <a href="{@NAME}.html"><xsl:value-of select="@NAME"/></a><br data="{@NAME}"></br>
        <xsl:text> &#xa0;   &#xa0;|</xsl:text>
        <br></br>
        <xsl:text> &#xa0;  &#xa0;+--</xsl:text>
    </xsl:when>
    <xsl:otherwise>
        <b><xsl:value-of select="@NAME"/></b><br data="{@NAME} -- {@SUPERCLASS}"></br>
    </xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="CLASS" mode="nohierarchy">
        <xsl:value-of select="@NAME"/><xsl:text> is a top level class.</xsl:text>
</xsl:template>
   

<!-- named template to do the descendent tracing -->
<xsl:template match="CLASS" mode="descendant">
<a href="{@NAME}.html"><xsl:value-of select="@NAME"/></a><br data="{@NAME}"></br>
<xsl:variable name="classname" select="@NAME"/>
    <xsl:for-each select="$documents">
        <xsl:apply-templates select="key('classsuper', $classname)" mode="descendant" />
    </xsl:for-each>
</xsl:template>


</xsl:stylesheet>
