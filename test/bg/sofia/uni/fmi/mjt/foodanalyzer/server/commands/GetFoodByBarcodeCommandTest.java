package bg.sofia.uni.fmi.mjt.foodanalyzer.server.commands;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.Product;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.exceptions.InvalidBarcodeArgumentsException;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.exceptions.NoInformationFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GetFoodByBarcodeCommandTest {
    private Product product = new Product("Branded Food Products Database",
            "RAFFAELLO, ALMOND COCONUT TREAT, UPC: 009800146130",
            "45142036",
            "Ferrero U.S.A., Incorporated");

    @Mock
    ConcurrentMap<String, Product> foodByUpcCacheMock = new ConcurrentHashMap<>();

    private GetFoodByBarcodeCommand foodByBarcodeCommand;

    private String expected;
    private String actual;
    private static final String ERROR = "Didn't get the expected result ";

    @Before
    public void setup() {
        foodByBarcodeCommand = new GetFoodByBarcodeCommand(foodByUpcCacheMock);
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

        foodByBarcodeCommand.execute("--upc=<777>");
    }

    @Test(expected = InvalidBarcodeArgumentsException.class)
    public void testProcessBarcodeWithInvalidBarcodeArgumentException()
            throws InvalidBarcodeArgumentsException, NoInformationFoundException {

        foodByBarcodeCommand.execute("--img=</resources/barcode.gif");
    }

    @Test
    public void testProcessBarcodeWithUpcCodeRegularCase()
            throws InvalidBarcodeArgumentsException, NoInformationFoundException {

        when(foodByUpcCacheMock.containsKey(product.getUpc())).thenReturn(true);
        when(foodByUpcCacheMock.get(product.getUpc())).thenReturn(product);

        expected = product.toString();
        actual = foodByBarcodeCommand.execute("--upc=<009800146130>");
        assertEquals(ERROR + "after testing processBarcode with --upc.", expected, actual);
    }

    @Test
    public void testProcessBarcodeWithImgPathRegularCase()
            throws InvalidBarcodeArgumentsException, NoInformationFoundException {

        when(foodByUpcCacheMock.containsKey(product.getUpc())).thenReturn(true);
        when(foodByUpcCacheMock.get(product.getUpc())).thenReturn(product);

        expected = product.toString();
        actual = foodByBarcodeCommand.execute("--img=</home/arndff/Downloads/raffaello_barcode.gif>");
        assertEquals(ERROR + "after testing processBarcode with --img.", expected, actual);
    }
}
