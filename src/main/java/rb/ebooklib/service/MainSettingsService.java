package rb.ebooklib.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rb.ebooklib.dto.MainSettingsDTO;
import rb.ebooklib.model.MainSettings;
import rb.ebooklib.persistence.MainSettingsRepository;
import rb.ebooklib.util.ViewObjectMappers;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class MainSettingsService {

    @Autowired
    private MainSettingsRepository mainSettingsRepository;
    @Autowired
    private ViewObjectMappers viewObjectMappers;

    private static final String SETTINGS_NOT_FOUND = "Instellingen voor id %d niet gevonden.";

    public MainSettings getMainSettings() {
        List<MainSettings> mainSettings = mainSettingsRepository.findAll();
        if (mainSettings.isEmpty()) {
            throw new EntityNotFoundException("Geen instellingen gevonden");
        }
        else {
            return mainSettings.get(0);
        }
    }

    @Transactional
    public MainSettings createSettings(final MainSettingsDTO mainSettingsDTO) {
        List<MainSettings> dbSettings = mainSettingsRepository.findAll();

        if (dbSettings.isEmpty()) {
            return updateSettings(mainSettingsDTO);
        }
        else {
            final MainSettings mainSettings = viewObjectMappers.convertMainSettingsDtoToMainSettings(mainSettingsDTO);
            return mainSettingsRepository.save(mainSettings);
        }
    }

    @Transactional
    public MainSettings updateSettings(final MainSettingsDTO mainSettingsDTO) {
        final MainSettings mainSettings = mainSettingsRepository.findById(mainSettingsDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException(String.format(SETTINGS_NOT_FOUND, mainSettingsDTO.getId())));

        mainSettings.setLibraryMap(mainSettingsDTO.getLibraryMap());
        mainSettings.setCalibreCommand(mainSettingsDTO.getCalibreCommand());
        return mainSettingsRepository.save(mainSettings);
    }
}
