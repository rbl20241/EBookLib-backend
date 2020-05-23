package rb.ebooklib.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class UserDTO {
    @JsonIgnore
    private int id;
    private String username;
    private String email;
    private String password;
}
