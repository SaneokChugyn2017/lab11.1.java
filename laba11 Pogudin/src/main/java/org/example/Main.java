// Main.java
package org.example;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.List;

public class Main {

    public static final SessionFactory sessionFactory;

    static {
        try {
            sessionFactory = new Configuration()
                    .addAnnotatedClass(Client.class)
                    .addAnnotatedClass(Product.class)
                    .configure("hibernate.cfg.xml")
                    .buildSessionFactory();
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static void main(String[] args) {
        try {
            UserAccess userAccess = new UserAccess(sessionFactory);

            Client client1 = new Client("Bob");
            userAccess.addClient(client1);

            Client client2 = new Client("Patricio");
            userAccess.addClient(client2);

            Product product1 = new Product("крендель", 10.00);
            userAccess.addProduct(product1, client1);

            Product product2 = new Product("пончик", 20.00);
            userAccess.addProduct(product2, client1);

            Product product3 = new Product("бублик", 30.00);
            userAccess.addProduct(product3, client1);

            Product product4 = new Product("шимпанзе", 40.00);
            userAccess.addProduct(product4, client2);

            Product product5 = new Product("пирожок", 9.99);
            userAccess.addProduct(product5, client2);

            Product product6 = new Product("казахстан", 99.99);
            userAccess.addProduct(product6, client2);

            List<Client> clients = userAccess.getAllClient();
            List<Product> products = userAccess.getAllProducts();

            System.out.println("All clients:");
            clients.forEach(c -> {
                System.out.println(c.getClientId() + ": " + c.getClientName());
                List<Product> customerProducts = userAccess.getProductsByClient(c);
                customerProducts.forEach(p -> System.out.println("   - " + p.getProductName() + " - " + p.getProductPrice()));
            });

            System.out.println("\nAll products:");
            products.forEach(p -> System.out.println(p.getProductId() + ": " + p.getProductName() + " - " + p.getProductPrice()));

        } finally {
            try {
                if (sessionFactory != null && !sessionFactory.isClosed()) {
                    sessionFactory.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}