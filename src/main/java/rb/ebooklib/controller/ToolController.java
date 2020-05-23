package rb.ebooklib.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rb.ebooklib.dto.BookDTO;
import rb.ebooklib.model.Book;
import rb.ebooklib.service.ToolService;

@CrossOrigin
@RestController
@RequestMapping("/tool")
public class ToolController {

    private static final Logger log = LoggerFactory.getLogger(ToolService.class);

    @Autowired
    private ToolService toolService;

    @GetMapping("/calibre")
    public void openCalibre(@RequestParam(value = "bookId") final Long bookId) {
        log.info("CALIBRE");
        final Book book = toolService.getBookById(bookId);
    }


}
