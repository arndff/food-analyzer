package bg.sofia.uni.fmi.mjt.foodanalyzer.server.exceptions;

public class InvalidBarcodeArgumentsException extends Exception {
    public InvalidBarcodeArgumentsException() {
        super();
    }

    public InvalidBarcodeArgumentsException(String message) {
        super(message);
    }
}
