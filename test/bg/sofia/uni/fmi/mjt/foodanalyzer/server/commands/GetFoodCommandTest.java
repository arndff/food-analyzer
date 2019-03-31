package bg.sofia.uni.fmi.mjt.foodanalyzer.server.commands;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.Product;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.exceptions.NoInformationFoundException;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class GetFoodCommandTest {
    private static final String BRANDED_FOOD_PRODUCTS = "Branded Food Products Database";
    private static final String ERROR = "Didn't get the expected result ";

    @Mock
    private ConcurrentMap<String, List<Product>> foodByNameCacheMock = new ConcurrentHashMap<>();

    private ConcurrentMap<String, Product> foodByUpcCache = new ConcurrentHashMap<>();
    private GetFoodCommand foodCommand;

    private String response;
    private JsonParser parser = new JsonParser();

    private String argument;
    private String expected;
    private String actual;

    @Test(expected = NoInformationFoundException.class)
    public void testExecuteMethodWithNoInformationFoundException() throws NoInformationFoundException {
        foodCommand = new GetFoodCommand(foodByNameCacheMock, foodByUpcCache) {
            @Override
            protected JsonObject urlResponseToJson(String url) {
                response = "{\n\"errors\": {\n\"error\": [\n{\n\"status\": 400,\n\"parameter\": \"results\",\n\"message\": \"Your search resulted in zero results.Change your parameters and try again\"\n}\n]\n}\n}";
                return parser.parse(response).getAsJsonObject();
            }
        };

        foodCommand.execute("alabala");
    }

    @Test
    public void testExecuteMethodWithSingleObjectRegularCase() throws NoInformationFoundException {
        foodCommand = new GetFoodCommand(foodByNameCacheMock, foodByUpcCache) {
            @Override
            protected JsonObject urlResponseToJson(String url) {
                response = "{\n\"list\": {\n\"q\": \"raffaello\",\n\"sr\": \"1\",\n\"ds\": \"any\",\n\"start\": 0,\n\"end\": 1,\n\"total\": 1,\n\"group\": \"\",\n        \"sort\": \"r\",\n\"item\": [\n{\n\"offset\": 0,\n\"group\": \"Branded Food Products Database\",\n\"name\": \"RAFFAELLO, ALMOND COCONUT TREAT, UPC: 009800146130\",\n\"ndbno\": \"45142036\",\n\"ds\": \"LI\",\n\"manu\": \"Ferrero U.S.A., Incorporated\"\n}\n]\n}\n}";
                return parser.parse(response).getAsJsonObject();
            }
        };

        Product product = new Product(BRANDED_FOOD_PRODUCTS,
                "RAFFAELLO, ALMOND COCONUT TREAT, UPC: 009800146130",
                "45142036",
                "Ferrero U.S.A., Incorporated");

        argument = "raffaello";
        expected = product.toString();
        actual = foodCommand.execute(argument);

        assertEquals(ERROR + " after running GetFood::execute with argument=raffaello.", expected, actual);

        List<Product> listOfProduct = new ArrayList<>();
        listOfProduct.add(product);

        when(foodByNameCacheMock.containsKey(argument)).thenReturn(true);
        when(foodByNameCacheMock.get(argument)).thenReturn(listOfProduct);

        actual = foodCommand.execute(argument);
        assertEquals(ERROR + "after testing GetFood::execute's if case with a valid condition.", expected, actual);
        assertEquals("foodByUpcCache's size must be equals to 1.", 1, foodByUpcCache.size());
        assertEquals(ERROR + "after trying to retrieve an object from foodByUpcCache.", foodByUpcCache.get(product.getUpc()).toString(), actual);
    }

    @Test
    public void testExecuteMethodWithMultipleObjectsRegularCase() throws NoInformationFoundException {
        foodCommand = new GetFoodCommand(foodByNameCacheMock, foodByUpcCache) {
          @Override
          protected JsonObject urlResponseToJson(String url) {
              response = "{\n\"list\": {\n\"q\": \"rogelio\",\n\"sr\": \"1\",\n\"ds\": \"any\",\n\"start\": 0,\n\"end\": 5,\n\"total\": 5,\n\"group\": \"\",\n\"sort\": \"r\",\n\"item\": [\n{\n\"offset\": 0,\n\"group\": \"Branded Food Products Database\",\n\"name\": \"ROGELIO BUENO, MEXICAN CONDIMENT MOLE, UPC: 044774114006\",\n\"ndbno\": \"45017046\",\n\"ds\": \"LI\",\n\"manu\": \"Bueno Alimentos, S.A.\"\n},\n{\n\"offset\": 1,\n\"group\": \"Branded Food Products Database\",\n\"name\": \"ROGELIO BUENO, ADOBO MEXICAN CONDIMENT, UPC: 044774301000\",\n\"ndbno\": \"45017044\",\n\"ds\": \"LI\",\n\"manu\": \"Bueno Alimentos, S.A.\"\n},\n{\n\"offset\": 2,\n\"group\": \"Branded Food Products Database\",\n\"name\": \"ROGELIO BUENO, PIPIAN MEXICAN CONDIMENT, UPC: 044774201003\",\n\"ndbno\": \"45103985\",\n\"ds\": \"LI\",\n\"manu\": \"Bueno Alimentos, S.A.\"\n},\n{\n\"offset\": 3,\n\"group\": \"Branded Food Products Database\",\n\"name\": \"ROGELIO BUENO, MEXICAN CONDIMENT, MOLE, UPC: 044774101006\",\n\"ndbno\": \"45232733\",\n\"ds\": \"LI\",\n\"manu\": \"Bueno Alimentos, S.A.\"\n},\n{\n\"offset\": 4,\n\"group\": \"Branded Food Products Database\",\n\"name\": \"ROGELIO BUENO, MOLE MEXICAN CONDIMENT, UPC: 044774401007\",\n\"ndbno\": \"45103488\",\n\"ds\": \"LI\",\n\"manu\": \"Bueno Alimentos, S.A.\"\n}\n]\n}\n}";
              return parser.parse(response).getAsJsonObject();
          }
        };

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
        Product p4 = new Product(BRANDED_FOOD_PRODUCTS,
                "ROGELIO BUENO, MEXICAN CONDIMENT, MOLE, UPC: 044774101006",
                "45232733",
                "Bueno Alimentos, S.A.");
        Product p5 = new Product(BRANDED_FOOD_PRODUCTS,
                "ROGELIO BUENO, MOLE MEXICAN CONDIMENT, UPC: 044774401007",
                "45103488",
                "Bueno Alimentos, S.A.");

        List<Product> products = new ArrayList<>();
        products.add(p1);
        products.add(p2);
        products.add(p3);
        products.add(p4);
        products.add(p5);

        argument = "rogelio";
        expected = products.stream()
                           .map(Product::toString)
                           .collect(Collectors.joining(";"));

        actual = foodCommand.execute(argument);
        assertEquals(ERROR + "after running GetFood::execute with argument=rogelio.", expected, actual);

        when(foodByNameCacheMock.containsKey(argument)).thenReturn(true);
        when(foodByNameCacheMock.get(argument)).thenReturn(products);

        actual = foodCommand.execute(argument);
        assertEquals(ERROR + "after testing GetFood::execute's if case with a valid condition.", expected, actual);
        assertEquals("foodByUpcCache's size must be equal to 5.", 5, foodByUpcCache.size());
        assertEquals(ERROR + "after trying to retrieve first object of foodByUpcCache.", foodByUpcCache.get(products.get(0).getUpc()).toString(), p1.toString());
    }
}
