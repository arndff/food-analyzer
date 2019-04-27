package bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.report;

import java.util.List;

public class ReportResponse {
    private List<ReportData> foods;

    public ReportResponse(List<ReportData> foods) {
        this.foods = foods;
    }

    public List<ReportData> getList() {
        return foods;
    }
}
