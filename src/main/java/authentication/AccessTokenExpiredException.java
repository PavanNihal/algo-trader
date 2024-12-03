package authentication;

public class AccessTokenExpiredException extends Exception {

    public AccessTokenExpiredException(String message) {
        super(message);
    }
    
}
