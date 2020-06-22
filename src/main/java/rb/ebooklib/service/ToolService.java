package rb.ebooklib.service;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rb.ebooklib.dto.RenameDTO;
import rb.ebooklib.model.*;
import rb.ebooklib.persistence.FormatRepository;
import rb.ebooklib.persistence.RenameRepository;
import rb.ebooklib.persistence.SeparatorRepository;
import rb.ebooklib.util.FormatTable;
import rb.ebooklib.util.RenameUtil;
import rb.ebooklib.util.SeparatorTable;
import rb.ebooklib.util.ViewObjectMappers;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ToolService {

    private static final Logger log = LoggerFactory.getLogger(ToolService.class);

    private Settings settings;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private UserService userService;

    @Autowired
    private RenameRepository renameRepository;

    @Autowired
    private SeparatorRepository separatorRepository;

    @Autowired
    private FormatRepository formatRepository;

    @Autowired
    private ViewObjectMappers viewObjectMappers;

    private static final String RENAME_NOT_FOUND = "Instellingen voor id %d niet gevonden.";

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

    @Transactional
    public void initRename() {
        final Boolean isSeparatorFilled = separatorRepository.count() > 0;
        if (!isSeparatorFilled) {
            fillSeparatorTable();
        }

        final Boolean isFormatFilled = formatRepository.count() > 0;
        if (!isFormatFilled) {
            fillFormatTable();
        }
    }

    private void fillSeparatorTable() {
        ArrayList<SeparatorTable> separatorTables = RenameUtil.getSeparators();
        for (SeparatorTable separatorTable: separatorTables) {
            separatorRepository.save(new Separator(separatorTable.getName(), separatorTable.getValue()));
        }
    }

    private void fillFormatTable() {
        ArrayList<FormatTable> formatTables = RenameUtil.getFormats();
        for (FormatTable formatTable: formatTables) {
            formatRepository.save(new Format(formatTable.getName(), formatTable.getValue()));
        }
    }

    @Transactional
    public List<Separator> getAllSeparators() {
        return separatorRepository.findAll();
    }

    @Transactional
    public Optional<Separator> getSeparatorByName(final String name) {
        return separatorRepository.findOneByName(name);
    }


    @Transactional
    public List<Format> getAllFormats() {
        return formatRepository.findAll();
    }

    @Transactional
    public Rename createRename(final RenameDTO renameDTO) {
        final User user = userService.getCurrentlyLoggedInUser();
        var dbRename = renameRepository.findOneByUserId(user.getId());

        if (dbRename.isPresent()) {
            return updateRename(renameDTO);
        }
        else {
            final Rename rename = viewObjectMappers.convertRenameDtoToRename(renameDTO);
            rename.setUserId(user.getId());
            return renameRepository.save(rename);
        }
    }

    @Transactional
    public Rename updateRename(final RenameDTO renameDTO) {
        final Rename rename = renameRepository.findById(renameDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException(String.format(RENAME_NOT_FOUND, renameDTO.getId())));

        rename.setId(renameDTO.getId());
        rename.setUserId(renameDTO.getUserId());
        rename.setSourceMap(renameDTO.getSourceMap());
        rename.setSourceTitleAuthorSeparator(renameDTO.getSourceTitleAuthorSeparator());
        rename.setSourceAuthornameSeparator(renameDTO.getSourceAuthornameSeparator());
        rename.setSourceFormat(renameDTO.getSourceFormat());
        rename.setDestMap(renameDTO.getDestMap());
        rename.setDestTitleAuthorSeparator(renameDTO.getDestTitleAuthorSeparator());
        rename.setDestAuthornameSeparator(renameDTO.getDestAuthornameSeparator());
        rename.setDestFormat(renameDTO.getDestFormat());

        return renameRepository.save(rename);
    }


    public Rename getByUserId(final Long userId) {
//        Optional<Rename> renameOptional = renameRepository.findOneByUserId(userId);
//        if (renameOptional.isPresent()) {
//            return renameOptional.get();
//        }
        return this.renameRepository.findOneByUserId(userId).orElseThrow(() -> new EntityNotFoundException());
    }
    
    public Rename getStandardRename(final Long userId) {
        Rename rename = new Rename();

        User user = userService.getCurrentlyLoggedInUser();
        settings = settingsService.getByUserId(user.getId());

//        Separator sourceTitleAuthorSeparator = separatorRepository.findOneByName("HYPHEN").get();
//        Separator sourceAuthornameSeparator = separatorRepository.findOneByName("COMMA").get();
//        Separator destTitleAuthorSeparator = separatorRepository.findOneByName("HYPHEN").get();
//        Separator destAuthornameSeparator = separatorRepository.findOneByName("COMMA").get();
//
//        Format sourceFormat = formatRepository.findOneByName("tav").get();
//        Format destFormat = formatRepository.findOneByName("avt").get();
        rename.setUserId(userId);
        rename.setSourceMap(settings.getLibraryMap());
        rename.setSourceTitleAuthorSeparator("DASH");
        rename.setSourceAuthornameSeparator("COMMA");
        rename.setSourceFormat("tav");

        rename.setDestMap(settings.getLibraryMap());
        rename.setDestTitleAuthorSeparator("DASH");
        rename.setDestAuthornameSeparator("COMMA");
        rename.setDestFormat("avt");
        
        return rename;
    }

    public void rename() {
        log.info("---- RENAME ------");
    }

}
