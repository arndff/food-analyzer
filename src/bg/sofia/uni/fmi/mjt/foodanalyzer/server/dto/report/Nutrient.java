package bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.report;

public class Nutrient {
    private double value;

    public Nutrient(double value) {
        this.value = value;
    }

    double getValue() {
        return value;
    }
}
