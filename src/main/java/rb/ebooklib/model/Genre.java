package rb.ebooklib.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "genre")
@Data
@NoArgsConstructor
public class Genre {
    @Id
    @GeneratedValue
    private Long id;
    private String name;

    public Genre(final String name) {
        this.name = name;
    }
}
