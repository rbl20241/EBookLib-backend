package rb.ebooklib.ebooks.epub.domain;

import rb.ebooklib.ebooks.util.StringUtil;

public class EpubAuthor {
	
	private String firstname;
	private String lastname;
	private Relator relator = Relator.AUTHOR;
	
	public EpubAuthor(String singleName) {
		this("", singleName);
	}
	
	public EpubAuthor(String firstname, String lastname) {
		this.firstname = firstname;
		this.lastname = lastname;
	}
	
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	
	public String toString() {
		return lastname + ", " + firstname;
	}
	
	public int hashCode() {
		return StringUtil.hashCode(firstname, lastname);
	}

	public boolean equals(Object authorObject) {
		if(! (authorObject instanceof EpubAuthor)) {
			return false;
		}
		EpubAuthor other = (EpubAuthor) authorObject;
		return StringUtil.equals(firstname, other.firstname)
		 && StringUtil.equals(lastname, other.lastname);
	}

	public Relator setRole(String code) {
		Relator result = Relator.byCode(code);
		if (result == null) {
			result = Relator.AUTHOR;
		}
		this.relator = result;
		return result;
	}

	public Relator getRelator() {
		return relator;
	}

	public void setRelator(Relator relator) {
		this.relator = relator;
	}
}
