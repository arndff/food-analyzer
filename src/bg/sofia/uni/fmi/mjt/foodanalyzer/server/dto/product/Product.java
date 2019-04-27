package bg.sofia.uni.fmi.mjt.foodanalyzer.server.dto.product;

import java.util.Objects;

public class Product {
    private static final String BRANDED_FOOD_PRODUCTS = "Branded Food Products Database";

    private String group;
    private String name;
    private String ndbno;
    private String manu;
    private String upc;

    public Product(String group, String name, String ndbno, String manu) {
        this.group = group;
        this.name = name;
        this.ndbno = ndbno;
        this.manu = manu;
    }

    public String getUpc() {
        return upc;
    }

    public void setNameAndUpc() {
        if (BRANDED_FOOD_PRODUCTS.equals(group)) {
            String[] splittedName = name.split(", UPC: ");
            setName(splittedName[0]);
            setUpc(splittedName[1]);
        }
    }

    private void setName(String name) {
        this.name = name;
    }

    private void setUpc(String upc) {
        this.upc = upc;
    }

    @Override
    public String toString() {
        String result = "[Product's name: " + name + ", ndbno: " + ndbno;

        if (upc != null) {
            result += ", upc: " + upc;

            if (!"none".equals(manu)) {
                result += ", manu: " + manu;
            }
        }

        result += "]";
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Product)) {
            return false;
        }

        Product product = (Product) o;
        return ndbno.equals(product.ndbno);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ndbno);
    }
}
