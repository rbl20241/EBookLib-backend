package rb.ebooklib.ebooks.util;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import rb.ebooklib.ebooks.epub.domain.EpubResource;
import rb.ebooklib.ebooks.epub.domain.MediaType;
import rb.ebooklib.ebooks.epub.domain.Resource;
import rb.ebooklib.ebooks.epub.reader.EpubProcessorSupport;
import rb.ebooklib.ebooks.epub.service.MediatypeService;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ResourceUtil {

    public static Resource createResource(File file) throws IOException {
        if (file == null) {
            return null;
        }
        MediaType mediaType = MediatypeService.determineMediaType(file.getName());
        byte[] data = IOUtil.toByteArray(new FileInputStream(file));
        return new EpubResource(data, mediaType);
    }

    public static Resource createResource(ZipEntry zipEntry, ZipInputStream zipInputStream) throws IOException {
        return new EpubResource(zipInputStream, zipEntry.getName());

    }

    private static InputSource getInputSource(Resource resource) throws IOException {
        if (resource == null) {
            return null;
        }
        Reader reader = resource.getReader();
        if (reader == null) {
            return null;
        }
        return new InputSource(reader);
    }

    public static Document getAsDocument(Resource resource) throws SAXException, IOException {
        return getAsDocument(resource, EpubProcessorSupport.createDocumentBuilder());
    }


    /**
     * Reads the given resources inputstream, parses the xml therein and returns the result as a Document
     *
     * @param resource
     * @param documentBuilder
     * @return the document created from the given resource
     * @throws UnsupportedEncodingException
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    private static Document getAsDocument(Resource resource, DocumentBuilder documentBuilder) throws SAXException, IOException {
        InputSource inputSource = getInputSource(resource);
        if (inputSource == null) {
            return null;
        }
        return documentBuilder.parse(inputSource);
    }
}