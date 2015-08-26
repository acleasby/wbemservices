<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0"
    xmlns:lxslt="http://xml.apache.org/xslt"
    xmlns:redirect="org.apache.xalan.xslt.extensions.Redirect"
    extension-element-prefixes="redirect">
<xsl:output method="html" indent="no"/>

<!-- apply templates to reach CLASS elements -->
<xsl:template match="/|CIM|DECLARATION|DECLGROUP|VALUE.OBJECT">
  <html>
  <head>
<link rel="stylesheet" type="text/css" href="mof.css"/>
  <title>CIMv2 Schema</title>
  </head>
  <body bgcolor="White">

<xsl:variable name="indexfile">index.html</xsl:variable>
<redirect:write select="$indexfile">
<html>

<!-- frames
<frameset  cols="30,70">
    <frame name="allclassesframe" src="allclasses.html" marginwidth="10" marginheight="10" 
    scrolling="auto" frameborder="1"></frame>
    <frame name="classdetail" src="classdetail.html" marginwidth="10" marginheight="10" 
    scrolling="auto" frameborder="1"></frame>
</frameset> -->

<!-- frames -->
<frameset cols="32,68">
 <frameset rows="34,66">
   <frame name="schemaframe" src="schema.html" marginwidth="10" marginheight="10" scrolling="auto" frameborder="1"></frame>
   <frame name="allclassesframe" src="allclasses.html" scrolling="Auto" marginwidth="10" marginheight="10"></frame>
 </frameset>
 <frame name="classdetail" src="classdetail.html" marginwidth="10" marginheight="10" scrolling="auto" frameborder="1"></frame>
</frameset>



</html>
</redirect:write>

<xsl:variable name="allclassfile">allclasses.html</xsl:variable>
<redirect:write select="$allclassfile">
<html>
<head>
<title>All Classes</title>
</head>
<body bgcolor="White">
<H2>All Classes</H2>
<BR></BR>
<xsl:for-each select="CLASSES/CLASS">
    <xsl:sort select="@NAME"/>
    <a href="{@NAME}.html" target="classdetail"><xsl:value-of select="@NAME"/></a>
    <BR></BR>
</xsl:for-each> 

<H3><A NAME="associations"></A>Associations</H3>
<BR></BR>
<xsl:for-each select="CLASSES/CLASS[@ASSOCIATION]">
    <xsl:sort select="@NAME"/>
    <a href="{@NAME}.html" target="classdetail"><xsl:value-of select="@NAME"/></a>
    <BR></BR>
</xsl:for-each> 

<!--<H3><A NAME="providers"></A>Providers</H3>
<BR></BR>
<xsl:for-each select="CLASSES/CLASS[@PROVIDER]">
    <xsl:sort select="@NAME"/>
    <a href="{@NAME}.html" target="classdetail"><xsl:value-of select="@NAME"/></a>
    <BR></BR>
</xsl:for-each> -->

</body>
</html>
</redirect:write>


<xsl:variable name="classdetail">classdetail.html</xsl:variable>
<redirect:write select="$classdetail">
<html>
<head>
<title>All Classes</title>
</head>
<body bgcolor="White">
<H2>WBEM Services</H2>

<h3>CIM and WBEM Services Class Detail</h3>

</body>
</html>
</redirect:write>



<xsl:variable name="schemafile">schema.html</xsl:variable>
<redirect:write select="$schemafile">
<html>
<head>
<title>WBEM Services</title>
</head>
<body bgcolor="White">
<H1>WBEM Services</H1>
<a href="allclasses.html" target="allclassesframe">All Classes</a>
<BR></BR>
<a href="allclasses.html#associations" target="allclassesframe">Association Classes</a>
<!--<BR></BR>
<a href="allclasses.html#providers" target="allclassesframe">Provider Classes</a>-->

</body>
</html>
</redirect:write>

<!--<xsl:variable name="schemafile">schema.html</xsl:variable>
<redirect:write select="$schemafile">
<html>
<head>
<title>WBEM Services</title>
</head>
<body bgcolor="White">
<H1>WBEM Services</H1>
<a href="allclasses.html" target="allclassesframe">All Classes</a>
<H2>Namespaces</H2>
<BR></BR>
<a href="cimv2classes.html" target="allclassesframe">\root\cimv2</a>
<BR></BR>
<a href="secclasses.html" target="allclassesframe">\root\security</a>
<BR></BR>
<a href="sysclasses.html" target="allclassesframe">\root\system</a>
</body>
</html>
</redirect:write>-->

<!--Comment out namespace processing
<xsl:variable name="nsfile">namespaces.html</xsl:variable>
<redirect:write select="$nsfile">
<html>
<head>
<title>Namespaces</title>
</head>
<body bgcolor="White">
<H1>Namespaces</H1>
<TABLE BORDER="3" CELLPADDING="6" WIDTH="90%">
<tr>
<TD VALIGN="TOP"><B>Namespace</B></TD>
<TD VALIGN="TOP"><B>Description</B></TD>
</tr>
<tr>
<td><a href="qualifiers.html" title="View Qualifier Types">\root\cimv2</a></td>
<td>Default CIM Classes that represent objects on your system. This is the default namespace.</td>
</tr>
<tr>
<td><a href="secclasses.html">\root\security</a></td>
<td>Security classes used by the CIM Object Manager to determine access rights on individual namespaces or for a user-namespace combination.</td>
</tr>
<tr>
<td><a href="sysclasses.html">\root\system</a></td>
<td>Classes that represent system properties.</td>
</tr>
</TABLE>
<p></p>
<hr></hr>
</body>
</html>
</redirect:write>-->


    <xsl:apply-templates/>
   </body>
  </html>
</xsl:template>


</xsl:stylesheet>
