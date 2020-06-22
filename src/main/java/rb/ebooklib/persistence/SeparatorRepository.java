package rb.ebooklib.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import rb.ebooklib.model.Separator;

import java.util.Optional;

public interface SeparatorRepository extends JpaRepository<Separator, Long> {

    Optional<Separator> findOneByName(String name);

}
