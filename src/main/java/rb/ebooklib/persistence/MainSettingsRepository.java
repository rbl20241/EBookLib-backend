package rb.ebooklib.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import rb.ebooklib.model.MainSettings;

import java.util.Optional;

public interface MainSettingsRepository extends JpaRepository<MainSettings, Long> {

    Optional<MainSettings> findOneByLibraryMap(String libraryMap);

}
