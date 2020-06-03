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
    private String imageLink;
    private String libraryMap;
    private String publisher;
    private String extension;

    @Column(length=10000)
    private String description;

    private String isRead;

    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    private List<Identifier> identifiers;

    @ManyToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    private List<Author> authors;

    @ManyToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    private List<Category> categories;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "genre_id")
    private Genre genre;

    private String timestamp;

}
