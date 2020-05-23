package rb.ebooklib.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

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
    private String libraryMap;
    private String imageLink;
    private String publisher;
    private String extension;

    @Column(length=10000)
    private String description;

    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    private List<Identifier> identifiers;

    @ManyToOne(cascade = CascadeType.ALL)
    private Genre genre;

    @ManyToMany
    private List<Author> authors;

    @ManyToMany
    private List<Category> categories;

    private String timestamp;

}
