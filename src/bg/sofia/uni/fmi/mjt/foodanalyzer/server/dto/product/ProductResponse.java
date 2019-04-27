package bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.product;

public class ProductResponse {
    private ProductList list;

    public ProductResponse(ProductList list) {
        this.list = list;
    }

    public ProductList getList() {
        return list;
    }
}
