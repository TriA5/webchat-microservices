package user_service.example.user_service.service.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService{
    @Autowired
    private JavaMailSender emailSender;

    @Override
    public void sendMessage(String from, String to, String subject, String message) {
        try{
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            // Thiết lập thông tin
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(message, true); // Bật chế độ HTML

            // Gửi email
            emailSender.send(mimeMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}

