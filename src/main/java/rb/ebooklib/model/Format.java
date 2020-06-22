package rb.ebooklib.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "lib_format")
@Data
@NoArgsConstructor
public class Format {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String value;

    public Format(final String name, final String value) {
        this.name = name;
        this.value = value;
    }
}
