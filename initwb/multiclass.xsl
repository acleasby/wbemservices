<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0"
    xmlns:lxslt="http://xml.apache.org/xslt"
    xmlns:redirect="org.apache.xalan.xslt.extensions.Redirect"
    extension-element-prefixes="redirect">
<xsl:output method="html"/>

<xsl:template match="CLASS">
<xsl:variable name="classname" select="@NAME"/>
<xsl:variable name="qualcount" select="count(.)"/>
<xsl:variable name="outfile"><xsl:value-of select="@NAME"/>.html</xsl:variable>
<redirect:write select="$outfile">
<head>
    <link rel="stylesheet" type="text/css" href="mof.css"/>
</head>
<html>
<body bgcolor="White">
<hr />
<xsl:choose>
    <xsl:when test="@SUPERCLASS">
        <a name="{@NAME}"></a><H1>Class <xsl:value-of select="@NAME"/> 
        <BR></BR>extends <a href="{@SUPERCLASS}.html">
            <xsl:value-of select="@SUPERCLASS"/></a></H1>
    </xsl:when>
    <xsl:otherwise>
        <H1>Class <xsl:value-of select="@NAME"/></H1>
    </xsl:otherwise>
</xsl:choose>

<xsl:value-of select="child::QUALIFIER[attribute::NAME='Description'][position()=1]"/>

<hr />

<H2>Class Hierarchy</H2>

<!-- This works. -->
<xsl:apply-templates select="." mode="hierarchy"> 
<xsl:with-param name="bottomNode">true</xsl:with-param>
</xsl:apply-templates>

<!--  <xsl:variable name="classname" select="@NAME"/>
<xsl:for-each select="$documents">
<xsl:choose>
<xsl:when test="key('classes', $parentname)">
    <xsl:apply-templates select="key('classes', $parentname)" mode="hierarchy">
    <xsl:with-param name="bottomNode">true</xsl:with-param>
    </xsl:apply-templates>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="$classname"/><xsl:text> is a top level class.</xsl:text>
</xsl:otherwise>
</xsl:choose>
</xsl:for-each>-->

<H2>Direct Known Subclasses</H2>

<xsl:for-each select="$documents">
<xsl:sort select="@NAME"/>
    <xsl:choose>
        <xsl:when test="key('classsuper', $classname)">
            <xsl:apply-templates select="key('classsuper', $classname)" mode="descendant">
            </xsl:apply-templates>
        </xsl:when>
    <xsl:otherwise>
        <xsl:text>None.</xsl:text>
    </xsl:otherwise>
    </xsl:choose>
</xsl:for-each>

<H2>Class Qualifiers</H2>

<TABLE BORDER="1" CELLPADDING="1" WIDTH="100%">
<TR>
<TH>Name</TH>
<TH>Data Type</TH>
<TH>Value</TH>
<TH>Scope</TH>
<TH>Flavors</TH>
</TR>
<xsl:for-each
  select="QUALIFIER[not(@NAME=preceding-sibling::QUALIFIER/@NAME)]">
      <xsl:sort select="@NAME"/>
      <xsl:call-template name="QUAL_ROW"/>
</xsl:for-each>
</TABLE>

<xsl:if test="PROPERTY[not(@PROPAGATED)]">
<H3>Local Class Properties</H3>

<TABLE WIDTH="100%" CELLPADDING="1" BORDER="1">
    <TR>
	<TH ROWSPAN="2"><B>Name</B></TH>
	<TH ROWSPAN="2"><B>Data Type</B></TH>
	<TH COLSPAN="5"><B>Qualifiers</B></TH>
    </TR>
    <TR>
    	<TH>Name</TH><TH>Data Type</TH><TH>Value</TH><TH>Scope</TH><TH>Flavors</TH>
    </TR>
    <xsl:for-each select="PROPERTY[not(@PROPAGATED)]">
        <xsl:sort select="@NAME"/>
       	<xsl:call-template name="LOCAL_PROP_ROW">
	<xsl:with-param name="count" select="count(child::QUALIFIER) + 1"/>
	</xsl:call-template>
             <xsl:for-each select="QUALIFIER">
	        <xsl:sort select="@NAME"/>
	        <xsl:call-template name="QUAL_ROW"/>
            </xsl:for-each>
    </xsl:for-each>
</TABLE>
</xsl:if>

<xsl:if test="PROPERTY.REFERENCE">
<H3>Association References</H3>
<TABLE BORDER="1" CELLPADDING="1" WIDTH="60%">
    <TR>
    <TH>Name</TH>
    <TH>Class Origin</TH>
    <TH>Reference Class</TH>
    </TR>
    <xsl:for-each select="PROPERTY.REFERENCE[not(@PROPAGATED)]">
        <xsl:sort select="@NAME"/>
        <xsl:call-template name="ASSOC_PROP_ROW"/>
    </xsl:for-each>
</TABLE>
</xsl:if>

<xsl:if test="PROPERTY.REFERENCE[@PROPAGATED]">
<H3>Propagated Association References</H3>
<TABLE BORDER="1" CELLPADDING="1" WIDTH="60%">
    <TR>
    <TH>Name</TH>
    <TH>Class Origin</TH>
    <TH>Reference Class</TH>
    </TR>
    <xsl:for-each select="PROPERTY.REFERENCE[@PROPAGATED]">
        <xsl:sort select="@NAME"/>
        <xsl:call-template name="ASSOC_PROP_ROW"/>
    </xsl:for-each>
</TABLE>
</xsl:if>


<xsl:if test="PROPERTY[@PROPAGATED]">
<H3>Inherited Properties</H3>
        <TABLE BORDER="1" CELLPADDING="1" WIDTH="60%">
        <TR>
        <TH>Name</TH>
        <TH>Data Type</TH>
        <TH>Class Origin</TH>
        </TR>
        <xsl:for-each select="PROPERTY[@PROPAGATED][not(@NAME=preceding-sibling::PROPERTY/@NAME)]">
            <xsl:sort select="@NAME"/>
            <xsl:call-template name="PROP_ROW"/>
        </xsl:for-each>
        </TABLE>
</xsl:if>

<xsl:apply-templates/>

</body>
</html>
</redirect:write>

<!-- End of CLASS Template-->
</xsl:template>

<!-- Output Rows of Property Values-->



<xsl:template name="PROP_ROW">
<TR>
<TD VALIGN="TOP"><xsl:value-of select="@NAME"/></TD>
<TD VALIGN="TOP"><xsl:value-of select="@TYPE"/></TD>
<!--no default values in MOFs. don't know how to determine default value. -->
<!-- <TD VALIGN="TOP"><xsl:value-of select="child::VALUE|/descendant::VALUE"/></TD> -->
<TD VALIGN="TOP"><a href="{@CLASSORIGIN}.html"><xsl:value-of select="@CLASSORIGIN"/></a></TD>
</TR>
</xsl:template>

<xsl:template name="LOCAL_PROP_ROW">
<xsl:param name="count" select="count(child::QUALIFIER) + 1"/>
<TR>
<TH ROWSPAN="{$count}" VALIGN="TOP"><xsl:value-of select="@NAME"/></TH>
<TD ROWSPAN="{$count}" VALIGN="TOP"><xsl:value-of select="@TYPE"/></TD>
</TR>
</xsl:template>

<xsl:template name="ASSOC_PROP_ROW">
<TR>
<TD VALIGN="TOP"><xsl:value-of select="@NAME"/></TD>
<TD VALIGN="TOP"><xsl:value-of select="@CLASSORIGIN"/></TD>
<TD VALIGN="TOP"><a href="{@REFERENCECLASS}.html"><xsl:value-of select="@REFERENCECLASS"/></a></TD>
</TR>
</xsl:template>


<xsl:template name="QUAL_ROW">
<TR>
<TD VALIGN="TOP"><xsl:value-of select="@NAME"/></TD>
<TD VALIGN="TOP"><xsl:value-of select="@TYPE"/></TD>
<xsl:if test="child::VALUE">
<TD VALIGN="TOP"><xsl:value-of select="."/></TD>
</xsl:if>
<xsl:if test="child::VALUE.ARRAY">
<TD VALIGN="TOP">  
    <xsl:for-each select="child::VALUE.ARRAY/child::VALUE">
        <xsl:value-of select="."/>
        <xsl:if test="not(position()=last())"><xsl:text>, </xsl:text>
        </xsl:if>
    </xsl:for-each></TD>
</xsl:if>

<xsl:choose>
    <xsl:when test="@TOSUBCLASS">
        <TD VALIGN="TOP"><xsl:text>TOSUBCLASS= </xsl:text><xsl:value-of select="@TOSUBCLASS"/></TD>
    </xsl:when>
    <xsl:otherwise>
        <TD VALIGN="TOP"><xsl:text>None </xsl:text></TD>
     </xsl:otherwise>
</xsl:choose>

<xsl:choose>
    <xsl:when test="@OVERRIDABLE|@TRANSLATABLE">
        <TD VALIGN="TOP"><xsl:if test="@OVERRIDABLE"><xsl:text>OVERRIDABLE= </xsl:text><xsl:value-of select="@OVERRIDABLE"/><br><xsl:text></xsl:text></br></xsl:if><xsl:if test="@TRANSLATABLE"><xsl:text>TRANSLATABLE= </xsl:text><xsl:value-of select="@TRANSLATABLE"/></xsl:if></TD>
     </xsl:when>
     <xsl:otherwise>
         <TD VALIGN="TOP"><xsl:text>None </xsl:text></TD>
     </xsl:otherwise>
</xsl:choose>
</TR>
</xsl:template>
</xsl:stylesheet>


