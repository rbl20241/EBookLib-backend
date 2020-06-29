package rb.ebooklib.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rb.ebooklib.dto.RenameDTO;
import rb.ebooklib.exception.RenameException;
import rb.ebooklib.model.*;
import rb.ebooklib.persistence.RenameRepository;
import rb.ebooklib.util.ViewObjectMappers;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ToolService {

    private static final Logger log = LoggerFactory.getLogger(ToolService.class);

    private MainSettings mainSettings;

    @Autowired
    private MainSettingsService mainSettingsService;

    @Autowired
    private UserService userService;

    @Autowired
    private RenameRepository renameRepository;

    @Autowired
    private ViewObjectMappers viewObjectMappers;

    private static final String RENAME_NOT_FOUND = "Instellingen voor id %d niet gevonden.";

    public void runCalibre(final Book book) {
        mainSettings = mainSettingsService.getMainSettings();

        String pathToBook = book.getFilename();
        List<String> params = new ArrayList<>();

        if (SystemUtils.IS_OS_LINUX) {
            params.add(mainSettings.getCalibreCommand());
            params.add(pathToBook);
        }
        else {
            params.add("\"" + mainSettings.getCalibreCommand() + "\"");
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
    public Rename saveRename(final RenameDTO renameDTO) {
        final User user = userService.getCurrentlyLoggedInUser();
        Optional<Rename> dbRename = renameRepository.findOneByUserId(user.getId());

        if (dbRename.isPresent()) {
            return updateRename(renameDTO);
        }
        else {
            final Rename rename = viewObjectMappers.convertRenameDtoToRename(renameDTO);
            rename.setUserId(user.getId());
            return renameRepository.save(rename);
        }
    }

    private Rename updateRename(final RenameDTO renameDTO) {
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
        return renameRepository.findOneByUserId(userId).orElseGet(() -> getStandardRename(userId));
    }
    
    private Rename getStandardRename(final Long userId) {
        Rename rename = new Rename();

        User user = userService.getCurrentlyLoggedInUser();
        mainSettings = mainSettingsService.getMainSettings();

        rename.setUserId(userId);
        rename.setSourceMap(mainSettings.getLibraryMap());
        rename.setSourceTitleAuthorSeparator("DASH");
        rename.setSourceAuthornameSeparator("COMMA");
        rename.setSourceFormat("tav");

        rename.setDestMap(mainSettings.getLibraryMap());
        rename.setDestTitleAuthorSeparator("DASH");
        rename.setDestAuthornameSeparator("COMMA");
        rename.setDestFormat("avt");

        return rename;
    }

    public void runRename(final RenameDTO renameDTO) {
        String sourceMap = renameDTO.getSourceMap();
        String destMap = renameDTO.getDestMap();

        Set<Path> sourceFiles = findSourceFilesAndDirs(sourceMap);
        Path sourcePath = new File(sourceMap).toPath();
        Map<Boolean, List<Path>> countsMap = sourceFiles.stream().collect(Collectors.partitioningBy(p -> Files.isDirectory(p)));
        int dirsCount = countsMap.get(true).size() - 1;
        int filesCount = countsMap.get(false).size();
        log.info("Filters applied. " +
                "Directories [" + ((dirsCount < 0) ? 0 : dirsCount) + "], " +
                "Files [" + filesCount + "].");

        // Thread.sleep(100);

        try {
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                    if (sourceFiles.contains(path)) {
                        String destinationMap = destMap;
                        File newFile;
                        try {
                            String newFileName = createNewFilename(path.toFile().getName(), renameDTO);
                            newFile = new File(newFileName);
                        }
                        catch (RenameException e) {
                            String failedMap = File.separator + "failed";
                            destinationMap = destMap + failedMap;
                            newFile = path.toFile();
                        }

                        File tempMap = new File(destinationMap);
                        if (!tempMap.exists()) {
                            tempMap.mkdir();
                        }

                        String dest = destinationMap + File.separator + newFile.getName();
                        Path target = Paths.get(dest);
                        CopyOption[] copyOptions = new CopyOption[] {StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING};
                        Files.copy(path, target, copyOptions);
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Set<Path> findSourceFilesAndDirs(final String root) {
        File dir = new File(root);
        Collection<File> col = FileUtils.listFilesAndDirs(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

        Set<Path> sourceFiles = new HashSet<>();
        sourceFiles.add(dir.toPath());
        for (File file : col) {
            Path sourceFile = file.toPath();

            sourceFiles.add(sourceFile);
        }

        return sourceFiles;
    }

    private String createNewFilename(final String curFilename, final RenameDTO renameDTO) throws RenameException {
        String titel = "";
        String achternaam = "";
        String voornaam = "";
        String filename = "";

        int posExt = curFilename.lastIndexOf('.');
        String extensie = curFilename.substring(posExt);
        String currentFilename = curFilename.substring(0, posExt);
        int pos1;
        int pos2;

        String sourceFormat = renameDTO.getSourceFormat();
        String sourceSeparatorTitleAuthor = getSourceSeparator(renameDTO.getSourceTitleAuthorSeparator());
        String sourceSeparatorAuthorname = getSourceSeparator(renameDTO.getSourceAuthornameSeparator());

        try {
            switch(sourceFormat) {
                case "tva":
                    pos1 = currentFilename.indexOf(sourceSeparatorTitleAuthor);
                    pos2 = currentFilename.indexOf(sourceSeparatorAuthorname);
                    titel = currentFilename.substring(0, pos1);
                    if (sourceSeparatorAuthorname.equals(" ")) {
                        currentFilename = currentFilename.substring(pos1+1).trim();
                        pos1 = -1;
                        pos2 = currentFilename.indexOf(sourceSeparatorAuthorname);
                    }
                    voornaam = currentFilename.substring(pos1+1, pos2);
                    achternaam = currentFilename.substring(pos2+1);
                    break;
                case "tav":
                    pos1 = currentFilename.indexOf(sourceSeparatorTitleAuthor);
                    pos2 = currentFilename.indexOf(sourceSeparatorAuthorname);
                    titel = currentFilename.substring(0, pos1);
                    achternaam = currentFilename.substring(pos1+1, pos2);
                    voornaam = currentFilename.substring(pos2+1);
                    break;
                case "vat":
                    pos1 = currentFilename.indexOf(sourceSeparatorAuthorname);
                    pos2 = currentFilename.indexOf(sourceSeparatorTitleAuthor);
                    voornaam = currentFilename.substring(0, pos1);
                    achternaam = currentFilename.substring(pos1+1, pos2);
                    titel = currentFilename.substring(pos2+1);
                    break;
                case "avt":
                    pos1 = currentFilename.indexOf(sourceSeparatorAuthorname);
                    pos2 = currentFilename.indexOf(sourceSeparatorTitleAuthor);
                    achternaam = currentFilename.substring(0, pos1);
                    voornaam = currentFilename.substring(pos1+1, pos2);
                    titel = currentFilename.substring(pos2+1);
                    break;
                default:
                    break;
            }
        }
        catch (Exception e) {
            throw new RenameException();
        }

        titel = titel.trim();
        achternaam = achternaam.trim();
        voornaam = voornaam.trim();

        String destFormat = renameDTO.getDestFormat();
        String destSeparatorTitleAuthor = getDestSeparator(renameDTO.getDestTitleAuthorSeparator());
        String destSeparatorAuthorname = getDestSeparator(renameDTO.getDestAuthornameSeparator());

        switch(destFormat) {
            case "tva":
                filename = titel + destSeparatorTitleAuthor + voornaam + destSeparatorAuthorname + achternaam;
                break;
            case "tav":
                filename = titel + destSeparatorTitleAuthor + achternaam + destSeparatorAuthorname + voornaam;
                break;
            case "vat":
                filename = voornaam + destSeparatorAuthorname + achternaam + destSeparatorTitleAuthor + titel;
                break;
            case "avt":
                filename = achternaam + destSeparatorAuthorname + voornaam + destSeparatorTitleAuthor + titel;
                break;
            default:
                break;
        }

        filename = filename + extensie;

        return filename;
    }

    private String getSourceSeparator(final String name) {
        return name.equals("DASH") ? "-" : name.equals("COMMA") ? "," : name.equals("SPACE") ? " " : null;
    }

    private String getDestSeparator(final String name) {
        return name.equals("DASH") ? " - " : name.equals("COMMA") ? ", " : name.equals("SPACE") ? " " : null;
    }


}
