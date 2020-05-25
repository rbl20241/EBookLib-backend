package rb.ebooklib.ebooks.epub.reader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import rb.ebooklib.ebooks.epub.domain.*;
import rb.ebooklib.ebooks.epub.service.MediatypeService;
import rb.ebooklib.ebooks.util.ResourceUtil;
import rb.ebooklib.util.StringUtil;

import static rb.ebooklib.ebooks.util.Constants.*;

public class PackageDocumentReader extends PackageDocumentBase {
	
	private static final Logger log = LoggerFactory.getLogger(PackageDocumentReader.class);
	private static final String[] POSSIBLE_NCX_ITEM_IDS = new String[] {"toc", "ncx", "ncxtoc"};

	public static void read(Resource packageResource, EpubReader epubReader, EpubBook epubBook, Resources resources) throws UnsupportedEncodingException, SAXException, IOException, ParserConfigurationException {
		Document packageDocument = ResourceUtil.getAsDocument(packageResource);
		String packageHref = packageResource.getHref();
		resources = fixHrefs(packageHref, resources);
		readGuide(packageDocument, epubReader, epubBook, resources);
		
		// Books sometimes use non-identifier ids. We map these here to legal ones
		Map<String, String> idMapping = new HashMap<String, String>();
		
		resources = readManifest(packageDocument, packageHref, epubReader, resources, idMapping);
		epubBook.setResources(resources);
		readCover(packageDocument, epubBook);
		epubBook.setMetadata(PackageDocumentMetadataReader.readMetadata(packageDocument));
		epubBook.setSpine(readSpine(packageDocument, epubBook.getResources(), idMapping));
		
		// if we did not find a cover page then we make the first page of the book the cover page
		if (epubBook.getCoverPage() == null && epubBook.getSpine().size() > 0) {
			epubBook.setCoverPage(epubBook.getSpine().getResource(0));
		}
	}
	
//	private static Resource readCoverImage(Element metadataElement, Resources resources) {
//		String coverResourceId = DOMUtil.getFindAttributeValue(metadataElement.getOwnerDocument(), NAMESPACE_OPF, OPFTags.meta, OPFAttributes.name, OPFValues.meta_cover, OPFAttributes.content);
//		if (StringUtil.isBlank(coverResourceId)) {
//			return null;
//		}
//		Resource coverResource = resources.getByIdOrHref(coverResourceId);
//		return coverResource;
//	}
	

	
	private static Resources readManifest(Document packageDocument, String packageHref,
			EpubReader epubReader, Resources resources, Map<String, String> idMapping) {
		Element manifestElement = DOMUtil.getFirstElementByTagNameNS(packageDocument.getDocumentElement(), NAMESPACE_OPF, OPFTags.manifest);
		Resources result = new Resources();
		if(manifestElement == null) {
			log.error("Package document does not contain element " + OPFTags.manifest);
			return result;
		}
		NodeList itemElements = manifestElement.getElementsByTagNameNS(NAMESPACE_OPF, OPFTags.item);
		for(int i = 0; i < itemElements.getLength(); i++) {
			Element itemElement = (Element) itemElements.item(i);
			String id = DOMUtil.getAttribute(itemElement, NAMESPACE_OPF, OPFAttributes.id);
			String href = DOMUtil.getAttribute(itemElement, NAMESPACE_OPF, OPFAttributes.href);
			try {
				href = URLDecoder.decode(href, CHARACTER_ENCODING);
			} catch (UnsupportedEncodingException e) {
				log.error(e.getMessage());
			}
			String mediaTypeName = DOMUtil.getAttribute(itemElement, NAMESPACE_OPF, OPFAttributes.media_type);
			Resource resource = resources.remove(href);
			if(resource == null) {
				log.error("resource with href '" + href + "' not found");
				continue;
			}
			resource.setId(id);
			MediaType mediaType = MediatypeService.getMediaTypeByName(mediaTypeName);
			if(mediaType != null) {
				resource.setMediaType(mediaType);
			}
			result.add(resource);
			idMapping.put(id, resource.getId());
		}
		return result;
	}	

	private static void readGuide(Document packageDocument,
                                  EpubReader epubReader, EpubBook epubBook, Resources resources) {
		Element guideElement = DOMUtil.getFirstElementByTagNameNS(packageDocument.getDocumentElement(), NAMESPACE_OPF, OPFTags.guide);
		if(guideElement == null) {
			return;
		}
		Guide guide = epubBook.getGuide();
		NodeList guideReferences = guideElement.getElementsByTagNameNS(NAMESPACE_OPF, OPFTags.reference);
		for (int i = 0; i < guideReferences.getLength(); i++) {
			Element referenceElement = (Element) guideReferences.item(i);
			String resourceHref = DOMUtil.getAttribute(referenceElement, NAMESPACE_OPF, OPFAttributes.href);
			if (StringUtil.isBlank(resourceHref)) {
				continue;
			}
			Resource resource = resources.getByHref(StringUtil.substringBefore(resourceHref, FRAGMENT_SEPARATOR_CHAR));
			if (resource == null) {
				log.error("Guide is referencing resource with href " + resourceHref + " which could not be found");
				continue;
			}
			String type = DOMUtil.getAttribute(referenceElement, NAMESPACE_OPF, OPFAttributes.type);
			if (StringUtil.isBlank(type)) {
				log.error("Guide is referencing resource with href " + resourceHref + " which is missing the 'type' attribute");
				continue;
			}
			String title = DOMUtil.getAttribute(referenceElement, NAMESPACE_OPF, OPFAttributes.title);
			if (GuideReference.COVER.equalsIgnoreCase(type)) {
				continue; // cover is handled elsewhere
			}
			GuideReference reference = new GuideReference(resource, type, title, StringUtil.substringAfter(resourceHref, FRAGMENT_SEPARATOR_CHAR));
			guide.addReference(reference);
		}
	}

	static Resources fixHrefs(String packageHref,
			Resources resourcesByHref) {
		int lastSlashPos = packageHref.lastIndexOf('/');
		if(lastSlashPos < 0) {
			return resourcesByHref;
		}
		Resources result = new Resources();
		for(Resource resource: resourcesByHref.getAll()) {
			if(StringUtil.isNotBlank(resource.getHref())
					&& resource.getHref().length() > lastSlashPos) {
				resource.setHref(resource.getHref().substring(lastSlashPos + 1));
			}
			result.add(resource);
		}
		return result;
	}

	private static Spine readSpine(Document packageDocument, Resources resources, Map<String, String> idMapping) {
		
		Element spineElement = DOMUtil.getFirstElementByTagNameNS(packageDocument.getDocumentElement(), NAMESPACE_OPF, OPFTags.spine);
		if (spineElement == null) {
			log.error("Element " + OPFTags.spine + " not found in package document, generating one automatically");
			return generateSpineFromResources(resources);
		}
		Spine result = new Spine();
		String tocResourceId = DOMUtil.getAttribute(spineElement, NAMESPACE_OPF, OPFAttributes.toc);
		result.setTocResource(findTableOfContentsResource(tocResourceId, resources));
		NodeList spineNodes = packageDocument.getElementsByTagNameNS(NAMESPACE_OPF, OPFTags.itemref);
		List<SpineReference> spineReferences = new ArrayList<SpineReference>(spineNodes.getLength());
		for(int i = 0; i < spineNodes.getLength(); i++) {
			Element spineItem = (Element) spineNodes.item(i);
			String itemref = DOMUtil.getAttribute(spineItem, NAMESPACE_OPF, OPFAttributes.idref);
			if(StringUtil.isBlank(itemref)) {
				log.error("itemref with missing or empty idref"); // XXX
				continue;
			}
			String id = idMapping.get(itemref);
			if (id == null) {
				id = itemref;
			}
			Resource resource = resources.getByIdOrHref(id);
			if(resource == null) {
				log.error("resource with id \'" + id + "\' not found");
				continue;
			}
			
			SpineReference spineReference = new SpineReference(resource);
			if (OPFValues.no.equalsIgnoreCase(DOMUtil.getAttribute(spineItem, NAMESPACE_OPF, OPFAttributes.linear))) {
				spineReference.setLinear(false);
			}
			spineReferences.add(spineReference);
		}
		result.setSpineReferences(spineReferences);
		return result;
	}

	private static Spine generateSpineFromResources(Resources resources) {
		Spine result = new Spine();
		List<String> resourceHrefs = new ArrayList<String>();
		resourceHrefs.addAll(resources.getAllHrefs());
		Collections.sort(resourceHrefs, String.CASE_INSENSITIVE_ORDER);
		for (String resourceHref: resourceHrefs) {
			Resource resource = resources.getByHref(resourceHref);
			if (resource.getMediaType() == MediatypeService.NCX) {
				result.setTocResource(resource);
			} else if (resource.getMediaType() == MediatypeService.XHTML) {
				result.addSpineReference(new SpineReference(resource));
			}
		}
		return result;
	}

	static Resource findTableOfContentsResource(String tocResourceId, Resources resources) {
		Resource tocResource = null;
		if (StringUtil.isNotBlank(tocResourceId)) {
			tocResource = resources.getByIdOrHref(tocResourceId);
		}
		
		if (tocResource != null) {
			return tocResource;
		}
		
		// get the first resource with the NCX mediatype
		tocResource = resources.findFirstResourceByMediaType(MediatypeService.NCX);

		if (tocResource == null) {
			for (int i = 0; i < POSSIBLE_NCX_ITEM_IDS.length; i++) {
				tocResource = resources.getByIdOrHref(POSSIBLE_NCX_ITEM_IDS[i]);
				if (tocResource != null) {
					break;
				}
				tocResource = resources.getByIdOrHref(POSSIBLE_NCX_ITEM_IDS[i].toUpperCase());
				if (tocResource != null) {
					break;
				}
			}
		}

		if (tocResource == null) {
			log.error("Could not find table of contents resource. Tried resource with id '" + tocResourceId + "', " + DEFAULT_TOC_ID + ", " + DEFAULT_TOC_ID.toUpperCase() + " and any NCX resource.");
		}
		return tocResource;
	}

	static Set<String> findCoverHrefs(Document packageDocument) {
		
		Set<String> result = new HashSet<String>();
		
		// try and find a meta tag with name = 'cover' and a non-blank id
		String coverResourceId = DOMUtil.getFindAttributeValue(packageDocument, NAMESPACE_OPF,
											OPFTags.meta, OPFAttributes.name, OPFValues.meta_cover,
											OPFAttributes.content);

		if (StringUtil.isNotBlank(coverResourceId)) {
			String coverHref = DOMUtil.getFindAttributeValue(packageDocument, NAMESPACE_OPF,
					OPFTags.item, OPFAttributes.id, coverResourceId,
					OPFAttributes.href);
			if (StringUtil.isNotBlank(coverHref)) {
				result.add(coverHref);
			} else {
				result.add(coverResourceId); // maybe there was a cover href put in the cover id attribute
			}
		}
		// try and find a reference tag with type is 'cover' and reference is not blank
		String coverHref = DOMUtil.getFindAttributeValue(packageDocument, NAMESPACE_OPF,
											OPFTags.reference, OPFAttributes.type, OPFValues.reference_cover,
											OPFAttributes.href);
		if (StringUtil.isNotBlank(coverHref)) {
			result.add(coverHref);
		}
		return result;
	}

	private static void readCover(Document packageDocument, EpubBook epubBook) {
		
		Collection<String> coverHrefs = findCoverHrefs(packageDocument);
		for (String coverHref: coverHrefs) {
			Resource resource = epubBook.getResources().getByHref(coverHref);
			if (resource == null) {
				log.error("Cover resource " + coverHref + " not found");
				continue;
			}
			if (resource.getMediaType() == MediatypeService.XHTML) {
				epubBook.setCoverPage(resource);
			} else if (MediatypeService.isBitmapImage(resource.getMediaType())) {
				epubBook.setCoverImage(resource);
			}
		}
	}
}
