package bg.sofia.uni.fmi.mjt.foodanalyzer.server.commands;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.product.Product;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.exceptions.InvalidBarcodeArgumentsException;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.exceptions.NoInformationFoundException;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.Assert.assertEquals;

public class FoodByBarcodeCommandTest {
    private static final String ERROR = "Didn't get the expected result ";

    private ConcurrentMap<String, Product> foodByUpcCache = new ConcurrentHashMap<>();
    private FoodByBarcodeCommand foodByBarcodeCommand;

    @Before
    public void setup() {
        foodByBarcodeCommand = new FoodByBarcodeCommand(null, foodByUpcCache);
    }

    @Test(expected = InvalidBarcodeArgumentsException.class)
    public void testExecuteMethodWithInvalidBarcodeArgumentsException()
            throws InvalidBarcodeArgumentsException, NoInformationFoundException {
        foodByBarcodeCommand.execute("009800146130");
        // should be "--upc"
        // foodByBarcodeCommand.execute("--up=<009800146130>");
        // should be "--img"
        // foodByBarcodeCommand.execute("--iMg=</resources/barcode.gif");
    }

    @Test(expected = NoInformationFoundException.class)
    public void testProcessBarcodeWithNoInformationFoundException()
            throws InvalidBarcodeArgumentsException, NoInformationFoundException {
       foodByBarcodeCommand.execute("--upc=<777777777777>");
    }

    @Test(expected = InvalidBarcodeArgumentsException.class)
    public void testProcessBarcodeWithInvalidBarcodeArgumentException()
            throws InvalidBarcodeArgumentsException, NoInformationFoundException {
        foodByBarcodeCommand.execute("--img=</resources/barcode.gif");
    }

    @Test
    public void testProcessBarcodeWithUpcCodeRegularCase()
            throws InvalidBarcodeArgumentsException, NoInformationFoundException {

        Product product = new Product("Branded Food Products Database",
                "RAFFAELLO, ALMOND COCONUT TREAT, UPC: 009800146130",
                "45142036",
                "Ferrero U.S.A., Incorporated");

        product.setNameAndUpc();
        foodByUpcCache.put(product.getUpc(), product);

        String expected = product.toString();
        String actual = foodByBarcodeCommand.execute("--upc=<009800146130>");
        // actual = foodByBarcodeCommand.execute("--upc=<009800146130>" + "|" + "--img</resources/raffaello_barcode.gif");
        assertEquals(ERROR + "after testing FoodByBarcodeCommand::processBarcode with --upc.", expected, actual);
    }

    @Test
    public void testProcessBarcodeWithImgPathRegularCase()
            throws InvalidBarcodeArgumentsException, NoInformationFoundException {

        Product product = new Product("Branded Food Products Database",
                "RAFFAELLO, ALMOND COCONUT TREAT, UPC: 009800146130",
                "45142036",
                "Ferrero U.S.A., Incorporated");

        product.setNameAndUpc();
        foodByUpcCache.put(product.getUpc(), product);

        String filePath = System.getProperty("user.dir") + "/resources/raffaello_barcode.gif";

        String expected = product.toString();
        String actual = foodByBarcodeCommand.execute("--img=<" + filePath + ">");
        assertEquals(String.format("%s after testing FoodByBarcodeCommand::processBarcode with --img.", ERROR), expected, actual);
    }

}
