package kqlhotel.gui.tabs;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import kqlhotel.gui.components.RoundedPanel;
import net.miginfocom.swing.MigLayout;

public class UnderDevelopmentPanel extends JPanel {
    public UnderDevelopmentPanel(String moduleName) {
        setOpaque(false);
        setLayout(new MigLayout("insets 20", "[grow]", "[grow]"));

        RoundedPanel card = new RoundedPanel(18, new Color(29, 46, 78), new Color(255, 255, 255, 20), 1f);
        card.setLayout(new MigLayout("wrap 1,insets 30,gap 8", "[grow,fill]", "[]"));

        JLabel icon = new JLabel("...", SwingConstants.CENTER);
        icon.setForeground(new Color(255, 171, 89));
        icon.setFont(icon.getFont().deriveFont(42f));

        JLabel title = new JLabel(moduleName, SwingConstants.CENTER);
        title.setForeground(new Color(239, 244, 255));
        title.setFont(title.getFont().deriveFont(34f));

        JLabel status = new JLabel("Đang phát triển", SwingConstants.CENTER);
        status.setForeground(new Color(149, 167, 202));
        status.setFont(status.getFont().deriveFont(24f));

        JLabel note = new JLabel("Module này đang được thành viên khác triển khai.", SwingConstants.CENTER);
        note.setForeground(new Color(119, 139, 176));

        card.add(icon);
        card.add(title);
        card.add(status);
        card.add(note);

        add(card, "alignx center,aligny center,w 760!,h 320!");
    }
}
