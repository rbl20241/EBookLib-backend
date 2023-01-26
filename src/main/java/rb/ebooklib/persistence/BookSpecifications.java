package rb.ebooklib.persistence;

import org.springframework.data.jpa.domain.Specification;
import rb.ebooklib.model.*;
import rb.ebooklib.model.Author_;
import rb.ebooklib.model.Book_;
import rb.ebooklib.model.Category_;
import rb.ebooklib.model.Genre_;

import jakarta.persistence.criteria.*;

import static rb.ebooklib.util.StringUtil.isEmpty;

public final class BookSpecifications {

    private static final String LIKE_QUERY_FORMAT = "%%%s%%";

    private BookSpecifications() {}

    public static Specification<Book> bookHasAuthorId(final Long authorId) {
        return (root, query, cb) -> {
            final Join<Book, Author> bookAuthorJoin = root.join(Book_.authors);
            return cb.equal(bookAuthorJoin.get(Author_.id), authorId);
        };
    }

    public static Specification<Book> bookHasGenreId(final Long genreId) {
        return (root, query, cb) -> {
            final Join<Book, Genre> bookGenreJoin = root.join(Book_.genre);
            return cb.equal(bookGenreJoin.get(Genre_.id), genreId);
        };
    }

    public static Specification<Book> bookHasCategoryId(final Long categoryId) {
        return (root, query, cb) -> {
            final Join<Book, Category> bookCategoryJoin = root.join(Book_.categories);
            return cb.equal(bookCategoryJoin.get(Category_.id), categoryId);
        };
    }

    public static Specification<Book> bookHasCategoryName(final String categoryName) {
        return (root, query, cb) -> {
            final Join<Book, Category> bookCategoryJoin = root.join(Book_.categories);
            return cb.equal(bookCategoryJoin.get(Category_.name), categoryName);
        };
    }

    public static Specification<Book> bookHasGenreName(final String genreName) {
        return (root, query, cb) -> {
            final Join<Book, Genre> bookGenreJoin = root.join(Book_.genre);
            return cb.equal(bookGenreJoin.get(Genre_.name), genreName);
        };
    }

    public static Specification<Book> searchBooks(final String whatToSearch, final String queryString, final String genre, final String category, final String extension, final String language) {
        final String likeQueryString = String.format(LIKE_QUERY_FORMAT, queryString.toLowerCase());
        final boolean searchInTitleAndAuthor = whatToSearch.equals("searchTitleOrAuthor");
        final boolean searchInDescription = whatToSearch.equals("searchDescription");
        return (root, query, cb) -> {
            Predicate authorPredicate =  null;
            Predicate titlePredicate = null;
            Predicate genrePredicate = null;
            Predicate categoryPredicate = null;
            Predicate extensionPredicate = null;
            Predicate languagePredicate = null;
            Predicate queryPredicate = null;

            if (searchInTitleAndAuthor && isSelected(queryString)) {
                final Join<Book, Author> bookAuthorJoin = root.join(Book_.authors);
                authorPredicate = cb.like(cb.lower(bookAuthorJoin.get(Author_.name)), likeQueryString);
                titlePredicate = cb.like(cb.lower(root.get(Book_.title)), likeQueryString);
                queryPredicate = cb.or(authorPredicate, titlePredicate);
            }

            if (searchInDescription && isSelected(queryString)) {
                queryPredicate = cb.like(cb.lower(root.get(Book_.description)), likeQueryString);
            }

            if (isSelected(genre)) {
                final Join<Book, Genre> bookGenreJoin = root.join(Book_.genre);
                genrePredicate = cb.like(cb.lower(bookGenreJoin.get(Genre_.name)), genre);
            }

            if (isSelected(category)) {
                final Join<Book, Category> bookCategoryJoin = root.join(Book_.categories);
                categoryPredicate = cb.like(cb.lower(bookCategoryJoin.get(Category_.name)), category);
            }

            if (isSelected(extension)) {
                extensionPredicate = cb.like(cb.lower(root.get(Book_.extension)), extension);
            }

            if (isSelected(language)) {
                languagePredicate = cb.like(cb.lower(root.get(Book_.language)), language);
            }

            query.distinct(true);

            if (isSelected(queryString) && isSelected(genre) && isSelected(category) && isSelected(extension) && isSelected(language)) {
                return cb.and(queryPredicate, genrePredicate, categoryPredicate, extensionPredicate, languagePredicate);
            } // 1
            else if (isSelected(queryString) && isSelected(genre) && isSelected(category) && isSelected(extension) && !isSelected(language)) {
                return cb.and(queryPredicate, genrePredicate, categoryPredicate, extensionPredicate);
            } // 2
            else if (isSelected(queryString) && isSelected(genre) && isSelected(category) && !isSelected(extension) && !isSelected(language)) {
                return cb.and(queryPredicate, genrePredicate, categoryPredicate);
            } // 3
            else if (isSelected(queryString) && isSelected(genre) && !isSelected(category) && !isSelected(extension) && !isSelected(language)) {
                return cb.and(queryPredicate, genrePredicate);
            } // 4
            else if (isSelected(queryString) && !isSelected(genre) && isSelected(category) && !isSelected(extension) && !isSelected(language)) {
                return cb.and(queryPredicate, categoryPredicate);
            } // 5
            else if (!isSelected(queryString) && isSelected(genre) && isSelected(category) && !isSelected(extension) && !isSelected(language)) {
                return cb.and(genrePredicate, categoryPredicate);
            } // 6
            else if (isSelected(queryString) && !isSelected(genre) && !isSelected(category) && !isSelected(extension) && !isSelected(language)) {
                return queryPredicate;
            } // 7
            else if (!isSelected(queryString) && isSelected(genre) && !isSelected(category) && !isSelected(extension) && !isSelected(language)) {
                return genrePredicate;
            } // 8
            else if (!isSelected(queryString) && !isSelected(genre) && isSelected(category) && !isSelected(extension) && !isSelected(language)) {
                return categoryPredicate;
            } // 9
            else if (isSelected(queryString) && isSelected(genre) && !isSelected(category) && isSelected(extension) && !isSelected(language)) {
                return cb.and(queryPredicate, genrePredicate, extensionPredicate);
            } // 10
            else if (isSelected(queryString) && !isSelected(genre) && !isSelected(category) && isSelected(extension) && !isSelected(language)) {
                return cb.and(queryPredicate, extensionPredicate);
            } // 11
            else if (!isSelected(queryString) && isSelected(genre) && !isSelected(category) && isSelected(extension) && !isSelected(language)) {
                return cb.and(genrePredicate, extensionPredicate);
            } // 12
            else if (!isSelected(queryString) && !isSelected(genre) && !isSelected(category) && isSelected(extension) && !isSelected(language)) {
                return extensionPredicate;
            } // 13
            else if (isSelected(queryString) && !isSelected(genre) && isSelected(category) && isSelected(extension) && !isSelected(language)) {
                return cb.and(queryPredicate, categoryPredicate, extensionPredicate);
            } // 14
            else if (!isSelected(queryString) && isSelected(genre) && isSelected(category) && isSelected(extension) && !isSelected(language)) {
                return cb.and(genrePredicate, categoryPredicate, extensionPredicate);
            } // 15
            else if (!isSelected(queryString) && !isSelected(genre) && isSelected(category) && isSelected(extension) && !isSelected(language)) {
                return cb.and(categoryPredicate, extensionPredicate);
            } // 16  
            else if (isSelected(queryString) && isSelected(genre) && isSelected(category) && !isSelected(extension) && isSelected(language)) {
                return cb.and(queryPredicate, genrePredicate, categoryPredicate, languagePredicate);
            } // 17
            else if (isSelected(queryString) && isSelected(genre) && !isSelected(category) && !isSelected(extension) && isSelected(language)) {
                return cb.and(queryPredicate, genrePredicate, languagePredicate);
            } // 18
            else if (isSelected(queryString) && !isSelected(genre) && isSelected(category) && !isSelected(extension) && isSelected(language)) {
                return cb.and(queryPredicate, categoryPredicate, languagePredicate);
            } // 19
            else if (!isSelected(queryString) && isSelected(genre) && isSelected(category) && !isSelected(extension) && isSelected(language)) {
                return cb.and(genrePredicate, categoryPredicate, languagePredicate);
            } // 20
            else if (isSelected(queryString) && !isSelected(genre) && !isSelected(category) && !isSelected(extension) && isSelected(language)) {
                return cb.and(queryPredicate, languagePredicate);
            } // 21
            else if (!isSelected(queryString) && isSelected(genre) && !isSelected(category) && !isSelected(extension) && isSelected(language)) {
                return cb.and(genrePredicate, languagePredicate);
            } // 22
            else if (!isSelected(queryString) && !isSelected(genre) && isSelected(category) && !isSelected(extension) && isSelected(language)) {
                return cb.and(categoryPredicate, languagePredicate);
            } // 23
            else if (isSelected(queryString) && isSelected(genre) && !isSelected(category) && isSelected(extension) && isSelected(language)) {
                return cb.and(queryPredicate, genrePredicate, extensionPredicate, languagePredicate);
            } // 24
            else if (isSelected(queryString) && !isSelected(genre) && !isSelected(category) && isSelected(extension) && isSelected(language)) {
                return cb.and(queryPredicate, extensionPredicate, languagePredicate);
            } // 25
            else if (!isSelected(queryString) && isSelected(genre) && !isSelected(category) && isSelected(extension) && isSelected(language)) {
                return cb.and(genrePredicate, extensionPredicate, languagePredicate);
            } // 26
            else if (!isSelected(queryString) && !isSelected(genre) && !isSelected(category) && isSelected(extension) && isSelected(language)) {
                return cb.and(extensionPredicate, languagePredicate);
            } // 27
            else if (isSelected(queryString) && !isSelected(genre) && isSelected(category) && isSelected(extension) && isSelected(language)) {
                return cb.and(queryPredicate, categoryPredicate, extensionPredicate, languagePredicate);
            } // 28
            else if (!isSelected(queryString) && isSelected(genre) && isSelected(category) && isSelected(extension) && isSelected(language)) {
                return cb.and(genrePredicate, categoryPredicate, extensionPredicate, languagePredicate);
            } // 29
            else if (!isSelected(queryString) && !isSelected(genre) && isSelected(category) && isSelected(extension) && isSelected(language)) {
                return cb.and(categoryPredicate, extensionPredicate, languagePredicate);
            } // 30
            else if (!isSelected(queryString) && !isSelected(genre) && !isSelected(category) && !isSelected(extension) && isSelected(language)) {
                return languagePredicate;
            } // 31

            return null;
        };
    }

    private static boolean isSelected(String value) {
        return !isEmpty(value) &&
               !value.equalsIgnoreCase("undefined") &&
               !(value.startsWith("--") && value.endsWith("--"));
    }
}
