package kqlhotel.main;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import kqlhotel.dao.ConnectDB;
import kqlhotel.gui.AppFrame;
import kqlhotel.gui.theme.UiTheme;

public final class AppLauncher {
    private AppLauncher() {
    }
    public static void main(String[] args) {
        try {
            ConnectDB.getInstance().connect();
            System.out.println("Kết nối database thành công!");
        } catch (Exception e) {
            String message = "Không thể kết nối database.\nVui lòng kiểm tra cấu hình SQL Server.\n\nChi tiet: " + e.getMessage();
            System.err.println(message);
            JOptionPane.showMessageDialog(null, message, "Lỗi kết nối database", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            UiTheme.setup();
            AppFrame frame = new AppFrame();
            frame.setVisible(true);
        });
    }
}