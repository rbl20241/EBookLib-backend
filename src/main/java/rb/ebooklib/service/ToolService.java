package rb.ebooklib.service;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rb.ebooklib.model.Book;
import rb.ebooklib.model.Settings;
import rb.ebooklib.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ToolService {

    private static final Logger log = LoggerFactory.getLogger(ToolService.class);

    private Settings settings;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private UserService userService;

    public void runCalibre(final Book book) {
        User user = userService.getCurrentlyLoggedInUser();
        settings = settingsService.getByUserId(user.getId());

        String pathToBook = book.getFilename();
        List<String> params = new ArrayList<>();

        if (SystemUtils.IS_OS_LINUX) {
            params.add(settings.getCalibreCommand());
            params.add(pathToBook);
        }
        else {
            params.add("\"" + settings.getCalibreCommand() + "\"");
            params.add("\"" + pathToBook + "\"");
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(params);
            @SuppressWarnings("unused") Process p = pb.start();
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

}
