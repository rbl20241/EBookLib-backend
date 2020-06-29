package rb.ebooklib.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import rb.ebooklib.model.UserSettings;

import java.util.Optional;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {

    Optional<UserSettings> findOneByUserId(Long userId);

}
