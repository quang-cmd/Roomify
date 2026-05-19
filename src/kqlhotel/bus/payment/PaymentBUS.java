package kqlhotel.bus.payment;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import kqlhotel.dao.payment.PaymentDAO;
import kqlhotel.entity.Payment;

public class PaymentBUS {
    private final PaymentDAO paymentDAO = new PaymentDAO();

    public boolean recordPayment(Payment payment) {
        return paymentDAO.create(payment);
    }

    public boolean recordPayment(Connection con, Payment payment) throws SQLException {
        return paymentDAO.create(con, payment);
    }

    public String getNextId() {
        return paymentDAO.getNextId();
    }

    public String getNextId(Connection con) throws SQLException {
        return paymentDAO.getNextId(con);
    }

    public double getTotalPaidByInvoice(String maHD) {
        return paymentDAO.getTotalPaidByInvoice(maHD);
    }

    public List<Payment> getPaymentsByInvoice(String maHD) {
        return paymentDAO.getByInvoice(maHD);
    }

    public double getSuccessfulRevenue(LocalDateTime start, LocalDateTime end) {
        return paymentDAO.getSuccessfulRevenue(start, end);
    }

    public Map<LocalDate, Double> getSuccessfulDailyRevenue(LocalDate start, LocalDate end) {
        return paymentDAO.getSuccessfulDailyRevenue(start, end);
    }

    public Map<String, Double> getSuccessfulMonthlyRevenue(LocalDate start, LocalDate end) {
        return paymentDAO.getSuccessfulMonthlyRevenue(start, end);
    }
}
