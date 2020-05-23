package rb.ebooklib.isbnapimodels.googleapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Item {

    private String id;
    private VolumeInfo volumeInfo;
    private SearchInfo searchInfo;

}
