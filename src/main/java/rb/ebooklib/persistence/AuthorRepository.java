package rb.ebooklib.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rb.ebooklib.model.Author;

import java.util.List;
import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    List<Author> findAllByOrderByNameAsc();

    Optional<Author> findOneByName(String name);

}
