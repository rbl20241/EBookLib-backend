package rb.ebooklib.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "Settings")
@Data
public class Settings {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name="user_id")
    private Long userId;

    private String libraryMap;
    private String calibreCommand;

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
}