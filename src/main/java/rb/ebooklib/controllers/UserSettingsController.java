package rb.ebooklib.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rb.ebooklib.dto.UserSettingsDTO;
import rb.ebooklib.models.UserSettings;
import rb.ebooklib.services.UserSettingsService;

@CrossOrigin
@RestController
@RequestMapping("/usersettings")
public class UserSettingsController {

    @Autowired
    private UserSettingsService userSettingsService;

    @PostMapping
    public ResponseEntity<UserSettings> createUserSettings(@RequestBody final UserSettingsDTO userSettingsDTO) {
        final UserSettings userSettings = userSettingsService.createSettings(userSettingsDTO);
        return new ResponseEntity<>(userSettings, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<UserSettings> updateUserSettings(@RequestBody final UserSettingsDTO userSettingsDTO) {
        final UserSettings userSettings = userSettingsService.updateSettings(userSettingsDTO);
        return new ResponseEntity<>(userSettings, HttpStatus.OK);
    }

    @GetMapping("/settingsId")
    public ResponseEntity<UserSettings> findById(@RequestParam(value="settingsId") final Long settingsId) {
        final UserSettings userSettings = userSettingsService.getById(settingsId);
        return new ResponseEntity<>(userSettings, HttpStatus.OK);
    }

    @GetMapping("/userId")
    public ResponseEntity<UserSettings> findByUserId(@RequestParam(value="userId") final Long userId) {
        final UserSettings userSettings = userSettingsService.getByUserId(userId);
        return new ResponseEntity<>(userSettings, HttpStatus.OK);
    }
}
