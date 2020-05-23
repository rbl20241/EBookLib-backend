package rb.ebooklib.util;

public enum Status {

    PENDING("Pending"),
    RETURNED("Returned"),
    APPROVED("Approved"),
    DECLINED("Declined");

    private final String value;

    Status(final String value) {
        this.value = value;
    }

    public String getStatus() {
        return this.value;
    }
}
