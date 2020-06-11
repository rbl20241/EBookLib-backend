package rb.ebooklib.persistence;

import org.springframework.data.jpa.domain.Specification;
import rb.ebooklib.model.*;
import rb.ebooklib.model.Author_;
import rb.ebooklib.model.Book_;
import rb.ebooklib.model.Category_;
import rb.ebooklib.model.Genre_;

import javax.persistence.criteria.*;

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

    public static Specification<Book> bookHasTitleOrAuthor(final String queryString) {
        final String likeQueryString = String.format(LIKE_QUERY_FORMAT, queryString.toLowerCase());
        return (root, query, cb) -> {
            final Join<Book, Author> bookAuthorJoin = root.join(Book_.authors);
            Predicate authorPredicate =  cb.like(cb.lower(bookAuthorJoin.get(Author_.name)), likeQueryString);
            Predicate titlePredicate = cb.like(cb.lower(root.get(Book_.title)), likeQueryString);
            query.distinct(true);
            return cb.or(authorPredicate, titlePredicate);
        };
    }

    public static Specification<Book> bookExtendedSearch(final String queryString, final String category, final String extension) {
        final String likeQueryString = String.format(LIKE_QUERY_FORMAT, queryString.toLowerCase());
        return (root, query, cb) -> {
            final Join<Book, Author> bookAuthorJoin = root.join(Book_.authors);
            final Join<Book, Category> bookCategoryJoin = root.join(Book_.categories);
            Predicate authorPredicate =  null;
            Predicate titlePredicate = null;
            Predicate categoryPredicate = null;
            Predicate extensionPredicate = null;
            Predicate authorOrTitlePredicate = null;

            if (!isEmpty(queryString)) {
                authorPredicate = cb.like(cb.lower(bookAuthorJoin.get(Author_.name)), likeQueryString);
                titlePredicate = cb.like(cb.lower(root.get(Book_.title)), likeQueryString);
                authorOrTitlePredicate = cb.or(authorPredicate, titlePredicate);
            }

            if (isSelected(category)) {
                categoryPredicate = cb.like(cb.lower(bookCategoryJoin.get(Category_.name)), category);
            }

            if (isSelected(extension)) {
                extensionPredicate = cb.like(cb.lower(root.get(Book_.extension)), extension);
            }

            query.distinct(true);
            if (!isEmpty(queryString) && isSelected(category) && isSelected(extension)) {
                return cb.and(authorOrTitlePredicate, categoryPredicate, extensionPredicate);
            }
            else if (!isEmpty(queryString) && isSelected(category) && !isSelected(extension)) {
                return cb.and(authorOrTitlePredicate, categoryPredicate);
            }
            if (!isEmpty(queryString) && !isSelected(category) && isSelected(extension)) {
                return cb.and(authorOrTitlePredicate, extensionPredicate);
            }
            if (!isEmpty(queryString) && !isSelected(category) && !isSelected(extension)) {
                return authorOrTitlePredicate;
            }
            if (isEmpty(queryString) && isSelected(category) && isSelected(extension)) {
                return cb.and(categoryPredicate, extensionPredicate);
            }
            if (isEmpty(queryString) && isSelected(category) && !isSelected(extension)) {
                return categoryPredicate;
            }
            if (isEmpty(queryString) && !isSelected(category) && isSelected(extension)) {
                return extensionPredicate;
            }

            return null;
        };
    }

    private static boolean isSelected(String value) {
        return !value.equalsIgnoreCase("undefined") &&
               !(value.startsWith("--") && value.endsWith("--"));
    }
}
