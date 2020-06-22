package rb.ebooklib.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rb.ebooklib.dto.RenameDTO;
import rb.ebooklib.model.Format;
import rb.ebooklib.model.Rename;
import rb.ebooklib.model.Separator;
import rb.ebooklib.service.ToolService;

import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/rename")
public class RenameController {

    private static final Logger log = LoggerFactory.getLogger(RenameController.class);

    @Autowired
    private ToolService toolService;

    @PostMapping
    public void initDatabase() {
        toolService.initRename();
    }

    @GetMapping("/separators")
    public ResponseEntity<List<Separator>> findAllSeparators() {
        final List<Separator> separators = toolService.getAllSeparators();
        return new ResponseEntity<>(separators, HttpStatus.OK);
    }

    @GetMapping("/separator")
    public ResponseEntity<Separator> findSeparatorByName(@RequestParam(value="name") final String name) {
        final Optional<Separator> separator = toolService.getSeparatorByName(name);
        return new ResponseEntity<>(separator.get(), HttpStatus.OK);
    }

    @GetMapping("/formats")
    public ResponseEntity<List<Format>> findAllFormats() {
        final List<Format> formats = toolService.getAllFormats();
        return new ResponseEntity<>(formats, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<Rename> createRename(@RequestBody final RenameDTO renameDTO) {
        log.info(renameDTO.getSourceTitleAuthorSeparator());
        final Rename rename = toolService.createRename(renameDTO);
        return new ResponseEntity<>(rename, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<Rename> updateRename(@RequestBody final RenameDTO renameDTO) {
        final Rename rename = toolService.updateRename(renameDTO);
        return new ResponseEntity<>(rename, HttpStatus.OK);
    }

    @GetMapping("/userId")
    public ResponseEntity<Rename> findByUserId(@RequestParam(value="userId") final Long userId) {
        final Rename rename = toolService.getByUserId(userId);
        return new ResponseEntity<>(rename, HttpStatus.OK);
    }

    @GetMapping("/standard")
    public ResponseEntity<Rename> getStandardValues(@RequestParam(value="userId") final Long userId) {
        final Rename rename = toolService.getStandardRename(userId);
        return new ResponseEntity<>(rename, HttpStatus.OK);
    }

    @GetMapping("/run")
    public ResponseEntity<String> rename() {
        toolService.rename();
        return new ResponseEntity<>("Ok", HttpStatus.OK);
    }

}
