dod-pdf-service
==================

Tilhørende projekt <https://sbprojects.statsbiblioteket.dk/display/DK/Projektblanket+KULA-170>

Se mere om hvad dette er på
* <https://sbprojects.statsbiblioteket.dk/display/SSYS/Overdragelse+af+DOD+PDF+Service+til+applikationsdrift>

Noget af kravene styres af
* <https://kbintern.sharepoint.com/:w:/s/Afd-Digital-kulturarv/ETQMQ8i4EedLnRLhpvsAMaMBL6qUckzlKMroQpBUClI49Q?e=bfZqU4>
  //TODO håndter de 4 forskellige situationer (ABCD)



Her er sandkasse eksempler på DRALAES fra Anna/Jeppe:

130023138892
130022785800
130007257539
130023745268


Hvorfor:
--------

Ideen er at lave en service, der skal stå i stedet for <http://www5.kb.dk/e-mat/dod/>

Det er den der bruges i ALMA når digitaliserede værker gøres tilgængelig.

Den service jeg har lavet kan

1. Fjerne eventuelle eksisternede forsider
2. Indsætte en moderne og korrekt forside med copyright informationer
3. Indsætte en side-footer med en copyright advarsel

Anvendelse:
-----------
Den tager pdf på helt samme måde som e-mat, dvs.

<http://www5.kb.dk/e-mat/dod/115808025291_bw.pdf>
->
<http://localhost:8080/pdf-service/api/getPdf/115808025307_bw.pdf>

Jeg kan lave den sti om til at være whatever der passer bedst for jer

### fra TGC:

webext-10 er prod host for adgang til e-mat/dod. Al prod adgang til filerne fra www5 mm. ender så vidt jeg ved der (vha
proxy), så den skal ikke peges andre steder hen. For at rokke så lidt med båden som muligt, så er min plan at webext-10
kommer til at køre din applikation i produktion da den allerede har adgang til filerne. Vi retter så eksisterende vhost
på webext-10 til så /e-mat/dod går igennem din applikation i stedet for direkte til disk. Al anden traffik igennem
webext-10 vil være uændret.



Behov:
------
Den benytter sig af tre ting

1. Den læser pdf filerne lokalt, så serveren skal have et mount hvor den kan læse
   `webext-10.kb.dk:/data1/e-mat/dod`

2. Et temp storage hvor den kan lægge producerede PDFer Det er ikke nødvendigt at dynamisk producere hver PDF igen og
   igen, når hverken den originale scanning eller ALMA posten ændrer sig, så jeg cacher producerede PDFer. Dvs. der skal
   være noget HDD plads til det. Man kan konfigurere hvor længe de skal holde osv.

3. En ALMA api key med rettigheder til

* <https://developers.exlibrisgroup.com/alma/apis/bibs/>
  Readonly. Til at slå copyright informationer op for PDF filen

* <https://developers.exlibrisgroup.com/alma/apis/conf/>
  Readonly. Til at logge om den kører mod Sandbox eller Prod

Denne service KAN fungere med ALMA sandbox

Test URLs
==========

Type A
http://localhost:8080/pdf-service/api/getPdf/130021854310-color.pdf
http://devel12.statsbiblioteket.dk:8211/pdf-service/api/getPdf/130021854310-color.pdf
Type B

http://localhost:8080/pdf-service/api/getPdf/130018794536-color.pdf
http://devel12.statsbiblioteket.dk:8211/pdf-service/api/getPdf/130018794536-color.pdf

Test-manus som er afleveret digitalt før GoAnyWhere har vi liggende samlet på pre-ingest. De er alle printede og har
fået fysiske poster. Tue bør afgøre om det gør dem uegnede til denne test. Men posterne er der og ”digitaliseringen” har
fundet sted.

### Orlando:

* <http://localhost:8080/pdf-service/api/getPdf/130018852943.pdf>
* <http://devel12.statsbiblioteket.dk:8211/pdf-service/api/getPdf/130018852943.pdf>
* <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99122277948105763&context=L&vid=45KBDK_KGL:KGL&lang=da>

599 __ |a DRA20110309 |5 dra

### Kjeld & Dirch : - En kærlighedshistorie

* <http://localhost:8080/pdf-service/api/getPdf/130018854342.pdf>
* <http://devel12.statsbiblioteket.dk:8211/pdf-service/api/getPdf/130018854342.pdf>
* <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99122165828205763&context=L&vid=45KBDK_KGL:KGL&lang=da>

599 __ |a DRA20110623 |5 dra

### Lang dags rejse mod nat

* <http://localhost:8080/pdf-service/api/getPdf/130022786122.pdf>
* <http://devel12.statsbiblioteket.dk:8211/pdf-service/api/getPdf/130022786122.pdf>
* <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99122805851105763&context=L&vid=45KBDK_KGL:KGL&lang=da>

595 __ |a gave |5 dra

DOD post
--------
Den navnkundige Engellænders Robinson Crusoe Levnet og meget selsomme Skiebne

* Elektronisk
    * <https://soeg.kb.dk/discovery/fulldisplay?docid=alma995529632805761&context=U&vid=45KBDK_KGL:KGL&lang=da>
* Fysisk
    * <https://soeg.kb.dk/discovery/fulldisplay?docid=alma995057234605761&context=U&vid=45KBDK_KGL:KGL&lang=da>

### Vol1

* <http://www5.kb.dk/e-mat/dod/115808025307_bw.pdf>
* <http://localhost:8080/pdf-service/api/getPdf/115808025307_bw.pdf>
* <http://devel12.statsbiblioteket.dk:8211/pdf-service/api/getPdf/115808025307_bw.pdf>

### Vol2

* <http://www5.kb.dk/e-mat/dod/115808025291_bw.pdf>
* <http://localhost:8080/pdf-service/api/getPdf/115808025291_bw.pdf>
* <http://devel12.statsbiblioteket.dk:8211/pdf-service/api/getPdf/115808025291_bw.pdf>


Roundtrip between Electronic Portfolios and Physical copies
===========================================================

Den post du gav mig var
* barcode 130020357089

Den svarer til 
* MMS ID 995077412905761

I den MMS post er der disse Marc21 felter
```
595	__ |a EMMSID99123898361705763
999	__ |a 1600talsKUM
999	__ |a digitaliseret
```

Dette kan lede mig videre til 
* MMS ID 99123898361705763

Det er den post der har linket til den online udgave
```
856	__ |z Link til elektronisk udgave |u https://www.kb.dk/e-mat/dod/130020357089-color.pdf
```

Og udfra det link kan jeg se barcode og derved kan jeg starte hele møllen igen. Så det hænger sammen :)

Men KUN for de poster der har fået MARC21 595a sat... Og det er ikke alle...

About dynamic urls in portfolio lists
======================================
You can, as detailed
in <https://knowledge.exlibrisgroup.com/Alma/Product_Documentation/010Alma_Online_Help_(English)/Electronic_Resource_Management/051Link_Resolver/030Access_to_Services#section_3>
use dynamic URLs instead of static URLs for Portfolios

This allows you to write rules like

```
IF (rft.issn, rft.year, rft.volume)
http://www.publisher.com/{rft.issn}/{rft.year}/{rft.volume}
IF (rft.issn, rft.year)
http://www.publisher.com/{rft.issn}/{rft.year}
IF (rft.issn) http://www.publisher.com/{rft.issn}
IF ()
http://www.publisher.com
```

The rft.* variables can be found from the CTO info

First see that you can get CTO info about a primo record with the `displayCTO=true` param
<https://soeg.kb.dk/discovery/fulldisplay?docid=alma99122905335805763&context=U&vid=45KBDK_KGL:KGL&lang=da&displayCTO=true>

Then, see guide at
<https://knowledge.exlibrisgroup.com/Alma/Product_Documentation/010Alma_Online_Help_(English)/Electronic_Resource_Management/051Link_Resolver/030Access_to_Services>

The CTO is
```xml
<u:uresolver_content xmlns:u="http://com/exlibris/urm/uresolver/xmlbeans/u">
<u:context_object>
<u:keys>
<u:key id="rft.stitle">Den navnkundige Engellænders Robinson Crusoe Levnet og meget selsomme Skiebne /</u:key>
<u:key id="rft.pub">Ernst Henrich Berling,</u:key>
<u:key id="pnx_doc">{"beaconO22":"1823","context":"L","@id":"https://eu01.alma.exlibrisgroup.com/primaws/rest/pub/pnxs/L/99122905335805763","adaptor":"Local Search Engine","pnx":{"display":{"source":["Alma"],"type":["book"],"language":["dan"],"title":["Den navnkundige Engellænders Robinson Crusoe Levnet og meget selsomme Skiebne "],"subject":["Literatur Romaner og Fortællinger"],"format":["1-2 i 2 bd. : ill."],"creationdate":["1744-1745"],"lds41":["digitaliseret"],"lds25":["1. Især da han i 28 Aar levede paa en øde og u-bebygget Øe ved Gabet af den store Strøm Oroonoko paa den Amerikanske Kust ....","2. Som indeholder mange underlige Hændelser, saavel paa hans Reyse tilbage til hans Øe, som paa andre nye Reyser ... fordansket, og prydet med mange smukke Figurer."],"creator":[" Daniel Defoe$$QDefoe Daniel"],"publisher":["Kjøbenhavn : Ernst Henrich Berling"],"description":["[Dan. Defoe] ; oversat i det Danske Sprog, og ziiret med smukke Figurer.","<a target=\"_blank\" href=\"http://images.kb.dk/bibliotheca_danica/bind 40244.jpg\">Citation/Reference - Beskrevet i: Bibliotheca Danica</a>"],"mms":["99122905335805763"],"contents":["1. Især da han i 28 Aar levede paa en øde og u-bebygget Øe ved Gabet af den store Strøm Oroonoko paa den Amerikanske Kust ....","2. Som indeholder mange underlige Hændelser, saavel paa hans Reyse tilbage til hans Øe, som paa andre nye Reyser ... fordansket, og prydet med mange smukke Figurer."],"relation":["$$Cedition$$VDen navnkundige Engellænders Robinson Crusoe Levnet og meget selsomme Skiebne /$$Z99121999521205763"],"place":["Kjøbenhavn :"],"version":["0"],"lds02":["58,-55$$Q58,-55"],"lds10":["KBD","DOD","kbd"],"lds24":["KBD","DOD","kbd"],"lds27":["$$Tdigitaliseret$$"]},"control":{"sourcerecordid":["99122905335805763"],"recordid":["alma99122905335805763"],"sourceid":"alma","originalsourceid":["002162394-KGL01"],"sourcesystem":["ILS"],"sourceformat":["MARC21"],"score":["0.35355338"],"isDedup":false},"addata":{"aulast":["Defoe"],"aufirst":["Daniel."],"auinit":["D"],"au":["Defoe, Daniel."],"date":["1744 - 1745","1744-1745"],"cop":["Kjøbenhavn"],"pub":["Ernst Henrich Berling"],"oclcid":["x481956432","(dk-810010)002162394kgl01"],"format":["book"],"genre":["book"],"ristype":["BOOK"],"btitle":["Den navnkundige Engellænders Robinson Crusoe Levnet og meget selsomme Skiebne /"]},"sort":{"title":["navnkundige Engellænders Robinson Crusoe Levnet og meget selsomme Skiebne / [Dan. Defoe] ; oversat i det Danske Sprog, og ziiret med smukke Figurer."],"author":["Defoe, Daniel."],"creationdate":["1744"]},"facets":{"frbrtype":["6"]}},"delivery":{"bestlocation":{"isValidUser":true,"organization":"45KBDK_KGL","libraryCode":"KBL","availabilityStatus":"available","subLocation":"Læsesalslån (skal bestilles)","subLocationCode":"KLDA_LFDA","mainLocation":"Nationalbiblioteket ","callNumber":"58,-55 8° 02529","callNumberType":"#","holdingURL":"OVP","adaptorid":"ALMA_01","ilsApiId":"99122905335805763","holdId":"222070815490005763","holKey":"HoldingResultKey [mid=222070815490005763, libraryId=439545770005763, locationCode=KLDA_LFDA, callNumber=58,-55 8° 02529]","matchForHoldings":[{"matchOn":"MainLocation","holdingRecord":"852##b"}],"stackMapUrl":"","relatedTitle":null,"yearFilter":null,"volumeFilter":null,"singleUnavailableItemProcessType":null,"boundWith":false,"@id":"_:0","pendingRender":false},"holding":[{"isValidUser":true,"organization":"45KBDK_KGL","libraryCode":"KBL","availabilityStatus":"available","subLocation":"Læsesalslån (skal bestilles)","subLocationCode":"KLDA_LFDA","mainLocation":"Nationalbiblioteket ","callNumber":"58,-55 8° 02529","callNumberType":"#","holdingURL":"OVP","adaptorid":"ALMA_01","ilsApiId":"99122905335805763","holdId":"222070815490005763","holKey":"HoldingResultKey [mid=222070815490005763, libraryId=439545770005763, locationCode=KLDA_LFDA, callNumber=58,-55 8° 02529]","matchForHoldings":[{"matchOn":"MainLocation","holdingRecord":"852##b"}],"stackMapUrl":"","relatedTitle":null,"yearFilter":null,"volumeFilter":null,"singleUnavailableItemProcessType":null,"boundWith":false,"@id":"_:0"}],"electronicServices":null,"filteredByGroupServices":null,"quickAccessService":null,"deliveryCategory":["Alma-P"],"serviceMode":["ovp"],"availability":["available_in_library"],"availabilityLinks":["detailsgetit1"],"availabilityLinksUrl":[],"displayedAvailability":null,"displayLocation":true,"additionalLocations":false,"physicalItemTextCodes":null,"feDisplayOtherLocations":false,"almaInstitutionsList":[],"recordInstitutionCode":null,"recordOwner":"45KBDK_NETWORK","hasFilteredServices":null,"digitalAuxiliaryMode":false,"hideResourceSharing":false,"sharedDigitalCandidates":null,"consolidatedCoverage":null,"electronicContextObjectId":null,"GetIt1":[{"category":"Alma-P","links":[{"isLinktoOnline":false,"getItTabText":"service_getit","adaptorid":"ALMA_01","ilsApiId":"99122905335805763","link":"OVP","inst4opac":"45KBDK_KGL","displayText":null,"@id":"_:0"}]}],"physicalServiceId":null,"link":[{"@id":":_0","linkType":"thumbnail","linkURL":"https://proxy-eu.hosted.exlibrisgroup.com/exl_rewrite/books.google.com/books?bibkeys=ISBN:,OCLC:,LCCN:&jscmd=viewapi&callback=updateGBSCover","displayLabel":"thumbnail"}],"hasD":null,"origAvailability":["available_in_library"]},"enrichment":{"virtualBrowseObject":{"isVirtualBrowseEnabled":true,"callNumber":"58,-55 8° 02529","callNumberBrowseField":"#"}},"cameFrom":"full"}</u:key>
<u:key id="rft.place">Kjøbenhavn :</u:key>
<u:key id="licenseEnable">true</u:key>
<u:key id="memberProxyServer">http://ez.statsbiblioteket.dk:2048/login</u:key>
<u:key id="sfx.sid">primo.exlibrisgroup.com-alma_local</u:key>
<u:key id="memberProxyIp">130.225.27.190</u:key>
<u:key id="svc.profile">viewit</u:key>
<u:key id="rft.btitle">Den navnkundige Engellænders Robinson Crusoe Levnet og meget selsomme Skiebne /</u:key>
<u:key id="rft.genre">book</u:key>
<u:key id="memberUseProxy">Selective</u:key>
<u:key id="Incoming_URL">http://soeg.kb.dk/view/uresolver/45KBDK_KGL/openurl?vid=45KBDK_KGL:KGL&rft.mms_id=99122905335805763&rfr_id=info:sid/primo.exlibrisgroup.com-alma_local&u.ignore_date_coverage=true&consolidate_coverage=true&request_system=VE</u:key>
<u:key id="vid">45KBDK_KGL:KGL</u:key>
<u:key id="institution">5763</u:key>
<u:key xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" id="memberProxySalt" xsi:nil="true"/>Updated ChangeLog.md
<u:key id="consolidate_coverage">true</u:key>
<u:key xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" id="req.id" xsi:nil="true"/>
<u:key id="rft.mms_id">99122905335805763</u:key>
<u:key id="rfr_id">info:sid/primo.exlibrisgroup.com-alma_local</u:key>
<u:key id="request_system">VE</u:key>
<u:key id="publication_place">Kjøbenhavn :</u:key>
<u:key id="rft.object_type">BOOK</u:key>
<u:key id="rft.sau">Defoe, Daniel.</u:key>
<u:key id="memberProxyType">EZProxy</u:key>
<u:key id="rft.publisher">Ernst Henrich Berling,</u:key>
<u:key id="rft.au">Defoe, Daniel.</u:key>
<u:key id="Related_MMS">99121999521205763 Other Edition notClosely related to: 99122905335805763</u:key>
<u:key id="rft.pubdate">1744-1745.</u:key>
<u:key id="u.ignore_date_coverage">true</u:key>
<u:key id="rft.title">Den navnkundige Engellænders Robinson Crusoe Levnet og meget selsomme Skiebne /</u:key>
<u:key id="customer">5760</u:key>
<u:key id="rfr.rfr">primo.exlibrisgroup.com-alma_local</u:key>
</u:keys>
</u:context_object>
...
```

Especially note the
line `<u:key id="Related_MMS">99121999521205763 Other Edition notClosely related to: 99122905335805763</u:key>`

There is no explicit reference in the bib record to this original record. See the guide at
<https://knowledge.exlibrisgroup.com/Alma/Product_Documentation/010Alma_Online_Help_(English)/Electronic_Resource_Management/051Link_Resolver/030Access_to_Services#section_15>
for how this works



