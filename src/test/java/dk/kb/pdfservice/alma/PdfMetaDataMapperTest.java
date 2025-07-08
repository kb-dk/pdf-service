package dk.kb.pdfservice.alma;

import dk.kb.alma.client.sru.Query;
import dk.kb.alma.gen.bibs.Bib;
import dk.kb.alma.gen.items.Item;
import dk.kb.pdfservice.config.ServiceConfig;
import dk.kb.pdfservice.model.PdfMetadata;
import dk.kb.pdfservice.webservice.exception.NotFoundServiceObjection;
import dk.kb.util.other.StringListUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PdfMetaDataMapperTest {

    @Test
    void getPdfMetadataFromRest() throws IOException {
        ServiceConfig.initialize("conf/*.yaml", Thread.currentThread().getContextClassLoader().getResource("pdf-service-test.yaml").getFile());
        String actualBarcode = "130008805998";

        Pair<Bib, Item> bibItem = AlmaLookupClient.getBib(actualBarcode);
        Bib bib = bibItem.getLeft();

        Element marc21 = bib.getAnies()
                .stream()
                .filter(element -> Objects.equals(element.getLocalName(), "record"))
                .findFirst()
                .orElseThrow(() -> new NotFoundServiceObjection("Failed to parse marc21 data for " + actualBarcode));

        PdfMetaDataMapper pdfMetaDataMapper = new PdfMetaDataMapper(marc21);
        PdfMetadata pdfMetadata = pdfMetaDataMapper.mapToPdfMetadata();

    }

    @Test
    void getPdfMetadataFromSruBarcode() throws IOException {

        ServiceConfig.initialize("conf/*.yaml", Thread.currentThread().getContextClassLoader().getResource("pdf-service-test.yaml").getFile());

        Iterator<Element> result = ServiceConfig.getAlmaSRUClient().search(Query.containsWords(Query.barcode, "130008805998"));
        List<Element> resultList = StringListUtils.asStream(result).collect(Collectors.toList());
        Element marc21 = resultList.get(0);

        PdfMetaDataMapper pdfMetaDataMapper = new PdfMetaDataMapper(marc21);
        PdfMetadata pdfMetadata = pdfMetaDataMapper.mapToPdfMetadata();

    }

    @Test
    void getPdfMetadataFromSruBarcode2() throws IOException {

        ServiceConfig.initialize("conf/*.yaml", Thread.currentThread().getContextClassLoader().getResource("pdf-service-test.yaml").getFile());

        Iterator<Element> result = ServiceConfig.getAlmaSRUClient().search(Query.containsWords(Query.barcode, "130024645461"));
        List<Element> resultList = StringListUtils.asStream(result).collect(Collectors.toList());
        Element marc21 = resultList.get(0);

        PdfMetaDataMapper pdfMetaDataMapper = new PdfMetaDataMapper(marc21);
        PdfMetadata pdfMetadata = pdfMetaDataMapper.mapToPdfMetadata();

    }

    @Test
    void getPdfMetadataFromSru() throws IOException {

        ServiceConfig.initialize("conf/*.yaml", Thread.currentThread().getContextClassLoader().getResource("pdf-service-test.yaml").getFile());

        Iterator<Element> result = ServiceConfig.getAlmaSRUClient().search(Query.containsWords(Query.barcode, "130008805998"));
        List<Element> resultList = StringListUtils.asStream(result).collect(Collectors.toList());
        Element marc21 = resultList.get(0);

        PdfMetaDataMapper pdfMetaDataMapper = new PdfMetaDataMapper(marc21);
        PdfMetadata pdfMetadata = pdfMetaDataMapper.mapToPdfMetadata();

    }

}


