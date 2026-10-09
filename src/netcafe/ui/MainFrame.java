package netcafe.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import netcafe.model.Customer;
import netcafe.model.Session;
import netcafe.persistence.DataStore;
import netcafe.service.CustomerService;
import netcafe.service.SessionManager;
import netcafe.service.Settings;

/** Cửa sổ chính: bảng khách hàng, ô tìm kiếm và các nút thao tác. */
public class MainFrame extends JFrame {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final String[] COLUMNS = {"Mã", "Tên", "SĐT", "Giờ còn lại", "Trạng thái", "Bắt đầu lúc"};

    private final CustomerService customerService;
    private final SessionManager sessionManager;
    private final Settings settings;
    private final DataStore store;

    private final JTextField searchField = new JTextField(20);
    private final JLabel rateLabel = new JLabel();
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    public MainFrame(CustomerService customerService, SessionManager sessionManager,
                     Settings settings, DataStore store) {
        super("NetCafe Management System");
        this.customerService = customerService;
        this.sessionManager = sessionManager;
        this.settings = settings;
        this.store = store;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                onExit();
            }
        });
        setSize(900, 500);
        setLocationRelativeTo(null);
        buildLayout();
        refreshTable();

        // Mỗi giây: cập nhật thời gian còn lại và tự kết thúc phiên hết giờ.
        new Timer(1000, e -> tick()).start();
    }

    private void buildLayout() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Tìm khách hàng (tên/SĐT):"));
        top.add(searchField);
        top.add(rateLabel);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshTable(); }
            public void removeUpdate(DocumentEvent e) { refreshTable(); }
            public void changedUpdate(DocumentEvent e) { refreshTable(); }
        });

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel buttons = new JPanel(new GridLayout(0, 1, 5, 5));
        buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttons.add(button("Thêm khách hàng", this::onAdd));
        buttons.add(button("Nạp tiền", this::onTopUp));
        buttons.add(button("Bắt đầu phiên", this::onStart));
        buttons.add(button("Kết thúc phiên", this::onEnd));
        buttons.add(button("Lịch sử phiên", this::onHistory));
        buttons.add(button("Xoá khách hàng", this::onDelete));
        buttons.add(button("Cài đặt tỉ lệ", this::onSettings));

        JPanel east = new JPanel(new BorderLayout());
        east.add(buttons, BorderLayout.NORTH);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(east, BorderLayout.EAST);
    }

    private JButton button(String text, Runnable action) {
        JButton b = new JButton(text);
        b.addActionListener(e -> action.run());
        return b;
    }

    // ---------- Hiển thị ----------

    private void refreshTable() {
        String selected = selectedId();
        rateLabel.setText("   Tỉ lệ: " + settings.getPricePerHour() + " VND / giờ");
        tableModel.setRowCount(0);
        for (Customer c : customerService.search(searchField.getText())) {
            Session s = sessionManager.getActive(c.getId());
            tableModel.addRow(new Object[] {
                c.getId(), c.getName(), c.getPhone(),
                formatDuration(sessionManager.liveRemainingSeconds(c.getId())),
                s != null ? "Đang chơi" : "Rảnh",
                s != null ? s.getStartTime().format(TIME_FMT) : ""
            });
        }
        reselect(selected);
    }

    private void tick() {
        List<Session> expired = sessionManager.checkExpired();
        refreshTable();
        for (Session s : expired) {
            Customer c = customerService.get(s.getCustomerId());
            JOptionPane.showMessageDialog(this,
                    "Khách hàng " + (c != null ? c.getName() : s.getCustomerId())
                            + " đã hết giờ chơi. Phiên đã tự động kết thúc lúc "
                            + s.getEndTime().format(TIME_FMT) + ".",
                    "Hết giờ", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ---------- Xử lý nút ----------

    private void onAdd() {
        JTextField name = new JTextField();
        JTextField phone = new JTextField();
        Object[] form = {"Tên:", name, "Số điện thoại:", phone};
        if (JOptionPane.showConfirmDialog(this, form, "Thêm khách hàng",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            run(() -> customerService.add(name.getText(), phone.getText()));
        }
    }

    private void onTopUp() {
        String id = requireSelection();
        if (id == null) return;
        String input = JOptionPane.showInputDialog(this,
                "Nhập số tiền (VND). Tỉ lệ: " + settings.getPricePerHour() + " VND / giờ");
        if (input == null) return;
        run(() -> {
            long seconds = customerService.topUp(id, parseAmount(input));
            info("Đã cộng " + formatDuration(seconds) + " giờ chơi.");
        });
    }

    private void onStart() {
        String id = requireSelection();
        if (id == null) return;
        run(() -> {
            Session s = sessionManager.start(id);
            info("Bắt đầu phiên lúc " + s.getStartTime().format(TIME_FMT));
        });
    }

    private void onEnd() {
        String id = requireSelection();
        if (id == null) return;
        run(() -> {
            Session s = sessionManager.end(id, false);
            info("Kết thúc lúc " + s.getEndTime().format(TIME_FMT)
                    + "\nThời gian chơi: " + formatDuration(s.elapsedSeconds(s.getEndTime()))
                    + "\nCòn lại: " + formatDuration(customerService.get(id).getRemainingSeconds()));
        });
    }

    private void onHistory() {
        String id = requireSelection();
        if (id == null) return;
        StringBuilder sb = new StringBuilder();
        for (Session s : sessionManager.history(id)) {
            sb.append(s.getStartTime().format(TIME_FMT)).append("  →  ")
              .append(s.getEndTime().format(TIME_FMT)).append("   (")
              .append(formatDuration(s.elapsedSeconds(s.getEndTime()))).append(")")
              .append(s.isAutoTerminated() ? "  [tự động kết thúc]" : "")
              .append('\n');
        }
        info(sb.length() == 0 ? "Chưa có phiên chơi nào." : sb.toString());
    }

    private void onDelete() {
        String id = requireSelection();
        if (id == null) return;
        if (sessionManager.isActive(id)) {
            error("Hãy kết thúc phiên chơi trước khi xoá khách hàng.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Xoá khách hàng " + customerService.get(id).getName() + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            run(() -> customerService.delete(id));
        }
    }

    private void onSettings() {
        String input = JOptionPane.showInputDialog(this, "Số tiền (VND) cho 1 giờ chơi:",
                settings.getPricePerHour());
        if (input == null) return;
        run(() -> {
            settings.setPricePerHour(parseAmount(input));
            store.saveSettings(settings);
        });
    }

    private void onExit() {
        if (JOptionPane.showConfirmDialog(this,
                "Thoát chương trình? Các phiên đang chạy sẽ được kết thúc và trừ giờ.",
                "Thoát", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        for (Customer c : customerService.findAll()) {
            if (sessionManager.isActive(c.getId())) {
                sessionManager.end(c.getId(), false);
            }
        }
        System.exit(0);
    }

    // ---------- Tiện ích ----------

    private void run(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException ex) {
            error(ex.getMessage());
        }
        refreshTable();
    }

    private String selectedId() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (String) tableModel.getValueAt(row, 0);
    }

    private String requireSelection() {
        String id = selectedId();
        if (id == null) {
            error("Vui lòng chọn một khách hàng trong bảng.");
        }
        return id;
    }

    private void reselect(String id) {
        if (id == null) return;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (id.equals(tableModel.getValueAt(i, 0))) {
                table.setRowSelectionInterval(i, i);
                return;
            }
        }
    }

    private static long parseAmount(String s) {
        try {
            return Long.parseLong(s.trim().replace(".", "").replace(",", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Số không hợp lệ: " + s);
        }
    }

    static String formatDuration(long seconds) {
        return String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
