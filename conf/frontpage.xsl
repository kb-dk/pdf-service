<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                exclude-result-prefixes="fo xs"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="
                http://www.w3.org/1999/XSL/Format https://svn.apache.org/viewvc/xmlgraphics/fop/trunk/fop/src/foschema/fop.xsd?view=co
                http://www.w3.org/2001/XMLSchema http://www.w3.org/2001/XMLSchema.xsd">

    <xsl:param name="authors" as="xs:string"/>
    <xsl:param name="title" as="xs:string"/>
    <xsl:param name="altTitle" as="xs:string"/>
    <xsl:param name="edition" as="xs:string"/>
    <xsl:param name="place" as="xs:string"/>
    <xsl:param name="size" as="xs:string"/>
    <xsl:param name="documentType" as="xs:string"/>


    <!--See https://sbprojects.statsbiblioteket.dk/display/DK/Record+Typer-->

    <xsl:variable name="typeA-text">
        <fo:block font-size="18pt" font-weight="bold"
                  space-after="5mm" text-align="center" line-height="15px">
            DK
        </fo:block>
        <fo:block text-align="left" font-size="14pt">
            Værket kan være ophavsretligt beskyttet, og så må du kun bruge PDF-filen til personlig brug. Hvis
            ophavsmanden er død for mere end 70 år siden, er værket fri af ophavsret (public domain), og så kan du bruge
            værket frit. Hvis der er flere ophavsmænd, gælder den længstlevendes dødsår. Husk altid at kreditere
            ophavsmanden.
        </fo:block>
        <fo:block space-after="10mm"/>
        <fo:block font-size="18pt" font-weight="bold"
                  space-after="5mm" text-align="center" line-height="15px">
            UK
        </fo:block>
        <fo:block text-align="left" font-size="14pt">
            The work may be copyrighted in which case the PDF file may only be used for personal use. If the author died
            more than 70 years ago, the work becomes public domain and can then be freely used. If there are several
            authors, the year of death of the longest living person applies. Always remember to credit the author
        </fo:block>

    </xsl:variable>
    <xsl:variable name="typeC-text">
        <fo:block font-size="18pt" font-weight="bold"
                  space-after="5mm" text-align="center" line-height="15px">
            DK
        </fo:block>
        <fo:block text-align="left" font-size="14pt">
            Dette manuskript er ophavsretligt beskyttet, og må kun benyttes til personlig brug. Du må dog også bruge
            manuskriptet i forbindelse med optagelsesprøve på de danske teaterskoler.
        </fo:block>
        <fo:block text-align="left" font-size="14pt">
            Hvis du vil opføre manuskriptet, skal du have samtykke fra rettighedshaveren. Du kan i den forbindelse
            kontakte fagorganisationen Danske Dramatikere.
        </fo:block>
        <fo:block text-align="left" font-size="14pt">
            Husk altid at kreditere ophavsmanden.
        </fo:block>
        <fo:block space-after="10mm"/>
        <fo:block font-size="18pt" font-weight="bold"
                  space-after="5mm" text-align="center" line-height="15px">
            UK
        </fo:block>
        <fo:block text-align="left" font-size="14pt">
            This manuscript is copyright protected and may only be used for personal use. However, you may also use the
            manuscript in connection with the entrance exam at the Danish theater schools.
        </fo:block>
        <fo:block text-align="left" font-size="14pt">
            If you want to perform the manuscript publicly, you must have the consent of the copyright holder. In this
            connection, you can contact the Danish Writers Guild.
        </fo:block>
        <fo:block text-align="left" font-size="14pt">
            Always remember to credit the author.
        </fo:block>
    </xsl:variable>

    <xsl:variable name="typeB-text">
        <fo:block font-size="18pt" font-weight="bold"
                  space-after="5mm" text-align="center" line-height="15px">
            DK
        </fo:block>
        <fo:block text-align="left" font-size="14pt">
            Materialet er fri af ophavsret. Du kan kopiere, ændre, distribuere eller fremføre værket, også til
            kommercielle formål, uden at bede om tilladelse. Husk altid at kreditere ophavsmanden.
        </fo:block>
        <fo:block space-after="10mm"/>
        <fo:block font-size="18pt" font-weight="bold"
                  space-after="5mm" text-align="center" line-height="15px">
            UK
        </fo:block>
        <fo:block text-align="left" font-size="14pt">
            The work is free of copyright. You can copy, change, distribute or present the work, even for commercial
            purposes, without asking for permission. Always remember to credit the author.
        </fo:block>
        <fo:block text-align="center" space-before="3cm">
            <fo:basic-link external-destination="https://creativecommons.org/publicdomain/mark/1.0/deed.da">
                <fo:external-graphic border-width="thick"
                                     border="none"
                                     content-width="scale-to-fit"
                                     content-height="30pt"
                                     scaling="uniform"
                                     src="publicdomain.svg"/>
            </fo:basic-link>
        </fo:block>
    </xsl:variable>


    <xsl:template match="/">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">

            <fo:layout-master-set>
                <fo:simple-page-master master-name="simpleA4" page-height="29.7cm" page-width="21cm" margin-top="2cm"
                                       margin-bottom="2cm" margin-left="2cm" margin-right="2cm">
                    <fo:region-body/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="simpleA4">
                <fo:flow flow-name="xsl-region-body">
                    <fo:block font-size="24pt" font-weight="bold" space-after="5mm" text-align="center">
                        Digitaliseret af | Digitised by
                    </fo:block>

                    <fo:block space-after="-10mm">
                        <fo:external-graphic border-width="thin"
                                             content-width="scale-to-fit"
                                             content-height="200pt"
                                             width="100%"
                                             height="270pt"
                                             scaling="uniform"
                                             text-align="center"
                                             src="KBlogo.png"/>
                    </fo:block>


                    <fo:block font-size="10px">
                        <fo:table table-layout="fixed" width="100%" border-collapse="separate">
                            <fo:table-column column-width="8cm"/>
                            <fo:table-column column-width="9cm"/>
                            <fo:table-body>
                                <fo:table-row> <!--Forfattere-->
                                    <fo:table-cell border="solid 0px black" text-align="left" font-weight="normal">
                                        <fo:block>
                                            Forfatter(e) | Author(s):
                                        </fo:block>
                                    </fo:table-cell>

                                    <fo:table-cell border="solid 0px black" text-align="left" font-weight="normal">
                                        <fo:block>
                                            <xsl:value-of select="$authors"/>
                                        </fo:block>
                                    </fo:table-cell>

                                </fo:table-row>


                                <fo:table-row> <!--Titel-->
                                    <fo:table-cell border="solid 0px black"
                                                   text-align="left" font-weight="normal">
                                        <fo:block>
                                            Titel | Title:
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell border="solid 0px black"
                                                   text-align="left" font-weight="normal">
                                        <fo:block>
                                            <xsl:value-of select="$title"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>

                                <xsl:if test="$altTitle !=''"> <!--Alternativ Titel-->
                                    <fo:table-row>
                                        <fo:table-cell border="solid 0px black"
                                                       text-align="left" font-weight="normal">
                                            <fo:block>
                                                Alternativ titel | Alternative title:
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell border="solid 0px black"
                                                       text-align="left" font-weight="normal">
                                            <fo:block>
                                                <xsl:value-of select="$altTitle"/>
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </xsl:if>


                                <xsl:if test="$edition != ''">
                                    <fo:table-row>
                                        <fo:table-cell border="solid 0px black"
                                                       text-align="left" font-weight="normal">
                                            <fo:block>
                                                Udgavebetegnelse | Edition Statement:
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell border="solid 0px black"
                                                       text-align="left" font-weight="normal">
                                            <fo:block>
                                                <xsl:value-of select="$edition"/>
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </xsl:if>

                                <fo:table-row>
                                    <fo:table-cell border="solid 0px black"
                                                   text-align="left" font-weight="normal">
                                        <fo:block>
                                            Udgivet år og sted | Publication time and place:
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell border="solid 0px black"
                                                   text-align="left" font-weight="normal">
                                        <fo:block>
                                            <xsl:value-of select="$place"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>


                                <fo:table-row>
                                    <fo:table-cell border="solid 0px black"
                                                   text-align="left" font-weight="normal">
                                        <fo:block>
                                            Fysiske størrelse | Physical extent:
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell border="solid 0px black"
                                                   text-align="left" font-weight="normal">
                                        <fo:block>
                                            <xsl:value-of select="$size"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>

                            </fo:table-body>
                        </fo:table>
                    </fo:block>

                    <fo:block space-after="10mm"/>

                    <fo:block>
                        <xsl:choose>
                            <xsl:when test="$documentType='A'">
                                <xsl:copy-of select="$typeA-text"/>
                            </xsl:when>
                            <xsl:when test="$documentType='B'">
                                <xsl:copy-of select="$typeB-text"/>
                            </xsl:when>
                            <xsl:when test="$documentType='C'">
                                <xsl:copy-of select="$typeC-text"/>
                            </xsl:when>
                            <xsl:otherwise>
                            </xsl:otherwise>
                        </xsl:choose>
                    </fo:block>

                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
</xsl:stylesheet>

