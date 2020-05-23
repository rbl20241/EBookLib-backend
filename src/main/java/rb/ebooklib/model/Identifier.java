package rb.ebooklib.model;

import lombok.Data;
import rb.ebooklib.ebooks.util.StringUtil;
import rb.ebooklib.util.Scheme;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

import static rb.ebooklib.ebooks.util.StringUtil.defaultIfNull;

@Entity
@Table(name = "identifier")
@Data
public class Identifier {

    @Id
    @GeneratedValue
    private Long id;

    private boolean bookId = false;
    private String scheme;
    private String value;

    public Identifier() {
        this(Scheme.UUID, UUID.randomUUID().toString());
    }

    public Identifier(String scheme, String value) {
        this.scheme = scheme;
        this.value = value;
    }

//	public static Identifier getBookIdIdentifier(List<Identifier> identifiers) {
//		if(identifiers == null || identifiers.isEmpty()) {
//			return null;
//		}
//
//		Identifier result = null;
//		for(Identifier identifier: identifiers) {
//			if(identifier.isBookId()) {
//				result = identifier;
//				break;
//			}
//		}
//
//		if(result == null) {
//			result = identifiers.get(0);
//		}
//
//		return result;
//	}
	
//	public String getScheme() {
//		return scheme;
//	}
//	public void setScheme(String scheme) {
//		this.scheme = scheme;
//	}
//    public String getValue() {
//        return value;
//    }
//
//    public void setValue(String value) {
//        this.value = value;
//    }
//
//    public void setBookId(boolean bookId) {
//        this.bookId = bookId;
//    }
//
//	private boolean isBookId() {
//		return bookId;
//	}

    public int hashCode() {
        return defaultIfNull(scheme).hashCode() ^ defaultIfNull(value).hashCode();
    }

    public boolean equals(Object otherIdentifier) {
        if(! (otherIdentifier instanceof Identifier)) {
            return false;
        }
        return StringUtil.equals(scheme, ((Identifier) otherIdentifier).scheme)
                && StringUtil.equals(value, ((Identifier) otherIdentifier).value);
    }

    public String toString() {
        if (StringUtil.isBlank(scheme)) {
            return "" + value;
        }
        return "" + scheme + ":" + value;
    }
}