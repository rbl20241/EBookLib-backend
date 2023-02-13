package rb.ebooklib.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rb.ebooklib.dto.BookDTO;
import rb.ebooklib.isbnapimodels.openlibraryapi.OpenLibraryBookResponse;
import rb.ebooklib.util.ViewObjectMappers;

import java.io.IOException;
import java.util.Collections;

@Service
public class OpenLibraryService {

    private static final String API_URL = "https://openlibrary.org/api/books?bibkeys=ISBN:{isbn}&jscmd=data&format=json";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ViewObjectMappers viewObjectMappers;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Search a book in the open library by its isbn number.
     *
     * @param isbn identifier
     * @return book transfer object
     */
    public BookDTO searchBookByIsbn(String isbn) {
        isbn = isbn.replaceAll("\\D", "");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        ResponseEntity<String> result = restTemplate.exchange(API_URL, HttpMethod.GET, entity, String.class, isbn);

        String jsonResponse = result.getBody();
        String response = removeDynamicPartOfResult(jsonResponse);
        try {
            OpenLibraryBookResponse openLibraryBookResponse = objectMapper.readValue(response, OpenLibraryBookResponse.class);
            return viewObjectMappers.prepareDTOFromOpenLibApiResponse(openLibraryBookResponse);
        } catch (IOException e) {
            throw new RuntimeException("Exception while processing Open library API" + e.getMessage());
        }
    }

    /**
     * Remove first dynamic part (eg {"ISBN:0991344618":) and ending bracket from result
     * then scan and replace [" and "] to just quotes from the identifier elements
     * @param jsonResponse response as received from openLibrary
     * @return response able to use in ObjectMapper
     */
    private String removeDynamicPartOfResult(String jsonResponse) {
        if (jsonResponse != null && !jsonResponse.isEmpty() && !jsonResponse.equals("{}")) {
            jsonResponse = jsonResponse.substring(jsonResponse.indexOf(":" , jsonResponse.indexOf(":") + 1) + 1);
            jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 1);
            jsonResponse = jsonResponse.replaceAll("\\[\"", "\"");
            jsonResponse = jsonResponse.replaceAll("\"\\]", "\"");

        }
        return jsonResponse;
    }
}
