package kqlhotel.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class EmailService {

    private static final String EMAIL_FROM = "anbernguyen29@gmail.com";

    // App Password Gmail, không phải mật khẩu Gmail thường.
    // Ưu tiên biến môi trường ROOMIFY_SMTP_PASSWORD để tránh lộ mật khẩu trong source.
    private static final String APP_PASSWORD = "zywr ktks yqxd zhaf";

    // Email quản lý nhận yêu cầu cấp lại mật khẩu
    private static final String MANAGER_EMAIL = "anbernguyen29@gmail.com";

    private EmailService() {
    }

    public static void sendPasswordResetRequest(String usernameOrEmail) throws Exception {
        Session session = createMailSession();

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(resolveSmtpUser()));
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

    public static void sendBookingConfirmation(
            String toEmail,
            String customerName,
            String phone,
            String idNo,
            String bookingCode,
            String invoiceCode,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            String roomSummary,
            long totalAmount,
            long paidAmount,
            long remainingAmount
    ) throws Exception {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Email khach hang khong hop le.");
        }

        Session session = createMailSession();
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(resolveSmtpUser()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail.trim()));
        message.setSubject("Xac nhan dat phong Roomify - " + bookingCode);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String body =
                "Xin chao " + safe(customerName) + ",\n\n"
                        + "Cam on quy khach da dat phong tai Roomify.\n\n"
                        + "THONG TIN DAT PHONG\n"
                        + "- Ma dat phong: " + safe(bookingCode) + "\n"
                        + "- Ma hoa don: " + safe(invoiceCode) + "\n"
                        + "- Ho ten khach chinh: " + safe(customerName) + "\n"
                        + "- So dien thoai: " + safe(phone) + "\n"
                        + "- CCCD/Passport: " + safe(idNo) + "\n"
                        + "- Ngay nhan phong: " + formatDate(checkInDate, dateFormatter) + "\n"
                        + "- Ngay tra phong: " + formatDate(checkOutDate, dateFormatter) + "\n"
                        + "- Phong da dat: " + safe(roomSummary) + "\n\n"
                        + "THANH TOAN\n"
                        + "- Tong hoa don: " + formatVND(totalAmount) + "\n"
                        + "- Da thanh toan/coc: " + formatVND(paidAmount) + "\n"
                        + "- Con lai: " + formatVND(remainingAmount) + "\n\n"
                        + "Khi nhan phong, vui long cung cap ma dat phong va CCCD/Passport de le tan xac nhan.\n\n"
                        + "Roomify Hotel";

        message.setText(body);
        Transport.send(message);
    }

    private static Session createMailSession() {
        Properties props = new Properties();

        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(resolveSmtpUser(), resolveSmtpPassword());
            }
        });
    }

    private static String resolveSmtpUser() {
        String value = System.getenv("ROOMIFY_SMTP_USER");
        return value == null || value.trim().isEmpty() ? EMAIL_FROM : value.trim();
    }

    private static String resolveSmtpPassword() {
        String value = System.getenv("ROOMIFY_SMTP_PASSWORD");
        return value == null || value.trim().isEmpty() ? APP_PASSWORD : value.trim();
    }

    private static String formatDate(LocalDate date, DateTimeFormatter formatter) {
        return date == null ? "--" : date.format(formatter);
    }

    private static String formatVND(long amount) {
        return String.format("%,d", Math.max(0L, amount)).replace(',', '.') + " d";
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "--" : value.trim();
    }
    public static void sendPasswordResetRequestToManager(
            String maNV,
            String hoTenNV,
            String sdt,
            String tenDangNhap
    ) throws Exception {
        String managerEmail = MANAGER_EMAIL; // Gmail quản lý

        Session session = createMailSession();

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(resolveSmtpUser()));
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
