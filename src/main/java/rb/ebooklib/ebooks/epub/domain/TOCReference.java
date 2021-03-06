package rb.ebooklib.ebooks.epub.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TOCReference extends TitledResourceReference {

	private List<TOCReference> children;

//	private static final Comparator<TOCReference> COMPARATOR_BY_TITLE_IGNORE_CASE = new Comparator<>() {
//
//		@Override
//		public int compare(TOCReference tocReference1, TOCReference tocReference2) {
//			return String.CASE_INSENSITIVE_ORDER.compare(tocReference1.getTitle(), tocReference2.getTitle());
//		}
//	};

	private static final Comparator<TOCReference> COMPARATOR_BY_TITLE_IGNORE_CASE = (tocReference1, tocReference2) -> String.CASE_INSENSITIVE_ORDER.compare(tocReference1.getTitle(), tocReference2.getTitle());

	public TOCReference() {
		this(null, null, null);
	}
	
	public TOCReference(String name, Resource resource) {
		this(name, resource, null);
	}
	
	public TOCReference(String name, Resource resource, String fragmentId) {
		this(name, resource, fragmentId, new ArrayList<TOCReference>());
	}
	
	public TOCReference(String title, Resource resource, String fragmentId, List<TOCReference> children) {
		super(resource, title, fragmentId);
		this.children = children;
	}

	public static Comparator<TOCReference> getComparatorByTitleIgnoreCase() {
		return COMPARATOR_BY_TITLE_IGNORE_CASE;
	}
	
	public List<TOCReference> getChildren() {
		return children;
	}

	public TOCReference addChildSection(TOCReference childSection) {
		this.children.add(childSection);
		return childSection;
	}
	
	public void setChildren(List<TOCReference> children) {
		this.children = children;
	}
}
