package rb.ebooklib.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rb.ebooklib.models.Author;
import rb.ebooklib.repositories.AuthorRepository;
import rb.ebooklib.repositories.BookRepository;
import rb.ebooklib.specifications.BookSpecifications;

import java.util.List;
import java.util.stream.Collectors;

import static rb.ebooklib.util.NullOrEmptyUtil.isNullOrEmpty;

@Service
public class AuthorService {

    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private BookRepository bookRepository;

    /**
     * Database call to retrieve all authors
     *
     * @return list of existing authors
     */
    public List<Author> getAllAuthors() {
        return authorRepository.findAllByOrderByNameAsc();
    }

    /**
     * Check if all the authors from the list exist in the database.
     * If an author does not exist, it will be added to the database.
     *
     * Access modifier is set to public to ba able to unit test the method in PersistenceTest.class
     *
     * @param authors a list of authors saved with the book.
     * @return list of authors with id's that are added or updated with the book.
     */
    @Transactional
    public List<Author> mergeNewAuthors(List<Author> authors) {
        return authors.stream().filter(author -> !isNullOrEmpty(author.getName()))
                .map(author -> authorRepository.findOneByName(author.getName())
                        .orElseGet(() -> authorRepository.save(author)))
                .collect(Collectors.toList());
    }

    @Transactional
    void removeAuthorWhenOrphan(final Long authorId) {
        if (bookRepository.count(BookSpecifications.bookHasAuthorId(authorId)) == 0) {
            authorRepository.findById(authorId).ifPresent(author -> authorRepository.deleteById(author.getId()));
        }
    }

}
