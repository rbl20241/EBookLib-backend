package rb.ebooklib.ebooks.epub.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import rb.ebooklib.ebooks.epub.domain.EpubBook;
import rb.ebooklib.ebooks.epub.domain.Resource;
import rb.ebooklib.ebooks.epub.domain.TOCReference;
import rb.ebooklib.ebooks.epub.domain.TableOfContents;
import rb.ebooklib.ebooks.util.ResourceUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import static rb.ebooklib.ebooks.util.Constants.CHARACTER_ENCODING;
import static rb.ebooklib.ebooks.util.Constants.FRAGMENT_SEPARATOR_CHAR;
import static rb.ebooklib.ebooks.util.StringUtil.*;

class NCXDocument {

    private static final String NAMESPACE_NCX = "http://www.daisy.org/z3986/2005/ncx/";
    public static final String PREFIX_NCX = "ncx";
    public static final String PREFIX_DTB = "dtb";

    private static final Logger log = LoggerFactory.getLogger(NCXDocument.class);

    @SuppressWarnings("unused")
    private interface NCXTags {
        String ncx = "ncx";
        String meta = "meta";
        String navPoint = "navPoint";
        String navMap = "navMap";
        String navLabel = "navLabel";
        String content = "content";
        String text = "text";
        String docTitle = "docTitle";
        String docAuthor = "docAuthor";
        String head = "head";
    }

    @SuppressWarnings("unused")
    private interface NCXAttributes {
        String src = "src";
        String name = "name";
        String content = "content";
        String id = "id";
        String playOrder = "playOrder";
        String clazz = "class";
        String version = "version";
    }

    @SuppressWarnings("ConstantConditions")
    public static Resource read(EpubBook epubBook) {
        Resource ncxResource = null;
        if(epubBook.getSpine().getTocResource() == null) {
            log.error("Book does not contain a table of contents file");
            return null;
        }
        try {
            ncxResource = epubBook.getSpine().getTocResource();
            if(ncxResource == null) {
                return null;
            }
            Document ncxDocument = ResourceUtil.getAsDocument(ncxResource);
            Element navMapElement = DOMUtil.getFirstElementByTagNameNS(ncxDocument.getDocumentElement(), NAMESPACE_NCX, NCXTags.navMap);
            TableOfContents tableOfContents = new TableOfContents(readTOCReferences(navMapElement.getChildNodes(), epubBook));
            epubBook.setTableOfContents(tableOfContents);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return ncxResource;
    }

    private static List<TOCReference> readTOCReferences(NodeList navpoints, EpubBook epubBook) {
        if(navpoints == null) {
            return new ArrayList<>();
        }
        List<TOCReference> result = new ArrayList<>(navpoints.getLength());
        for(int i = 0; i < navpoints.getLength(); i++) {
            Node node = navpoints.item(i);
            if (node.getNodeType() != Document.ELEMENT_NODE) {
                continue;
            }
            if (! (node.getLocalName().equals(NCXTags.navPoint))) {
                continue;
            }
            TOCReference tocReference = readTOCReference((Element) node, epubBook);
            result.add(tocReference);
        }
        return result;
    }

    private static TOCReference readTOCReference(Element navpointElement, EpubBook epubBook) {
        String label = readNavLabel(navpointElement);
        String tocResourceRoot = substringBeforeLast(epubBook.getSpine().getTocResource().getHref(), '/');
        if (tocResourceRoot.length() == epubBook.getSpine().getTocResource().getHref().length()) {
            tocResourceRoot = "";
        } else {
            tocResourceRoot = tocResourceRoot + "/";
        }
        String reference = tocResourceRoot + readNavReference(navpointElement);
        String href = substringBefore(reference, FRAGMENT_SEPARATOR_CHAR);
        String fragmentId = substringAfter(reference, FRAGMENT_SEPARATOR_CHAR);
        Resource resource = epubBook.getResources().getByHref(href);
        if (resource == null) {
            log.error("Resource with href " + href + " in NCX document not found");
        }
        TOCReference result = new TOCReference(label, resource, fragmentId);
        readTOCReferences(navpointElement.getChildNodes(), epubBook);
        result.setChildren(readTOCReferences(navpointElement.getChildNodes(), epubBook));
        return result;
    }

    private static String readNavReference(Element navpointElement) {
        Element contentElement = DOMUtil.getFirstElementByTagNameNS(navpointElement, NAMESPACE_NCX, NCXTags.content);
        if (contentElement == null) {
            return null;
        }
        String result = DOMUtil.getAttribute(contentElement, NAMESPACE_NCX, NCXAttributes.src);
        try {
            result = URLDecoder.decode(result, CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
        }
        return result;
    }

    private static String readNavLabel(Element navpointElement) {
        Element navLabel = DOMUtil.getFirstElementByTagNameNS(navpointElement, NAMESPACE_NCX, NCXTags.navLabel);
        if (navLabel == null) {
            return null;
        }

        return DOMUtil.getTextChildrenContent(DOMUtil.getFirstElementByTagNameNS(navLabel, NAMESPACE_NCX, NCXTags.text));
    }
}
