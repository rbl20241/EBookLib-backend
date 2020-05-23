package rb.ebooklib.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import rb.ebooklib.model.Settings;

import java.util.Optional;

public interface SettingsRepository extends JpaRepository<Settings, Long> {

    Optional<Settings> findOneByLibraryMap(String libraryMap);
    Optional<Settings> findOneByUserId(Long userId);

}
