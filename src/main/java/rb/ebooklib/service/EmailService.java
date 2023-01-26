package rb.ebooklib.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import rb.ebooklib.model.Book;
import rb.ebooklib.model.User;
import rb.ebooklib.model.UserSettings;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.util.Properties;

import static rb.ebooklib.ebooks.util.BookUtil.isNullOrEmptyString;

@Service
@Slf4j
public class EmailService {

    private UserSettings userSettings;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private UserSettingsService userSettingsService;

    @Autowired
    private UserService userService;

    private JavaMailSender getJavaMailSender() {
        User user = userService.getCurrentlyLoggedInUser();
        userSettings = userSettingsService.getByUserId(user.getId());
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(userSettings.getMailHost());
        mailSender.setPort(Integer.parseInt(userSettings.getMailPort()));
        mailSender.setUsername(userSettings.getMailUserName());
        mailSender.setPassword(userSettings.getMailPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");

        return mailSender;
    }

    public void sendBook(final Book book, final String mailTo) {
        javaMailSender = getJavaMailSender();

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, true);
            helper.setSubject("Je hebt het boek '" + book.getTitle()  + "' ontvangen");
            helper.setFrom(userSettings.getMailUserName());
            helper.setTo(isNullOrEmptyString(mailTo) ? userSettings.getMailTo() : mailTo);
            helper.setReplyTo(userSettings.getMailUserName());
            helper.setText(getText(book.getDescription()), true);
            helper.addAttachment(book.getAuthor() + " - " + book.getTitle() + "." + book.getExtension(), new File(book.getFilename()));
            javaMailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String getText(String description) {
        return "Hoi, <br/><br/>" +
                "Je hebt bovenstaand boek ontvangen.<br><br>" +
                description +
                "<br/><br/>" +
                "Veel leesplezier!";
    }
}
