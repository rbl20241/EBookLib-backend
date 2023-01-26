package rb.ebooklib.model;

import lombok.Data;

import jakarta.persistence.*;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "book")
@Data
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
    private List<Identifier> identifiers;

    @ManyToMany(cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    private List<Author> authors;

    @ManyToMany(cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    private List<Category> categories;

    @ManyToOne(cascade = ALL)
    @JoinColumn(name = "genre_id")
    private Genre genre;

    private String timestamp;

}
