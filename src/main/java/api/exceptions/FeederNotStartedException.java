package api.exceptions;

public class FeederNotStartedException extends RuntimeException {
    
    public FeederNotStartedException(String message) {
        super(message);
    }
}
