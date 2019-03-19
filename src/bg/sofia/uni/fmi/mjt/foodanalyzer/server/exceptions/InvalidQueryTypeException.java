package bg.sofia.uni.fmi.mjt.foodanalyzer.server.exceptions;

public class InvalidQueryTypeException extends Exception {
    private static final String EXCEPTION_MESSAGE = "Invalid query type.";

    public InvalidQueryTypeException() {
        super(EXCEPTION_MESSAGE);
    }
}
