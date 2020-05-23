package rb.ebooklib.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rb.ebooklib.model.Book;

@Service
public class ToolService {

    @Autowired
    private BookService bookService;

    public Book getBookById(final Long bookId) {
        return bookService.getById(bookId);
    }
}
