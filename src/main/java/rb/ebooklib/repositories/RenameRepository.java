package rb.ebooklib.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rb.ebooklib.models.Rename;

import java.util.Optional;

@Repository
public interface RenameRepository extends JpaRepository<Rename, Long> {

    Optional<Rename> findOneByUserId(Long userId);

}
