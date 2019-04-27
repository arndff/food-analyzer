package bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.report;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.entity.Report;

import java.util.List;

public class ReportData {
    private ReportFood food;

    Report createReportObject() {
        food.getDesc().setProperName();
        String name = food.getDesc().getName();

        String ingredients = food.getIng().getDesc();

        List<Nutrient> nutrients = food.getNutrients();

        double kcal = nutrients.get(0).getValue();
        double protein = nutrients.get(1).getValue();
        double fat = nutrients.get(2).getValue();
        double carbohydrate = nutrients.get(3).getValue();
        double fiber = nutrients.get(4).getValue();

        return new Report(name, ingredients, kcal, protein, fat, carbohydrate, fiber);
    }

    public ReportData(ReportFood food) {
        this.food = food;
    }

    public ReportFood getFood() {
        return food;
    }
}
