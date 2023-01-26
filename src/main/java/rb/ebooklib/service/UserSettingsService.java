package rb.ebooklib.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rb.ebooklib.dto.UserSettingsDTO;
import rb.ebooklib.model.UserSettings;
import rb.ebooklib.model.User;
import rb.ebooklib.persistence.UserSettingsRepository;
import rb.ebooklib.util.ViewObjectMappers;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class UserSettingsService {

    @Autowired
    private UserService userService;
    @Autowired
    private UserSettingsRepository userSettingsRepository;
    @Autowired
    private ViewObjectMappers viewObjectMappers;

    private static final String SETTINGS_NOT_FOUND = "Instellingen voor id %d niet gevonden.";

    public UserSettings getById(final Long settingsId) {
        return this.userSettingsRepository.findById(settingsId)
                .orElseThrow(() -> new EntityNotFoundException("Geen instellingen gevonden voor id " + settingsId));
    }

    public UserSettings getByUserId(final Long userId) {
        return this.userSettingsRepository.findOneByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Geen instellingen gevonden voor userid " + userId));
    }

    @Transactional
    public UserSettings createSettings(final UserSettingsDTO userSettingsDTO) {
        final User user = userService.getCurrentlyLoggedInUser();
        Optional<UserSettings> dbSettings = userSettingsRepository.findOneByUserId(user.getId());

        if (dbSettings.isPresent()) {
            return updateSettings(userSettingsDTO);
        }
        else {
            final UserSettings userSettings = viewObjectMappers.convertUserSettingsDtoToUserSettings(userSettingsDTO);
            userSettings.setUserId(user.getId());
            return userSettingsRepository.save(userSettings);
        }
    }

    @Transactional
    public UserSettings updateSettings(final UserSettingsDTO userSettingsDTO) {
        final UserSettings userSettings = userSettingsRepository.findById(userSettingsDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException(String.format(SETTINGS_NOT_FOUND, userSettingsDTO.getId())));

        userSettings.setCopyTo(userSettingsDTO.getCopyTo());
        userSettings.setMailTo(userSettingsDTO.getMailTo());
        userSettings.setIsDateSort(userSettingsDTO.getIsDateSort());
        userSettings.setIsNameSort(userSettingsDTO.getIsNameSort());
        userSettings.setIsEpubSelected(userSettingsDTO.getIsEpubSelected());
        userSettings.setIsMobiSelected(userSettingsDTO.getIsMobiSelected());
        userSettings.setIsPdfSelected(userSettingsDTO.getIsPdfSelected());
        userSettings.setIsCbrSelected(userSettingsDTO.getIsCbrSelected());
        userSettings.setMailHost(userSettingsDTO.getMailHost());
        userSettings.setMailPort(userSettingsDTO.getMailPort());
        userSettings.setMailUserName(userSettingsDTO.getMailUserName());
        userSettings.setMailPassword(userSettingsDTO.getMailPassword());
        return userSettingsRepository.save(userSettings);
    }
}
