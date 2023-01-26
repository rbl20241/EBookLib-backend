package rb.ebooklib.model;

import lombok.Data;

import jakarta.persistence.*;

@Entity
@Table(name = "lib_rename")
@Data
public class Rename {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name="user_id")
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
