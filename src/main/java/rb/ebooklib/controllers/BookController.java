package rb.ebooklib.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rb.ebooklib.dto.BookDTO;
import rb.ebooklib.dto.PageDTO;
import rb.ebooklib.models.Book;
import rb.ebooklib.services.BookService;
import rb.ebooklib.services.EmailService;
import rb.ebooklib.services.ToolService;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static rb.ebooklib.ebooks.util.BookUtil.*;

@CrossOrigin
@RestController
@RequestMapping("/books")
public class BookController {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);
    private static final String defaultPageSize = "50";

    @Autowired
    private BookService bookService;

    @Autowired
    private ToolService toolService;

    @Autowired
    private EmailService emailService;

    @PostMapping
    public void createBooks(@RequestParam(value = "useApi") final String useApi) {
        if (useApi.equalsIgnoreCase("true")) {
            bookService.updateAllBooks();
        }
        else {
            String timestamp = createTimestamp();
            String root = bookService.getRoot();
            //Boolean isOnlyAdd = addOnly.equalsIgnoreCase("true");
            createBookTree(root, timestamp, true);

            bookService.deleteNotExistingsBooksFromDatabase(timestamp);
        }
    }

    private void createBookTree(String root, String timestamp, final Boolean isOnlyAdd) {
        File[] files = new File(root).listFiles();
        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList);
        for (File file: fileList) {
            if (file.isDirectory()) {
                createBookTree(file.getAbsolutePath(), timestamp, isOnlyAdd);
            }
            else if (isAcceptedFile(file.getAbsolutePath())) {
                bookService.updateDatasbase(file, timestamp, isOnlyAdd);
            }
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Book> updateBook(@RequestBody final BookDTO bookDTO) {
        final Book book = bookService.updateBook(bookDTO);
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    @GetMapping("/bookid")
    public ResponseEntity<Book> findById(@RequestParam(value = "bookId") final Long bookId) {
        final Book book = bookService.getById(bookId);
        if (isNotNullOrEmptyString(book.getTempImageLink())) {
            removeLastTempImageLink();
            createTempImageLink(book);
        }
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    private void createTempImageLink(Book book) {
        try {
            Path tempImageLink = Paths.get(book.getTempImageLink());
            Path tempPath = Paths.get("c:/Temp/boeken");
            if (!Files.exists(tempPath)) {
                Files.createDirectory(tempPath);
            }

            Path epub = Paths.get(book.getFilename());
            FileSystem fileSystem = FileSystems.newFileSystem(epub, (Map<String, ?>) null);
            Path fileToExtract = fileSystem.getPath(book.getImageLink());
            Files.copy(fileToExtract, tempImageLink);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeLastTempImageLink() {
        Path tempPath = Paths.get("c:/Temp/boeken");
        try {
            Files.walk(tempPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping
    public ResponseEntity<PageDTO<Book>> findAll(@RequestParam(value = "size", required = false, defaultValue = defaultPageSize) final Integer size,
                                                 @RequestParam(value = "pageNo", required = false, defaultValue = "1") final Integer pageNo) {
        final PageDTO<Book> pageDTO = bookService.getAllBooks(size, pageNo);
        return new ResponseEntity<>(pageDTO, HttpStatus.OK);
    }

    @GetMapping("/category")
    public ResponseEntity<PageDTO<Book>> findByCategory(@RequestParam(value = "category") final String category,
                                                        @RequestParam(value = "size", required = false, defaultValue = defaultPageSize) final Integer size,
                                                        @RequestParam(value = "pageNo", required = false, defaultValue = "1") final Integer pageNo) {
        final PageDTO<Book> pageDTO = bookService.getBooksForCategory(category, size, pageNo);
        return new ResponseEntity<>(pageDTO, HttpStatus.OK);
    }

    @GetMapping("/genre")
    public ResponseEntity<PageDTO<Book>> findByGenre(@RequestParam(value = "genre") final String genre,
                                                     @RequestParam(value = "size", required = false, defaultValue = defaultPageSize) final Integer size,
                                                     @RequestParam(value = "pageNo", required = false, defaultValue = "1") final Integer pageNo) {
        final PageDTO<Book> pageDTO = bookService.getBooksForGenre(genre, size, pageNo);
        return new ResponseEntity<>(pageDTO, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<PageDTO<Book>> searchBooks(@RequestParam(value = "whatToSearch") final String whatToSearch,
                                                             @RequestParam(value = "query") final String query,
                                                             @RequestParam(value = "genre") final String genre,
                                                             @RequestParam(value = "category") final String category,
                                                             @RequestParam(value = "extension") final String extension,
                                                             @RequestParam(value = "language") final String language,
                                                             @RequestParam(value = "size", required = false, defaultValue = defaultPageSize) final Integer size,
                                                             @RequestParam(value = "pageNo", required = false, defaultValue = "1") final Integer pageNo) {
        final PageDTO<Book> pageDTO = bookService.getSearchBooks(whatToSearch, query, genre, category, extension, language, size, pageNo);
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
}
