package bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.report;

public class Description {
    private String name;

    public Description(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    void setProperName() {
        name = name.split(", U")[0];
    }
}
