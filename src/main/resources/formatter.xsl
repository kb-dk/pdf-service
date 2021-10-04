<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:ns="http://www.loc.gov/zing/srw/"
                xmlns:marc="http://www.loc.gov/MARC21/slim"
                xmlns:str="http://exslt.org/strings"
                xmlns:date="http://exslt.org/dates-and-times"
                exclude-result-prefixes="fo" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://www.w3.org/1999/XSL/Format https://svn.apache.org/viewvc/xmlgraphics/fop/trunk/fop/src/foschema/fop.xsd?view=co">
    <xsl:import href="http://exslt.org/str/functions/tokenize/str.tokenize.function.xsl"/>
    <xsl:import href="http://exslt.org/date/functions/date/date.date.function.xsl"/>

    <!--TODO TEST OF INPUT VARS FOR THE XSLT FOP TRANSFORMATION-->
    <xsl:param name="input1"/>

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

    <xsl:template match="/ns:searchRetrieveResponse">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">


            <xsl:variable name="tag100a"
                          select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='100']/marc:subfield[@code='a']"/>

            <xsl:variable name="tag245a"
                          select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='a']"/>
            <xsl:variable name="tag245b"
                          select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='b']"/>
            <xsl:variable name="tag245c"
                          select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='c']"/>

            <xsl:variable name="tag246a"
                          select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='246']/marc:subfield[@code='a']"/>

            <xsl:variable name="tag250a"
                          select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='250']/marc:subfield[@code='a']"/>

            <xsl:variable name="tag260a"
                          select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='a']"/>
            <xsl:variable name="tag260b"
                          select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='b']"/>
            <xsl:variable name="tag260c"
                          select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='c']"/>

            <xsl:variable name="tag300a"
                          select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='300']/marc:subfield[@code='a']"/>

            <xsl:variable name="tag700a"
                          select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='700']/marc:subfield[@code='a']"/>
            <xsl:variable name="tag700c"
                          select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='700']/marc:subfield[@code='c']"/>
            <xsl:variable name="tag700d"
                          select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='700']/marc:subfield[@code='d']"/>


            <fo:layout-master-set>
                <fo:simple-page-master master-name="simpleA4" page-height="29.7cm" page-width="21cm" margin-top="2cm"
                                       margin-bottom="2cm" margin-left="2cm" margin-right="2cm">
                    <fo:region-body/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="simpleA4">
                <fo:flow flow-name="xsl-region-body">
                    <fo:block>TODO TEST OF INPUT VARS FOR THE XSLT FOP TRANSFORMATION
                        <xsl:value-of select="$input1"/>
                    </fo:block>
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

                                            <xsl:choose>
                                                <xsl:when
                                                        test="$tag100a = '' and $tag700a = '' and $tag700c = '' and $tag245c = ''">
                                                    <xsl:message>
                                                        []
                                                    </xsl:message>
                                                </xsl:when>
                                                <xsl:when test="$tag100a">
                                                    <xsl:value-of select="$tag100a"/>
                                                </xsl:when>
                                                <xsl:when test="$tag700a">
                                                    <xsl:value-of select="$tag700a"/>
                                                    <xsl:value-of select="$tag700d"/>
                                                </xsl:when>
                                                <xsl:when test="$tag245c">
                                                    <xsl:value-of select="$tag245c"/>
                                                </xsl:when>
                                            </xsl:choose>
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
                                            <xsl:value-of select="$tag245a"/>
                                            <xsl:value-of select="$tag245b"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>

                                <xsl:if test="$tag246a !=''"> <!--Alternativ Titel-->
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
                                                        select="$tag246a"/>
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </xsl:if>


                                <xsl:if test="$tag250a != ''">
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
                                                <xsl:value-of select="$tag250a"/>
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
                                            <xsl:choose>
                                                <xsl:when test="$tag260a != ''">
                                                    <xsl:value-of
                                                            select="$tag260a"/>
                                                    <xsl:value-of
                                                            select="$tag260b"/>
                                                    <xsl:value-of
                                                            select="$tag260c"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:for-each
                                                            select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='500']/marc:subfield[@code='a']">
                                                        <xsl:variable name="p500a"
                                                                      select="str:tokenize(normalize-space(.), ' ')"/>
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
                                            <xsl:value-of select="$tag300a"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>

                            </fo:table-body>
                        </fo:table>
                    </fo:block>

                    <fo:block>

                        <xsl:choose>
                            <xsl:when test="($tag260c != '')">
                                <xsl:value-of select="$tag260c"/>  -->

                                <!--Today as a string without -. I.e. 2021-07-27+02:00 becomes 20210727-->
                                <xsl:variable name="today" select="substring(translate(date:date(), '-', ''),1,8)"/>
                                <!--$tag260c[2] is the premiere date-->
                                <!--It might just be a year, or it might be a full date-->
                                <!--Either way, we add -01-01, remove all non-num chars and take the first 8 chars-->
                                <!--So 1993 becomes 19930101-->
                                <xsl:variable name="cutoff"
                                              select="substring(translate(concat($tag260c, '-01-01'),'-.',''),1,8)"/>
                                <!--<xsl:value-of select="cutoff" />-->
                                <!--1400000 is a hundred and fourty years. So we test if today is before the premiere date + 140 years-->
                                <xsl:if test="$today &lt;= ($cutoff+$cutoff-span)">
                                    <!-- today is before cutoff -->
                                    <xsl:copy-of select="$dk-fixed-text"/>
                                    <xsl:copy-of select="$uk-fixed-text"/>
                                </xsl:if>
                                <xsl:if test="$today &gt;= ($cutoff+$cutoff-span)">
                                    <!--today is after cutoff-->
                                    <xsl:copy-of select="$dk-after-cutoff-text"/>
                                    <xsl:copy-of select="$uk-after-cutoff-text"/>
                                </xsl:if>
                            </xsl:when>
                            <xsl:otherwise>
                                <fo:block>
                                    <xsl:for-each
                                            select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='500']/marc:subfield[@code='a']">
                                        <xsl:if test="starts-with(.,'Premiere')">
                                            <xsl:variable name="p500b" select="str:tokenize(normalize-space(.), ' ')"/>
                                            <fo:block>
                                                <!--Today as a string without -. I.e. 2021-07-27+02:00 becomes 20210727-->
                                                <xsl:variable name="today"
                                                              select="substring(translate(date:date(), '-', ''),1,8)"/>
                                                <!--$p500b[2] is the premiere date-->
                                                <!--It might just be a year, or it might be a full date-->
                                                <!--Either way, we add -01-01, remove all non-num chars and take the first 8 chars-->
                                                <!--So 1993 becomes 19930101-->
                                                <xsl:variable name="cutoff"
                                                              select="substring(translate(concat($p500b[2], '-01-01'),'- ',''),1,8)"/>
                                                <!--1400000 is a hundred and fourty years. So we test if today is before the premiere date + 140 years-->
                                                <xsl:if test="$today &lt;= ($cutoff+$cutoff-span)">
                                                    <!-- today is before cutoff -->
                                                    <xsl:copy-of select="$dk-fixed-text"/>
                                                    <xsl:copy-of select="$uk-fixed-text"/>
                                                </xsl:if>
                                                <xsl:if test="$today &gt;= ($cutoff+$cutoff-span)">
                                                    <!--today is after cutoff-->
                                                    <xsl:copy-of select="$dk-after-cutoff-text"/>
                                                    <xsl:copy-of select="$uk-after-cutoff-text"/>
                                                </xsl:if>
                                            </fo:block>
                                            <fo:block>
                                            </fo:block>
                                        </xsl:if>
                                    </xsl:for-each>
                                </fo:block>
                            </xsl:otherwise>
                        </xsl:choose>
                    </fo:block>

                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
</xsl:stylesheet>

