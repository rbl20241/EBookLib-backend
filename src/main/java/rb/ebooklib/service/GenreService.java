package rb.ebooklib.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rb.ebooklib.model.Author;
import rb.ebooklib.model.Genre;
import rb.ebooklib.persistence.AuthorRepository;
import rb.ebooklib.persistence.BookRepository;
import rb.ebooklib.persistence.BookSpecifications;
import rb.ebooklib.persistence.GenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static rb.ebooklib.util.NullOrEmptyUtil.isNullOrEmpty;

@Service
public class GenreService {
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private BookRepository bookRepository;

    public List<Genre> getAllGenres() {
        return genreRepository.findAllByOrderByNameAsc();
    }

    @Transactional
    public Genre mergeNewGenre(Genre genre) {
        return genreRepository.findOneByName(genre.getName()).orElseGet(() -> genreRepository.save(genre));
    }

    @Transactional
    void removeGenreWhenOrphan(final Long genreId) {
        if (bookRepository.count(BookSpecifications.bookHasGenreId(genreId)) == 0) {
            genreRepository.findById(genreId).ifPresent(genre -> genreRepository.deleteById(genre.getId()));
        }
    }

}
