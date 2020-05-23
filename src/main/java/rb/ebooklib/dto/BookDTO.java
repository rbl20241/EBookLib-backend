package rb.ebooklib.dto;

import lombok.Data;
import rb.ebooklib.model.Identifier;
import rb.ebooklib.model.Author;
import rb.ebooklib.model.Category;
import rb.ebooklib.model.Genre;

import java.util.List;

@Data
public class BookDTO {
    private Long id;
    private String filename;
    private String isbn;
    private String author;
    private String title;
    private String libraryMap;
    private String imageLink;
    private String publisher;
    private String extension;
    private String description;
    private Genre genre;
    private List<Identifier> identifiers;
    private List<Author> authors;
    private List<Category> categories;

}
