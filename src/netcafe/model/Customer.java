package netcafe.model;

public class Customer {
    private final String id;
    private String name;
    private String phone;
    private long remainingSeconds;

    public Customer(String id, String name, String phone, long remainingSeconds) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.remainingSeconds = remainingSeconds;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public long getRemainingSeconds() { return remainingSeconds; }

    public void addSeconds(long seconds) {
        remainingSeconds += seconds;
    }

    public void deductSeconds(long seconds) {
        remainingSeconds = Math.max(0, remainingSeconds - seconds);
    }
}
