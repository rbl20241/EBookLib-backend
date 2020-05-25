package rb.ebooklib.ebooks.epub.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static rb.ebooklib.util.StringUtil.isBlank;

public class Spine implements Serializable {

	private Resource tocResource;
	private List<SpineReference> spineReferences;

	public Spine() {
		this(new ArrayList<>());
	}
	
	public Spine(TableOfContents tableOfContents) {
		this.spineReferences = createSpineReferences(tableOfContents.getAllUniqueResources());
	}

	public Spine(List<SpineReference> spineReferences) {
		this.spineReferences = spineReferences;
	}

	private static List<SpineReference> createSpineReferences(Collection<Resource> resources) {
		List<SpineReference> result = new ArrayList<>(resources.size());
		for (Resource resource: resources) {
			result.add(new SpineReference(resource));
		}
		return result;
	}
	
	public List<SpineReference> getSpineReferences() {
		return spineReferences;
	}
	public void setSpineReferences(List<SpineReference> spineReferences) {
		this.spineReferences = spineReferences;
	}

	public Resource getResource(int index) {
		if (index < 0 || index >= spineReferences.size()) {
			return null;
		}
		return spineReferences.get(index).getResource();
	}
	
	public int findFirstResourceById(String resourceId) {
		if (isBlank(resourceId)) {
			return -1;
		}
		
		for (int i = 0; i < spineReferences.size(); i++) {
			SpineReference spineReference = spineReferences.get(i);
			if (resourceId.equals(spineReference.getResourceId())) {
				return i;
			}
		}
		return -1;
	}
	
	public SpineReference addSpineReference(SpineReference spineReference) {
		if (spineReferences == null) {
			this.spineReferences = new ArrayList<>();
		}
		spineReferences.add(spineReference);
		return spineReference;
	}

	public SpineReference addResource(Resource resource) {
		return addSpineReference(new SpineReference(resource));
	}

	public int size() {
		return spineReferences.size();
	}

	public void setTocResource(Resource tocResource) {
		this.tocResource = tocResource;
	}

	public Resource getTocResource() {
		return tocResource;
	}

	public int getResourceIndex(Resource currentResource) {
		if (currentResource == null) {
			return -1;
		}
		return getResourceIndex(currentResource.getHref());
	}

	private int getResourceIndex(String resourceHref) {
		int result = -1;
		if (isBlank(resourceHref)) {
			return result;
		}
		for (int i = 0; i < spineReferences.size(); i++) {
			if (resourceHref.equals(spineReferences.get(i).getResource().getHref())) {
				result = i;
				break;
			}
		}
		return result;
	}

	public boolean isEmpty() {
		return spineReferences.isEmpty();
	}
}
