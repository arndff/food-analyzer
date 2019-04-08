package bg.sofia.uni.fmi.mjt.foodanalyzer.server.commands;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.Report;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.exceptions.NoInformationFoundException;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RunWith(MockitoJUnitRunner.class)
public class FoodReportCommandTest {
    private final static String ERROR = "Didn't get the expected result ";

    @Mock
    private ConcurrentMap<String, Report> foodByNdbnoCacheMock = new ConcurrentHashMap<>();

    private FoodReportCommand foodReportCommand;

    private JsonParser parser = new JsonParser();
    private String response;

    @Test(expected = NoInformationFoundException.class)
    public void testExecuteMethodWithNoInformationFoundException() throws NoInformationFoundException {
        foodReportCommand = new FoodReportCommand(foodByNdbnoCacheMock) {
          @Override
          protected JsonObject urlResponseToJson(String url) {
              response = "{\"foods\":[{\"error\":\"No data for ndbno 77777777\"}],\"count\":1,\"notfound\":1,\"api\":2.0}";
              return parser.parse(response).getAsJsonObject();
          }
        };

        foodReportCommand.execute("77777777");
    }

    @Test
    public void testExecuteMethodRegularCase() throws NoInformationFoundException {
        foodReportCommand = new FoodReportCommand(foodByNdbnoCacheMock) {
          @Override
          protected JsonObject urlResponseToJson(String url) {
              String response = "{\"foods\":[{\"food\":{\"sr\":\"July, 2018\",\"type\":\"b\",\"desc\":{\"ndbno\":\"45142036\",\"name\":\"RAFFAELLO, ALMOND COCONUT TREAT, UPC: 009800146130\",\"ds\":\"Label Insight\",\"manu\":\"Ferrero U.S.A., Incorporated\",\"ru\":\"g\"},\"ing\":{\"desc\":\"VEGETABLE OILS (PALM AND SHEANUT). DRY COCONUT, SUGAR, ALMONDS, SKIM MILK POWDER, WHEY POWDER (MILK), WHEAT FLOUR, NATURAL AND ARTIFICIAL FLAVORS, LECITHIN AS EMULSIFIER (SOY), SALT, SODIUM BICARBONATE AS LEAVENING AGENT.\",\"upd\":\"07/14/2017\"},\"nutrients\":[{\"nutrient_id\":\"208\",\"name\":\"Energy\",\"derivation\":\"LCCS\",\"group\":\"Proximates\",\"unit\":\"kcal\",\"value\":\"633\",\"measures\":[{\"label\":\"PIECES\",\"eqv\":30.0,\"eunit\":\"g\",\"qty\":3.0,\"value\":\"190\"}]},{\"nutrient_id\":\"203\",\"name\":\"Protein\",\"derivation\":\"LCCS\",\"group\":\"Proximates\",\"unit\":\"g\",\"value\":\"6.67\",\"measures\":[{\"label\":\"PIECES\",\"eqv\":30.0,\"eunit\":\"g\",\"qty\":3.0,\"value\":\"2.00\"}]},{\"nutrient_id\":\"204\",\"name\":\"Total lipid (fat)\",\"derivation\":\"LCCS\",\"group\":\"Proximates\",\"unit\":\"g\",\"value\":\"50.00\",\"measures\":[{\"label\":\"PIECES\",\"eqv\":30.0,\"eunit\":\"g\",\"qty\":3.0,\"value\":\"15.00\"}]},{\"nutrient_id\":\"205\",\"name\":\"Carbohydrate, by difference\",\"derivation\":\"LCCS\",\"group\":\"Proximates\",\"unit\":\"g\",\"value\":\"40.00\",\"measures\":[{\"label\":\"PIECES\",\"eqv\":30.0,\"eunit\":\"g\",\"qty\":3.0,\"value\":\"12.00\"}]},{\"nutrient_id\":\"291\",\"name\":\"Fiber, total dietary\",\"derivation\":\"LCCS\",\"group\":\"Proximates\",\"unit\":\"g\",\"value\":\"3.3\",\"measures\":[{\"label\":\"PIECES\",\"eqv\":30.0,\"eunit\":\"g\",\"qty\":3.0,\"value\":\"1.0\"}]},{\"nutrient_id\":\"269\",\"name\":\"Sugars, total\",\"derivation\":\"LCCS\",\"group\":\"Proximates\",\"unit\":\"g\",\"value\":\"33.33\",\"measures\":[{\"label\":\"PIECES\",\"eqv\":30.0,\"eunit\":\"g\",\"qty\":3.0,\"value\":\"10.00\"}]},{\"nutrient_id\":\"301\",\"name\":\"Calcium, Ca\",\"derivation\":\"LCCD\",\"group\":\"Minerals\",\"unit\":\"mg\",\"value\":\"133\",\"measures\":[{\"label\":\"PIECES\",\"eqv\":30.0,\"eunit\":\"g\",\"qty\":3.0,\"value\":\"40\"}]},{\"nutrient_id\":\"303\",\"name\":\"Iron, Fe\",\"derivation\":\"LCCD\",\"group\":\"Minerals\",\"unit\":\"mg\",\"value\":\"1.20\",\"measures\":[{\"label\":\"PIECES\",\"eqv\":30.0,\"eunit\":\"g\",\"qty\":3.0,\"value\":\"0.36\"}]},{\"nutrient_id\":\"307\",\"name\":\"Sodium, Na\",\"derivation\":\"LCCS\",\"group\":\"Minerals\",\"unit\":\"mg\",\"value\":\"117\",\"measures\":[{\"label\":\"PIECES\",\"eqv\":30.0,\"eunit\":\"g\",\"qty\":3.0,\"value\":\"35\"}]},{\"nutrient_id\":\"401\",\"name\":\"Vitamin C, total ascorbic acid\",\"derivation\":\"LCCD\",\"group\":\"Vitamins\",\"unit\":\"mg\",\"value\":\"0.0\",\"measures\":[{\"label\":\"PIECES\",\"eqv\":30.0,\"eunit\":\"g\",\"qty\":3.0,\"value\":\"0.0\"}]},{\"nutrient_id\":\"318\",\"name\":\"Vitamin A, IU\",\"derivation\":\"LCCD\",\"group\":\"Vitamins\",\"unit\":\"IU\",\"value\":\"0\",\"measures\":[{\"label\":\"PIECES\",\"eqv\":30.0,\"eunit\":\"g\",\"qty\":3.0,\"value\":\"0\"}]},{\"nutrient_id\":\"606\",\"name\":\"Fatty acids, total saturated\",\"derivation\":\"LCCS\",\"group\":\"Lipids\",\"unit\":\"g\",\"value\":\"30.000\",\"measures\":[{\"label\":\"PIECES\",\"eqv\":30.0,\"eunit\":\"g\",\"qty\":3.0,\"value\":\"9.000\"}]},{\"nutrient_id\":\"605\",\"name\":\"Fatty acids, total trans\",\"derivation\":\"LCCS\",\"group\":\"Lipids\",\"unit\":\"g\",\"value\":\"0.000\",\"measures\":[{\"label\":\"PIECES\",\"eqv\":30.0,\"eunit\":\"g\",\"qty\":3.0,\"value\":\"0.000\"}]},{\"nutrient_id\":\"601\",\"name\":\"Cholesterol\",\"derivation\":\"LCSL\",\"group\":\"Lipids\",\"unit\":\"mg\",\"value\":\"17\",\"measures\":[{\"label\":\"PIECES\",\"eqv\":30.0,\"eunit\":\"g\",\"qty\":3.0,\"value\":\"5\"}]}],\"footnotes\":[]}}],\"count\":1,\"notfound\":0,\"api\":2.0}";
              return parser.parse(response).getAsJsonObject();
          }
        };

        Report report = new Report("RAFFAELLO, ALMOND COCONUT TREAT",
                "VEGETABLE OILS (PALM AND SHEANUT). DRY COCONUT, SUGAR, ALMONDS, SKIM MILK POWDER, WHEY POWDER (MILK), WHEAT FLOUR, NATURAL AND ARTIFICIAL FLAVORS, LECITHIN AS EMULSIFIER (SOY), SALT, SODIUM BICARBONATE AS LEAVENING AGENT.",
                633.0, 6.67, 50.0, 40.0, 3.3);

        String argument = "45142036";
        String expected = report.toString();
        String actual = foodReportCommand.execute(argument);

        assertEquals(ERROR + "after running FoodReportCommand::execute with argument=45142036.", expected, actual);

        when(foodByNdbnoCacheMock.containsKey(argument)).thenReturn(true);
        when(foodByNdbnoCacheMock.get(argument)).thenReturn(report);

        actual = foodReportCommand.execute(argument);
        assertEquals(ERROR + "after testing GetFood::execute's if case with a valid condition.", expected, actual);
    }
}
