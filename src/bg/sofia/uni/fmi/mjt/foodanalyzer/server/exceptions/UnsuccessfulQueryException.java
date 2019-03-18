package bg.sofia.uni.fmi.mjt.foodanalyzer.server.exceptions;

public class UnsuccessfulQueryException extends Exception {
    private static final String EXCEPTION_MSG = "Your query wasn't executed successfully.";

    public UnsuccessfulQueryException() {
        super(EXCEPTION_MSG);
    }
}
