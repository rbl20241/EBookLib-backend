package rb.ebooklib.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDTO {
    @JsonIgnore
    private int id;

    @JsonIgnore
    private LocalDate creationDate;

    private String email;
    private String password;
    private String username;
}
