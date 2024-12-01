package authentication;

public interface Authenticator {

    enum Status {
        SUCCESS,
        FAILURE
    }

    interface AuthListener {
        void onComplete(Status status);
    }

    public void authenticate();
    public void addListener(AuthListener listener);
}
