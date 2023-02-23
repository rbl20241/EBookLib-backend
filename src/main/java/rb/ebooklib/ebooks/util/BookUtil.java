package rb.ebooklib.ebooks.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import rb.ebooklib.ebooks.epub.domain.EpubBook;
import rb.ebooklib.ebooks.epub.domain.Metadata;
import rb.ebooklib.ebooks.epub.domain.Resource;
import rb.ebooklib.models.Author;
import rb.ebooklib.models.Book;
import rb.ebooklib.models.Category;
import rb.ebooklib.models.Identifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static rb.ebooklib.ebooks.util.Constants.*;
import static rb.ebooklib.util.StringUtil.isBlank;
import static rb.ebooklib.util.StringUtil.startWithCapital;

public class BookUtil {

    private static final Log log = LogFactory.getLog(BookUtil.class);

    public static Boolean isAcceptedFile(String path) {
        return path.endsWith(EPUB) ||
                path.endsWith(MOBI) ||
                path.endsWith(PDF) ||
                path.endsWith(CBR);
    }

    public static String getGenre(String path) {
        return path.substring(path.lastIndexOf(File.separator) + 1);
    }

    public static String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    public static String getAuthor(String currentFile) {
        String author = "";
        String fileName = currentFile.substring(currentFile.lastIndexOf(File.separator)+1);
        int posMinus = fileName.indexOf(" - ");
        boolean isKnownAuthor =  posMinus > 0;
        if (isKnownAuthor) {
            int posComma = fileName.indexOf(",");
            boolean isCommaSeparated =  posComma > 0;
            if (isCommaSeparated) {
                try {
                    String lastName = fileName.substring(0, posComma);
                    String firstName = fileName.substring(posComma + 2, posMinus);
                    // author = firstName + " " + lastName;
                    author = lastName + ", " + firstName;
                }
                catch (StringIndexOutOfBoundsException e) {
                    author = "Onbekend";
                }
            }
            else {
                author = fileName.substring(0, posMinus);
            }
        }

        return author;
    }

    public static String getTitle(String currentFile) {
        return currentFile.substring(currentFile.lastIndexOf(File.separator)+1, currentFile.lastIndexOf("."));
    }

    public static boolean isEpub(final String fileName) {
        return fileName.toLowerCase().endsWith(EXT_EPUB);
    }

    public static boolean isPdf(final String fileName) {
        return fileName.toLowerCase().endsWith(EXT_PDF);
    }

    public static boolean isMobi(final String fileName) {
        return fileName.toLowerCase().endsWith(EXT_MOBI);
    }

    public static boolean isCbr(final String fileName) {
        return fileName.toLowerCase().endsWith(EXT_CBR);
    }

    public static List<Identifier> readIdentifiers(Metadata metadata) {
        return metadata.getIdentifiers();
    }

    public static List<Author> readAuthors(final Metadata metadata) {
        List<Author> authors = new ArrayList<>();

        if (isNotNullObject(metadata)) {
            if (isNotNullOrEmptyList(metadata.getEpubAuthors())) {
                for (int i = 0; i < metadata.getEpubAuthors().size(); i++) {
                    if (isNotNullObject(metadata.getEpubAuthors().get(i))) {
                        String name = "";

                        if (isNotNullOrEmptyString(metadata.getEpubAuthors().get(i).getFirstname())) {
                            name = metadata.getEpubAuthors().get(i).getFirstname() + " ";
                        }
                        if (isNotNullOrEmptyString(metadata.getEpubAuthors().get(i).getLastname())) {
                            name += metadata.getEpubAuthors().get(i).getLastname();
                        }

                        if (name.contains(",")) {
                            while (name.contains(",")) {
                                int pos = name.indexOf(",");
                                String newName = name.substring(0, pos).trim();
                                name = name.substring(pos+1);
                                Author author = new Author(newName);
                                if (!isAuthorAlreadyInList(authors, author)) {
                                    authors.add(author);
                                }
                            }
                        }
                        else {
                            Author author = new Author(name);
                            if (!isAuthorAlreadyInList(authors, author)) {
                                authors.add(author);
                            }
                        }
                    }
                }
            }
        }

        return authors;
    }

    public static List<Category> readCategories(Metadata metadata) {
        List<Category> categories = new ArrayList<>();

        if (isNotNullObject(metadata)) {
            if (isNotNullOrEmptyList(metadata.getSubjects())) {
                for (int i = 0; i < metadata.getSubjects().size(); i++) {
                    if (isNotNullObject(metadata.getSubjects().get(i))) {
                        if (isNotNullOrEmptyString(metadata.getSubjects().get(i))) {
                            Category category = new Category(metadata.getSubjects().get(i));
                            category.setName(startWithCapital(category.getName()));
                            categories.add(category);
                        }
                    }
                }
            }
        }

        return categories;
    }

    public static String readTitle(EpubBook epubBook) {
        String title = "";

        if (isNotNullObject(epubBook)) {
            if (isNotNullOrEmptyString(epubBook.getTitle())) {
                title = epubBook.getTitle();
            }
        }

        return title;
    }

    public static String readPublisher(Metadata metadata) {
        String publisher = "";

        if (isNotNullObject(metadata)) {
            if (isNotNullOrEmptyList(metadata.getPublishers())) {
                if (isNotNullOrEmptyString(metadata.getPublishers().get(0))) {
                    publisher = metadata.getPublishers().get(0);
                }
            }
        }

        return publisher;
    }

    public static String createDescription(Metadata metadata) {

        StringBuilder strBuffer = new StringBuilder();

        for (int i = 0; i < metadata.getDescriptions().size(); i++) {
            Object value = metadata.getDescriptions().get(i);
            if (value == null) {
                continue;
            }
            String valueString = String.valueOf(value);
            if (isBlank(valueString)) {
                continue;
            }

            strBuffer.append(valueString);
        }

        String strResult = strBuffer.toString();

        while (strResult.contains("<")) {
            if (strResult.contains(">")) {
                int posLT = strResult.indexOf("<");
                int posGT = strResult.indexOf(">");
                if (posLT < posGT) {
                    strResult = strResult.substring(0, posLT) + strResult.substring(posGT + 1);
                }
                else {
                    strResult = strBuffer.toString();
                    break;
                }
            }
        }

//        if (strResult.equals("")) {
//            strResult = "Helaas geen beschrijving gevonden.";
//        }

        return strResult;
    }

    public static String readIsbn(Metadata metadata) {
        String isbn = "";

        if (isNotNullOrEmptyList(metadata.getIdentifiers())) {
            for (Identifier identifier: metadata.getIdentifiers()) {
                if (identifier.getScheme().equalsIgnoreCase("ISBN")) {
                    isbn = getIsbn(identifier.getValue());
                }
                else if (isNullOrEmptyString(identifier.getScheme())) {
                    isbn = getIsbn(identifier.getValue());
                }
            }
        }

        if (isNullOrEmptyString(isbn)) {
            isbn = "";
        }

        return isbn;
    }

    private static String getIsbn(String value) {
        String isbn = null;
        String strValue = value.replace("-", "");
        try {
            if (strValue.length() == 10 || strValue.length() == 13) {
                Long longValue = Long.parseLong(strValue);
                isbn = convertIsbn10ToIsbn13(strValue);
            }
        }
        catch (NumberFormatException e) {
        }

        return isbn;
    }

    public static String readImageLink(EpubBook epubBook) {
        String imageLink = "";
        Resource coverImage = epubBook.getCoverImage();

        if (isNotNullObject(coverImage)) {
            imageLink = coverImage.getOriginalHref();
        }

        return imageLink;
    }

    public static String readTempImageLink(final Book book, final String tempMap) {
        var tempImageLink = "";
        if (isNotNullOrEmptyString(book.getImageLink())) {
            tempImageLink = makeTempImageLink(book, tempMap);
        }

        return tempImageLink;
    }

    private static String makeTempImageLink(final Book book, final String tempMap) {
//        var tempImageLink = "";
//        try {
            String imageLink = book.getImageLink();
            String imageFile = imageLink.substring(imageLink.lastIndexOf("/"));
            return tempMap + "/current-cover.jpg";
//            Path tempPath = Paths.get(tempMap);
//            if (!Files.exists(tempPath)) {
//                Files.createDirectory(tempPath);
//            }
//
//            //Path epub = Paths.get(book.getFilename());
//            //FileSystem fileSystem = FileSystems.newFileSystem(epub, null);
//            //Path fileToExtract = fileSystem.getPath(imageLink);
//            Path tempImage = Paths.get(tempPath.toString(), imageFile);
//            //Files.copy(fileToExtract, tempImage);
//            tempImageLink = tempImage.toString();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        return tempImageLink;
    }

    public static String readLanguage(final Metadata metadata) {
        if ((isNullOrEmptyString(metadata.getLanguage())) ||
                metadata.getLanguage().equalsIgnoreCase("UND")) {
            return "";
        }
        else {
            return metadata.getLanguage();
        }
    }

    public static String convertIsbn10ToIsbn13(final String currentIsbn) {
        String isbn13 = "";

        String isbn = currentIsbn.replace("-", "").trim();

        if (isbn.length() == 10) {
            String tmp = "978" + isbn.substring(0, 9);
            int sum = 0;
            for (int i = 0; i < tmp.length(); i++) {
                int f = (i % 2 == 0) ? 1 : 3;
                sum += ((int) tmp.charAt(i) - 48) * f;
            }
            sum = 10 - (sum % 10);
            isbn13 = tmp + sum;
        }
        else {
            isbn13 = isbn;
        }

        StringBuilder result = new StringBuilder();
        result = result
                .append(isbn13.substring(0,3))
                .append("-")
                .append(isbn13.substring(3,5))
                .append("-")
                .append(isbn13.substring(5,8))
                .append("-")
                .append(isbn13.substring(8,12))
                .append("-")
                .append(isbn13.substring(12));

        return result.toString();
    }

    public static boolean isNullObject(Object obj) {
        return obj == null;
    }

    public static boolean isNotNullObject(Object obj) {
        return obj != null;
    }

    public static boolean isNullOrEmptyString(String str) {
        boolean isNullOrEmpty;

        isNullOrEmpty = str == null;
        if (!isNullOrEmpty) {
            isNullOrEmpty = str.isEmpty();
        }

        return isNullOrEmpty;
    }

    public static boolean isNotNullOrEmptyString(String str) {
        return !isNullOrEmptyString(str);
    }

    public static boolean isNullOrEmptyList(List<?> list) {
        boolean isNullOrEmpty;

        isNullOrEmpty = list == null;
        if (!isNullOrEmpty) {
            isNullOrEmpty = list.isEmpty() || list.equals("[]");
        }

        return isNullOrEmpty;
    }

    public static boolean isNotNullOrEmptyList(List<?> list) {
        return !isNullOrEmptyList(list);
    }

    public static String createTimestamp() {
        Date date = new Date();
        Timestamp ts = new Timestamp(date.getTime());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(ts);
    }

    public static boolean isAuthorAlreadyInList(final List<Author> authorList, final Author author) {
        for (Author a : authorList) {
            if (author.getName().equalsIgnoreCase(a.getName())) {
                return true;
            }
        }

        return false;
    }


}
