package netcafe.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import netcafe.model.Customer;
import netcafe.model.Session;
import netcafe.service.Settings;


public class DataStore {
    private static final String SEP = ";";
    private static final long DEFAULT_PRICE_PER_HOUR = 10000;

    private final Path customersFile;
    private final Path sessionsFile;
    private final Path configFile;

    public DataStore(String dir) {
        Path base = Paths.get(dir);
        try {
            Files.createDirectories(base);
        } catch (IOException e) {
            throw new RuntimeException("Không tạo được thư mục dữ liệu " + base, e);
        }
        customersFile = base.resolve("customers.csv");
        sessionsFile = base.resolve("sessions.csv");
        configFile = base.resolve("config.properties");
    }

    public Settings loadSettings() {
        Properties p = new Properties();
        if (Files.exists(configFile)) {
            try (InputStream in = Files.newInputStream(configFile)) {
                p.load(in);
            } catch (IOException e) {
                System.err.println("Không đọc được cấu hình: " + e.getMessage());
            }
        }
        long price = DEFAULT_PRICE_PER_HOUR;
        try {
            price = Long.parseLong(p.getProperty("pricePerHour", String.valueOf(DEFAULT_PRICE_PER_HOUR)));
        } catch (NumberFormatException ignored) {

        }
        return new Settings(price);
    }

    public void saveSettings(Settings settings) {
        Properties p = new Properties();
        p.setProperty("pricePerHour", String.valueOf(settings.getPricePerHour()));
        try (OutputStream out = Files.newOutputStream(configFile)) {
            p.store(out, "NetCafe settings: VND per hour");
        } catch (IOException e) {
            throw new RuntimeException("Không lưu được cấu hình", e);
        }
    }

    public List<Customer> loadCustomers() {
        List<Customer> result = new ArrayList<>();
        for (String line : readLines(customersFile)) {
            String[] f = line.split(SEP, -1);
            if (f.length >= 4) {
                result.add(new Customer(f[0], f[1], f[2], Long.parseLong(f[3])));
            }
        }
        return result;
    }

    public void saveCustomers(List<Customer> customers) {
        List<String> lines = new ArrayList<>();
        for (Customer c : customers) {
            lines.add(c.getId() + SEP + clean(c.getName()) + SEP + clean(c.getPhone()) + SEP + c.getRemainingSeconds());
        }
        try {
            Files.write(customersFile, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Không lưu được danh sách khách hàng", e);
        }
    }

    public List<Session> loadSessions() {
        List<Session> result = new ArrayList<>();
        for (String line : readLines(sessionsFile)) {
            String[] f = line.split(SEP, -1);
            if (f.length >= 4) {
                result.add(new Session(f[0], LocalDateTime.parse(f[1]), LocalDateTime.parse(f[2]),
                        Boolean.parseBoolean(f[3])));
            }
        }
        return result;
    }

    public void appendSession(Session s) {
        String line = s.getCustomerId() + SEP + s.getStartTime() + SEP + s.getEndTime() + SEP
                + s.isAutoTerminated() + System.lineSeparator();
        try (BufferedWriter w = Files.newBufferedWriter(sessionsFile, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(line);
        } catch (IOException e) {
            throw new RuntimeException("Không lưu được lịch sử phiên chơi", e);
        }
    }

    private List<String> readLines(Path file) {
        List<String> lines = new ArrayList<>();
        if (!Files.exists(file)) {
            return lines;
        }
        try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Không đọc được " + file + ": " + e.getMessage());
        }
        return lines;
    }

    private static String clean(String s) {
        return s == null ? "" : s.replace(SEP, ",");
    }
}
