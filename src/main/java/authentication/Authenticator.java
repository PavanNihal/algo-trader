package authentication;

public interface Authenticator {

    public enum Status {
        SUCCESS,
        FAILURE
    }

    public enum BROKER {
        UPSTOX,
        ZERODHA
    }

    interface AuthListener {
        void onComplete(Status status);
    }

    public void authenticate();
    public void addListener(AuthListener listener);
}
