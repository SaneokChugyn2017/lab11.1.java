package org.example;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import java.util.List;

public class UserAccess {

    private final SessionFactory sessionFactory;

    public UserAccess(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void addClient(Client client) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(client);
            session.getTransaction().commit();
        }
    }

    public void addProduct(Product product, Client client) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            product.setClient(client);

            session.persist(product);
            session.update(client);

            session.getTransaction().commit();
        }
    }

    public List<Client> getAllClient() {
        try (Session session = sessionFactory.openSession()) {
            Criteria criteria = session.createCriteria(Client.class);
            return criteria.list();
        }
    }

    public List<Product> getAllProducts() {
        try (Session session = sessionFactory.openSession()) {
            Criteria criteria = session.createCriteria(Product.class);
            return criteria.list();
        }
    }

    public Client getClientByName(Session session, String clientName) {
        Criteria criteria = session.createCriteria(Client.class);
        criteria.add(Restrictions.eq("clientName", clientName));
        return (Client) criteria.uniqueResult();
    }

    public Client getClientById(int clientId) {
        try (Session session = sessionFactory.openSession()) {
            return session.find(Client.class, clientId);
        }
    }

    public void show(int clientId) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            Client client = getClientById(clientId);

            if (client != null) {
                Criteria criteria = session.createCriteria(Product.class);
                criteria.add(Restrictions.eq("customer", client));
                List<Product> products = criteria.list();

                System.out.println("Продукты для клиента с ID " + clientId + ": " + client.getClientName());

                for (int i = 0; i < products.size(); i++) {
                    Product product = products.get(i);
                    System.out.println(product.toString());
                }
            } else {
                System.out.println("Клиент с ID " + clientId + " не найден.");
            }

            session.getTransaction().commit();
        }
    }

    public List<Client> getClientByProductTitle(int productId) {
        try (Session session = sessionFactory.openSession()) {
            Criteria criteria = session.createCriteria(Product.class);
            criteria.add(Restrictions.eq("id", productId));
            criteria.setProjection(null);  // To select the entire entity (Client)
            return criteria.list();
        }
    }

    public List<Product> getProductsByClient(Client client) {
        try (Session session = sessionFactory.openSession()) {
            Criteria criteria = session.createCriteria(Product.class);
            criteria.add(Restrictions.eq("clent", client));
            return criteria.list();
        }
    }

    public void deleteClientByName(String clientName) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Client client = getClientByName(session, clientName);
            if (client != null) {
                session.delete(client);
            }
            session.getTransaction().commit();
        }
    }

    public void deleteProductByName(String productName) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Product product = getProductByName(session, productName);
            if (product != null) {
                session.delete(product);
            }
            session.getTransaction().commit();
        }
    }

    public Product getProductByName(Session session, String productName) {
        Criteria criteria = session.createCriteria(Product.class);
        criteria.add(Restrictions.eq("productName", productName));
        return (Product) criteria.uniqueResult();
    }

    public List<Client> getClientByProductTitle(Session session, String productName) {
        Criteria criteria = session.createCriteria(Product.class);
        criteria.add(Restrictions.eq("productName", productName));
        criteria.setProjection(null);  // To select the entire entity (Client)
        return criteria.list();
    }
}