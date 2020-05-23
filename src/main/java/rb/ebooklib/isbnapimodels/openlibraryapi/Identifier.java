package rb.ebooklib.isbnapimodels.openlibraryapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Identifier {

    private String isbn_13;
    private String openlibrary;
    private String isbn_10;
}
