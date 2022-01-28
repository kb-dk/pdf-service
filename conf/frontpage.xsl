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

    <!--    TODO ensure this stays on one page
    See http://localhost:8080/pdf-service/api/getPdf/130019369456-color.pdf
    -->
    <!--TODO algorithm to ensure max number of lines is not exceeded-->
    <xsl:param name="authors" as="xs:string"/>
    <xsl:param name="title" as="xs:string"/>
    <xsl:param name="altTitle" as="xs:string"/>
    <xsl:param name="edition" as="xs:string"/>
    <xsl:param name="placeAndYear" as="xs:string"/>
    <xsl:param name="size" as="xs:string"/>

    <!--DocumentType can be one of A,B,C,D,E,.... Currently only A,B,C is used-->
    <xsl:param name="documentType" as="xs:string"/>

    <xsl:param name="volume" as="xs:string"/>


    <xsl:param name="metadataTableFont" as="xs:string"/>
    <xsl:param name="metadataTableFontSize" as="xs:string"/>
    <xsl:param name="metadataTableWidth" as="xs:string"/>


    <!--See https://sbprojects.statsbiblioteket.dk/display/DK/Record+Typer-->

    <xsl:variable name="typeA-text">
        <fo:block font-size="18pt" font-weight="bold"
                  space-after="5mm" text-align="center" line-height="15px">
            DK - (A)
        </fo:block>
        <fo:block text-align="left" font-size="14pt" keep-with-previous="always">
            Værket kan være ophavsretligt beskyttet, og så må du kun bruge PDF-filen til personlig brug. Hvis
            ophavsmanden er død for mere end 70 år siden, er værket fri af ophavsret (public domain), og så kan du bruge
            værket frit. Hvis der er flere ophavsmænd, gælder den længstlevendes dødsår. Husk altid at kreditere
            ophavsmanden.
        </fo:block>
        <fo:block space-after="10mm" keep-with-previous="always"/>
        <fo:block font-size="18pt" font-weight="bold"
                  space-after="5mm" text-align="center" line-height="15px" keep-with-previous="always">
            UK - (A)
        </fo:block>
        <fo:block text-align="left" font-size="14pt" keep-with-previous="always">
            The work may be copyrighted in which case the PDF file may only be used for personal use. If the author died
            more than 70 years ago, the work becomes public domain and can then be freely used. If there are several
            authors, the year of death of the longest living person applies. Always remember to credit the author
        </fo:block>
    </xsl:variable>


    <xsl:variable name="typeB-text">
        <fo:block font-size="18pt" font-weight="bold"
                  space-after="5mm" text-align="center" line-height="15px">
            DK - (B)
        </fo:block>
        <fo:block text-align="left" font-size="14pt" keep-with-previous="always">
            Materialet er fri af ophavsret. Du kan kopiere, ændre, distribuere eller fremføre værket, også til
            kommercielle formål, uden at bede om tilladelse. Husk altid at kreditere ophavsmanden.
        </fo:block>
        <fo:block space-after="10mm" keep-with-previous="always"/>
        <fo:block font-size="18pt" font-weight="bold"
                  space-after="5mm" text-align="center" line-height="15px" keep-with-previous="always">
            UK - (B)
        </fo:block>
        <fo:block text-align="left" font-size="14pt" keep-with-previous="always">
            The work is free of copyright. You can copy, change, distribute or present the work, even for commercial
            purposes, without asking for permission. Always remember to credit the author.
        </fo:block>
        <!-- this makes the picture align to the bottom of the page: https://stackoverflow.com/a/55305033/4527948-->
        <fo:block text-align="center" keep-with-previous="always"
                  space-before.minimum="1cm"
                  space-before.optimum="30cm"
                  space-before.maximum="30cm">
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


    <xsl:variable name="typeC-text">
        <fo:block font-size="18pt" font-weight="bold"
                  space-after="5mm" text-align="center" line-height="15px">
            DK - (C)
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
            UK - (C)
        </fo:block>
        <fo:block text-align="left" font-size="14pt">
            This manuscript is copyright protected and is only for personal use. However, you may also use the
            manuscript in connection with the entrance exam at the Danish theatre schools.
        </fo:block>
        <fo:block text-align="left" font-size="14pt">
            If you want to perform the manuscript, you must have the consent of the copyright holder. In that regard,
            you can contact the Danish Writers Guild.
        </fo:block>
        <fo:block text-align="left" font-size="14pt">
            Always remember to credit the author.
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

                    <fo:block space-after="-10mm" keep-with-previous="always">
                        <fo:external-graphic border-width="thin"
                                             content-width="scale-to-fit"
                                             content-height="200pt"
                                             width="100%"
                                             height="270pt"
                                             scaling="uniform"
                                             text-align="center"
                                             src="KBlogo.png"/>
                    </fo:block>


                    <!--TODO There can be issues if a single word in a non-standard language is longer than the available space
                    See https://stackoverflow.com/a/20918320/4527948
                    And perhaps https://stackoverflow.com/a/29632518/4527948
                    -->
                    <xsl:element name="block" namespace="http://www.w3.org/1999/XSL/Format">
                        <xsl:attribute name="font-size">
                            <xsl:value-of select="$metadataTableFontSize"/>px
                        </xsl:attribute>
                        <!-- https://stackoverflow.com/a/35186571/4527948 -->
                        <!--You can add multiple fonts here, and it will use the first that can represent a character-->
                        <xsl:attribute name="font-family">
                            <xsl:value-of select="$metadataTableFont"/>
                        </xsl:attribute>
                        <xsl:attribute name="keep-with-previous">always</xsl:attribute>


                        <!--                    <fo:block font-size="10px" keep-with-previous="always" font-family="Helvetica">-->
                        <fo:table table-layout="fixed" width="100%" border-collapse="separate">
                            <fo:table-column column-width="8.6cm"/>
                            <xsl:element name="table-column" namespace="http://www.w3.org/1999/XSL/Format">
                                <xsl:attribute name="column-width"><xsl:value-of select="$metadataTableWidth"/>cm</xsl:attribute>
                            </xsl:element>
<!--                            <fo:table-column column-width="9cm"/>-->
                            <fo:table-body keep-together.within-page="always">
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

                                <xsl:if test="$volume != ''">
                                    <fo:table-row>
                                        <fo:table-cell border="solid 0px black"
                                                       text-align="left" font-weight="normal">
                                            <fo:block>
                                                Bindbetegnelse | Volume Statement:
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell border="solid 0px black"
                                                       text-align="left" font-weight="normal">
                                            <fo:block>
                                                <xsl:value-of select="$volume"/>
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </xsl:if>

                                <xsl:if test="$placeAndYear !=''">
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
                                                <xsl:value-of select="$placeAndYear"/>
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </xsl:if>

                                <xsl:if test="$size !=''">
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
                                </xsl:if>

                            </fo:table-body>
                        </fo:table>
                    </xsl:element>

                    <fo:block space-after="10mm" keep-with-previous="always"/>

                    <fo:block keep-with-previous="always">
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

