package bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto;

import java.util.Objects;

public class Report {

    private String name;
    private String ingredients;
    private double kcal;
    private double protein;
    private double fat;
    private double carbohydrate;
    private double fiber;

    public Report(String name, String ingredients,
                  double kcal, double protein, double fat, double carbohydrate, double fiber) {
        this.name = name;
        this.ingredients = ingredients;
        this.kcal = kcal;
        this.protein = protein;
        this.fat = fat;
        this.carbohydrate = carbohydrate;
        this.fiber = fiber;
    }

    @Override
    public String toString() {
        return "[Report for " + name + " -> "
                + "ingredients: " + ingredients + ", "
                + "kcal: " + kcal + ", "
                + "protein: " + protein + ", "
                + "fat: " + fat + ", "
                + "carbohydrate: " + carbohydrate + ", "
                + "fiber: " + fiber + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Report)) {
            return false;
        }

        Report report = (Report) o;
        return Objects.equals(name, report.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
