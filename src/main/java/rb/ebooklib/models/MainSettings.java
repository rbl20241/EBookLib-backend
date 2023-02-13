package rb.ebooklib.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Objects;

@Entity
@Table(name = "mainsettings")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class MainSettings {

    @Id
    @GeneratedValue
    private Long id;

    private String libraryMap;
    private String calibreCommand;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        MainSettings that = (MainSettings) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
