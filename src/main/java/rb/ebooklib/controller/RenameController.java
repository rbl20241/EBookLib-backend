package rb.ebooklib.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rb.ebooklib.dto.RenameDTO;
import rb.ebooklib.model.Rename;
import rb.ebooklib.service.ToolService;

@CrossOrigin
@RestController
@RequestMapping("/rename")
public class RenameController {

    private static final Logger log = LoggerFactory.getLogger(RenameController.class);

    @Autowired
    private ToolService toolService;

    @PostMapping("/save")
    public ResponseEntity<Rename> createRename(@RequestBody final RenameDTO renameDTO) {
        final Rename rename = toolService.saveRename(renameDTO);
        return new ResponseEntity<>(rename, HttpStatus.OK);
    }

    @GetMapping("/userId")
    public ResponseEntity<Rename> findByUserId(@RequestParam(value="userId") final Long userId) {
        final Rename rename = toolService.getByUserId(userId);
        return new ResponseEntity<>(rename, HttpStatus.OK);
    }

    @PostMapping("/run")
    public ResponseEntity<String> runRename(@RequestBody final RenameDTO renameDTO) {
        toolService.runRename(renameDTO);
        return new ResponseEntity<>("Ok", HttpStatus.OK);
    }

}
