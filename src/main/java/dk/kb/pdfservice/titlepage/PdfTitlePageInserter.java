package dk.kb.pdfservice.titlepage;

import dk.kb.pdfservice.alma.PdfInfo;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class PdfTitlePageInserter {
    
    public static Logger log = LoggerFactory.getLogger(PdfTitlePageInserter.class);
    
    public static InputStream mergeApronWithPdf(InputStream apronFile,
                                                InputStream resultingPdf,
                                                PDDocumentInformation origPdfMetadata,
                                                PdfInfo pdfInfo)
            throws IOException {
        final var completePDF = new ByteArrayOutputStream();
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        if (apronFile.available() > 0) {
            pdfMergerUtility.addSource(apronFile);
            pdfMergerUtility.addSource(resultingPdf);
            pdfMergerUtility.setDestinationStream(completePDF);
            
            PDDocumentInformation pdDocumentInformation = constructPdfMetadata(pdfInfo, origPdfMetadata);
            pdfMergerUtility.setDestinationDocumentInformation(pdDocumentInformation);
    
    
            PDMetadata metadata = createXMPmetadata();
            pdfMergerUtility.setDestinationMetadata(metadata);
            
            
            //TODO Configurable memory settings
            //Do NOT use main memory as we do not want to risk running out on many concurrent requests. Use unlimited temp files
            pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
            completePDF.flush(); //just in case it is not done automatically
            
            return completePDF.toInputStream();
        } else {
            return resultingPdf;
        }
        
    }
    
    @Nonnull
    private static PDMetadata createXMPmetadata() throws IOException {
        //TODO set correct values here, if we care
        PDMetadata metadata = new PDMetadata(new COSStream());
        String newMetadata = "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n"
                   + "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n"
                   + "<rdf:Description xmlns:dc=\"http://purl.org/dc/elements/1.1/\" rdf:about=\"\">\n"
                   + "<dc:format>application/pdf</dc:format>\n"
                   + "<dc:language>x-unknown</dc:language>\n"
                   + "<dc:date>2022-01-28T13:09:27+01:00</dc:date>\n"
                   + "</rdf:Description>\n"
                   + "<rdf:Description xmlns:pdf=\"http://ns.adobe.com/pdf/1.3/\" rdf:about=\"\">\n"
                   + "<pdf:Producer>Apache FOP Version SVN branches/fop-2_6/fop-core</pdf:Producer>\n"
                   + "<pdf:PDFVersion>1.4</pdf:PDFVersion>\n"
                   + "</rdf:Description>\n"
                   + "<rdf:Description xmlns:xmp=\"http://ns.adobe.com/xap/1.0/\" rdf:about=\"\">\n"
                   + "<xmp:CreatorTool>Apache FOP Version SVN branches/fop-2_6/fop-core</xmp:CreatorTool>\n"
                   + "<xmp:MetadataDate>2022-01-28T13:09:27+01:00</xmp:MetadataDate>\n"
                   + "<xmp:CreateDate>2022-01-28T13:09:27+01:00</xmp:CreateDate>\n"
                   + "</rdf:Description>\n"
                   + "</rdf:RDF>\n"
                   + "</x:xmpmeta>";
        metadata.importXMPMetadata(newMetadata.getBytes(StandardCharsets.UTF_8));
        return metadata;
    }
    
    @Nonnull
    private static PDDocumentInformation constructPdfMetadata(PdfInfo pdfInfo,
                                                              PDDocumentInformation origPdfMetadata) {
        PDDocumentInformation pddocInfo = new PDDocumentInformation();
        
                        /*
            Title –  the title of the document
            Author – who created the document
            Subject – what is the document about
            Keywords – keywords can be comma-separated
            CreationDate – the date and time when the document was originally created
            ModDate -the latest modification date and time
            
            Creator – the originating application or library
            Producer – the product that created the PDF. In the early days of PDF people would use a Creator application like Microsoft Word to write a document, print it to a PostScript file and then the Producer would be Acrobat Distiller, the application that converted the PostScript file to a PDF. Nowadays Creator and Producer are often the same or one field is left blank.
             */
        
        if (pdfInfo.getTitle() != null && !pdfInfo.getTitle().isEmpty()) {
            pddocInfo.setTitle(pdfInfo.getTitle());
        }
        
        if (pdfInfo.getAuthors() != null && !pdfInfo.getAuthors().isEmpty()) {
            pddocInfo.setAuthor(pdfInfo.getAuthors());
        }
        
        //agent.setSubject();
        
        if (pdfInfo.getKeywords() != null && !pdfInfo.getKeywords().isEmpty()) {
            pddocInfo.setKeywords(pdfInfo.getKeywords());
        }
        
        //Calendar publicationDateCalendar = localDateToCalendar(pdfInfo.getPublicationDate());
        
        //Preserve the old creationDate as this will probably be the digitization time
        pddocInfo.setCreationDate(origPdfMetadata.getCreationDate());
    
        pddocInfo.setModificationDate(calendarNow());
        
        //Creator is not used, so let's use it for the publicationDate
        if (pdfInfo.getPublicationDateString() != null) {
            pddocInfo.setCreator("Published: " + pdfInfo.getPublicationDateString());
        }
        //The original software that produced this scan
        pddocInfo.setProducer(origPdfMetadata.getProducer());
        
        return pddocInfo;
        
    }
    
    @Nonnull
    private static Calendar calendarNow() {
        Calendar instance = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        instance.setTime(new Date());
        return instance;
    }
    
    @Nonnull
    private static Calendar localDateToCalendar(LocalDate publicationDate) {
        Calendar publicationDateCalendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        publicationDateCalendar.setTimeInMillis(publicationDate.toEpochDay() * ChronoUnit.DAYS.getDuration()
                                                                                              .toMillis());
        return publicationDateCalendar;
    }
    
    
}
