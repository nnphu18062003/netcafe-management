package netcafe;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import netcafe.persistence.DataStore;
import netcafe.service.CustomerService;
import netcafe.service.SessionManager;
import netcafe.service.Settings;
import netcafe.ui.MainFrame;

/** Điểm khởi chạy chương trình NetCafe Management System. */
public class Main {
    public static void main(String[] args) {
        DataStore store = new DataStore("data");
        Settings settings = store.loadSettings();
        CustomerService customerService = new CustomerService(store, settings);
        SessionManager sessionManager = new SessionManager(customerService, store);

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // dùng giao diện mặc định nếu không đặt được
            }
            new MainFrame(customerService, sessionManager, settings, store).setVisible(true);
        });
    }
}
