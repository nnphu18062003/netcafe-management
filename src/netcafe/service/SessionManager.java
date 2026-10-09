package netcafe.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import netcafe.model.Customer;
import netcafe.model.Session;
import netcafe.persistence.DataStore;

public class SessionManager {
    private final CustomerService customerService;
    private final DataStore store;
    private final Map<String, Session> active = new HashMap<>();

    public SessionManager(CustomerService customerService, DataStore store) {
        this.customerService = customerService;
        this.store = store;
    }

    public boolean isActive(String customerId) {
        return active.containsKey(customerId);
    }

    public Session getActive(String customerId) {
        return active.get(customerId);
    }

    public Session start(String customerId) {
        Customer c = customerService.get(customerId);
        if (c == null) {
            throw new IllegalArgumentException("Không tìm thấy khách hàng");
        }
        if (active.containsKey(customerId)) {
            throw new IllegalStateException(c.getName() + " đang trong phiên chơi");
        }
        if (c.getRemainingSeconds() <= 0) {
            throw new IllegalStateException(c.getName() + " đã hết giờ chơi, vui lòng nạp thêm tiền");
        }
        Session s = new Session(customerId, LocalDateTime.now());
        active.put(customerId, s);
        return s;
    }

    public Session end(String customerId, boolean autoTerminated) {
        Session s = active.remove(customerId);
        if (s == null) {
            throw new IllegalStateException("Khách hàng không có phiên đang chạy");
        }
        s.end(LocalDateTime.now(), autoTerminated);
        Customer c = customerService.get(customerId);
        if (c != null) {
            c.deductSeconds(s.elapsedSeconds(s.getEndTime()));
            customerService.save();
        }
        store.appendSession(s);
        return s;
    }

    public long liveRemainingSeconds(String customerId) {
        Customer c = customerService.get(customerId);
        if (c == null) {
            return 0;
        }
        Session s = active.get(customerId);
        long used = s == null ? 0 : s.elapsedSeconds(LocalDateTime.now());
        return Math.max(0, c.getRemainingSeconds() - used);
    }

    public List<Session> checkExpired() {
        List<Session> expired = new ArrayList<>();
        for (String id : new ArrayList<>(active.keySet())) {
            if (liveRemainingSeconds(id) <= 0) {
                expired.add(end(id, true));
            }
        }
        return expired;
    }

    public List<Session> history(String customerId) {
        List<Session> result = new ArrayList<>();
        for (Session s : store.loadSessions()) {
            if (s.getCustomerId().equals(customerId)) {
                result.add(s);
            }
        }
        return result;
    }
}
