package bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.product;

import java.util.List;

public class ProductList {
    private List<Product> item;

    public ProductList(List<Product> item) {
        this.item = item;
    }

    public List<Product> getItem() {
        return item;
    }
}
