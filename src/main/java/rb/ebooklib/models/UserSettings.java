package rb.ebooklib.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Objects;

@Entity
@Table(name = "usersettings")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class UserSettings {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name="user_id")
    private Long userId;

    private String copyTo;
    private String mailTo;

    private Boolean isDateSort;
    private Boolean isNameSort;

    private Boolean isEpubSelected;
    private Boolean isMobiSelected;
    private Boolean isPdfSelected;
    private Boolean isCbrSelected;

    private String mailHost;
    private String mailPort;
    private String mailUserName;
    private String mailPassword;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserSettings that = (UserSettings) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
