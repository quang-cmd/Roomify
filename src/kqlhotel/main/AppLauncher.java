package kqlhotel.main;

import javax.swing.SwingUtilities;
import kqlhotel.gui.AppFrame;
import kqlhotel.gui.theme.UiTheme;

public final class AppLauncher {
    private AppLauncher() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UiTheme.setup();
            AppFrame frame = new AppFrame();
            frame.setVisible(true);
        });
    }
}
