package rb.ebooklib.ebooks.epub.reader;

import org.w3c.dom.*;
import rb.ebooklib.ebooks.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

class DOMUtil {
    public static String getAttribute(Element element, String namespace, String attribute) {
        String result = element.getAttributeNS(namespace, attribute);
        if (StringUtil.isEmpty(result)) {
            result = element.getAttribute(attribute);
        }
        return result;
    }

    public static List<String> getElementsTextChild(Element parentElement, String namespace, String tagname) {
        NodeList elements = parentElement.getElementsByTagNameNS(namespace, tagname);
        List<String> result = new ArrayList<>(elements.getLength());
        for(int i = 0; i < elements.getLength(); i++) {
            result.add(getTextChildrenContent((Element) elements.item(i)));
        }
        return result;
    }

    public static String getFindAttributeValue(Document document, String namespace, String elementName, String findAttributeName, String findAttributeValue, String resultAttributeName) {
        NodeList metaTags = document.getElementsByTagNameNS(namespace, elementName);
        for(int i = 0; i < metaTags.getLength(); i++) {
            Element metaElement = (Element) metaTags.item(i);
            if(findAttributeValue.equalsIgnoreCase(metaElement.getAttribute(findAttributeName))
                    && StringUtil.isNotBlank(metaElement.getAttribute(resultAttributeName))) {
                return metaElement.getAttribute(resultAttributeName);
            }
        }
        return null;
    }

    public static Element getFirstElementByTagNameNS(Element parentElement, String namespace, String tagName) {
        NodeList nodes = parentElement.getElementsByTagNameNS(namespace, tagName);
        if(nodes.getLength() == 0) {
            return null;
        }
        return (Element) nodes.item(0);
    }

    public static String getTextChildrenContent(Element parentElement) {
        if(parentElement == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        NodeList childNodes = parentElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if ((node == null) ||
                    (node.getNodeType() != Node.TEXT_NODE)) {
                continue;
            }
            result.append(((Text) node).getData());
        }
        return result.toString().trim();
    }
}
