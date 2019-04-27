package bg.sofia.uni.fmi.mjt.foodanalyzer.server.commands;

import static org.junit.Assert.assertEquals;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.product.Product;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.product.ProductList;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.product.ProductResponse;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.exceptions.NoInformationFoundException;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FoodCommandTest {
    private static final String BRANDED_FOOD_PRODUCTS = "Branded Food Products Database";
    private static final String ERROR = "Didn't get the expected result ";

    @Mock
    private HttpClient httpClientMock;

    @Mock
    private HttpResponse<String> httpResponseMock;

    private FoodCommand foodCommand;
    private Gson gson;

    @Before
    public void setup() {
        ConcurrentMap<String, List<Product>> foodByNameCache = new ConcurrentHashMap<>();
        ConcurrentMap<String, Product> foodByUpcCache = new ConcurrentHashMap<>();
        foodCommand = new FoodCommand(httpClientMock, foodByNameCache, foodByUpcCache);
        gson = new Gson();
    }

    @Test(expected = NoInformationFoundException.class)
    public void testExecuteMethodWithNoInformationFoundException() throws Exception {
        ProductResponse productResponse = new ProductResponse(null);
        String response = gson.toJson(productResponse, ProductResponse.class);

        when(httpClientMock.send(Mockito.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(httpResponseMock);

        when(httpResponseMock.body()).thenReturn(response);

        foodCommand.execute("alabala");
    }

    @Test
    public void testExecuteMethodWithSingleObjectRegularCase() throws Exception {
        List<Product> item = List.of(new Product(BRANDED_FOOD_PRODUCTS,
                "RAFFAELLO, ALMOND COCONUT TREAT, UPC: 009800146130",
                "45142036",
                "Ferrero U.S.A., Incorporated"));
        ProductList productList = new ProductList(item);
        ProductResponse productResponse = new ProductResponse(productList);
        String jsonResponse = gson.toJson(productResponse, ProductResponse.class);

        when(httpClientMock.send(Mockito.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(httpResponseMock);

        when(httpResponseMock.body()).thenReturn(jsonResponse);

        item.get(0).setNameAndUpc();
        String expected = item.get(0).toString();

        String argument = "raffaello";
        String actual = foodCommand.execute(argument);

        assertEquals(ERROR + String.format(" after running FoodCommand::execute with argument=%s.", argument), expected, actual);
    }

    @Test
    public void testExecuteMethodWithMultipleObjectsRegularCase() throws Exception {
        Product p1 = new Product(BRANDED_FOOD_PRODUCTS,
                "ROGELIO BUENO, MEXICAN CONDIMENT MOLE, UPC: 044774114006",
                "45017046",
                "Bueno Alimentos, S.A.");
        Product p2 = new Product(BRANDED_FOOD_PRODUCTS,
                "ROGELIO BUENO, ADOBO MEXICAN CONDIMENT, UPC: 044774301000",
                "45017044",
                "Bueno Alimentos, S.A.");
        Product p3 = new Product(BRANDED_FOOD_PRODUCTS,
                "ROGELIO BUENO, PIPIAN MEXICAN CONDIMENT, UPC: 044774201003",
                "45103985",
                "Bueno Alimentos, S.A.");

        List<Product> item = List.of(p1, p2, p3);
        ProductList productList = new ProductList(item);
        ProductResponse productResponse = new ProductResponse(productList);
        String jsonResponse = gson.toJson(productResponse, ProductResponse.class);

        when(httpClientMock.send(Mockito.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(httpResponseMock);

        when(httpResponseMock.body()).thenReturn(jsonResponse);

        item.forEach(Product::setNameAndUpc);
        String expected = item.stream()
                              .map(Product::toString)
                              .collect(Collectors.joining(";"));

        String argument = "rogelio";
        String actual = foodCommand.execute(argument);

        assertEquals(ERROR + String.format(" after running FoodCommand::execute with argument=%s.", argument), expected, actual);
    }
}