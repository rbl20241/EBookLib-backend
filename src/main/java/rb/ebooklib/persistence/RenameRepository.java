package rb.ebooklib.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import rb.ebooklib.model.Rename;

import java.util.Optional;

public interface RenameRepository extends JpaRepository<Rename, Long> {

    Optional<Rename> findOneByUserId(Long userId);

}
