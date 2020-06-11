package rb.ebooklib.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rb.ebooklib.dto.BookDTO;
import rb.ebooklib.dto.PageDTO;
import rb.ebooklib.model.Book;
import rb.ebooklib.service.BookService;
import rb.ebooklib.service.EmailService;
import rb.ebooklib.service.ToolService;
import rb.ebooklib.util.FilePath;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static rb.ebooklib.ebooks.util.BookUtil.*;

@CrossOrigin
@RestController
@RequestMapping("/books")
public class BookController {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    @Autowired
    private BookService bookService;

    @Autowired
    private ToolService toolService;

    @Autowired
    private EmailService emailService;

//    @PostMapping
//    public ResponseEntity<Book> createBook(@RequestBody final BookDTO bookDTO) {
//        if (bookDTO.getAuthors() == null) {
//            List<Author> authors = new ArrayList<>();
//            authors.add(new Author(bookDTO.getAuthor()));
//            bookDTO.setAuthors(authors);
//        }
//        if (bookDTO.getCategories() == null) {
//            List<Category> categories = new ArrayList<>();
//            categories.add(new Category(bookDTO.getGenre()));
//            bookDTO.setCategories(categories);
//        }
//        final Book book = bookService.createBookWithBookOwner(bookDTO);
//        return new ResponseEntity<>(book, HttpStatus.OK);
//    }

    @PostMapping
    public void createBooks() {
        String timestamp = createTimestamp();
        String root = bookService.getRoot();
        createBookTree(root, timestamp);

        bookService.deleteNotExistingsBooksFromDatabase(timestamp);
    }

    private void createBookTree(String root, String timestamp) {
        File[] files = new File(root).listFiles();
        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList);
        for (File file: fileList) {
            if (file.isDirectory()) {
                createBookTree(file.getAbsolutePath(), timestamp);
            }
            else if (isAcceptedFile(file.getAbsolutePath())) {
                bookService.updateDatasbase(file, timestamp);
            }
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Book> updateBook(@RequestBody final BookDTO bookDTO) {
        final Book book = bookService.updateBook(bookDTO);
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    //    public ResponseEntity<Book> createBook(@RequestBody final BookDTO bookDTO) {
//        final Book book = bookService.createBookWithBookOwner(bookDTO);
//        return new ResponseEntity<>(book, HttpStatus.OK);
//    }

    /*
    @PutMapping("/update")
    public void updateBooks() {
        bookService.updateBooks();
    }

*/
//    @PutMapping("/update")
//    public ResponseEntity<Book> updateBook(@RequestBody final BookDTO bookDTO) {
//        final Book book = bookService.updateBook(bookDTO);
//        return new ResponseEntity<>(book, HttpStatus.OK);
//    }

    @GetMapping("/bookid")
    public ResponseEntity<Book> findById(@RequestParam(value = "bookId") final Long bookId) {
        final Book book = bookService.getById(bookId);
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<PageDTO<Book>> findAll(@RequestParam(value = "size", required = false, defaultValue = "10") final Integer size,
                                                 @RequestParam(value = "pageNo", required = false, defaultValue = "1") final Integer pageNo) {
        final PageDTO<Book> pageDTO = bookService.getAllBooks(size, pageNo);
        return new ResponseEntity<>(pageDTO, HttpStatus.OK);
    }

    @GetMapping("/category")
    public ResponseEntity<PageDTO<Book>> findByCategory(@RequestParam(value = "category") final String category,
                                                        @RequestParam(value = "size", required = false, defaultValue = "10") final Integer size,
                                                        @RequestParam(value = "pageNo", required = false, defaultValue = "1") final Integer pageNo) {
        final PageDTO<Book> pageDTO = bookService.getBooksForCategory(category, size, pageNo);
        return new ResponseEntity<>(pageDTO, HttpStatus.OK);
    }

    @GetMapping("/genre")
    public ResponseEntity<PageDTO<Book>> findByGenre(@RequestParam(value = "genre") final String genre,
                                                     @RequestParam(value = "size", required = false, defaultValue = "10") final Integer size,
                                                     @RequestParam(value = "pageNo", required = false, defaultValue = "1") final Integer pageNo) {
        final PageDTO<Book> pageDTO = bookService.getBooksForGenre(genre, size, pageNo);
        return new ResponseEntity<>(pageDTO, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<PageDTO<Book>> searchBooksByTitleOrAuthor(@RequestParam(value = "query") final String query,
                                                                    @RequestParam(value = "size", required = false, defaultValue = "10") final Integer size,
                                                                    @RequestParam(value = "pageNo", required = false, defaultValue = "1") final Integer pageNo) {
            final PageDTO<Book> pageDTO = bookService.getBooksForTitleOrAuthor(query, size, pageNo);
        return new ResponseEntity<>(pageDTO, HttpStatus.OK);
    }

    @GetMapping("/extendedsearch")
    public ResponseEntity<PageDTO<Book>> extendedSearchBooks(@RequestParam(value = "query") final String query,
                                                             @RequestParam(value = "category") final String category,
                                                             @RequestParam(value = "extension") final String extension,
                                                             @RequestParam(value = "size", required = false, defaultValue = "10") final Integer size,
                                                             @RequestParam(value = "pageNo", required = false, defaultValue = "1") final Integer pageNo) {
        final PageDTO<Book> pageDTO = bookService.getBooksExtendedSearch(query, category, extension, size, pageNo);
        return new ResponseEntity<>(pageDTO, HttpStatus.OK);
    }

    @GetMapping("/copy")
    public ResponseEntity<Book> copyBook(@RequestParam(value = "bookId") final Long bookId,
                                         @RequestParam(value = "copyTo") final String copyTo) {
        final Book book = bookService.getById(bookId);
        bookService.copyBook(book, copyTo);
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    @GetMapping("/mail")
    public ResponseEntity<Book> sendBook(@RequestParam(value = "bookId") final Long bookId,
                                         @RequestParam(value = "mailTo") final String mailTo) {
        final Book book = bookService.getById(bookId);
        emailService.sendBook(book, mailTo);
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    @GetMapping("/calibre")
    public ResponseEntity<Book> openCalibre(@RequestParam(value = "bookId") final Long bookId) {
        final Book book = bookService.getById(bookId);
        toolService.runCalibre(book);
        return new ResponseEntity<>(book, HttpStatus.OK);
    }


//    @GetMapping("/my-books")
//    public ResponseEntity<PageDTO<Book>> findMyBooks(@RequestParam(value = "size", required = false, defaultValue = "10") final Integer size,
//                                                     @RequestParam(value = "pageNo", required = false, defaultValue = "1") final Integer pageNo) {
//        final PageDTO<Book> pageDTO = bookService.findMyBooks(size, pageNo);
//        return new ResponseEntity<>(pageDTO, HttpStatus.OK);
//    }

}
