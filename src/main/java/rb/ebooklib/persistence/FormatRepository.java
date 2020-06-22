package rb.ebooklib.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import rb.ebooklib.model.Format;

import java.util.Optional;

public interface FormatRepository extends JpaRepository<Format, Long> {

    Optional<Format> findOneByName(String name);

}
