import java.sql.*;
import java.time.LocalDateTime;
import kqlhotel.dao.payment.PaymentDAO;
import kqlhotel.entity.Payment;
import kqlhotel.dao.ConnectDB;

public class TestPayment {
    public static void main(String[] args) {
        try {
            // Initialize ConnectDB
            ConnectDB.getInstance().connect();
            Connection con = ConnectDB.getConnection();
            if (con == null) {
                System.out.println("Connection failed!");
                return;
            }

            PaymentDAO dao = new PaymentDAO();
            String nextId = dao.getNextId(con);

            // Create a test payment with maPC = null, and a dummy/incorrect maNV = "NV001" (which has no open shift)
            // The globally open shift is PC005 owned by NV002.
            Payment testPayment = new Payment(
                nextId,
                LocalDateTime.now(),
                1000.0,
                "Test Auto ca PC resolving",
                "TienMat",
                "ThanhToanThanhCong",
                "HD001",
                null,
                "NV001"
            );

            System.out.println("Creating test payment with payment.maPC = null, payment.maNV = NV001...");
            boolean success = dao.create(con, testPayment);
            System.out.println("Insert success: " + success);
            System.out.println("Resolved payment properties after insert: ");
            System.out.println("  maPC: " + testPayment.getMaPC());
            System.out.println("  maNV: " + testPayment.getMaNV());

            // Query database directly to verify it was stored correctly
            String sql = "SELECT maTT, maPC, maNV, soTienTT, ghiChu FROM ThanhToan WHERE maTT = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nextId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Database values:");
                        System.out.println("  maPC in DB: " + rs.getString("maPC"));
                        System.out.println("  maNV in DB: " + rs.getString("maNV"));
                    }
                }
            }

            // Cleanup the test record
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM ThanhToan WHERE maTT = ?")) {
                ps.setString(1, nextId);
                ps.executeUpdate();
                System.out.println("Deleted test payment record.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ConnectDB.getInstance().disconnect();
        }
    }
}
