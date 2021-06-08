<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:ns="http://www.loc.gov/zing/srw/"
                xmlns:marc="http://www.loc.gov/MARC21/slim"
                xmlns:str="http://exslt.org/strings"

                exclude-result-prefixes="fo">
    <xsl:import href="http://exslt.org/str/functions/tokenize/str.tokenize.function.xsl"/>
    <xsl:template match="/ns:searchRetrieveResponse">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="simpleA4" page-height="29.7cm" page-width="21cm" margin-top="2cm" margin-bottom="2cm" margin-left="2cm" margin-right="2cm">
                    <fo:region-body/>
                    <!--
                    <fo:region-after region-name="my_footer"
                                     background-color="#ffeeee" extent="1.5cm"/>
                                     -->
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="simpleA4">
                <!--
                <fo:static-content flow-name="my_footer">
                    <fo:table font-size="9pt"
                              border-top-width="1px" border-top-style="solid">
                        <fo:table-body>
                            <fo:table-row>
                                <fo:table-cell>
                                    <fo:block >Tale 53</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block text-align="center">Page <fo:page-number/></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block text-align="end">by A. Zick</fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                        </fo:table-body>
                    </fo:table>
                </fo:static-content>
                -->

                <fo:flow flow-name="xsl-region-body">
                    <fo:block>
                        <fo:external-graphic src="src/main/resources/images/KBlogo.png"  height="270pt"
                                             border-width="thin" content-width="scale-to-fit"
                                             content-height="200pt" width="100%" scaling="uniform" text-align="center">
                        </fo:external-graphic>
                        <!--
                        <fo-external-graphic width="311pt" height="386pt" overflow="hidden" src="file://$PROJECT_DIR$/src/main/resources/images/KBlogo.png" />
                        -->
                        <!--
                        <fo-external-graphic content-height="38.0mm" content-width="31.1mm" scaling="non-uniform" src="@src">
                            <attribute name="src">
                                <xsl-value-of select='url("src/main/resources/KBlogo.png")'/>
                            </attribute>
                        </fo-external-graphic>
                        -->
                    </fo:block>
                   <!--  <fo:block text-align="center">
                        <img src="src/main/resources/KBlogo.png" width="311" height="380" align="middle"/>
                    </fo:block>
                    -->
                    <fo:block font-size="24pt" font-weight="bold"  space-after="5mm" text-align="center">
                        Digitaliseret af | Digitised by
                    </fo:block>
                    <fo:block font-size="10px">
                        <fo:table table-layout="fixed" width="100%" border-collapse="separate" >
                            <fo:table-column column-width="9cm" />
                            <fo:table-column column-width="8cm" />

                            <fo:table-body>
                                <fo:table-row>
                                    <fo:table-cell border="solid 0px black"
                                                   text-align="left" font-weight="normal">
                                        <fo:block>
                                            Forfatter(e) | Authors:
                                        </fo:block>
                                    </fo:table-cell>

                                    <fo:table-cell border="solid 0px black"
                                                   text-align="left" font-weight="normal">
                                        <fo:block>
                                            <xsl:variable name="tag100a"
                                                          select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='100']/marc:subfield[@code='a']"/>
                                            <xsl:variable name="tag700a"
                                                          select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='700']/marc:subfield[@code='a']"/>
                                            <xsl:variable name="tag245c"
                                                          select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='c']"/>
                                            <xsl:choose>
                                                <xsl:when test="@tag100a = '' and @tag700a = '' and @tag700c = '' and @tag245c = ''">
                                                    <xsl:message>
                                                        []
                                                    </xsl:message>
                                                </xsl:when>
                                                <xsl:when test="$tag100a" >
                                                    <xsl:value-of select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='100']/marc:subfield[@code='a']"/>
                                                </xsl:when>
                                                <xsl:when  test="$tag700a">
                                                    <xsl:value-of select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='700']/marc:subfield[@code='a']"/>
                                                    <xsl:value-of select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='700']/marc:subfield[@code='d']"/>
                                                </xsl:when>
                                                <xsl:when  test="$tag245c">
                                                    <xsl:value-of select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='c']"/>
                                                </xsl:when>
                                            </xsl:choose>
                                        </fo:block>
                                    </fo:table-cell>

                                </fo:table-row>


                                <fo:table-row>
                                    <fo:table-cell border="solid 0px black"
                                            text-align="left" font-weight="normal">
                                        <fo:block>
                                            Titel | Title:
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell border="solid 0px black"
                                                   text-align="left" font-weight="normal">
                                        <fo:block>
                                            <xsl:value-of select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='a']"/>
                                            <xsl:value-of select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='b']"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>


                                <fo:table-row>
                                    <fo:table-cell border="solid 0px black"
                                                   text-align="left" font-weight="normal">
                                        <fo:block>
                                            <xsl:if test="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='246']/marc:subfield[@code='a'] !=''">
                                                Alternativ titel | Alternative title:
                                            </xsl:if>
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell border="solid 0px black"
                                                   text-align="left" font-weight="normal">
                                        <fo:block>
                                            <xsl:value-of select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='246']/marc:subfield[@code='a']"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>


                                <fo:table-row>
                                    <fo:table-cell border="solid 0px black"
                                                   text-align="left" font-weight="normal">
                                        <fo:block>
                                            <xsl:if test="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='250']/marc:subfield[@code='a'] != ''">
                                                Udgavebetegnelse | Edition Statement:
                                            </xsl:if>
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell border="solid 0px black"
                                                   text-align="left" font-weight="normal">
                                        <fo:block>
                                            <xsl:value-of select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='250']/marc:subfield[@code='a']"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>


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
                                            <xsl:variable name="tag260"
                                                          select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='a']"/>
                                            <xsl:choose>
                                                <xsl:when test="$tag260 != ''">
                                                    <xsl:value-of select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='a']"/>
                                                    <xsl:value-of select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='b']"/>
                                                    <xsl:value-of select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='c']"/>
                                                </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:for-each select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='500']/marc:subfield[@code='a']">
                            <!--                <xsl:value-of select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='500']/marc:subfield[@code='a']"/>  <-->
                                                    <xsl:variable name="p500a" select="str:tokenize(normalize-space(.), ' ')"/>
                                                    <xsl:if test="starts-with(.,'Premiere')">
                                                        <xsl:value-of select="concat($p500a[1],' ', $p500a[2])"/>
                                                    </xsl:if>
                                                </xsl:for-each>
                                            </xsl:otherwise>
                                            </xsl:choose>
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
                                            <xsl:value-of select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='300']/marc:subfield[@code='a']"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>

                            </fo:table-body>
                        </fo:table>
                        </fo:block>
                        <fo:block font-size="16pt" font-weight="bold" font-style="italic" space-before="15mm" space-after="5mm" text-align="center" line-height="15px">DK
                        </fo:block>
                        <fo:block text-align="left" font-style="italic" font-size="10pt">
                            Dette manuskript kan være ophavsretligt beskyttet. Den ophavsretlige beskyttelsestid er 70 år efter ophavsmandens død. Eventuelle oversættere
                            har ophavsrettigheder til den oversatte version af manuskriptet. Værker hvor ophavsretten er udløbet er fri af ophavsret
                        </fo:block><fo:block space-after="2mm"/>
                        <fo:block text-align="left" font-style="italic" font-size="10pt">
                            Hvis manuskriptet er ophavsretligt beskyttet, må det kun anvendes til privat brug. Du må dog også bruge manuskriptet i forbindelse med optagelsesprøve på de danske teaterskoler.
                        </fo:block><fo:block space-after="2mm"/>
                        <fo:block/>
                        <fo:block text-align="left" font-style="italic" font-size="10pt">
                            Hvis du vil opføre manuskriptet, skal du have samtykke fra rettighedshaveren. Du kan i den forbindelse  kontakte rettighedsorganisationen Danske Dramatikere.
                        </fo:block>
                        <fo:block font-size="16pt" font-weight="bold" font-style="italic" space-before="15mm" space-after="5mm" text-align="center" line-height="15px">UK
                        </fo:block>
                        <fo:block text-align="left" font-style="italic" font-size="10pt">
                            This manuscript may be copyrighted. The copyright protection period is 70 years after
                            the death of the author. Any translators have copyright to the translated version of
                            the script. Works where the copyright has expired are free of copyright.<fo:block space-after="2mm"/>
                            If the manuscript is copyrighted, it may only be used for private use. However, you may also use
                            the manuscript in connection with the entrance examination at the Danish theater schools.<fo:block space-after="2mm"/>
                            If you want to write the manuscript, you must have the consent of the copyright holder. You can in that regard
                            contact the rights organization Danske Dramatikere.
                        </fo:block>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
</xsl:stylesheet>

