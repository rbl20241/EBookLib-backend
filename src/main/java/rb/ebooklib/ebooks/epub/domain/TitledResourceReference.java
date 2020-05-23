package rb.ebooklib.ebooks.epub.domain;

import static rb.ebooklib.ebooks.util.Constants.FRAGMENT_SEPARATOR_CHAR;
import static rb.ebooklib.ebooks.util.StringUtil.isBlank;

public class TitledResourceReference extends ResourceReference {

    private String fragmentId;
    private String title;

	public TitledResourceReference(Resource resource) {
		this(resource, null);
	}

	public TitledResourceReference(Resource resource, String title) {
		this(resource, title, null);
	}
	
	public TitledResourceReference(Resource resource, String title, String fragmentId) {
		super(resource);
		this.title = title;
		this.fragmentId = fragmentId;
	}
	
	public String getFragmentId() {
		return fragmentId;
	}

	public void setFragmentId(String fragmentId) {
		this.fragmentId = fragmentId;
	}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


	public String getCompleteHref() {
		if (isBlank(fragmentId)) {
			return resource.getHref();
		} else {
			return resource.getHref() + FRAGMENT_SEPARATOR_CHAR + fragmentId;
		}
	}
	
	private void setResource(Resource resource, String fragmentId) {
		super.setResource(resource);
		this.fragmentId = fragmentId;
	}

    public void setResource(Resource resource) {
        setResource(resource, null);
    }
}