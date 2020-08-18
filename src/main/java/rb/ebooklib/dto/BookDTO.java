package rb.ebooklib.dto;

import lombok.Data;
import rb.ebooklib.model.Author;
import rb.ebooklib.model.Category;
import rb.ebooklib.model.Genre;
import rb.ebooklib.model.Identifier;

import java.util.List;

@Data
public class BookDTO {
    private Long id;
    private String filename;
    private String isbn;
    private String author;
    private String title;
    private String imageLink;
    private String libraryMap;
    private String publisher;
    private String extension;
    private String language;
    private String description;
    private String isRead;
    private List<Identifier> identifiers;
    private List<Author> authors;
    private List<Category> categories;
    private Genre genre;
    private String timestamp;

}
