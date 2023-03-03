package rb.ebooklib.services;

import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import rb.ebooklib.models.Book;
import rb.ebooklib.models.User;
import rb.ebooklib.models.UserSettings;
import rb.ebooklib.repositories.UserRepository;

import java.io.File;
import java.util.Properties;

import static rb.ebooklib.ebooks.util.BookUtil.isNullOrEmptyString;

@Service
@Slf4j
public class EmailService {
    @Autowired
    private UserRepository userRepository;

    private UserSettings userSettings;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private UserSettingsService userSettingsService;

    @Autowired
    private UserService userService;

    @Value("${spring.mail.debug}")
    private String mailDebug;

    @Value("${spring.mail.auth}")
    private String mailAuth;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private String smtpAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private String smtpStarttlsEnable;

    @Value("${spring.mail.properties.mail.smtp.starttls.required}")
    private String smtpStarttlsRequired;

    @Value("${spring.mail.properties.mail.transport.protocol}")
    private String transportProtocol;

    private JavaMailSender getJavaMailSender() {
        User user = userService.getCurrentlyLoggedInUser();
        userSettings = userSettingsService.getByUserId(user.getId());
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(userSettings.getMailHost());
        mailSender.setPort(Integer.parseInt(userSettings.getMailPort()));
        mailSender.setUsername(userSettings.getMailUserName());
        mailSender.setPassword(userSettings.getMailPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("spring.mail.host", userSettings.getMailHost());
        props.put("spring.mail.port", Integer.parseInt(userSettings.getMailPort()));
        props.put("spring.mail.username", userSettings.getMailUserName());
        props.put("spring.mail.password", userSettings.getMailPassword());
        props.put("spring.mail.auth", mailAuth);
        props.put("spring.mail.debug", mailDebug);
        //props.put("spring.mail.properties.mail.smtp.port", 587);
        props.put("spring.mail.properties.mail.smtp.auth", smtpAuth);
        props.put("spring.mail.properties.mail.smtp.starttls.enable", smtpStarttlsEnable);
        props.put("spring.mail.properties.mail.smtp.starttls.required", smtpStarttlsRequired);
        props.put("spring.mail.properties.mail.transport.protocol", transportProtocol);

        props.put("spring.mail.properties.mail.smtp.ssl.trust", userSettings.getMailHost());


        return mailSender;
    }

    public void sendBook(final Book book, final String mailTo) {
        javaMailSender = getJavaMailSender();

        var message = javaMailSender.createMimeMessage();
        try {
            var helper = new MimeMessageHelper(message, true);
            helper.setSubject("Je hebt het boek '" + book.getTitle()  + "' ontvangen");
            helper.setFrom(userSettings.getMailUserName());
            helper.setTo(isNullOrEmptyString(mailTo) ? userSettings.getMailTo() : mailTo);
            helper.setReplyTo(userSettings.getMailUserName());
            helper.setText(getText(book.getDescription()), true);

            val attachmentText = book.getAuthor() + " - " + book.getTitle() + "." + book.getExtension();
            val attachmentFile = new FileSystemResource((new File(book.getFilename())));
            helper.addAttachment(attachmentText, attachmentFile);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

//    public void sendBook(final Book book, final String mailTo) {
//        javaMailSender = getJavaMailSender();
//
//        MimeMessage message = javaMailSender.createMimeMessage();
//        MimeMessageHelper helper;
//        try {
//            helper = new MimeMessageHelper(message, true);
//            helper.setSubject("Je hebt het boek '" + book.getTitle()  + "' ontvangen");
//            helper.setFrom(userSettings.getMailUserName());
//            helper.setTo(isNullOrEmptyString(mailTo) ? userSettings.getMailTo() : mailTo);
//            helper.setReplyTo(userSettings.getMailUserName());
//            helper.setText(getText(book.getDescription()), true);
//
//            val attachmentText = book.getAuthor() + " - " + book.getTitle() + "." + book.getExtension();
//            val attachmentFile = new FileSystemResource((new File(book.getFilename())));
//            helper.addAttachment(attachmentText, attachmentFile);;
//            //helper.addAttachment(book.getAuthor() + " - " + book.getTitle() + "." + book.getExtension(), new File(book.getFilename()));
//            javaMailSender.send(message);
//        } catch (MessagingException e) {
//            e.printStackTrace();
//        }
//    }

    private String getText(String description) {
        return "Hoi, <br/><br/>" +
                "Je hebt bovenstaand boek ontvangen.<br><br>" +
                description +
                "<br/><br/>" +
                "Veel leesplezier!";
    }
}
