package rb.ebooklib.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import rb.ebooklib.model.Author;
import rb.ebooklib.model.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    Optional<Book> findOneByFilename(String filename);
    List<Author> findAuthorsByFilename(String filename);
    List<Book> findAll();
}
