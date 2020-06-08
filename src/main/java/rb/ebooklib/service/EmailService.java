package rb.ebooklib.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import rb.ebooklib.model.Book;
import rb.ebooklib.model.Settings;
import rb.ebooklib.model.User;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Properties;

import static rb.ebooklib.ebooks.util.BookUtil.isNullOrEmptyString;

@Service
@Slf4j
public class EmailService {

    private Settings settings;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private BookService bookService;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private UserService userService;

    private JavaMailSender getJavaMailSender() {
        User user = userService.getCurrentlyLoggedInUser();
        settings = settingsService.getByUserId(user.getId());
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(settings.getMailHost());
        mailSender.setPort(Integer.parseInt(settings.getMailPort()));
        mailSender.setUsername(settings.getMailUserName());
        mailSender.setPassword(settings.getMailPassword());

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
        MimeMessageHelper helper = null;
        try {
            helper = new MimeMessageHelper(message, true);
            helper.setSubject("Je hebt het boek '" + book.getTitle()  + "' ontvangen");
            helper.setFrom(settings.getMailUserName());
            helper.setTo(isNullOrEmptyString(mailTo) ? settings.getMailTo() : mailTo);
            helper.setReplyTo(settings.getMailUserName());
            helper.setText(getText(book.getDescription()), true);
            helper.addAttachment(book.getAuthor() + " - " + book.getTitle(), new File(book.getFilename()));
            javaMailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String getText(String description) {
        StringBuilder content = new StringBuilder();
        content.append("Hoi, <br/><br/>");
        content.append("Je hebt bovenstaand boek ontvangen.<br><br>");
        content.append(description);
        content.append("<br/><br/>");
        content.append("Veel leesplezier!");

        return content.toString();
    }
}
