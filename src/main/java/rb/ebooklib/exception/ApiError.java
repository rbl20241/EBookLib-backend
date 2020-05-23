package rb.ebooklib.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
public class ApiError {

    /**
     * The operation call status. It will be anything from 4xx to signalize client errors
     * or 5xx to mean server errors. A common scenario is a http code 400 that means a BAD_REQUEST, when the client,
     * for example, sends an improperly formatted field, like an invalid email address.
     */
    private HttpStatus status;

    /**
     * The date-time instance of when the error happened.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private LocalDateTime timestamp;

    /**
     * a user-friendly message about the error.
     */
    private String message;

    /**
     * a system message describing the error in more detail.
     */
    private String debugMessage;

    private ApiError() {
        timestamp = LocalDateTime.now();
    }

    ApiError(HttpStatus status) {
        this();
        this.status = status;
    }

    ApiError(HttpStatus status, String message, Throwable ex) {
        this();
        this.status = status;
        this.message = message;
        this.debugMessage = ex.getLocalizedMessage();
    }

}
