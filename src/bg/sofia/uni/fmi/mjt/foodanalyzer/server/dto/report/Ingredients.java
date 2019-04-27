package bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.report;

public class Ingredients {
    private String desc;

    public Ingredients(String desc) {
        this.desc = desc;
    }

    String getDesc() {
        return desc;
    }
}
