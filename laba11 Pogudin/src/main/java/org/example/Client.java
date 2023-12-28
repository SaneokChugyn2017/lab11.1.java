package org.example;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "client")
public class Client {

    // Уникальный идентификатор клиента
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "Client_id")
    private int clientId;

    // Имя клиента
    @Column(name = "client_name")
    private String clientName;

    // Список продуктов, принадлежащих клиенту (один клиент может иметь много продуктов)
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<Product> products;

    // Конструкторы

    // Пустой конструктор (необходим для Hibernate)
    public Client() {
    }

    // Конструктор с параметром для установки имени клиента
    public Client(String clientName) {
        this.clientName = clientName;
    }

    // Методы доступа (геттеры и сеттеры)

    // Получение уникального идентификатора клиента
    public int getClientId() {
        return clientId;
    }

    // Установка уникального идентификатора клиента
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    // Получение имени клиента
    public String getClientName() {
        return clientName;
    }

    // Установка имени клиента
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    // Получение списка продуктов, принадлежащих клиенту
    @OneToMany(mappedBy = "client", fetch = FetchType.EAGER)
    public List<Product> getProducts() {
        return products;
    }

    // Установка списка продуктов для клиента
    public void setProducts(List<Product> products) {
        this.products = products;
    }

    // Методы equals и hashCode (опциональные, генерируются на основе ваших потребностей)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return clientId == client.clientId &&
                Objects.equals(clientName, client.clientName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, clientName);
    }

    @Override
    public String toString() {
        double totalAmount = products.stream()
                .mapToDouble(Product::getProductPrice)
                .sum();
        return clientName + " - Итоговая сумма всех продуктов: " + totalAmount + " тенге";
    }
}
