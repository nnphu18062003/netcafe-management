package netcafe.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import netcafe.model.Customer;
import netcafe.persistence.DataStore;

/** Tìm, thêm, nạp tiền và xoá khách hàng. */
public class CustomerService {
    private final DataStore store;
    private final Settings settings;
    private final Map<String, Customer> customers = new LinkedHashMap<>();

    public CustomerService(DataStore store, Settings settings) {
        this.store = store;
        this.settings = settings;
        for (Customer c : store.loadCustomers()) {
            customers.put(c.getId(), c);
        }
    }

    public List<Customer> findAll() {
        return new ArrayList<>(customers.values());
    }

    /** Tìm theo tên hoặc số điện thoại, không phân biệt hoa thường. */
    public List<Customer> search(String keyword) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        List<Customer> result = new ArrayList<>();
        for (Customer c : customers.values()) {
            if (k.isEmpty()
                    || c.getName().toLowerCase(Locale.ROOT).contains(k)
                    || c.getPhone().contains(k)) {
                result.add(c);
            }
        }
        return result;
    }

    public Customer get(String id) {
        return customers.get(id);
    }

    public Customer add(String name, String phone) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khách hàng không được để trống");
        }
        Customer c = new Customer(UUID.randomUUID().toString().substring(0, 8),
                name.trim(), phone == null ? "" : phone.trim(), 0);
        customers.put(c.getId(), c);
        save();
        return c;
    }

    /** Nạp tiền: quy đổi sang giây theo tỉ lệ hiện tại và cộng vào số dư. */
    public long topUp(String customerId, long amount) {
        Customer c = require(customerId);
        long seconds = settings.moneyToSeconds(amount);
        c.addSeconds(seconds);
        save();
        return seconds;
    }

    public void delete(String customerId) {
        customers.remove(customerId);
        save();
    }

    public void save() {
        store.saveCustomers(findAll());
    }

    private Customer require(String id) {
        Customer c = customers.get(id);
        if (c == null) {
            throw new IllegalArgumentException("Không tìm thấy khách hàng " + id);
        }
        return c;
    }
}
