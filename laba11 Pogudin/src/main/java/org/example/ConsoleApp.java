package org.example;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Predicate;

public class ConsoleApp {

    private static final UserAccess userAccess;

    static {
        userAccess = new UserAccess(Main.getSessionFactory());
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try (Session session = Main.getSessionFactory().openSession()) {
            while (true) {
                System.out.println("Введите команду:");
                System.out.println("/showProductsByPerson <client_id>");
                System.out.println("/findPersonsByProductTitle <product_name>");
                System.out.println("/removePerson <client_id>");
                System.out.println("/removeProduct <product_name>");
                System.out.println("/buy <client_id> <product_name>");
                System.out.println("/exit");

                String command = scanner.nextLine();
                String[] tokens = command.split("\\s+");

                switch (tokens[0]) {
                    case "/showProductsByPerson":
                        showProductsByPerson(session, tokens);
                        break;
                    case "/findPersonsByProductTitle":
                        findPersonsByProductTitle(session, tokens);
                        break;
                    case "/removePerson":
                        removePerson(session, tokens);
                        break;
                    case "/removeProduct":
                        removeProduct(session, tokens);
                        break;
                    case "/buy":
                        buyProduct(session, tokens);
                        break;
                    case "/exit":
                        return;
                    default:
                        System.out.println("Неверная команда. Попробуйте снова.");
                }

                // Добавьте ожидание завершения транзакции перед переходом к следующей итерации
                try {
                    Thread.sleep(1000); // Может потребоваться настроить под ваши нужды
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            scanner.close();
        }
    }

    private static void showProductsByPerson(Session session, String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("Неверный формат команды. Использование: /showProductsByPerson <customer_id>");
            return;
        }
        try {
            int customerId = Integer.parseInt(tokens[1]);
            Client client = session.get(Client.class, customerId);
            if (client != null) {
                session.refresh(client); // Обновление состояния клиента перед загрузкой продуктов
                System.out.println("Продукты, купленные " + client.getClientName() + ":");

                double totalAmount = 0.0; // Итоговая сумма всех товаров

                for (Product product : client.getProducts()) {
                    System.out.println(product.toString()); // Вывод информации о продукте

                    // Подсчет общей суммы товаров
                    totalAmount += product.getProductPrice();
                }

                System.out.println("Итоговая сумма всех товаров: " + totalAmount);
            } else {
                System.out.println("Клиент не найден.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат идентификатора клиента. Введите корректное целое число.");
        }
    }


    private static void buyProduct(Session session, String[] tokens) {
        if (tokens.length != 3) {
            System.out.println("Неверный формат команды. Использование: /buy <customer_id> <product_name>");
            return;
        }

        try {
            int clientId = Integer.parseInt(tokens[1]);

            // Извлечение объекта клиента
            Client client = session.get(Client.class, clientId);

            if (client != null) {
                String productName = tokens[2].trim();

                // Запрос цены у пользователя
                Scanner scanner = new Scanner(System.in);
                System.out.println("Введите цену продукта:");
                double productPrice = scanner.nextDouble();

                // Создание нового объекта продукта
                Product product = new Product();
                product.setProductName(productName);
                product.setProductPrice(productPrice);

                // Используем текущую сессию для сохранения продукта
                try {
                    Transaction transaction = session.beginTransaction();

                    // Обновляем связь с клиентом
                    product.setClient(client);
                    // Сохраняем продукт
                    session.persist(product);

                    transaction.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println("Продукт " + productName + " куплен клиентом " + client.getClientName() + " по цене " + productPrice + ".");
            } else {
                System.out.println("Клиент не найден.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат идентификатора клиента. Введите корректное целое число.");
        }
    }

    private static void findPersonsByProductTitle(Session session, String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("Неверный формат команды. Использование: /findPersonsByProductTitle <product_name>");
            return;
        }

        String productName = tokens[1].trim();

        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Product> query = builder.createQuery(Product.class);
            Root<Product> productRoot = query.from(Product.class);
            Predicate productNamePredicate = builder.equal(productRoot.get("productName"), productName);
            query.select(productRoot).where(productNamePredicate);

            List<Product> products = session.createQuery(query).getResultList();

            Map<Integer, Map<String, Long>> productsByClient = products.stream()
                    .collect(Collectors.groupingBy(p -> p.getClient().getClientId(),
                            Collectors.groupingBy(Product::getProductName, Collectors.counting())));

            System.out.print("Клиенты, купившие " + productName + ":");

            productsByClient.forEach((clientId, productCounts) -> {
                Client client = session.get(Client.class, clientId);
                System.out.print(clientId + ": " + client.getClientName() + ": ");

                productCounts.forEach((productNameCount, count) -> {
                    System.out.print(count + " раз(а), ");
                });

               // System.out.println(); // Перенос строки для отделения цены от следующего клиента

                List<Product> clientProducts = products.stream()
                        .filter(p -> p.getClient().getClientId() == clientId && p.getProductName().equals(productName))
                        .collect(Collectors.toList());

                clientProducts.forEach(p -> System.out.print(p.getProductPrice() + " тенге; "));
                System.out.println(); // Перенос строки для отделения от следующего клиента
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private static void removePerson(Session session, String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("Неверный формат команды. Использование: /removePerson <customer_id>");
            return;
        }

        try {
            int clientId = Integer.parseInt(tokens[1]);

            // Извлечение объекта клиента
            Client client = session.get(Client.class, clientId);

            if (client != null) {
                // Используем текущую сессию для удаления клиента
                Transaction transaction = session.beginTransaction();
                session.delete(client);
                transaction.commit();

                System.out.println("Клиент с ID " + clientId + " удален.");
            } else {
                System.out.println("Клиент не найден.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат идентификатора клиента. Введите корректное целое число.");
        }
    }

    private static void removeProduct(Session session, String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("Неверный формат команды. Использование: /removeProduct <product_name>");
            return;
        }

        String productName = tokens[1].trim();

        // Используем текущую сессию для удаления продукта
        try {
            Product product = userAccess.getProductByName(session, productName);

            if (product != null) {
                Transaction transaction = session.beginTransaction();
                session.delete(product);
                transaction.commit();

                System.out.println("Продукт " + productName + " удален.");
            } else {
                System.out.println("Продукт не найден.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}