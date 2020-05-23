package rb.ebooklib.isbnapimodels.googleapi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ImageLinks {

    private String smallThumbnail;
    private String thumbnail;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

}
