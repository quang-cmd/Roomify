package kqlhotel.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class EmailService {

    private static final String EMAIL_FROM = "nguykhaluan2501@gmail.com";

    // App Password Gmail, không phải mật khẩu Gmail thường
    private static final String APP_PASSWORD = "mijljfznuageeyrm";

    // Email quản lý nhận yêu cầu cấp lại mật khẩu
    private static final String MANAGER_EMAIL = "nguykhaluan2501@gmail.com";

    private EmailService() {
    }

    public static void sendPasswordResetRequest(String usernameOrEmail) throws Exception {
        Session session = createMailSession();

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(EMAIL_FROM));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(MANAGER_EMAIL)
        );

        String time = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        );

        message.setSubject("Yeu cau cap lai mat khau - KQL HOTEL");
        message.setText(
                "Xin chao quan ly,\n\n"
                        + "Co mot nhan vien vua gui yeu cau cap lai mat khau.\n\n"
                        + "Thong tin tai khoan/email: " + usernameOrEmail + "\n"
                        + "Thoi gian gui yeu cau: " + time + "\n\n"
                        + "Vui long kiem tra va cap lai mat khau moi cho nhan vien.\n\n"
                        + "KQL HOTEL"
        );

        Transport.send(message);
    }

    private static Session createMailSession() {
        Properties props = new Properties();

        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        System.out.println("EMAIL_FROM = " + EMAIL_FROM);
        System.out.println("APP_PASSWORD length = " + APP_PASSWORD.length());

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, APP_PASSWORD);
            }
        });
    }
    public static void sendPasswordResetRequestToManager(
            String maNV,
            String hoTenNV,
            String sdt,
            String tenDangNhap
    ) throws Exception {
        String managerEmail = "nguykhaluan2501@gmail.com"; // Gmail quản lý

        Session session = createMailSession();

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(EMAIL_FROM));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(managerEmail)
        );

        message.setSubject("Yeu cau cap lai mat khau - KQL HOTEL");
        message.setText(
                "Xin chao quan ly,\n\n"
                        + "Co mot nhan vien vua gui yeu cau cap lai mat khau.\n\n"
                        + "Ma nhan vien: " + maNV + "\n"
                        + "Ten nhan vien: " + hoTenNV + "\n"
                        + "So dien thoai: " + sdt + "\n"
                        + "Ten dang nhap: " + tenDangNhap + "\n\n"
                        + "Vui long doi lai mat khau moi va thong bao cho nhan vien.\n\n"
                        + "KQL HOTEL"
        );

        Transport.send(message);
    }
}
