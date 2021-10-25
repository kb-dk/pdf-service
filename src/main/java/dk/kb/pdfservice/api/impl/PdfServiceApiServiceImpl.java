package dk.kb.pdfservice.api.impl;

import dk.kb.alma.client.AlmaInventoryClient;
import dk.kb.alma.gen.bibs.Bib;
import dk.kb.alma.gen.items.Item;
import dk.kb.pdfservice.api.PdfServiceApi;
import dk.kb.pdfservice.config.ServiceConfig;
import dk.kb.pdfservice.webservice.exception.InternalServiceException;
import dk.kb.pdfservice.webservice.exception.ServiceException;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * pdf-service
 *
 * <p>This pom can be inherited by projects wishing to integrate to the SBForge development platform.
 */
public class PdfServiceApiServiceImpl implements PdfServiceApi {
    // Setup directories
    File baseDir = new File(".");
    File outDir = new File(baseDir, "out");
    private final Logger log = LoggerFactory.getLogger(this.toString());
    
    
    /* How to access the various web contexts. See https://cxf.apache.org/docs/jax-rs-basics.html#JAX-RSBasics-Contextannotations */
    @Context
    private transient UriInfo uriInfo;
    
    @Context
    private transient SecurityContext securityContext;
    
    @Context
    private transient HttpHeaders httpHeaders;
    
    @Context
    private transient Providers providers;
    
    @Context
    private transient Request request;
    
    @Context
    private transient ContextResolver<?> contextResolver;
    
    @Context
    private transient HttpServletRequest httpServletRequest;
    
    @Context
    private transient HttpServletResponse httpServletResponse;
    
    @Context
    private transient ServletContext servletContext;
    
    @Context
    private transient ServletConfig servletConfig;
    
    @Context
    private transient MessageContext messageContext;
    
    
    public InputStream produceHeaderPage(String barCode) throws TransformerException, SAXException, IOException {
        
        String actualBarcode = barCode.split("-", 2)[0];
        
        AlmaInventoryClient almaInventoryClient = new AlmaInventoryClient(ServiceConfig.getAlmaRestClient());
        Item item = almaInventoryClient.getItem(actualBarcode);
        
        String mmsID = item.getBibData().getMmsId();
        Bib bib = almaInventoryClient.getBib(mmsID);
        
        //Portfolios portFolios = almaInventoryClient.getBibPortfolios(mmsID);
        
        Element marc21 = bib.getAnies().get(0);
        String authors = getAuthors(marc21);
        String title = getTitle(marc21);
        String alternativeTitle = getAlternativeTitle(marc21);
        String udgavebetegnelse = getUdgavebetegnelse(marc21);
        String place = getPlace(marc21);
        String size = getSize(marc21);
        
        boolean isWithinCopyright = isWithinCopyright(bib, marc21);
        
        
        FopFactoryBuilder builder = new FopFactoryBuilder(new File(".").toURI());
        builder.setAccessibility(true);
        FopFactory fopFactory = builder.build();
        
        
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, outStream);
            
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setErrorListener(new ErrorListener() {
                @Override
                public void warning(TransformerException exception) {
                    log.warn("Transformer warning", exception);
                }
                
                @Override
                public void error(TransformerException exception) {
                    log.error("Transformer Exception", exception);
                }
                
                @Override
                public void fatalError(TransformerException exception) throws TransformerException {
                    log.error("Transformer Fatal Exception", exception);
                    throw exception;
                }
            });
            
            try (InputStream formatterStream = new FileInputStream(ServiceConfig.getFrontPageFopFile().toFile())) {
                Transformer xslfoTransformer = factory.newTransformer(new StreamSource(formatterStream));
                xslfoTransformer.setParameter("authors", authors);
                xslfoTransformer.setParameter("title", title);
                xslfoTransformer.setParameter("altTitle", alternativeTitle);
                xslfoTransformer.setParameter("edition", udgavebetegnelse);
                xslfoTransformer.setParameter("place", place);
                xslfoTransformer.setParameter("size", size);
                xslfoTransformer.setParameter("isWithinCopyright", isWithinCopyright);
    
                final String logoPath = ServiceConfig.getLogoPath();
                xslfoTransformer.setParameter("logoPath", logoPath);
                
                xslfoTransformer.transform(new StreamSource(new StringReader("<xml/>")),
                                           new SAXResult(fop.getDefaultHandler()));
            }
            outStream.flush(); //just in case it is not done automatically
            return outStream.toInputStream();
        }
    }
    
    private boolean isWithinCopyright(Bib bib, Element marc21) {
        
        LocalDate dateOfPublication = getPublicationDate(marc21);
        
        if (dateOfPublication == null) {
            return true;
        }
        return dateOfPublication.plusYears(ServiceConfig.getYearsSincePublicationToBeOutsideCopyright())
                                .isAfter(LocalDate.now(ZoneId.systemDefault()));
    }
    
    
    private String getSize(Element marc21) {
        // Forlæggets fysiske størrelse:
        // hentes fra Marc21 300a
        XPathSelector xpath = XpathUtils.createXPathSelector();
        
        return xpath.selectString(marc21, "/record/datafield[@tag='300']/subfield[@code='a']");
    }
    
    private LocalDate getPublicationDate(Element marc21) {
        XPathSelector xpath = XpathUtils.createXPathSelector();
        String tag260c = xpath.selectString(marc21, "/record/datafield[@tag='260']/subfield[@code='c']", null);
        //String dateOfPublicationString = bib.getDateOfPublication();
        
        Pattern year2year = Pattern.compile("(\\d{4})-\\d{4}\\.?");
        final Matcher matcher = year2year.matcher(tag260c);
        if (matcher.matches()) {
            String firstYearString = matcher.group(1);
            int firstYear = Integer.parseInt(firstYearString);
            return LocalDate.of(firstYear, 1, 1);
        }
        
        String tag500a = xpath.selectString(marc21, "/record/datafield[@tag='500']/subfield[@code='a']", "");
        if (tag500a.startsWith("Premiere")) {
            String date = tag500a.split(" ", 2)[1];
            //TODO implement other date patterns
            System.out.println(date);
        }
        
        return null;
    }
    
    private String getPlace(Element marc21) {
        //  * hentes fra Marc21 260a + b + c eller 500a (hvis man kan afgrænse til *premiere*, da man ved overførslen til ALMA slog mange felter sammen til dette felt),
        //  * 710 (udfaset felt, der stadig er data i) eller
        //  * felt 96 (i 96 kan dog også være rettighedsbegrænsninger indskrevet).
        
        XPathSelector xpath = XpathUtils.createXPathSelector();
        String tag260a = xpath.selectString(marc21, "/record/datafield[@tag='260']/subfield[@code='a']", null);
        if (tag260a != null) {
            String tag260b = xpath.selectString(marc21, "/record/datafield[@tag='260']/subfield[@code='b']", null);
            String tag260c = xpath.selectString(marc21, "/record/datafield[@tag='260']/subfield[@code='c']", null);
            return Stream.of(tag260a, tag260b, tag260c).filter(Objects::nonNull).collect(Collectors.joining(" "));
        } else {
            List<String> tag500As = xpath.selectStringList(marc21,
                                                           "/record/datafield[@tag='500']/subfield[@code='a']");
            return "TODO500A";
            //TODO
            /*
                                                                <xsl:for-each
                                                            select="ns:records/ns:record/ns:recordData/marc:record/marc:datafield[@tag='500']/marc:subfield[@code='a']">
                                                        <xsl:variable name="p500a"
                                                                      select="str:tokenize(normalize-space(.), ' ')"/>
                                                        <xsl:if test="starts-with(.,'Premiere')">
                                                            <xsl:value-of select="concat($p500a[1],' ', $p500a[2])"/>
                                                        </xsl:if>
                                                    </xsl:for-each>
             */
        }
    }
    
    private String getUdgavebetegnelse(Element marc21) {
        //* Udgavebetegnelse:
        //  * 250a
        //  * hvis ikke noget = ingenting (ikke relevant for teatermanuskripter)
        XPathSelector xpath = XpathUtils.createXPathSelector();
        String tag250a = xpath.selectString(marc21, "/record/datafield[@tag='250']/subfield[@code='a']", null);
        return Stream.of(tag250a).filter(Objects::nonNull).collect(Collectors.joining(", "));
    }
    
    private String getAlternativeTitle(Element marc21) {
        XPathSelector xpath = XpathUtils.createXPathSelector();
        return xpath.selectString(marc21, "/record/datafield[@tag='246']/subfield[@code='a']");
    }
    
    
    private String getTitle(Element marc21) {
        //* Titel:
        //  * hentes fra Marc21 245a + b
        //  * opdelt i flere linjer hvis længere end ?
        XPathSelector xpath = XpathUtils.createXPathSelector();
        String tag245a = xpath.selectString(marc21, "/record/datafield[@tag='245']/subfield[@code='a']", null);
        String tag245b = xpath.selectString(marc21, "/record/datafield[@tag='245']/subfield[@code='b']", null);
        return Stream.of(tag245a, tag245b).filter(Objects::nonNull).collect(Collectors.joining(", "));
    }
    
    private String getAuthors(Element marc21) {
        //* Forfatter(e):
        //  * hentes fra Marc21 100a,
        //  * 700a + d ( kan forekomme flere gange),
        //  * 245c - komma separeret og opdelt i flere linier hvis længere end ?
        //  * [] - hvis ingen værdi(er).
        XPathSelector xpath = XpathUtils.createXPathSelector();
        String tag100a = xpath.selectString(marc21, "/record/datafield[@tag='100']/subfield[@code='a']", null);
        String tag700a = xpath.selectString(marc21, "/record/datafield[@tag='700']/subfield[@code='a']", null);
        String tag245c = xpath.selectString(marc21, "/record/datafield[@tag='245']/subfield[@code='c']", null);
        if (tag100a == null && tag700a == null && tag245c == null) {
            return "[]";
        }
        return Stream.of(tag100a, tag700a, tag245c).filter(Objects::nonNull).collect(Collectors.joining(", "));
    }
    
    
    /**
     * Request a theater manuscript summary in pdf format.
     *
     * @param barcode  : code to get xml result tree String containing info about pdf
     * @param pdflink2 : Relative path including .pdf file
     * @return <ul>
     *         <li>code = 200, message = "A pdf with attached page", response = String.class</li>
     *         </ul>
     * @throws ServiceException when other http codes should be returned
     * @implNote return will always produce a HTTP 200 code. Throw ServiceException if you need to return other
     *         codes
     **/
    @Override
    public StreamingOutput getPdf(String barcode, String pdflink2) {
        
        
        httpServletResponse.setHeader("Content-disposition", "inline; filename=\"" + barcode + "\"");
        
        try {
            InputStream apronFile = produceHeaderPage(barcode);
            
            final URL url = new URL(ServiceConfig.getPdfSourcePath() + barcode);
            try (InputStream inPdf = url.openStream()) {
                InputStream resultingPdf = PdfBoxCopyrightInserter.insertCopyrightFooter(inPdf);
                log.info("Finished inserting footers");
                
                PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
                pdfMergerUtility.addSource(apronFile);
                pdfMergerUtility.addSource(resultingPdf);
                try (final var completePDF = new org.apache.commons.io.output.ByteArrayOutputStream()) {
                    pdfMergerUtility.setDestinationStream(completePDF);
                    //TODO Configurable memory settings
                    pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupMixed(1024 * 1024 * 500));
                    log.debug("Finished merging documents");
                    return output -> {
                        completePDF.flush(); //just in case it is not done automatically
                        try (var resultInputStream = completePDF.toInputStream();) {
                            IOUtils.copy(resultInputStream, output);
                        }
                        log.debug("Finished returning pdf");
                    };
                    
                }
            }
            
        } catch (TransformerException | SAXException | IOException e) {
            log.error("Fejl", e);
            throw new InternalServiceException("Fejl med getPdf", e);
        }
    }
    
    
}
