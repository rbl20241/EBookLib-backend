package rb.ebooklib.dto;

import lombok.Data;

@Data
public class RenameDTO {
    private Long id;
    private Long userId;
    String sourceMap;
    String sourceTitleAuthorSeparator;
    String sourceAuthornameSeparator;
    String sourceFormat;
    String destMap;
    String destTitleAuthorSeparator;
    String destAuthornameSeparator;
    String destFormat;
}
