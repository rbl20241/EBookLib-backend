package rb.ebooklib.payload;

import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private long tokenExpirationMsec;

    public AuthResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}