package rb.ebooklib.ebooks.epub.domain;

import static rb.ebooklib.ebooks.util.StringUtil.isNotBlank;

public class GuideReference extends TitledResourceReference {

    /**
     * the book cover(s), jacket information, etc.
     */
    public static final String COVER = "cover";
	/**
	 * human-readable page with title, author, publisher, and other metadata
	 */
	public static String TITLE_PAGE = "title-page";
	
	/**
	 * Human-readable table of contents.
	 * Not to be confused the file table of contents
	 * 
	 */
	public static String TOC = "toc";
	
	/**
	 * back-of-book style index
	 */
	public static String INDEX = "index";
	public static String GLOSSARY = "glossary";
	public static String ACKNOWLEDGEMENTS = "acknowledgements";
	public static String BIBLIOGRAPHY = "bibliography";
	public static String COLOPHON = "colophon";
	public static String COPYRIGHT_PAGE = "copyright-page";
	public static String DEDICATION = "dedication";
	public static String EPIGRAPH = "epigraph";
	public static String FOREWORD = "foreword";
	public static String LOI = "loi";
	public static String LOT = "lot";
	public static String NOTES = "notes";
	public static String PREFACE = "preface";
	public static String TEXT = "text";
    private String type;

	public GuideReference(Resource resource) {
		this(resource, null);
	}
	
	public GuideReference(Resource resource, String title) {
		super(resource, title);
	}
	
	public GuideReference(Resource resource, String type, String title) {
		this(resource, type, title, null);
	}
	
    public GuideReference(Resource resource, String type, String title, String fragmentId) {
        super(resource, title, fragmentId);
        this.type = isNotBlank(type) ? type.toLowerCase() : null;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}