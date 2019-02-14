package bg.sofia.uni.fmi.mjt.foodanalyzer.dto;

import java.util.Objects;

public class Product {
    private String group;
    private String name;
    private int ndbno;
    private String manu;
    private String upc;

    public Product(String group, String name, String ndbno, String manu) {
        this.group = group;
        this.name = name;
        this.ndbno = Integer.parseInt(ndbno);
        this.manu = manu;
        this.setNameAndUpc();
    }

    public String getUpc() {
        return this.upc;
    }

    public void setNameAndUpc() {
        if(this.group.equals("Branded Food Products Database")) {
            String[] splittedName = this.name.split(", UPC: ");
            this.setName(splittedName[0]);
            this.setUpc(splittedName[1]);
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
        String result = "[Product's name: " + name + ", ndbno: " + this.ndbno;

        if (this.upc != null) {
            result += ", upc: " + this.upc;

            if (!this.manu.equals("none")) {
                result += ", manu: " + this.manu;
            }
        }

        result += "]";

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return ndbno == product.ndbno;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ndbno);
    }
}
