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
import rb.ebooklib.persistence.BookRepository;
import rb.ebooklib.tree.BinaryTree;
import rb.ebooklib.util.ViewObjectMappers;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
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
    private UserService userService;
    @Autowired
    private ViewObjectMappers viewObjectMappers;
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private GoogleApiService googleApiService;

    private static final String BOOK_NOT_FOUND = "Book with id %d not found";

    private BinaryTree rootTree;

    @Transactional
    public void updateDatasbase(final File file, final String timestamp) {
        final String filename = file.toPath().toString();
        log.info("======> Boek " + filename);

        final Optional<Book> optionalBook = this.bookRepository.findOneByFilename(filename);

        Book bookDb;
        if (optionalBook.isPresent()) {
            BookDTO bookDTO = viewObjectMappers.convertBookToBookDto(optionalBook.get());
            bookDb = updateBook(bookDTO);
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
        final List<Author> authorList = this.authorService.mergeNewAuthors(book.getAuthors());
        final List<Category> categoryList = this.categoryService.mergeNewCategories(book.getCategories());
        final Genre genre = this.genreService.mergeNewGenre(book.getGenre());

        book.setAuthors(authorList);
        book.setCategories(categoryList);
        book.setGenre(genre);

        return book;
    }

    @Transactional
    public Book updateBook(final BookDTO bookDTO) {
        final Book currentBook = this.bookRepository.findById(bookDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException(String.format(BOOK_NOT_FOUND, bookDTO.getId())));

        currentBook.setDescription(bookDTO.getDescription());
        currentBook.setImageLink(bookDTO.getImageLink());
        currentBook.setCategories(bookDTO.getCategories());
        currentBook.setAuthors(bookDTO.getAuthors());
//        if (isNullOrEmptyString(currentBook.getIsbn()) && isNotNullOrEmptyString(bookDTO.getIsbn())) {
        if (!currentBook.getIsbn().equals(bookDTO.getIsbn())) {
            String isbn = convertIsbn10ToIsbn13(bookDTO.getIsbn());
            currentBook.setIsbn(isbn);
            boolean haveImageLink = currentBook.getImageLink().startsWith("http");
            boolean readDescription = currentBook.getDescription().startsWith("Helaas geen beschrijving");
            Book bookApi = readApi(currentBook);
            if (!haveImageLink) {
                currentBook.setImageLink(bookApi.getImageLink());
            }
            if (readDescription) {
                currentBook.setDescription(bookApi.getDescription());
            }



            int nrOfAuthors = currentBook.getAuthors().size();
            currentBook.setAuthors(bookApi.getAuthors());

            currentBook.getCategories().size();
            currentBook.setCategories(bookApi.getCategories());

            if (currentBook.getAuthor().equalsIgnoreCase("Onbekend")) {
                if (nrOfAuthors > 0) {
                    currentBook.setAuthor(readFirstAuthorFromAuthors(bookApi.getAuthors()));
                }
            }
        }
        else {
            currentBook.setIsbn(bookDTO.getIsbn());
        }

        currentBook.setTitle(bookDTO.getTitle());
        currentBook.setLibraryMap(bookDTO.getLibraryMap());
        currentBook.setIdentifiers(bookDTO.getIdentifiers());
        currentBook.setIsRead(bookDTO.getIsRead());
        //currentBook.getCategories().size();
//        final List<Category> checkOrphanCategories = currentBook.getCategories();
//        final List<Author> checkOrphanAuthors = currentBook.getAuthors();
//        final Genre checkOrphanGenre = currentBook.getGenre();
//        currentBook.setCategories(this.categoryService.mergeNewCategories(bookDTO.getCategories()));
//        currentBook.setAuthors(this.authorService.mergeNewAuthors(bookDTO.getAuthors()));
//        // currentBook.setGenre(this.genreService.mergeNewGenre(bookDTO.getGenre()));
//        currentBook.setGenre(this.genreService.mergeNewGenre(new Genre(bookDTO.getLibraryMap())));
        currentBook.setGenre(bookDTO.getGenre());
        currentBook.setTimestamp(bookDTO.getTimestamp());
//        checkOrphanAuthors.forEach(author -> this.authorService.removeAuthorWhenOrphan(author.getId()));
//        checkOrphanCategories.forEach(category -> this.categoryService.removeCategoryWhenOrphan(category.getId()));//
//        this.genreService.removeGenreWhenOrphan(checkOrphanGenre.getId());

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
        Long userId = userService.getCurrentUserId();
        Settings settings = settingsService.getByUserId(userId);
        return settings.getLibraryMap();
    }

    @Transactional
    public File[] getBooksFromFolder() {
        Long userId = userService.getCurrentUserId();
        Settings settings = settingsService.getByUserId(userId);
        String root = settings.getLibraryMap();
        return new File(root).listFiles();
    }

    @Transactional
    public Book getNewBook(String librayMap, String path) {
        Book book = new Book();
        book.setFilename(path);
        book.setAuthor(getAuthor(path));;
        book.setLibraryMap(librayMap);
        book.setGenre(new Genre(startWithCapital(librayMap)));
        book.setExtension(getExtension(path));
        book.setIsRead("N");
        if (isEpub(path)) {
            book = readEpubBook(book, path);
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

            currentBook = readApi(currentBook);
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

    private Book convertEpubBookToBook(Book book, EpubBook epubBook) {
        Book currentBook = book;

        if (currentBook != null) {
            currentBook.setAuthors(readAuthors(epubBook.getMetadata()));
            currentBook.setDescription(createDescription(epubBook.getMetadata()));
            currentBook.setPublisher(readPublisher(epubBook.getMetadata()));
            currentBook.setTitle(readTitle(epubBook));
            currentBook.setImageLink(readImageLink(epubBook));
            currentBook.setIsbn(readIsbn(epubBook.getMetadata()));
            currentBook.setCategories(readCategories(epubBook.getMetadata()));
            currentBook.setIdentifiers(readIdentifiers(epubBook.getMetadata()));
        }

        return currentBook;
    }

    private Book convertFromApiToBook(Book book, BookDTO bookDTO) {
        Book currentBook = book;

//        if (isNullOrEmptyString(book.getImageLink())) {
//            currentBook.setImageLink(bookDTO.getImageLink());
//        }

        if (isNotNullOrEmptyString(bookDTO.getImageLink())) {
            currentBook.setImageLink(bookDTO.getImageLink());
        }

        if (isNullOrEmptyString(book.getDescription()) || book.getDescription().startsWith("Helaas geen beschrijving")) {
            currentBook.setDescription(bookDTO.getDescription());
        }

        if (isNotNullOrEmptyList(bookDTO.getCategories())) {
            currentBook = addCategories(book, bookDTO);
        }

        if (isNotNullOrEmptyList(bookDTO.getAuthors())) {
            currentBook = addAuthors(book, bookDTO);
        }

        return currentBook;
    }

    private Book readApi(Book book) {
        Book currentBook = book;
        if (isNotNullOrEmptyString(book.getIsbn())) {
            BookDTO bookDTO = googleApiService.searchBookByIsbn(book.getIsbn());
            currentBook = convertFromApiToBook(book, bookDTO);
        }
        return currentBook;
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
            this.bookRepository.deleteById(bookId);
            book.getAuthors().forEach(author -> this.authorService.removeAuthorWhenOrphan(author.getId()));
            book.getCategories()
                    .forEach(category -> this.categoryService.removeCategoryWhenOrphan(category.getId()));
            this.genreService.removeGenreWhenOrphan(book.getGenre().getId());
            return true;
        }

        return false;
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

    /**
     * Find books where the title or author contains a specified word/sentence.
     *
     * @param query  the word/sentence a user is searching for
     * @param size   how many books needs to be returned
     * @param pageNo the page number to calculate which set of books to return
     * @return a PageDTO with the total number and list of Books.
     */
    public PageDTO<Book> getBooksForTitleOrAuthor(final String query, final Integer size, final Integer pageNo) {
        Page<Book> page = bookRepository
                .findAll(bookHasTitleOrAuthor(query), getPageRequestWithTitleSort(size, pageNo));
        return new PageDTO<>(page);
    }

    public PageDTO<Book> getBooksExtendedSearch(final String whatToSearch, final String query, final String genre, final String category,final String extension, final Integer size, final Integer pageNo) {
        Page<Book> page = bookRepository
                .findAll(bookExtendedSearch(whatToSearch, query, genre, category, extension), getPageRequestWithTitleSort(size, pageNo));
        return new PageDTO<>(page);
    }

    /**
     * Find books with the current user as owner.
     *
     * @param size how many books needs to be returned
     * @param pageNo the page number to calculate which set of books to return
     * @return a PageDTO with the total number and list of Books.
     */
//    public PageDTO<Book> findMyBooks(final Integer size, final Integer pageNo) {
//        return this.getBooksForOwner(this.userService.getCurrentUserId(), size, pageNo);
//    }

//    private PageDTO<Book> getBooksForOwner(final Long ownerId, final Integer size, final Integer pageNo) {
//        Page<Book> page = bookRepository.findAll(bookHasOwnerId(ownerId), getPageRequestWithTitleSort(size, pageNo));
//        return new PageDTO<>(page);
//    }

    private PageRequest getPageRequestWithTitleSort(int size, int pageNo) {
//        return PageRequest.of(pageNo - 1, size, Sort.by(Book_.title.getName()));
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
