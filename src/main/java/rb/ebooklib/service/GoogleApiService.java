package rb.ebooklib.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rb.ebooklib.dto.BookDTO;
import rb.ebooklib.isbnapimodels.googleapi.GoogleBookResponse;
import rb.ebooklib.util.ViewObjectMappers;

import java.io.IOException;
import java.util.Collections;

@Service
public class GoogleApiService {

    @Value( "${google.api.key}" )
    private String googleApiKey;

    private static final String API_URL = "https://www.googleapis.com/books/v1/volumes?q=isbn:{isbn}&key={key}";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ViewObjectMappers viewObjectMappers;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Search a book in the google library by its isbn number.
     *
     * @param isbn identifier
     * @return book transfer object
     */
    public BookDTO searchBookByIsbn(String isbn) {
        isbn = isbn.replaceAll("\\D", "");

        var headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        var entity = new HttpEntity<>("parameters", headers);
        var result = restTemplate.exchange(API_URL, HttpMethod.GET, entity, String.class, isbn, googleApiKey);

        var jsonResponse = result.getBody();
        try {
            var googleBookResponse = objectMapper.readValue(jsonResponse, GoogleBookResponse.class);
            return viewObjectMappers.prepareDTOFromGoogleApiResponse(googleBookResponse);
        } catch (IOException e) {
            throw new RuntimeException("Exception while processing google API" + e.getMessage() );
        }
    }

}
