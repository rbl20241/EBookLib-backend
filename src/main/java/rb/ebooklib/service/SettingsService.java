package rb.ebooklib.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rb.ebooklib.dto.SettingsDTO;
import rb.ebooklib.model.Settings;
import rb.ebooklib.model.User;
import rb.ebooklib.persistence.SettingsRepository;
import rb.ebooklib.util.ViewObjectMappers;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class SettingsService {

    @Autowired
    private UserService userService;
    @Autowired
    private SettingsRepository settingsRepository;
    @Autowired
    private ViewObjectMappers viewObjectMappers;

    private static final String SETTINGS_NOT_FOUND = "Instellingen voor id %d niet gevonden.";

    public Settings getById(final Long settingsId) {
        return this.settingsRepository.findById(settingsId)
                .orElseThrow(() -> new EntityNotFoundException("Geen instellingen gevonden voor id " + settingsId));
    }

    public Settings getByUserId(final Long userId) {
        return this.settingsRepository.findOneByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Geen instellingen gevonden voor userid " + userId));
    }

    @Transactional
    public Settings createSettings(final SettingsDTO settingsDTO) {
        final User user = userService.getCurrentlyLoggedInUser();
        Optional<Settings> dbSettings = settingsRepository.findOneByUserId(user.getId());

        if (dbSettings.isPresent()) {
            return updateSettings(settingsDTO);
        }
        else {
            final Settings settings = viewObjectMappers.convertSettingsDtoToSettings(settingsDTO);
            settings.setUserId(user.getId());
            return settingsRepository.save(settings);
        }
    }

    @Transactional
    public Settings updateSettings(final SettingsDTO settingsDTO) {
        final Settings settings = settingsRepository.findById(settingsDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException(String.format(SETTINGS_NOT_FOUND, settingsDTO.getId())));

        settings.setLibraryMap(settingsDTO.getLibraryMap());
        settings.setCalibreCommand(settingsDTO.getCalibreCommand());
        settings.setCopyTo(settingsDTO.getCopyTo());
        settings.setMailTo(settingsDTO.getMailTo());
        settings.setIsDateSort(settingsDTO.getIsDateSort());
        settings.setIsNameSort(settingsDTO.getIsNameSort());
        settings.setIsEpubSelected(settingsDTO.getIsEpubSelected());
        settings.setIsMobiSelected(settingsDTO.getIsMobiSelected());
        settings.setIsPdfSelected(settingsDTO.getIsPdfSelected());
        settings.setIsCbrSelected(settingsDTO.getIsCbrSelected());
        settings.setMailHost(settingsDTO.getMailHost());
        settings.setMailPort(settingsDTO.getMailPort());
        settings.setMailUserName(settingsDTO.getMailUserName());
        settings.setMailPassword(settingsDTO.getMailPassword());
        return settingsRepository.save(settings);
    }
}
