package rb.ebooklib.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "lib_separator")
@Data
@NoArgsConstructor
public class Separator {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String value;

    public Separator(final String name, final String value) {
        this.name = name;
        this.value = value;
    }
}
