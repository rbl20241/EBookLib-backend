package rb.ebooklib.ebooks.epub.reader;

import rb.ebooklib.ebooks.epub.domain.EpubBook;

public interface BookProcessor {

    BookProcessor IDENTITY_BOOKPROCESSOR = epubBook -> epubBook;

    EpubBook processBook(EpubBook epubBook);
}