package rb.ebooklib.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rb.ebooklib.dto.SettingsDTO;
import rb.ebooklib.model.Settings;
import rb.ebooklib.service.SettingsService;

@CrossOrigin
@RestController
@RequestMapping("/settings")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    @PostMapping
    public ResponseEntity<Settings> createSettings(@RequestBody final SettingsDTO settingsDTO) {
        final Settings settings = settingsService.createSettings(settingsDTO);
        return new ResponseEntity<>(settings, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<Settings> updateSettings(@RequestBody final SettingsDTO settingsDTO) {
        final Settings settings = settingsService.updateSettings(settingsDTO);
        return new ResponseEntity<>(settings, HttpStatus.OK);
    }

    @GetMapping("/settingsId")
    public ResponseEntity<Settings> findById(@RequestParam(value="settingsId") final Long settingsId) {
        final Settings settings = settingsService.getById(settingsId);
        return new ResponseEntity<>(settings, HttpStatus.OK);
    }

    @GetMapping("/userId")
    public ResponseEntity<Settings> findByUserId(@RequestParam(value="userId") final Long userId) {
        final Settings settings = settingsService.getByUserId(userId);
        return new ResponseEntity<>(settings, HttpStatus.OK);
    }
}
