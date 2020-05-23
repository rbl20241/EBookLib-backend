package rb.ebooklib.isbnapimodels.openlibraryapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreType
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class OpenLibraryBookResponse {

    private List<Publisher> publishers = null;
    @JsonProperty("identifiers")
    private Identifier identifier = null;
    private String title;
    private String url;
    private Cover cover;
    private List<Author> authors = null;

}
