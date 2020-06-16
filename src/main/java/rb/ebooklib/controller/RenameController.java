package rb.ebooklib.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rb.ebooklib.service.ToolService;

@CrossOrigin
@RestController
@RequestMapping("/rename")
public class RenameController {

    private static final Logger log = LoggerFactory.getLogger(RenameController.class);

    @Autowired
    private ToolService toolService;

    @GetMapping
    public ResponseEntity<String> copyBook() {
        toolService.rename();
        return new ResponseEntity<>("Ok", HttpStatus.OK);
    }

}
