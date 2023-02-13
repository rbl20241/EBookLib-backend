package rb.ebooklib.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import rb.ebooklib.models.User;
import rb.ebooklib.util.Status;

import java.time.LocalDate;

@Data
public class ReservationDTO {

    private Long id;

    private User borrower;
    private User owner;
    private BookDTO book;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    private LocalDate endDate;

    private Status status;
    private String declineReason;
}
