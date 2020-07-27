package rb.ebooklib.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rb.ebooklib.dto.BookDTO;
import rb.ebooklib.dto.PageDTO;
import rb.ebooklib.ebooks.epub.domain.EpubBook;
import rb.ebooklib.ebooks.epub.reader.EpubReader;
import rb.ebooklib.model.*;
import rb.ebooklib.model.Book_;
import rb.ebooklib.persistence.BookRepository;
import rb.ebooklib.util.ViewObjectMappers;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static rb.ebooklib.ebooks.util.BookUtil.*;
import static rb.ebooklib.ebooks.util.Constants.CHARACTER_ENCODING;
import static rb.ebooklib.persistence.BookSpecifications.*;
import static rb.ebooklib.util.StringUtil.nrOfChars;
import static rb.ebooklib.util.StringUtil.startWithCapital;

@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);
    
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private GenreService genreService;
    @Autowired
    private AuthorService authorService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ViewObjectMappers viewObjectMappers;
    @Autowired
    private MainSettingsService mainSettingsService;
    @Autowired
    private GoogleApiService googleApiService;
    @Autowired
    private GoogleApiService openLibraryApiService;

    private static final String BOOK_NOT_FOUND = "Boek met id %d niet gevonden";

    private ArrayList<LocalDateTime> time_1s;
    private int curIndex_1s;
    private int prevIndex_1s;
    private int nrOfRequests_1s;

    private ArrayList<LocalDateTime> time_100s;
    private int curIndex_100s;
    private int prevIndex_100s;
    private int nrOfRequests_100s;

    @Transactional
    public void updateDatasbase(final File file, final String timestamp) {
        init_1sec_test();
        init_100sec_test();

        final String filename = file.toPath().toString();
        final Optional<Book> optionalBook = this.bookRepository.findOneByFilename(filename);

        Book bookDb;
        if (optionalBook.isPresent()) {
            BookDTO bookDTO = viewObjectMappers.convertBookToBookDto(optionalBook.get());
            log.info("Update boek: " + bookDTO.getTitle() + " (" + bookDTO.getGenre() + ")");
            bookDb = existingBook(bookDTO);
        }
        else {
            final String root = getRoot();
            final String librayMap = filename.substring(root.length()+1, filename.lastIndexOf(File.separator));
            Book book = getNewBook(librayMap, filename);
            bookDb = createBook(book);
        }

        bookDb.setTimestamp(timestamp);
        this.bookRepository.save(bookDb);
    }

    @Transactional
    private Book createBook(final Book book) {

        log.info("Current book: " + book.getTitle() + "(" + book.getGenre().getName() + ")");
        final List<Author> authorList = this.authorService.mergeNewAuthors(book.getAuthors());
        final List<Category> categoryList = this.categoryService.mergeNewCategories(book.getCategories());
        final Genre genre = this.genreService.mergeNewGenre(book.getGenre());

        book.setAuthors(authorList);
        book.setCategories(categoryList);
        book.setGenre(genre);

        return book;
    }

    @Transactional
    public void updateAllBooks() {
        List<Book> bookList = bookRepository.findAll();
        for (Book book : bookList) {
            BookDTO bookDTO = viewObjectMappers.convertBookToBookDto(book);
            book = updateBook(bookDTO);
            bookRepository.save(book);
        }
    }

    public Book existingBook(final BookDTO bookDTO) {
        final Book currentBook = this.bookRepository.findById(bookDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException(String.format(BOOK_NOT_FOUND, bookDTO.getId())));

        currentBook.setDescription(bookDTO.getDescription());
        currentBook.setImageLink(bookDTO.getImageLink());
        currentBook.setCategories(bookDTO.getCategories());
        currentBook.setAuthors(bookDTO.getAuthors());
        if (!currentBook.getIsbn().equals(bookDTO.getIsbn())) {
            String isbn = convertIsbn10ToIsbn13(bookDTO.getIsbn());
            currentBook.setIsbn(isbn);
        }
        else {
            currentBook.setIsbn(bookDTO.getIsbn());
        }

        currentBook.setTitle(bookDTO.getTitle());
        currentBook.setLibraryMap(bookDTO.getLibraryMap());
        currentBook.setIdentifiers(bookDTO.getIdentifiers());
        currentBook.setIsRead(bookDTO.getIsRead());
        currentBook.setGenre(bookDTO.getGenre());
        currentBook.setTimestamp(bookDTO.getTimestamp());

        return bookRepository.save(currentBook);
    }

    @Transactional
    public Book updateBook(final BookDTO bookDTO) {
        Book currentBook = this.bookRepository.findById(bookDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException(String.format(BOOK_NOT_FOUND, bookDTO.getId())));

        currentBook.setDescription(bookDTO.getDescription());
        currentBook.setImageLink(bookDTO.getImageLink());
        currentBook.setCategories(bookDTO.getCategories());
        currentBook.setAuthors(bookDTO.getAuthors());
        if (!currentBook.getIsbn().equals(bookDTO.getIsbn())) {
            String isbn = convertIsbn10ToIsbn13(bookDTO.getIsbn());
            currentBook.setIsbn(isbn);
        }
        else {
            currentBook.setIsbn(bookDTO.getIsbn());
        }

        if (isNotNullOrEmptyString(currentBook.getIsbn())) {
            Book googleApiBook = readGoogleApi(currentBook);
            currentBook = readApi(currentBook, googleApiBook);

            Book openLibApiBook = readOpenLibraryApi(currentBook);
            currentBook = readApi(currentBook, openLibApiBook);
        }

        currentBook.setTitle(bookDTO.getTitle());
        currentBook.setLibraryMap(bookDTO.getLibraryMap());
        currentBook.setIdentifiers(bookDTO.getIdentifiers());
        currentBook.setIsRead(bookDTO.getIsRead());
        currentBook.setGenre(bookDTO.getGenre());
        currentBook.setTimestamp(bookDTO.getTimestamp());

        return bookRepository.save(currentBook);
    }

    @Transactional
    public void deleteNotExistingsBooksFromDatabase(String timestamp) {
        bookRepository.findAll()
                .stream()
                .filter(book -> !book.getTimestamp().equalsIgnoreCase(timestamp))
                .collect(Collectors.toList())
                .forEach(book -> removeBookWhenOrphan(book.getId()));
    }

    @Transactional
    public String getRoot() {
        MainSettings mainSettings = mainSettingsService.getMainSettings();
        return mainSettings.getLibraryMap();
    }

    @Transactional
    public File[] getBooksFromFolder() {
        MainSettings mainSettings = mainSettingsService.getMainSettings();
        String root = mainSettings.getLibraryMap();
        return new File(root).listFiles();
    }

    @Transactional
    public Book getNewBook(String libraryMap, String path) {
        Book book = new Book();
        book.setFilename(path);
        book.setAuthor(getAuthor(path));;
        book.setLibraryMap(libraryMap);
        book.setGenre(new Genre(startWithCapital(libraryMap)));
        book.setExtension(getExtension(path));
        book.setIsRead("N");
        if (isEpub(path)) {
            book = readEpubBook(book, path);
        }
        else if (isPdf(path) || isMobi(path) || isCbr(path)) {
            book = readOtherBook(book, path);
        }

        if (isNullOrEmptyString(book.getTitle())) {
            book.setTitle(getTitle(path));
        }

        if (isNullOrEmptyString(book.getAuthor())) {
            List<Author> authors = bookRepository.findAuthorsByFilename(path);
            if (isNotNullOrEmptyList(authors)) {
                book.setAuthor(authors.get(0).getName());
            }
            else {
                book.setAuthor("Onbekend");
            }
        }

        return book;
    }

    private Book readEpubBook(Book book, String currentFile) {
        try {
            EpubBook epubBook = (new EpubReader()).readEpub(new FileInputStream(currentFile), CHARACTER_ENCODING);
            Book currentBook = convertEpubBookToBook(book, epubBook);

            // currentBook = readGoogleApi(currentBook);
            currentBook = addGenreToCategories(currentBook);
            currentBook = fillEmptyFields(currentBook);

            return currentBook;
        }
        catch (IOException e) {
            log.error(currentFile);
            log.error(e.getMessage());
        }
        return null;
    }

    private Book convertEpubBookToBook(final Book book, final EpubBook epubBook) {

        if (book != null) {
            book.setAuthors(readAuthors(epubBook.getMetadata()));
            book.setDescription(createDescription(epubBook.getMetadata()));
            book.setPublisher(readPublisher(epubBook.getMetadata()));
            book.setTitle(readTitle(epubBook));
            book.setImageLink(readImageLink(epubBook));
            book.setIsbn(readIsbn(epubBook.getMetadata()));
            book.setCategories(readCategories(epubBook.getMetadata()));
            book.setIdentifiers(readIdentifiers(epubBook.getMetadata()));
        }

        return book;
    }

    private Book convertFromApiToBook(BookDTO bookApi, Book book) {
        Book currentBook = book;

        if (isNotNullOrEmptyString(bookApi.getImageLink())) {
            currentBook.setImageLink(bookApi.getImageLink());
        }

        if ((isNullOrEmptyString(book.getDescription()) || book.getDescription().startsWith("Helaas geen beschrijving")) &&
                isNotNullOrEmptyString(bookApi.getDescription())) {
            currentBook.setDescription(bookApi.getDescription());
        }

        if (isNotNullOrEmptyList(bookApi.getCategories())) {
            currentBook = addCategories(book, bookApi);
        }

        if (isNotNullOrEmptyList(bookApi.getAuthors())) {
            currentBook = addAuthors(book, bookApi);
        }

        return currentBook;
    }

    private Book readGoogleApi(Book book) {
        Book currentBook = book;

        if (isNotNullOrEmptyString(book.getIsbn())) {
            try {
                long time_dif_1s = ChronoUnit.MILLIS.between(time_1s.get(prevIndex_1s), time_1s.get(curIndex_1s));
                if (time_dif_1s < 1000 && nrOfRequests_1s == 10) {
                    log.info("Teveel aanvragen binnen een seconde. Even wachten...");
                    TimeUnit.MILLISECONDS.sleep(1000 - time_dif_1s);
                    init_1sec_test();
                }
                long time_dif_100s = ChronoUnit.MILLIS.between(time_100s.get(prevIndex_100s), time_100s.get(curIndex_100s));
                if (time_dif_100s < 100000 && nrOfRequests_100s == 100) {
                    log.info("Teveel aanvragen binnen 100 seconden. Even wachten...");
                    TimeUnit.MILLISECONDS.sleep(100000 - time_dif_100s);
                    init_100sec_test();
                }
                BookDTO bookDTO = googleApiService.searchBookByIsbn(book.getIsbn());
                currentBook = convertFromApiToBook(bookDTO, book);
                update_index_1sec_test();
                update_index_100sec_test();
            } catch (Exception e) {
                log.info(e.getMessage());
            }
        }

        return currentBook;
    }

    private Book readOpenLibraryApi(Book book) {
        Book currentBook = book;

        if (isNotNullOrEmptyString(book.getIsbn())) {
            try {
                BookDTO bookDTO = openLibraryApiService.searchBookByIsbn(book.getIsbn());
                currentBook = convertFromApiToBook(bookDTO, book);
            } catch (Exception e) {
                log.info(e.getMessage());
            }
        }

        return currentBook;
    }

    private Book readApi(final Book currentBook, final Book apiBook) {
        boolean haveImageLink =
                currentBook.getImageLink().startsWith("http") &&
                        isNotNullOrEmptyString(apiBook.getImageLink());
        boolean readDescription =
                currentBook.getDescription().startsWith("Helaas geen beschrijving") &&
                        isNotNullOrEmptyString(apiBook.getDescription());

        if (!haveImageLink) {
            currentBook.setImageLink(apiBook.getImageLink());
        }
        if (readDescription) {
            currentBook.setDescription(apiBook.getDescription());
        }

        int nrOfAuthors = currentBook.getAuthors().size();
        currentBook.setAuthors(apiBook.getAuthors());

        currentBook.getCategories().size();
        currentBook.setCategories(apiBook.getCategories());

        if (currentBook.getAuthor().equalsIgnoreCase("Onbekend")) {
            if (nrOfAuthors > 0) {
                currentBook.setAuthor(readFirstAuthorFromAuthors(apiBook.getAuthors()));
            }
        }

        return currentBook;
    }

    private void init_1sec_test() {
        nrOfRequests_1s = 0;
        time_1s = new ArrayList<>();
        curIndex_1s = 0;
        prevIndex_1s = 0;
        time_1s.add(0, LocalDateTime.now());
    }

    private void update_index_1sec_test() {
        nrOfRequests_1s++;
        curIndex_1s++;
        if (curIndex_1s >= 10) {
            prevIndex_1s++;
        }
    }

    private void init_100sec_test() {
        nrOfRequests_100s = 0;
        time_100s = new ArrayList<>();
        curIndex_100s = 0;
        prevIndex_100s = 0;
        time_100s.add(0, LocalDateTime.now());
    }

    private void update_index_100sec_test() {
        nrOfRequests_100s++;
        curIndex_100s++;
        if (curIndex_100s >= 100) {
            prevIndex_100s++;
        }
    }

    private Book addAuthors(Book book, BookDTO bookDTO) {
        Book currentBook = book;

        List<Author> authorsDTO = bookDTO.getAuthors();
        for (Author author : authorsDTO) {
            if (!isAuthorAlreadyInList(currentBook.getAuthors(), author)) {
                currentBook.getAuthors().add(author);
            }
        }

        return currentBook;
    }

    private Book addCategories(Book book, BookDTO bookDTO) {
        Book currentBook = book;

        if (isNotNullOrEmptyList(bookDTO.getCategories())) {
            List<Category> categoriesDTO = bookDTO.getCategories();
            for (Category category : categoriesDTO) {
                if (!isCategoryFound(book.getCategories(), category)) {
                    currentBook.getCategories().add(category);
                }
            }
        }

        return currentBook;
    }

    private Boolean isCategoryFound(List<Category> categories, Category newCategory) {
        String currentCategory = newCategory.getName();
        for (Category category: categories) {
            if (category.getName().equalsIgnoreCase(currentCategory)) {
                return true;
            }
        }

        return false;
    }

    private Book addGenreToCategories(Book book) {
        Book currentBook = book;

        List<Category> categories = book.getCategories();
        Category genre = new Category(book.getGenre().getName());
        if (!isCategoryFound(categories, genre)) {
            categories.add(genre);
        }

        currentBook.setCategories(categories);

        return currentBook;
    }

    private Book fillEmptyFields(Book book) {
        Book currentBook = book;

        if (isNullOrEmptyString(currentBook.getDescription())) {
            currentBook.setDescription("Helaas geen beschrijving gevonden.");
        }

        if (isNullOrEmptyString(currentBook.getImageLink())) {
            currentBook.setImageLink(System.getProperty("user.dir") + File.separator + "images" + File.separator + "book.jpg");
        }

        return currentBook;
    }

    @Transactional
    public Boolean removeBookWhenOrphan(final Long bookId) {
        final Optional<Book> bookOptional = this.bookRepository.findById(bookId);
        if (bookOptional.isPresent()) {
            final Book book = bookOptional.get();
            book.getAuthors().size();
            book.getCategories().size();
            List<Category> categories = book.getCategories();
            this.bookRepository.deleteById(bookId);
            book.getAuthors().forEach(author -> this.authorService.removeAuthorWhenOrphan(author.getId()));
            categories.forEach(category -> this.categoryService.removeCategoryWhenOrphan(category.getId()));
            this.genreService.removeGenreWhenOrphan(book.getGenre().getId());

            return true;
        }

        return false;
    }

    private Book readOtherBook(final Book book, final String currentFile) {
        book.setTitle(getTitle(currentFile));

        List<Author> authors = new ArrayList<>();
        authors.add(new Author(getAuthor(currentFile)));
        book.setAuthors(authors);

        List<Category> categories = new ArrayList<>();
        categories.add(new Category(book.getGenre().getName()));
        book.setCategories(categories);

        book.setDescription("Helaas geen beschrijving gevonden.");
        book.setImageLink(System.getProperty("user.dir") + File.separator + "images" + File.separator + "book.jpg");
        book.setIsbn("");
        book.setPublisher("");

        return book;
    }

    public Book getById(final Long bookId) {
        return this.bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(BOOK_NOT_FOUND, bookId)));
    }

    public PageDTO<Book> getAllBooks(final Integer size, final Integer pageNo) {
        final Page<Book> page = this.bookRepository.findAll(getPageRequestWithTitleSort(size, pageNo));
        return new PageDTO<>(page);
    }

    /**
     * Find books that contain a specified category.
     *
     * @param categoryName the category to search the books
     * @param size         how many books needs to be returned
     * @param pageNo       the page number to calculate which set of books to return
     * @return a PageDTO with the total number and list of Books.
     */
    public PageDTO<Book> getBooksForCategory(final String categoryName, final Integer size, final Integer pageNo) {
        Page<Book> page = bookRepository
                .findAll(bookHasCategoryName(categoryName), getPageRequestWithTitleSort(size, pageNo));
        return new PageDTO<>(page);
    }

    public PageDTO<Book> getBooksForGenre(final String genreName, final Integer size, final Integer pageNo) {
        Page<Book> page = bookRepository
                .findAll(bookHasGenreName(genreName), getPageRequestWithTitleSort(size, pageNo));
        return new PageDTO<>(page);
    }

    public PageDTO<Book> getSearchBooks(final String whatToSearch, final String query, final String genre, final String category, final String extension, final Integer size, final Integer pageNo) {
        Page<Book> page = bookRepository
                .findAll(searchBooks(whatToSearch, query, genre, category, extension), getPageRequestWithTitleSort(size, pageNo));
        return new PageDTO<>(page);
    }

    private PageRequest getPageRequestWithTitleSort(int size, int pageNo) {
        return PageRequest.of(pageNo - 1, size, Sort.by(Book_.libraryMap.getName()).and(Sort.by(Book_.author.getName())).and(Sort.by(Book_.title.getName())));
    }

    private String readFirstAuthorFromAuthors(List<Author> authors) {
        String author = "";
        String authorName = authors.get(0).getName();
        if (authorName.contains(",")) {
            author = authorName;
        }
        else if (nrOfChars(authorName, ' ') == 1) {
            int p = authorName.indexOf(' ');
            String firstName = authorName.substring(0, p);
            String lastName = authorName.substring(p+1);
            author = lastName + ", " + firstName;
        }
        else {
            author = authorName;
        }

        return author;
    }

    public void copyBook(final Book book, final String copyTo) {
        String copyFrom = book.getFilename();
        String libraryMap = book.getLibraryMap();
        String newRoot = copyTo;
        try {
            Path source = Paths.get(copyFrom);
            String pathTo = newRoot + File.separator + libraryMap;
            Path target = Paths.get( pathTo+ File.separator + source.getFileName());
            if (Files.notExists(Paths.get(pathTo))) {
                Files.createDirectories(Paths.get(pathTo));
            }
            Files.copy(source, target, REPLACE_EXISTING);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }



}
