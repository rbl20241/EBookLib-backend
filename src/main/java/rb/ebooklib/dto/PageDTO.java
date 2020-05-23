package rb.ebooklib.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PageDTO<T> {

    private long totalElements;
    private int totalPages;
    private List<T> content;

    public PageDTO(Page<T> page) {
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.content = page.getContent();
    }
}
