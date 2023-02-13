package rb.ebooklib.models.view;

import lombok.Data;

@Data
public class ResetPasswordRequestVO {

    private String previousPassword;
    private String newPassword;
    private String newPasswordConfirmation;

}
