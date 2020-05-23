package rb.ebooklib.ebooks.epub.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;

public class EpubProcessorSupport {

    private static final Logger log = LoggerFactory.getLogger(EpubProcessorSupport.class);

    private static DocumentBuilderFactory documentBuilderFactory;

    static {
        init();
    }

    static class EntityResolverImpl implements EntityResolver {
        private String previousLocation;

        @Override
        public InputSource resolveEntity(String publicId, String systemId)
                throws IOException {
            String resourcePath;
            if (systemId.startsWith("http:")) {
                URL url = new URL(systemId);
                resourcePath = "dtd/" + url.getHost() + url.getPath();
                previousLocation = resourcePath.substring(0, resourcePath.lastIndexOf('/'));
            } else {
                resourcePath = previousLocation + systemId.substring(systemId.lastIndexOf('/'));
            }

            if (this.getClass().getClassLoader().getResource(resourcePath) == null) {
                throw new RuntimeException("remote resource is not cached : [" + systemId + "] cannot continue");
            }

            InputStream in = EpubProcessorSupport.class.getClassLoader().getResourceAsStream(resourcePath);
            return new InputSource(in);
        }
    }

    private static void init() {
        EpubProcessorSupport.documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setValidating(false);
    }

    private static EntityResolver getEntityResolver() {
        return new EntityResolverImpl();
    }

    public static DocumentBuilder createDocumentBuilder() {
        DocumentBuilder result = null;
        try {
            result = documentBuilderFactory.newDocumentBuilder();
            result.setEntityResolver(getEntityResolver());
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage());
        }
        return result;
    }
}