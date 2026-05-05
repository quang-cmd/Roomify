package kqlhotel.gui.components;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import kqlhotel.gui.theme.ThemeColors;
import kqlhotel.gui.tabs.RoomManagementPanel;
import kqlhotel.entity.Room;
import kqlhotel.entity.RoomType;
import kqlhotel.bus.room.RoomBUS;

public class RoomSearchDialog extends JDialog {

    private JTextField txtRoomId;
    private JComboBox<String> cbRoomType;
    private final kqlhotel.dao.room.RoomTypeDAO roomTypeDAO = new kqlhotel.dao.room.RoomTypeDAO();
    private JComboBox<String> cbStatus;
    private JPanel resultContainer;
    private final RoomManagementPanel roomPanel;

    public RoomSearchDialog(Window owner, RoomManagementPanel roomPanel) {
        super(owner, "Search Room", ModalityType.APPLICATION_MODAL);
        this.roomPanel = roomPanel;
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        RoundedPanel rootPanel = new RoundedPanel(16, Color.WHITE, new Color(226, 232, 240), 1);
        rootPanel.setLayout(new MigLayout("insets 0, wrap 1, gap 0", "[fill, 800!]", "[][][][grow,fill]"));
        rootPanel.setOpaque(false);

        rootPanel.add(createHeader());
        rootPanel.add(createFieldsRow(), "growx");
        rootPanel.add(createActionRow(), "growx");

        JPanel resultWrapper = new JPanel(new MigLayout("insets 0 20 20 20", "[grow,fill]", "[grow,fill]"));
        resultWrapper.setOpaque(false);
        resultWrapper.add(createResultArea());
        rootPanel.add(resultWrapper, "grow");

        setContentPane(rootPanel);
        pack();
        setLocationRelativeTo(owner);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new MigLayout("insets 16 20 16 20", "[][grow][]", "[]")) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 20, 16, 16);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        JLabel title = new JLabel("Room Search");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(15, 23, 42));
        
        JButton btnClose = new JButton("×");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 24));
        btnClose.setForeground(new Color(100, 116, 139));
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        header.add(new JLabel("🔍"), "w 32!, h 32!");
        header.add(title, "gapx 10");
        header.add(btnClose, "top");
        return header;
    }

    private JPanel createFieldsRow() {
        JPanel panel = new JPanel(new MigLayout("insets 20 20 10 20, gap 16", "[grow][grow][grow]", "[]"));
        panel.setOpaque(false);

        txtRoomId = new JTextField();
        cbRoomType = new JComboBox<>();
        cbRoomType.addItem("All room types");
        java.util.List<RoomType> types = roomTypeDAO.getAll();
        for (RoomType rt : types) {
            cbRoomType.addItem(rt.getRoomTypeName());
        }

        cbStatus = new JComboBox<>(new String[]{"All statuses", "Vacant", "Occupied", "Maintenance"});

        panel.add(createFieldCol("Room ID", txtRoomId));
        panel.add(createFieldCol("Room Type", cbRoomType));
        panel.add(createFieldCol("Status", cbStatus));

        return panel;
    }

    private JPanel createFieldCol(String label, JComponent comp) {
        JPanel col = new JPanel(new MigLayout("insets 0, wrap 1, gap 8", "[grow,fill]", "[][]"));
        col.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(100, 116, 139));
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        col.add(lbl);
        col.add(comp, "h 36!");
        return col;
    }

    private JPanel createActionRow() {
        JPanel panel = new JPanel(new MigLayout("insets 0 20 20 20, gap 12", "[][]", "[]"));
        panel.setOpaque(false);

        JButton btnSearch = new JButton("Search");
        btnSearch.setBackground(new Color(15, 23, 42));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSearch.addActionListener(e -> performSearch());

        JButton btnReset = new JButton("Reset");
        btnReset.setBackground(Color.WHITE);
        btnReset.addActionListener(e -> resetForm());

        panel.add(btnSearch, "h 36!, w 120!");
        panel.add(btnReset, "h 36!, w 100!");

        return panel;
    }

    private JPanel createResultArea() {
        RoundedPanel panel = new RoundedPanel(12, Color.WHITE, new Color(226, 232, 240), 1);
        panel.setLayout(new MigLayout("insets 0, wrap 1, gap 0", "[grow,fill]", "[][grow,fill]"));

        resultContainer = new JPanel(new BorderLayout());
        resultContainer.setOpaque(false);
        resultContainer.setPreferredSize(new Dimension(0, 300));

        panel.add(new JLabel(" Search Results") {{ setFont(new Font("Segoe UI", Font.BOLD, 14)); setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20)); }}, "growx");
        panel.add(resultContainer);

        return panel;
    }

    private void performSearch() {
        resultContainer.removeAll();

        String roomQuery = txtRoomId.getText().trim().toLowerCase();
        String typeQuery = (String) cbRoomType.getSelectedItem();
        String statusQuery = (String) cbStatus.getSelectedItem();

        List<Room> results = new ArrayList<>();
        RoomBUS bus = roomPanel.getRoomBUS();
        List<Room> currentRooms = roomPanel.getRoomList();
        
        if (currentRooms != null) {
            for (Room r : currentRooms) {
                String guiStatus = bus.mapDbStatusToGuiStatus(r.getStatus());
                boolean matchId = roomQuery.isEmpty() || r.getRoomId().toLowerCase().contains(roomQuery);
                boolean matchType = "All room types".equals(typeQuery) || r.getRoomType().getRoomTypeName().equals(typeQuery);
                boolean matchStatus = "All statuses".equals(statusQuery) || guiStatus.equals(statusQuery);
                
                if (matchId && matchType && matchStatus) {
                    results.add(r);
                }
            }
        }

        if (results.isEmpty()) {
            resultContainer.add(new JLabel("No results found", SwingConstants.CENTER));
        } else {
            JPanel grid = new JPanel(new MigLayout("insets 16, wrap 3, gap 16", "[grow,fill]", "[]"));
            grid.setOpaque(false);
            for (Room r : results) {
                grid.add(roomPanel.createRoomCard(r));
            }
            JScrollPane sp = new JScrollPane(grid);
            sp.setBorder(null);
            resultContainer.add(sp);
        }

        resultContainer.revalidate();
        resultContainer.repaint();
    }

    private void resetForm() {
        txtRoomId.setText("");
        cbRoomType.setSelectedIndex(0);
        cbStatus.setSelectedIndex(0);
        performSearch();
    }
}
