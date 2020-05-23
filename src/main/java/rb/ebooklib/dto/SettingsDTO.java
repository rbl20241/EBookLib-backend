package rb.ebooklib.dto;

import lombok.Data;

@Data
public class SettingsDTO {
    private Long id;
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
