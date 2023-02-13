package rb.ebooklib.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rb.ebooklib.models.MainSettings;

import java.util.Optional;

@Repository
public interface MainSettingsRepository extends JpaRepository<MainSettings, Long> {

    Optional<MainSettings> findOneByLibraryMap(String libraryMap);

}
