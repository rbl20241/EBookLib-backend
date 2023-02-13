package rb.ebooklib.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.List;
import java.util.Objects;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "book")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Book {

    @Id
    @GeneratedValue
    private Long id;
    private String filename;
    private String isbn;
    private String author;
    private String title;
    private String imageLink;
    private String tempImageLink;
    private String libraryMap;
    private String publisher;
    private String extension;
    private String language;

    @Column(length=10000)
    private String description;

    private String isRead;

    @OneToMany(cascade= ALL, fetch= LAZY)
    @ToString.Exclude
    private List<Identifier> identifiers;

    @ManyToMany(cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @ToString.Exclude
    private List<Author> authors;

    @ManyToMany(cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @ToString.Exclude
    private List<Category> categories;

    @ManyToOne(cascade = ALL)
    @JoinColumn(name = "genre_id")
    private Genre genre;

    private String timestamp;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Book book = (Book) o;
        return id != null && Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
