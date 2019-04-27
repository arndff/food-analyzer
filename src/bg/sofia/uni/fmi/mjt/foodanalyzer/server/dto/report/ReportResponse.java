package bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.report;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.entity.Report;

import java.util.List;

public class ReportResponse {
    private List<ReportData> foods;

    public ReportResponse(List<ReportData> foods) {
        this.foods = foods;
    }

    public List<ReportData> getList() {
        return foods;
    }

    public Report createReportObject() {
        return foods.get(0).createReportObject();
    }
}
