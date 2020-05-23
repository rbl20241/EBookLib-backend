package rb.ebooklib.isbnapimodels.googleapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class VolumeInfo {

    private String title;
    private List<String> authors = null;
    private String description;
    private List<IndustryIdentifier> industryIdentifiers = null;
    private List<String> categories = null;
    private ImageLinks imageLinks;
    private String language;

}
