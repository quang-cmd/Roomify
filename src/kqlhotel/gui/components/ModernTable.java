package kqlhotel.gui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ModernTable extends JTable {
    public ModernTable(Object[][] data, String[] columns) {
        super(new DefaultTableModel(data, columns));
        setRowHeight(35);
        getTableHeader().setFont(new Font("Inter", Font.BOLD, 12));
        getTableHeader().setBackground(Color.WHITE);
        setFont(new Font("Inter", Font.PLAIN, 12));
        setShowVerticalLines(false);
        setGridColor(new Color(240, 240, 240));
        setSelectionBackground(new Color(245, 246, 250));
        setSelectionForeground(Color.BLACK);
    }
}
