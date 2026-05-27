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
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(resolveSmtpUser(), "Roomify Hotel", "UTF-8"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail.trim()));
        message.setSubject("Xác nhận đặt phòng Roomify - " + safe(bookingCode), "UTF-8");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String html = buildBookingConfirmationHtml(
                customerName,
                phone,
                idNo,
                bookingCode,
                invoiceCode,
                formatDate(checkInDate, dateFormatter),
                formatDate(checkOutDate, dateFormatter),
                roomSummary,
                totalAmount,
                paidAmount,
                remainingAmount
        );

        message.setContent(html, "text/html; charset=UTF-8");
        Transport.send(message);
    }

    private static String buildBookingConfirmationHtml(
            String customerName,
            String phone,
            String idNo,
            String bookingCode,
            String invoiceCode,
            String checkInDate,
            String checkOutDate,
            String roomSummary,
            long totalAmount,
            long paidAmount,
            long remainingAmount
    ) {
        String safeCustomerName = escapeHtml(safe(customerName));
        String safeBookingCode = escapeHtml(safe(bookingCode));
        String safeInvoiceCode = escapeHtml(safe(invoiceCode));

        return """
                <!doctype html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Xác nhận đặt phòng Roomify</title>
                </head>
                <body style="margin:0;padding:0;background:#eef3f8;font-family:'Segoe UI',Arial,sans-serif;color:#0f172a;">
                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#eef3f8;padding:28px 12px;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="640" cellpadding="0" cellspacing="0" style="width:640px;max-width:100%%;background:#ffffff;border:1px solid #dbe5f1;border-radius:18px;overflow:hidden;box-shadow:0 18px 42px rgba(15,23,42,0.12);">
                                    <tr>
                                        <td style="background:#172554;padding:28px 32px;color:#ffffff;">
                                            <div style="font-size:13px;letter-spacing:0.08em;text-transform:uppercase;color:#bfdbfe;font-weight:700;">Roomify Hotel</div>
                                            <h1 style="margin:8px 0 6px;font-size:26px;line-height:1.25;color:#ffffff;">Đặt phòng thành công</h1>
                                            <div style="font-size:15px;color:#dbeafe;">Cảm ơn quý khách đã tin tưởng và đặt phòng tại Roomify.</div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:28px 32px 8px;">
                                            <p style="margin:0 0 18px;font-size:15px;line-height:1.7;">Xin chào <strong>%s</strong>,</p>
                                            <p style="margin:0 0 20px;font-size:15px;line-height:1.7;color:#334155;">Roomify gửi quý khách thông tin xác nhận đặt phòng. Khi đến nhận phòng, vui lòng cung cấp <strong>mã đặt phòng</strong> và <strong>CCCD/Hộ chiếu</strong> để lễ tân kiểm tra.</p>
                                            <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="margin:0 0 18px;">
                                                <tr>
                                                    <td style="width:50%%;padding:12px 14px;background:#eff6ff;border:1px solid #bfdbfe;border-radius:12px;">
                                                        <div style="font-size:12px;color:#64748b;margin-bottom:4px;">Mã đặt phòng</div>
                                                        <div style="font-size:22px;font-weight:800;color:#1d4ed8;">%s</div>
                                                    </td>
                                                    <td style="width:14px;"></td>
                                                    <td style="width:50%%;padding:12px 14px;background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;">
                                                        <div style="font-size:12px;color:#64748b;margin-bottom:4px;">Mã hóa đơn</div>
                                                        <div style="font-size:22px;font-weight:800;color:#0f172a;">%s</div>
                                                    </td>
                                                </tr>
                                            </table>
                                            %s
                                            %s
                                            <div style="background:#fff7ed;border:1px solid #fed7aa;border-radius:12px;padding:14px 16px;margin:20px 0 4px;color:#7c2d12;font-size:14px;line-height:1.6;">
                                                <strong>Lưu ý:</strong> Quý khách vui lòng kiểm tra kỹ ngày nhận phòng, ngày trả phòng và thông tin liên hệ. Nếu cần điều chỉnh, hãy liên hệ khách sạn trước thời gian nhận phòng.
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:18px 32px 28px;">
                                            <div style="height:1px;background:#e2e8f0;margin-bottom:18px;"></div>
                                            <div style="font-size:14px;line-height:1.7;color:#475569;">
                                                Trân trọng,<br>
                                                <strong style="color:#0f172a;">Roomify Hotel</strong>
                                            </div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                safeCustomerName,
                safeBookingCode,
                safeInvoiceCode,
                bookingInfoTable(customerName, phone, idNo, checkInDate, checkOutDate, roomSummary),
                paymentInfoTable(totalAmount, paidAmount, remainingAmount)
        );
    }

    private static String bookingInfoTable(
            String customerName,
            String phone,
            String idNo,
            String checkInDate,
            String checkOutDate,
            String roomSummary
    ) {
        return """
                <h2 style="margin:20px 0 10px;font-size:17px;color:#0f172a;">Thông tin đặt phòng</h2>
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="border-collapse:separate;border-spacing:0;border:1px solid #e2e8f0;border-radius:12px;overflow:hidden;">
                    %s
                    %s
                    %s
                    %s
                    %s
                    %s
                </table>
                """.formatted(
                infoRow("Khách đại diện", safe(customerName), false),
                infoRow("Số điện thoại", safe(phone), true),
                infoRow("CCCD/Hộ chiếu", safe(idNo), false),
                infoRow("Ngày nhận phòng", safe(checkInDate), true),
                infoRow("Ngày trả phòng", safe(checkOutDate), false),
                infoRow("Phòng đã đặt", safe(roomSummary), true)
        );
    }

    private static String paymentInfoTable(long totalAmount, long paidAmount, long remainingAmount) {
        return """
                <h2 style="margin:22px 0 10px;font-size:17px;color:#0f172a;">Thông tin thanh toán</h2>
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="border-collapse:separate;border-spacing:0;border:1px solid #e2e8f0;border-radius:12px;overflow:hidden;">
                    %s
                    %s
                    %s
                </table>
                """.formatted(
                moneyRow("Tổng hóa đơn (tại thời điểm đặt cọc)", totalAmount, false, "#0f172a"),
                moneyRow("Đã thanh toán/đặt cọc", paidAmount, true, "#15803d"),
                moneyRow("Còn lại", remainingAmount, false, "#b45309")
        );
    }

    private static String infoRow(String label, String value, boolean shaded) {
        String background = shaded ? "#f8fafc" : "#ffffff";
        return """
                <tr>
                    <td style="padding:11px 14px;background:%s;border-bottom:1px solid #e2e8f0;color:#64748b;font-size:13px;width:38%%;">%s</td>
                    <td style="padding:11px 14px;background:%s;border-bottom:1px solid #e2e8f0;color:#0f172a;font-size:14px;font-weight:600;">%s</td>
                </tr>
                """.formatted(background, escapeHtml(label), background, escapeHtml(value));
    }

    private static String moneyRow(String label, long amount, boolean shaded, String color) {
        String background = shaded ? "#f8fafc" : "#ffffff";
        return """
                <tr>
                    <td style="padding:12px 14px;background:%s;border-bottom:1px solid #e2e8f0;color:#64748b;font-size:13px;width:48%%;">%s</td>
                    <td style="padding:12px 14px;background:%s;border-bottom:1px solid #e2e8f0;color:%s;font-size:16px;font-weight:800;text-align:right;">%s</td>
                </tr>
                """.formatted(background, escapeHtml(label), background, color, escapeHtml(formatVND(amount)));
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
        return String.format("%,d", Math.max(0L, amount)).replace(',', '.') + " đ";
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "--" : value.trim();
    }

    private static String escapeHtml(String value) {
        return safe(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
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
