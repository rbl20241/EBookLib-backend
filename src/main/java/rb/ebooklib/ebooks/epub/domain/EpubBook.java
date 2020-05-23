package rb.ebooklib.ebooks.epub.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;




public class EpubBook {
	
	private Resources resources = new Resources();
	private Metadata metadata = new Metadata();
	private Spine spine = new Spine();
	private TableOfContents tableOfContents = new TableOfContents();
	private Guide guide = new Guide();
	private Resource opfResource;
	private Resource ncxResource;
	private Resource coverImage;
	
	public TOCReference addSection(TOCReference parentSection, String sectionTitle,
			Resource resource) {
		getResources().add(resource);
		if (spine.findFirstResourceById(resource.getId()) < 0)  {
			spine.addSpineReference(new SpineReference(resource));
		}
		return parentSection.addChildSection(new TOCReference(sectionTitle, resource));
	}

	public void generateSpineFromTableOfContents() {
		Spine spine = new Spine(tableOfContents);
		
		// in case the tocResource was already found and assigned
		spine.setTocResource(this.spine.getTocResource());
		
		this.spine = spine;
	}
	
	public TOCReference addSection(String title, Resource resource) {
		getResources().add(resource);
		TOCReference tocReference = tableOfContents.addTOCReference(new TOCReference(title, resource));
		if (spine.findFirstResourceById(resource.getId()) < 0)  {
			spine.addSpineReference(new SpineReference(resource));
		}
		return tocReference;
	}
	
	public Metadata getMetadata() {
		return metadata;
	}
	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public void setResources(Resources resources) {
		this.resources = resources;
	}

	public Resource addResource(Resource resource) {
		return resources.add(resource);
	}
	
	public Resources getResources() {
		return resources;
	}

	public Spine getSpine() {
		return spine;
	}

	public void setSpine(Spine spine) {
		this.spine = spine;
	}

	public TableOfContents getTableOfContents() {
		return tableOfContents;
	}

	public void setTableOfContents(TableOfContents tableOfContents) {
		this.tableOfContents = tableOfContents;
	}
	
	public Resource getCoverPage() {
		Resource coverPage = guide.getCoverPage();
		if (coverPage == null) {
			coverPage = spine.getResource(0);
		}
		return coverPage;
	}

	public void setCoverPage(Resource coverPage) {
		if (coverPage == null) {
			return;
		}
		if (! resources.containsByHref(coverPage.getHref())) {
			resources.add(coverPage);
		}
		guide.setCoverPage(coverPage);
	}
	
	public String getTitle() {
		return getMetadata().getFirstTitle();
	}

	public Resource getCoverImage() {
		return coverImage;
	}

	public void setCoverImage(Resource coverImage) {
		if (coverImage == null) {
			return;
		}
		if (! resources.containsByHref(coverImage.getHref())) {
			resources.add(coverImage);
		}
		this.coverImage = coverImage;
	}
	
	public Guide getGuide() {
		return guide;
	}

	public List<Resource> getContents() {
		Map<String, Resource> result = new LinkedHashMap<String, Resource>();
		addToContentsResult(getCoverPage(), result);

		for (SpineReference spineReference: getSpine().getSpineReferences()) {
			addToContentsResult(spineReference.getResource(), result);
		}

		for (Resource resource: getTableOfContents().getAllUniqueResources()) {
			addToContentsResult(resource, result);
		}
		
		for (GuideReference guideReference: getGuide().getReferences()) {
			addToContentsResult(guideReference.getResource(), result);
		}

		return new ArrayList<Resource>(result.values());
	}
	
	private static void addToContentsResult(Resource resource, Map<String, Resource> allReachableResources){
		if (resource != null && (! allReachableResources.containsKey(resource.getHref()))) {
			allReachableResources.put(resource.getHref(), resource);
		}
	}

	public Resource getOpfResource() {
		return opfResource;
	}
	
	public void setOpfResource(Resource opfResource) {
		this.opfResource = opfResource;
	}
	
	public void setNcxResource(Resource ncxResource) {
		this.ncxResource = ncxResource;
	}

	public Resource getNcxResource() {
		return ncxResource;
	}
}

