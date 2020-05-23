package rb.ebooklib.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
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
