package rb.ebooklib.model.view;

import lombok.Data;

@Data
public class ResetPasswordRequestVO {

    private String previousPassword;
    private String newPassword;
    private String newPasswordConfirmation;

}
