package rb.ebooklib.util;

import org.springframework.stereotype.Component;
import rb.ebooklib.dto.*;
import rb.ebooklib.isbnapimodels.googleapi.GoogleBookResponse;
import rb.ebooklib.isbnapimodels.googleapi.VolumeInfo;
import rb.ebooklib.isbnapimodels.openlibraryapi.OpenLibraryBookResponse;
import rb.ebooklib.models.*;
import rb.ebooklib.payload.request.SignupRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static rb.ebooklib.predicate.GoogleApiPredicates.isIsbn13;
import static rb.ebooklib.util.NullOrEmptyUtil.isNullOrEmpty;

@Component
public class ViewObjectMappers {

    public User convertUserDTOToUser(final UserDTO userDTO) {
        final User user = new User();

        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());

        return user;
    }

    public UserSettings convertUserSettingsDtoToUserSettings(final UserSettingsDTO userSettingsDTO) {
        final UserSettings userSettings = new UserSettings();
        userSettings.setId(userSettingsDTO.getId());
        userSettings.setCopyTo(userSettingsDTO.getCopyTo());
        userSettings.setMailTo(userSettingsDTO.getMailTo());
        userSettings.setIsDateSort(userSettingsDTO.getIsDateSort());
        userSettings.setIsNameSort(userSettingsDTO.getIsNameSort());
        userSettings.setIsEpubSelected(userSettingsDTO.getIsEpubSelected());
        userSettings.setIsMobiSelected(userSettingsDTO.getIsMobiSelected());
        userSettings.setIsPdfSelected(userSettingsDTO.getIsPdfSelected());
        userSettings.setIsCbrSelected(userSettingsDTO.getIsCbrSelected());
        userSettings.setMailHost(userSettingsDTO.getMailHost());
        userSettings.setMailPort(userSettingsDTO.getMailPort());
        userSettings.setMailUserName(userSettingsDTO.getMailUserName());
        userSettings.setMailPassword(userSettingsDTO.getMailPassword());
        return userSettings;
    }

    public MainSettings convertMainSettingsDtoToMainSettings(final MainSettingsDTO mainSettingsDTO) {
        final MainSettings mainSettings = new MainSettings();
        mainSettings.setId(mainSettingsDTO.getId());
        mainSettings.setLibraryMap(mainSettingsDTO.getLibraryMap());
        mainSettings.setCalibreCommand(mainSettingsDTO.getCalibreCommand());
        return mainSettings;
    }

    public BookDTO convertBookToBookDto(final Book book) {
        final BookDTO bookDTO = new BookDTO();

        bookDTO.setId(book.getId());
        bookDTO.setIsbn(book.getIsbn());
        bookDTO.setAuthor(book.getAuthor());
        bookDTO.setTitle(book.getTitle());
        bookDTO.setLibraryMap(book.getLibraryMap());
        bookDTO.setImageLink(book.getImageLink());
        bookDTO.setTempImageLink(book.getTempImageLink());
        bookDTO.setGenre(book.getGenre());
        bookDTO.setPublisher(book.getPublisher());
        bookDTO.setExtension(book.getExtension());
        bookDTO.setDescription(book.getDescription());
        bookDTO.setIdentifiers(book.getIdentifiers());
        bookDTO.setIsRead(book.getIsRead());
        bookDTO.setLanguage(book.getLanguage());
        bookDTO.setTimestamp(book.getTimestamp());
        bookDTO.setTempImageLink(book.getTempImageLink());

        if (book.getAuthors() == null) {
            List<Author> authors = new ArrayList<>();
            authors.add(new Author(book.getAuthor()));
            bookDTO.setAuthors(authors);
        }
        else {
            bookDTO.setAuthors(book.getAuthors());
        }

        if (book.getCategories() == null) {
            List<Category> categories = new ArrayList<>();
            categories.add(new Category(book.getGenre().getName()));
            bookDTO.setCategories(categories);
        }
        else {
            bookDTO.setCategories(book.getCategories());
        }

        return bookDTO;
    }

    public Book convertBookDtoToBook(final BookDTO bookDTO) {
        final Book book = new Book();

        book.setId(bookDTO.getId());
        book.setIsbn(bookDTO.getIsbn());
        book.setAuthor(bookDTO.getAuthor());
        book.setTitle(bookDTO.getTitle());
        book.setLibraryMap(bookDTO.getLibraryMap());
        book.setImageLink(bookDTO.getImageLink());
        book.setTempImageLink(bookDTO.getTempImageLink());
        book.setGenre(bookDTO.getGenre());
        book.setGenre(new Genre(bookDTO.getLibraryMap()));
        book.setPublisher(bookDTO.getPublisher());
        book.setExtension(bookDTO.getExtension());
        book.setDescription(bookDTO.getDescription());
        book.setIdentifiers(bookDTO.getIdentifiers());
        book.setIsRead(bookDTO.getIsRead());
        book.setLanguage(bookDTO.getLanguage());
        book.setTimestamp(bookDTO.getTimestamp());

        if (bookDTO.getAuthors() == null) {
            List<Author> authors = new ArrayList<>();
            authors.add(new Author(bookDTO.getAuthor()));
            book.setAuthors(authors);
        }
        else {
            book.setAuthors(bookDTO.getAuthors());
        }

        if (bookDTO.getCategories() == null) {
            List<Category> categories = new ArrayList<>();
            categories.add(new Category(bookDTO.getLibraryMap()));
            book.setCategories(categories);
        }
        else {
            book.setCategories(bookDTO.getCategories());
        }

        return book;
    }

    public BookDTO prepareDTOFromGoogleApiResponse(GoogleBookResponse googleBookResponse) {
        final BookDTO bookDTO = new BookDTO();

        if (googleBookResponse.getTotalItems() > 0) {
            final VolumeInfo volumeInfo = googleBookResponse.getItems().get(0).getVolumeInfo();
            bookDTO.setTitle(volumeInfo.getTitle());
            if (!isNullOrEmpty(volumeInfo.getAuthors())) {
                bookDTO.setAuthors(volumeInfo.getAuthors()
                        .stream()
                        .map(Author::new)
                        .collect(Collectors.toList()));
            }
            if (!isNullOrEmpty(volumeInfo.getCategories())) {
                bookDTO.setCategories(volumeInfo.getCategories()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(Category::new)
                        .collect(Collectors.toList()));
            }
            if (!isNullOrEmpty(volumeInfo.getIndustryIdentifiers())) {
                volumeInfo.getIndustryIdentifiers()
                        .stream()
                        .filter(isIsbn13())
                        .findFirst().ifPresent(i -> bookDTO.setIsbn(i.getIdentifier()));
            }

            Optional.ofNullable(volumeInfo.getImageLinks())
                    .ifPresent(imageLinks -> bookDTO.setImageLink(imageLinks.getThumbnail()));

            bookDTO.setDescription(volumeInfo.getDescription());
            bookDTO.setLanguage(volumeInfo.getLanguage());
        }

        return bookDTO;
    }

    public BookDTO prepareDTOFromOpenLibApiResponse(OpenLibraryBookResponse response) {
        final BookDTO bookDTO = new BookDTO();

        if (!isNullOrEmpty(response) && !isNullOrEmpty(response.getPublishers())) {
            bookDTO.setTitle(response.getTitle());
            bookDTO.setDescription(response.getTitle());
            if (!isNullOrEmpty(response.getAuthors())) {
                bookDTO.setAuthors(response.getAuthors()
                        .stream()
                        .map(author -> new Author(author.getName()))
                        .collect(Collectors.toList()));
            }
            if (!isNullOrEmpty(response.getAuthors())) {
                bookDTO.setAuthors(response.getAuthors()
                        .stream()
                        .map(author -> new Author(author.getName()))
                        .collect(Collectors.toList()));
            }
            bookDTO.setIsbn(response.getIdentifier().getIsbn_13());
            Optional.ofNullable(response.getCover()).ifPresent(cover -> bookDTO.setImageLink(cover.getMedium()));
        }

        return bookDTO;
    }
    
    public Rename convertRenameDtoToRename(RenameDTO renameDTO) {
        Rename rename = new Rename();

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
        
        return rename;
    }

    public User convertSignupRequestToUser(final SignupRequest signupRequest) {
        final User user = new User();

        user.setPassword(signupRequest.getPassword());
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        //user.setRoles(signupRequest.getRole());

        return user;
    }

}
