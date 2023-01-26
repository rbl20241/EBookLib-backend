package rb.ebooklib.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "LibUser")
@Data
public class User {

    @Id
    @GeneratedValue
    private Long id;

    private String username;
    private String email;

    @JsonIgnore
    private String password;

    @JsonIgnore
    private LocalDate creationDate = LocalDate.now();

}
