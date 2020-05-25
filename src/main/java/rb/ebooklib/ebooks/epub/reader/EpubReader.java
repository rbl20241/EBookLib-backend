package rb.ebooklib.ebooks.epub.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import rb.ebooklib.ebooks.epub.domain.EpubBook;
import rb.ebooklib.ebooks.epub.domain.Resource;
import rb.ebooklib.ebooks.epub.domain.Resources;
import rb.ebooklib.ebooks.util.ResourceUtil;
import rb.ebooklib.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import static rb.ebooklib.ebooks.util.Constants.CHARACTER_ENCODING;

public class EpubReader {
    private static final Logger log = LoggerFactory.getLogger(EpubReader.class);
    private final BookProcessor bookProcessor = BookProcessor.IDENTITY_BOOKPROCESSOR;

    public EpubBook readEpub(InputStream in) throws IOException {
        return readEpub(in, CHARACTER_ENCODING);
    }

    public EpubBook readEpub(InputStream in, String encoding) throws IOException {
        return readEpub(new ZipInputStream(in), encoding);
    }

    private EpubBook readEpub(ZipInputStream in, String encoding) throws IOException {
        return readEpub(EpubResourcesLoader.loadResources(in, encoding));
    }

    private EpubBook readEpub(Resources resources) {
        return readEpub(resources, new EpubBook());
    }

    private EpubBook readEpub(Resources resources, EpubBook result) {
        if (result == null) {
            result = new EpubBook();
        }
        handleMimeType(resources);
        String packageResourceHref = getPackageResourceHref(resources);
        Resource packageResource = processPackageResource(packageResourceHref, result, resources);
        result.setOpfResource(packageResource);
        Resource ncxResource = processNcxResource(result);
        result.setNcxResource(ncxResource);
        result = postProcessBook(result);
        return result;
    }

    private EpubBook postProcessBook(EpubBook epubBook) {
        if (bookProcessor != null) {
            epubBook = bookProcessor.processBook(epubBook);
        }
        return epubBook;
    }

    private Resource processNcxResource(EpubBook epubBook) {
        return NCXDocument.read(epubBook);
    }

    private Resource processPackageResource(String packageResourceHref, EpubBook epubBook, Resources resources) {
        Resource packageResource = resources.remove(packageResourceHref);
        try {
            PackageDocumentReader.read(packageResource, this, epubBook, resources);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return packageResource;
    }

    private String getPackageResourceHref(Resources resources) {
        String defaultResult = "OEBPS/content.opf";
        String result = defaultResult;

        Resource containerResource = resources.remove("META-INF/container.xml");
        if(containerResource == null) {
            return result;
        }
        try {
            Document document = ResourceUtil.getAsDocument(containerResource);
            Element rootFileElement = (Element) ((Element) document.getDocumentElement().getElementsByTagName("rootfiles").item(0)).getElementsByTagName("rootfile").item(0);
            result = rootFileElement.getAttribute("full-path");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        if(StringUtil.isBlank(result)) {
            result = defaultResult;
        }
        return result;
    }

    private void handleMimeType(Resources resources) {
        resources.remove("mimetype");
    }
}
