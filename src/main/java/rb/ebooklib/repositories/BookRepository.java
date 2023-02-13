package rb.ebooklib.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import rb.ebooklib.models.Author;
import rb.ebooklib.models.Book;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    Optional<Book> findOneByFilename(String filename);
    List<Author> findAuthorsByFilename(String filename);
    List<Book> findAll();
}
