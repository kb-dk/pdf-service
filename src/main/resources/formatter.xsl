<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:ns="http://www.loc.gov/zing/srw/"
                xmlns:marc="http://www.loc.gov/MARC21/slim"
                exclude-result-prefixes="fo">
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
                        <fo:external-graphic src="src/main/resources/images/KBlogo.png"  height="300pt"
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
                                                 <xsl:value-of select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='700']/marc:subfield[@code='a']"/>
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
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>


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
                                            <xsl:value-of select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='505']/marc:subfield[@code='a']"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>


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
                                            <xsl:value-of select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='a']"/>
                                            <xsl:value-of select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='b']"/>
                                            <xsl:value-of select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='c']"/>
                                            <xsl:value-of select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='500']/marc:subfield[@code='a']"/>
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

                                <xsl:for-each select="records/record/recordData/record/datafield[@tag=700]/subfield">
                                        <fo:table-cell border="solid 1px black" text-align="center">
                                            <fo:block>
                                                <xsl:attribute name="code">
                                                    <xsl-value-of select="@code='a'"/>
                                                </xsl:attribute>
                                            </fo:block>
                                        </fo:table-cell>
                                </xsl:for-each>
                            </fo:table-body>
                        </fo:table>
                     </fo:block>
                    <fo:block font-size="16pt" font-weight="bold" space-before="15mm" space-after="5mm" text-align="center" line-height="15px" >DK
                    </fo:block>
                    <fo:block text-align="left">
                        Værket kan være ophavsretligt beskyttet, og så må du kun bruge PDF-filen til personlig brug. Hvis ophavsmanden er død for
                        mere end 70 år siden, er værket fri af ophavsret (public domain), og så kan du bruge værket frit. Hvis der er flere ophavsmænd,
                        gælder den længstlevendes dødsår. Husk altid at kreditere ophavsmanden
                    </fo:block>
                    <fo:block font-size="16pt" font-weight="bold" space-before="15mm" space-after="5mm" text-align="center" line-height="15px" >UK
                    </fo:block>
                    <fo:block text-align="left">
                        The work may be copyrighted in which case the PDF file may only be used for personal use. If the author died more than 70
                        years ago, the work becomes public domain and can then be freely used. If there are several authors, the year of death of
                        the longest living person applies. Always remember to credit the author
                    </fo:block>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
</xsl:stylesheet>

