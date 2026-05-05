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
            System.out.println("Ket noi Database thanh cong!");
        } catch (Exception e) {
            String message = "Khong the ket noi database.\nVui long kiem tra cau hinh SQL Server.\n\nChi tiet: " + e.getMessage();
            System.err.println(message);
            JOptionPane.showMessageDialog(null, message, "Loi ket noi Database", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            UiTheme.setup();
            AppFrame frame = new AppFrame();
            frame.setVisible(true);
        });
    }
}