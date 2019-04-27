package bg.sofia.uni.fmi.mjt.foodanalyzer.server.commands;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.report.ReportResponse;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.entity.Report;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.exceptions.NoInformationFoundException;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

public class FoodReportCommand extends AbstractCommand {
    private ConcurrentMap<String, Report> foodByNdbnoCache;

    FoodReportCommand(HttpClient client, ConcurrentMap<String, Report> foodByNdbnoCache) {
        super(client);
        this.foodByNdbnoCache = foodByNdbnoCache;
    }

    @Override
    public String execute(String argument) throws NoInformationFoundException {
        if (foodByNdbnoCache.containsKey(argument)) {
            return foodByNdbnoCache.get(argument).toString();
        }

        String url = API_URL + "/V2/reports?ndbno=" + argument + "&format=json&api_key=" + API_KEY;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        try {
            Gson gson = new Gson();

            String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            ReportResponse reportResponse = gson.fromJson(response, ReportResponse.class);

            if (reportResponse.getList() == null) {
                throw new NoInformationFoundException("No information found for ndbno " + argument + ".");
            }

            Report report = reportResponse.getList().get(0).createReportObject();

            // Updating foodByNdbnoCache
            foodByNdbnoCache.put(argument, report);

            return report.toString();
        } catch (IOException | InterruptedException e) {
            foodServerLogger.log(Level.WARNING, "Exception caught in FoodReportCommand::execute.", e);
        }

        return null;
    }
}
