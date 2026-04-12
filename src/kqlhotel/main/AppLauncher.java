package kqlhotel.main;

import javax.swing.SwingUtilities;
import kqlhotel.gui.AppFrame;
import kqlhotel.gui.theme.UiTheme;

public final class AppLauncher {
    private AppLauncher() {
    }

    public static void main(String[] args) {
        // Cần kết nối Database trước khi load dữ liệu
        /*
        try {
            kqlhotel.dao.ConnectDB.getInstance().connect();
            System.out.println("Kết nối Database thành công!");
        } catch (Exception e) {
            System.err.println("Lỗi kết nối Database: " + e.getMessage());
        }
        */

        SwingUtilities.invokeLater(() -> {
            UiTheme.setup();
            AppFrame frame = new AppFrame();
            frame.setVisible(true);
        });
    }
}