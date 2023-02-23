package rb.ebooklib.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rb.ebooklib.dto.MainSettingsDTO;
import rb.ebooklib.models.MainSettings;
import rb.ebooklib.services.MainSettingsService;

@CrossOrigin
@RestController
@RequestMapping("/mainsettings")
public class MainSettingsController {

    @Autowired
    private MainSettingsService mainSettingsService;

    @PostMapping("/")
    public ResponseEntity<MainSettings> createMainSettings(@RequestBody final MainSettingsDTO mainSettingsDTO) {
        final MainSettings mainSettings = mainSettingsService.createSettings(mainSettingsDTO);
        return new ResponseEntity<>(mainSettings, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<MainSettings> updateMainSettings(@RequestBody final MainSettingsDTO mainSettingsDTO) {
        final MainSettings mainSettings = mainSettingsService.updateSettings(mainSettingsDTO);
        return new ResponseEntity<>(mainSettings, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<MainSettings> findMainSettings() {
        final MainSettings mainSettings = mainSettingsService.getMainSettings();
        return new ResponseEntity<>(mainSettings, HttpStatus.OK);
    }
}
