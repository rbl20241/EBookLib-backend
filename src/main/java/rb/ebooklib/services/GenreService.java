package rb.ebooklib.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rb.ebooklib.models.Genre;
import rb.ebooklib.repositories.BookRepository;
import rb.ebooklib.specifications.BookSpecifications;
import rb.ebooklib.repositories.GenreRepository;

import java.util.List;

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
