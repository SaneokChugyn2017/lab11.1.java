package org.example;

import javax.persistence.*;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name = "Products")
public class Product {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "product_id")
    private int productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_price")
    private double productPrice;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    public Product() {
    }

    public Product(String productName, double productPrice) {
        this.productName = productName;
        this.productPrice = productPrice;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public String toString() {
        double productTotalAmount = getProductPrice();
        return productName + " " + productPrice + " тенге. Сумма: " + productTotalAmount + " тенге";
    }

    public void calculateAndPrintProductInfo() {
        double productTotalAmount = getProductPrice();
        System.out.println("   - " + productName + " - " + getProductPrice() + " тенге. Сумма: " + productTotalAmount + " тенге");
    }

    public static void calculateAndPrintTotalAmountForProducts(Client client) {
        System.out.println("Продукты, купленные " + client.getClientName() + ":");

        Map<String, Long> productCounts = client.getProducts().stream()
                .collect(Collectors.groupingBy(Product::getProductName, Collectors.counting()));

        double totalAmount = 0.0; // Итоговая сумма всех товаров

        for (Map.Entry<String, Long> entry : productCounts.entrySet()) {
            String productName = entry.getKey();
            long count = entry.getValue();

            double productTotalAmount = client.getProducts().stream()
                    .filter(p -> p.productName.equals(productName))
                    .mapToDouble(Product::getProductPrice)
                    .sum();

            totalAmount += productTotalAmount;

            System.out.println(productName + " " + count + " шт. Сумма: " + productTotalAmount + " тенге");
        }

        System.out.println("Итоговая сумма всех товаров: " + totalAmount);
    }
}
