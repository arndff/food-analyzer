package bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.report;

import java.util.List;

public class ReportFood {
    private Description desc;
    private Ingredients ing;
    private List<Nutrient> nutrients;

    public ReportFood(Description desc, Ingredients ing, List<Nutrient> nutrients) {
        this.desc = desc;
        this.ing = ing;
        this.nutrients = nutrients;
    }

    Description getDesc() {
        return desc;
    }

    Ingredients getIng() {
        return ing;
    }

    List<Nutrient> getNutrients() {
        return nutrients;
    }
}
