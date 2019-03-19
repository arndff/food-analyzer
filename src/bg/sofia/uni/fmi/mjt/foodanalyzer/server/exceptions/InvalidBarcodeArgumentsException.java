package bg.sofia.uni.fmi.mjt.foodanalyzer.server.exceptions;

public class InvalidBarcodeArgumentsException extends Exception {
    private static final String EXCEPTION_MESSAGE = "Get food by barcode was called with invalid argument(s).";

    public InvalidBarcodeArgumentsException() {
        super(EXCEPTION_MESSAGE);
    }
}
