package rb.ebooklib.ebooks.epub.domain;

import rb.ebooklib.models.Identifier;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static rb.ebooklib.ebooks.epub.service.MediatypeService.EPUB;
import static rb.ebooklib.util.StringUtil.isNotBlank;

public class Metadata {

	private static final String DEFAULT_LANGUAGE = "en";

	private boolean autoGeneratedId;
    private List<EpubAuthor> epubAuthors = new ArrayList<>();
    
    private List<EpubAuthor> contributors = new ArrayList<>();
    private List<Date> dates = new ArrayList<>();
    private String language = DEFAULT_LANGUAGE;
    
    private Map<QName, String> otherProperties = new HashMap<>();
    private List<String> rights = new ArrayList<>();
    private List<String> titles = new ArrayList<>();
    private List<Identifier> identifiers = new ArrayList<>();
    private List<String> subjects = new ArrayList<>();
    private String format;
    private List<String> types = new ArrayList<>();
    private List<String> descriptions = new ArrayList<>();
    private List<String> publishers = new ArrayList<>();
    private int numberOfPages;
    
    private Map<String, String> metaAttributes = new HashMap<>();

    public Metadata() {
        format = setDefaultFormat();
        identifiers.add(new Identifier());
		autoGeneratedId = true;
	}

    public String setDefaultFormat() {
        return EPUB.getName();
    }

	public boolean isAutoGeneratedId() {
		return autoGeneratedId;
	}

	public Map<QName, String> getOtherProperties() {
		return otherProperties;
	}
    public void setOtherProperties(Map<QName, String> otherProperties) {
        this.otherProperties = otherProperties;
    }

	public Date addDate(Date date) {
		this.dates.add(date);
		return date;
	}
	
    public List<Date> getDates() {
        return dates;
    }
    public void setDates(List<Date> dates) {
        this.dates = dates;
    }

	public EpubAuthor addAuthor(EpubAuthor epubAuthor) {
		epubAuthors.add(epubAuthor);
		return epubAuthor;
	}
	
    public List<EpubAuthor> getEpubAuthors() {
        return epubAuthors;
    }
    public void setEpubAuthors(List<EpubAuthor> epubAuthors) {
        this.epubAuthors = epubAuthors;
    }

	public EpubAuthor addContributor(EpubAuthor contributor) {
		contributors.add(contributor);
		return contributor;
	}
	
	public List<EpubAuthor> getContributors() {
		return contributors;
	}
    public void setContributors(List<EpubAuthor> contributors) {
        this.contributors = contributors;
    }

    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }
    public List<String> getSubjects() {
        return subjects;
    }
    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }
    public void setRights(List<String> rights) {
        this.rights = rights;
    }
    public List<String> getRights() {
        return rights;
    }
    public String getFirstTitle() {
        if (titles == null || titles.isEmpty()) {
            return "";
        }
        for (String title: titles) {
            if (isNotBlank(title)) {
                return title;
            }
        }
        return "";
    }

	public String addTitle(String title) {
		this.titles.add(title);
		return title;
	}
    public void setTitles(List<String> titles) {
        this.titles = titles;
    }
	public List<String> getTitles() {
		return titles;
	}
		
	public String addPublisher(String publisher) {
		this.publishers.add(publisher);
		return publisher;
	}
    public void setPublishers(List<String> publishers) {
        this.publishers = publishers;
    }
    public List<String> getPublishers() {
        return publishers;
    }

	public String addDescription(String description) {
		this.descriptions.add(description);
		return description;
	}
    public void setDescriptions(List<String> descriptions) {
        this.descriptions = descriptions;
    }
    public List<String> getDescriptions() {
        return descriptions;
    }

	public Identifier addIdentifier(Identifier identifier) {
		if (autoGeneratedId && (! (identifiers.isEmpty()))) {
			identifiers.set(0, identifier);
		} else {
			identifiers.add(identifier);
		}
		autoGeneratedId = false;
		return identifier;
	}
    public void setIdentifiers(List<Identifier> identifiers) {
        this.identifiers = identifiers;
		autoGeneratedId = false;
    }

    public List<Identifier> getIdentifiers() {
        return identifiers;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    public String getFormat() {
        return format;
    }

	public String addType(String type) {
		this.types.add(type);
		return type;
	}
	
    public List<String> getTypes() {
        return types;
    }
    public void setTypes(List<String> types) {
        this.types = types;
    }

	public String getMetaAttribute(String name) {
		return metaAttributes.get(name);
	}

    public void setMetaAttributes(Map<String, String> metaAttributes) {
        this.metaAttributes = metaAttributes;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }
}
