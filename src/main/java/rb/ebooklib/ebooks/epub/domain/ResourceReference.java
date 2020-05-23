package rb.ebooklib.ebooks.epub.domain;

public class ResourceReference {

	Resource resource;

	public ResourceReference(Resource resource) {
		this.resource = resource;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public String getResourceId() {
		if (resource != null) {
			return resource.getId();
		}
		return null;
	}
}
