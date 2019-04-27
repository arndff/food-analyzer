package bg.sofia.uni.fmi.mjt.foodanalyzer.server.commands;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.report.*;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.entity.Report;
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FoodReportCommandTest {
    private final static String ERROR = "Didn't get the expected result ";

    @Mock
    private HttpClient httpClientMock;

    @Mock
    private HttpResponse<String> httpResponseMock;

    private FoodReportCommand foodReportCommand;
    private Gson gson;

    @Before
    public void setup() {
        ConcurrentMap<String, Report> foodByNdbnoCache = new ConcurrentHashMap<>();
        foodReportCommand = new FoodReportCommand(httpClientMock, foodByNdbnoCache);
        gson = new Gson();
    }

    @Test(expected = NoInformationFoundException.class)
    public void testExecuteMethodWithNoInformationFoundException() throws Exception {
        ReportResponse reportResponse = new ReportResponse(null);
        String response = gson.toJson(reportResponse, ReportResponse.class);

        when(httpClientMock.send(Mockito.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(httpResponseMock);

        when(httpResponseMock.body()).thenReturn(response);

        foodReportCommand.execute("77777777");
    }

    @Test
    public void testExecuteMethodRegularCase() throws Exception {
        Description desc = new Description("RAFFAELLO, ALMOND COCONUT TREAT, UPC: 009800146130");
        Ingredients ing = new Ingredients("VEGETABLE OILS (PALM AND SHEANUT). DRY COCONUT, SUGAR, ALMONDS, SKIM MILK POWDER, WHEY POWDER (MILK), WHEAT FLOUR, NATURAL AND ARTIFICIAL FLAVORS, LECITHIN AS EMULSIFIER (SOY), SALT, SODIUM BICARBONATE AS LEAVENING AGENT.");
        List<Nutrient> nutrients = List.of(new Nutrient(633.0),
                new Nutrient(6.67),
                new Nutrient(50.0),
                new Nutrient(40.0),
                new Nutrient(3.3));

        ReportData reportData = new ReportData(new ReportFood(desc, ing, nutrients));
        ReportResponse reportResponse = new ReportResponse(List.of(reportData));

        String jsonResponse = gson.toJson(reportResponse, ReportResponse.class);

        when(httpClientMock.send(Mockito.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(httpResponseMock);

        when(httpResponseMock.body()).thenReturn(jsonResponse);

        String argument = "45142036";
        String expected = reportResponse.createReportObject().toString();
        String actual = foodReportCommand.execute(argument);

        assertEquals(ERROR + "after running FoodReportCommand::execute with argument=45142036.", expected, actual);
    }
}