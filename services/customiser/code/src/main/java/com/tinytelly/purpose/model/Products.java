package com.tinytelly.purpose.model;

import java.util.List;

/**
 * Created by mattheww on 16/11/2016.
 */
public class Products {
    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    private List<Product> products;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Products{");
        sb.append("products=").append(products);
        sb.append('}');
        return sb.toString();
    }
}
