package rb.ebooklib.model;

import lombok.Data;

import jakarta.persistence.*;

@Entity
@Table(name = "mainsettings")
@Data
public class MainSettings {

    @Id
    @GeneratedValue
    private Long id;

    private String libraryMap;
    private String calibreCommand;
}
