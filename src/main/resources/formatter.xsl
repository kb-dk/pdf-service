<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="https://www.w3.org/2001/XMLSchema"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                exclude-result-prefixes="fo xs" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://www.w3.org/1999/XSL/Format https://svn.apache.org/viewvc/xmlgraphics/fop/trunk/fop/src/foschema/fop.xsd?view=co">

    <xsl:param name="authors" as="xs:string"/>
    <xsl:param name="title" as="xs:string"/>
    <xsl:param name="altTitle" as="xs:string"/>
    <xsl:param name="edition" as="xs:string"/>
    <xsl:param name="place" as="xs:string"/>
    <xsl:param name="size" as="xs:string"/>
    <xsl:param name="isWithinCopyright" as="xs:boolean"/>

    <xsl:variable name="dk-fixed-text">
        <fo:block font-size="16pt" font-weight="bold" font-style="italic" space-before="15mm"
                  space-after="5mm" text-align="center" line-height="15px">DK
        </fo:block>
        <fo:block text-align="left" font-style="italic" font-size="10pt">Dette manuskript kan være
            ophavsretligt beskyttet. Den ophavsretlige beskyttelsestid
            er 70 år efter ophavsmandens død. Eventuelle oversættere har ophavsrettigheder til den
            oversatte version af manuskriptet. Værker hvor ophavsretten er
            udløbet er fri af ophavsret.
            <fo:block space-after="2mm"/>
            Hvis manuskriptet er ophavsretligt beskyttet må det kun benyttes til privat brug. Du må
            dog også benytte manuskriptet i forbindelse med optagelsesprøve
            på de danske teaterskoler.
        </fo:block>
        <fo:block text-align="left" font-style="italic" font-size="10pt">
            Hvis du vil opføre manuskriptet, skal du have samtykke fra rettighedshaveren. Du kan i
            den forbindelse kontakte rettighedsorganisationen Danske Dramatikere.
        </fo:block>
        <fo:block space-after="2mm"/>
    </xsl:variable>

    <xsl:variable name="uk-fixed-text">
        <fo:block font-size="16pt" font-weight="bold" font-style="italic" space-before="15mm"
                  space-after="5mm" text-align="center" line-height="15px">UK
        </fo:block>
        <fo:block text-align="left" font-style="italic" font-size="10pt">
            This manuscript may be copyrighted. The copyright protection period is 70 years after
            the death of the author. Any translators have copyright to the translated version of
            the script. Works where the copyright has expired are free of copyright.
            <fo:block space-after="2mm"/>
            If the manuscript is copyrighted, it may only be used for private use. However, you may
            also use
            the manuscript in connection with the entrance examination at the Danish theater
            schools.
            <fo:block space-after="2mm"/>
            If you want to write the manuscript, you must have the consent of the copyright holder.
            You can in that regard
            contact the rights organization Danske Dramatikere.
        </fo:block>
    </xsl:variable>

    <xsl:variable name="dk-after-cutoff-text">
        Værket er fri af ophavsret. Husk altid at kreditere ophavsmanden, selvom værket er fri af
        ophavsret.
    </xsl:variable>

    <xsl:variable name="uk-after-cutoff-text">
        The work is free of copyright. Always remember to credit the author, even if the work is
        free of copyright.
    </xsl:variable>

    <xsl:variable name="cutoff-span">
        1400000
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
                    <fo:block>
                        <fo:external-graphic src="src/main/resources/images/KBlogo.png" height="270pt"
                                             border-width="thin" content-width="scale-to-fit"
                                             content-height="200pt" width="100%" scaling="uniform" text-align="center">
                        </fo:external-graphic>
                    </fo:block>

                    <fo:block font-size="24pt" font-weight="bold" space-after="5mm" text-align="center">
                        Digitaliseret af | Digitised by
                    </fo:block>

                    <fo:block font-size="10px" space-after="10mm">
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
                                                <xsl:value-of
                                                        select="$altTitle"/>
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
                                            Forlæggets fysiske størrelse | Physical extent of source:
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

                    <fo:block>
                        <xsl:choose>
                            <xsl:when test="$isWithinCopyright">
                                    <xsl:copy-of select="$dk-fixed-text"/>
                                    <xsl:copy-of select="$uk-fixed-text"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <!--today is after cutoff-->
                                <xsl:copy-of select="$dk-after-cutoff-text"/>
                                <xsl:copy-of select="$uk-after-cutoff-text"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </fo:block>

                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
</xsl:stylesheet>

