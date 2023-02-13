package rb.ebooklib.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Objects;

@Entity
@Table(name = "lib_rename")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Rename rename = (Rename) o;
        return id != null && Objects.equals(id, rename.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
