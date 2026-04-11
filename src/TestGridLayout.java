import javax.swing.*;
import java.awt.*;
public class TestGridLayout {
    public static void main(String[] args) {
        JFrame f = new JFrame();
        JPanel p = new JPanel(new GridLayout(0, 5, 10, 10));
        p.add(new JButton("1"));
        p.add(new JButton("2"));
        f.add(p, BorderLayout.NORTH);
        f.setSize(500, 200);
        f.setVisible(true);
        SwingUtilities.invokeLater(() -> {
            System.out.println("Button 1 size: " + p.getComponent(0).getSize());
            System.out.println("Button 2 size: " + p.getComponent(1).getSize());
            System.out.println("Panel size: " + p.getSize());
            System.exit(0);
        });
    }
}
