package rb.ebooklib.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rb.ebooklib.dto.BookDTO;
import rb.ebooklib.services.GoogleApiService;
import rb.ebooklib.services.OpenLibraryService;

@CrossOrigin
@RestController
@RequestMapping("/search")
public class GoogleBooksApiController {

    @Autowired
    private GoogleApiService googleApiService;
    @Autowired
    private OpenLibraryService openLibraryService;

    @GetMapping("/isbn")
    public ResponseEntity<BookDTO> SearchByIsbn(@RequestParam("identifier") final String identifier) {
        BookDTO bookDTO = this.googleApiService.searchBookByIsbn(identifier);
        if (bookDTO.getIsbn() == null) {
            bookDTO = openLibraryService.searchBookByIsbn(identifier);
        }
        return new ResponseEntity<>(bookDTO, HttpStatus.OK);
    }
}
