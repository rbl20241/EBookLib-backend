package rb.ebooklib.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static rb.ebooklib.util.NullOrEmptyUtil.isNullOrEmpty;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private BookService bookService;

//    public void sendReservationRequest(final Reservation reservation) {
//        var requester = reservation.getBorrower().getEmail();
//        var bookTitle = bookService.getById(reservation.getBookOwner().getBookId()).getTitle();
//        var toAddress = reservation.getBookOwner().getOwner().getEmail();
//
//        final SimpleMailMessage msg = new SimpleMailMessage();
//        msg.setTo(toAddress);
//        msg.setSubject("Reservation request from the Ordina Library Sharing App");
//        msg.setText(String.format("Reservation request for book: \n\n %s \n\n from the Ordina Library Sharing App \n Requested by: %s", bookTitle, requester));
//        sendMessage(msg);
//    }
//
//    public void sendReservationConfirmation(final Reservation reservation) {
//        var owner = reservation.getBookOwner().getOwner().getEmail();
//        var bookTitle = bookService.getById(reservation.getBookOwner().getBookId()).getTitle();
//        var toAddress = reservation.getBorrower().getEmail();
//
//        final SimpleMailMessage msg = new SimpleMailMessage();
//        msg.setTo(toAddress);
//        msg.setSubject("Confirmation on reservation request from the Ordina Library Sharing App");
//        String message = String.format("Confirmation on reservation request for book: \n\n %1$s \n\n from the Ordina Library Sharing App \n\nfrom: %2$s \n\nStatus: %3$s", bookTitle, owner, reservation.getStatus());
//        if (!StringUtils.isEmpty(reservation.getDeclineReason())) {
//            message = message + "\n\nReason: " + reservation.getDeclineReason();
//        }
//        msg.setText(message);
//        sendMessage(msg);
//    }
//
    private void sendMessage(final SimpleMailMessage msg) {
        String subject;
        if (!isNullOrEmpty(msg.getSubject()) && msg.getSubject().startsWith("Reservation request")) {
            subject = "request";
        } else {
            subject = "confirmation";
        }
        try {
            javaMailSender.send(msg);
        } catch (MailSendException mse) {
            log.info(String
                    .format("Email service not available!!!! Email message should be done by hand %s", msg.toString()));
        }
        log.info(String.format("Reservation %s emailed to %s", subject, Arrays.toString(msg.getTo())));
    }
}
