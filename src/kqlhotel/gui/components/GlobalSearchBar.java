package kqlhotel.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import kqlhotel.bus.dashboard.GlobalSearchBUS;
import kqlhotel.bus.dashboard.SearchResult;
import kqlhotel.gui.theme.ThemeColors;
import kqlhotel.gui.utils.IconLoader;
import net.miginfocom.swing.MigLayout;

/**
 * Thanh tìm kiếm toàn cục đặt ở topbar.
 *
 * Component này chỉ phụ trách trải nghiệm tìm kiếm: nhận input, debounce,
 * gọi BUS ở background thread và render popup. Việc điều hướng thật sự được
 * truyền từ AppFrame qua callback onSelect.
 */
public class GlobalSearchBar extends JPanel {
    private static final String SEARCH_ICON_FILE = "search.png";

    // Không tìm khi người dùng mới gõ 1 ký tự vì kết quả thường quá nhiều.
    private static final int MIN_QUERY_LENGTH = 2;

    // Debounce giúp gom các lần gõ liên tiếp thành một lần query DB.
    private static final int DEBOUNCE_MS = 300;

    private final JTextField searchField = new JTextField();
    private final JPopupMenu popup = new JPopupMenu();
    private final GlobalSearchBUS searchBUS = new GlobalSearchBUS();
    private final Consumer<SearchResult> onSelect;
    private final Timer debounceTimer;

    // Worker hiện tại; nếu người dùng gõ tiếp thì worker cũ sẽ bị hủy.
    private SwingWorker<List<SearchResult>, Void> currentWorker;

    // Số phiên request để bỏ qua kết quả cũ trả về muộn hơn request mới.
    private int requestVersion = 0;

    public GlobalSearchBar(Consumer<SearchResult> onSelect) {
        this.onSelect = onSelect;
        setOpaque(false);
        setLayout(new BorderLayout());

        JPanel box = new JPanel(new MigLayout("insets 0,gap 8", "[][grow,fill]", "[]"));
        box.setBackground(ThemeColors.SURFACE);
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.BORDER_SOFT, 1, true),
            new EmptyBorder(0, 12, 0, 12)
        ));

        JLabel icon = new JLabel("", SwingConstants.CENTER);
        javax.swing.ImageIcon searchIcon = IconLoader.loadIcon(SEARCH_ICON_FILE, 18, 18);
        if (searchIcon != null) {
            icon.setIcon(searchIcon);
        } else {
            icon.setText("S");
        }
        icon.setForeground(ThemeColors.TEXT_MUTED);
        icon.setFont(icon.getFont().deriveFont(14f));
        box.add(icon, "w 26!,aligny center");

        searchField.putClientProperty("JTextField.placeholderText", "Tìm khách hàng, phòng, hóa đơn...");
        searchField.setBorder(BorderFactory.createEmptyBorder());
        searchField.setOpaque(false);
        searchField.setFont(searchField.getFont().deriveFont(13f));
        searchField.setForeground(ThemeColors.TEXT_PRIMARY);
        box.add(searchField, "h 34!,growx");

        add(box, BorderLayout.CENTER);
        setPreferredSize(new Dimension(520, 36));

        popup.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER, 1, true));
        popup.setFocusable(false);

        // Timer chỉ chạy sau khi người dùng ngừng gõ trong DEBOUNCE_MS.
        debounceTimer = new Timer(DEBOUNCE_MS, e -> runSearch());
        debounceTimer.setRepeats(false);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { scheduleSearch(); }
            @Override public void removeUpdate(DocumentEvent e) { scheduleSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { scheduleSearch(); }
        });

        searchField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (searchField.getText().trim().length() >= MIN_QUERY_LENGTH) {
                    scheduleSearch();
                }
            }

            @Override public void focusLost(FocusEvent e) {
                Timer hideTimer = new Timer(160, ev -> popup.setVisible(false));
                hideTimer.setRepeats(false);
                hideTimer.start();
            }
        });
    }

    private void scheduleSearch() {
        String query = searchField.getText().trim();
        if (query.length() < MIN_QUERY_LENGTH) {
            // Query quá ngắn thì đóng popup và hủy request đang chạy.
            requestVersion++;
            debounceTimer.stop();
            if (currentWorker != null) {
                currentWorker.cancel(true);
            }
            popup.setVisible(false);
            return;
        }
        debounceTimer.restart();
    }

    private void runSearch() {
        String query = searchField.getText().trim();
        if (query.length() < MIN_QUERY_LENGTH) {
            popup.setVisible(false);
            return;
        }

        int version = ++requestVersion;
        if (currentWorker != null) {
            currentWorker.cancel(true);
        }

        showMessage("Đang tìm kiếm...");

        // SwingWorker chạy query DB ngoài EDT để giao diện không bị đơ.
        // done() quay lại EDT nên có thể cập nhật popup an toàn.
        currentWorker = new SwingWorker<>() {
            @Override
            protected List<SearchResult> doInBackground() {
                return searchBUS.search(query);
            }

            @Override
            protected void done() {
                // Nếu đây là request cũ, bỏ qua để tránh popup bị dữ liệu cũ ghi đè.
                if (version != requestVersion || isCancelled()) {
                    return;
                }
                try {
                    showResults(get());
                } catch (Exception ex) {
                    showMessage("Không thể tìm kiếm lúc này.");
                }
            }
        };
        currentWorker.execute();
    }

    private void showResults(List<SearchResult> results) {
        if (results == null || results.isEmpty()) {
            showMessage("Không tìm thấy kết quả phù hợp");
            return;
        }

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(ThemeColors.SURFACE);

        for (SearchResult result : results) {
            list.add(createResultRow(result));
        }

        showContent(list, Math.min(results.size() * 58 + 8, 330));
    }

    private void showMessage(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColors.SURFACE);
        JLabel label = new JLabel(text);
        label.setForeground(ThemeColors.TEXT_MUTED);
        label.setFont(label.getFont().deriveFont(13f));
        label.setBorder(new EmptyBorder(12, 14, 12, 14));
        panel.add(label, BorderLayout.CENTER);
        showContent(panel, 46);
    }

    private void showContent(Component content, int height) {
        popup.removeAll();
        int width = Math.max(getWidth(), 380);
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(ThemeColors.SURFACE);
        scroll.setPreferredSize(new Dimension(width, height));
        popup.add(scroll);
        popup.pack();

        // Chỉ hiện popup khi component đang hiển thị và ô search còn focus.
        if (isShowing() && searchField.hasFocus()) {
            popup.show(this, 0, getHeight() + 4);
        }
    }

    private JPanel createResultRow(SearchResult result) {
        JPanel row = new JPanel(new MigLayout("insets 8 12,gap 10", "[][grow,fill]", "[][]"));
        row.setBackground(ThemeColors.SURFACE);
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JLabel badge = new JLabel(iconFor(result.getIconKey()), SwingConstants.CENTER);
        badge.setOpaque(true);
        badge.setBackground(colorFor(result.getIconKey()));
        badge.setForeground(ThemeColors.PRIMARY);
        badge.setFont(badge.getFont().deriveFont(Font.BOLD, 13f));
        badge.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER_SOFT, 1, true));
        row.add(badge, "w 34!,h 34!,spany 2");

        JLabel title = new JLabel(result.getType() + " · " + result.getTitle());
        title.setForeground(ThemeColors.TEXT_PRIMARY);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
        row.add(title, "growx,wrap");

        JLabel subtitle = new JLabel(result.getSubtitle());
        subtitle.setForeground(ThemeColors.TEXT_MUTED);
        subtitle.setFont(subtitle.getFont().deriveFont(12f));
        row.add(subtitle, "growx");

        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                row.setBackground(ThemeColors.SURFACE_HOVER);
            }

            @Override public void mouseExited(MouseEvent e) {
                row.setBackground(ThemeColors.SURFACE);
            }

            @Override public void mousePressed(MouseEvent e) {
                popup.setVisible(false);
                searchField.setText("");
                if (onSelect != null) {
                    // V1: click kết quả chỉ điều hướng route, chưa prefill bản ghi.
                    onSelect.accept(result);
                }
            }
        });
        return row;
    }

    private String iconFor(String key) {
        if ("booking".equals(key)) return "ĐP";
        if ("customers".equals(key)) return "KH";
        if ("room".equals(key)) return "P";
        if ("invoices".equals(key)) return "HĐ";
        if ("staff".equals(key)) return "NV";
        return "?";
    }

    private Color colorFor(String key) {
        if ("booking".equals(key)) return ThemeColors.PRIMARY_SOFT;
        if ("customers".equals(key)) return ThemeColors.SUCCESS_SOFT;
        if ("room".equals(key)) return ThemeColors.INFO_SOFT;
        if ("invoices".equals(key)) return ThemeColors.WARNING_SOFT;
        if ("staff".equals(key)) return ThemeColors.PREMIUM_ACCENT_SOFT;
        return ThemeColors.SURFACE_LIGHT;
    }
}
