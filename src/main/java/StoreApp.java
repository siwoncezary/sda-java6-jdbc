import java.math.BigDecimal;
import java.sql.*;
import java.util.Scanner;
import java.util.concurrent.ConcurrentNavigableMap;

public class StoreApp {
    static Scanner scanner = new Scanner(System.in);

    static void createStoreTable(Connection connection) throws SQLException {
        Statement create = connection.createStatement();
        create.execute("create table store " +
                "(" +
                " id integer primary key auto_increment," +
                " name varchar(25)," +
                " category enum('beer', 'whisky', 'vodka', 'wine')," +
                " voltage decimal(4,1)," +
                " capacity decimal(4,3)" +
                ");"
        );
        create.execute("create table account(" +
                "id integer primary key auto_increment," +
                "points integer" +
                ");");
        create.close();
    }

    static void insertRowsIntoStoreTable(Connection connection) throws SQLException {
        Statement insert = connection.createStatement();
        insert.executeUpdate("insert into store values(1, 'kasztelan', 'beer', 5.5, 0.5);");
        insert.executeUpdate("insert into account values(1, 100)");
        insert.executeUpdate("insert into  account values (2, 50);");
        insert.close();
    }

    static void deleteStoreTable(Connection connection) throws SQLException {
        Statement drop = connection.createStatement();
        drop.execute("drop table store");
        drop.close();
    }

    static void showRowsFromStoreTable(Connection connection) throws SQLException {
        Statement select = connection.createStatement();
        ResultSet set = select.executeQuery("select * from store");
        while (set.next()) {
            int id = set.getInt("id");
            String name = set.getString("name");
            String category = set.getString("category");
            BigDecimal voltage = set.getBigDecimal("voltage");
            BigDecimal capacity = set.getBigDecimal("capacity");
            System.out.println(id + " " + name + " " + category + " " + voltage);
        }
        set.close();
        select.close();
    }

    static void insertNewRowIntoStoreTable(Connection connection) throws SQLException {
        System.out.println("Wpisz nazwę:");
        String name = scanner.nextLine();
        System.out.println("Wpisz kategorię:");
        String category = scanner.nextLine();
        System.out.println("Wpisz zawartość alkoholu:");
        float voltage = scanner.nextFloat();
        System.out.println("Wpisz pojemność:");
        float capacity = scanner.nextFloat();

        PreparedStatement insert = connection.prepareStatement("insert into " +
                "store(`name`,`category`,`voltage`,`capacity`) " +
                "values(?, ?, ?, ?);");
        insert.setString(1, name);
        insert.setString(2, category);
        insert.setBigDecimal(4, new BigDecimal(capacity));
        insert.setBigDecimal(3, new BigDecimal(voltage));
        int count = insert.executeUpdate();
        System.out.println(count == 1 ? "Sukces" : "Błąd");
        insert.close();
    }

    static void selectByIdFromStoreTable(Connection connection) throws SQLException {
        System.out.println("Wpisz id:");
        String id = scanner.nextLine();
        PreparedStatement select = connection.prepareStatement("select * from store where id = ?");
        select.setInt(1, Integer.parseInt(id));
        ResultSet set = select.executeQuery();
        while (set.next()) {
            int i = set.getInt("id");
            String name = set.getString("name");
            System.out.println(i + " " + name);
        }
    }

    static void insertIntoResultSet(Connection connection) throws SQLException {
        Statement select = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        ResultSet set = select.executeQuery("select * from store");
        while (set.next()) {
            String category = set.getString("category");
            if ("wine".equals(category)) {
                System.out.println("Wstaw nowy rekord:");
                System.out.println("Podaj nazwę:");
                String name = scanner.nextLine();
                System.out.println("Podaj zawartość:");
                float voltage = scanner.nextFloat();
                System.out.println("Podaj objętość:");
                float capacity = scanner.nextFloat();
                scanner.nextLine();//czyszczenie bufora klawiatury

                //wstawiamy nowy rekord
                set.moveToInsertRow();
                set.updateString("name", name);
                set.updateBigDecimal("voltage", new BigDecimal(voltage));
                set.updateBigDecimal("capacity", new BigDecimal(capacity));
                set.updateString("category", "wine");
                set.insertRow();
                set.close();
                return;
            }
        }
    }

    static void deleteRowFromStoreTable(Connection connection) throws SQLException {
        System.out.println("Podaj id usuwanego wiersza:");
        int id = scanner.nextInt();
        scanner.nextLine();
        Statement delete = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        ResultSet set = delete.executeQuery("Select * from store");
        while (set.next()) {
            //napisać warunek czy id bieżącego to id wczytane z klawiatury
            if (id == set.getInt("id")) {
                set.deleteRow();
                set.close();
                return;
            }
        }
    }

    static void transferPoints(Connection connection) throws SQLException {
        int pointsToTransfer = 20;
        Statement select = connection.createStatement();
        //connection.setTransactionIsolation(Connection.);
        connection.setAutoCommit(false);
        Savepoint savepoint = connection.setSavepoint("transfer");
        ResultSet set = select.executeQuery("select sum(points) from account where id in (1,2);");
        set.next();
        int sumBefore =  set.getInt(1);
        set = select.executeQuery("select points from account where id = 1;");
        set.next();
        int points = set.getInt(1);
        System.out.println("POINTS BEFORE: " + points);
        PreparedStatement updateSource = connection.prepareStatement("update account set points = (select points - ? from account where id = 1) where id = 1;");
        updateSource.setInt(1, pointsToTransfer);
        updateSource.executeUpdate();
        System.out.println("Opps!!");
        scanner.nextLine();
        PreparedStatement updateTarget = connection.prepareStatement("update account set points = (select points + ? from account where id = 2) where id = 2;");
        updateTarget.setInt(1, pointsToTransfer);
        updateTarget.executeUpdate();
        set = select.executeQuery("select sum(points) from account where id in (1,2);");
        set.next();
        int sumAfter = set.getInt(1);
        set = select.executeQuery("select points from account where id = 1;");
        set.next();
        points = set.getInt(1);
        System.out.println("POINTS AFTER: " + points);
        System.out.println("before: " + sumBefore +", after: " + sumAfter +", points: " + points);
        if (sumAfter != sumBefore || points < 0){
            System.out.println("ROLLBACK");
            connection.rollback(savepoint);
        } else {
            System.out.println("COMMIT");
            connection.commit();
        }
    }

    static int menu() {
        System.out.println("1. Utwórz tabelę");
        System.out.println("2. Dodaj kilka rekordów");
        System.out.println("3. Usuń tabelę");
        System.out.println("4. Wyświetl wiersze z tabeli");
        System.out.println("5. Wpisz i dodaj wiersz do tabeli");
        System.out.println("6. Znajdź wiersz o numerze");
        System.out.println("7. Dodaj nowe wino jeśli jest w tabeli wino");
        System.out.println("8. Usuń wiersz");
        System.out.println("9. Przekaż środki");
        System.out.println("0. Wyjście z programu");
        while (!scanner.hasNextInt()) {
            System.out.println("Wpisz numer polecenia z menu!!!");
            scanner.nextLine(); //wyczyszczenie zawartości bufora klawiatury
        }
        int option = scanner.nextInt();
        scanner.nextLine();
        return option;
    }

    public static void main(String[] args) throws SQLException {
        Connection connection = JdbcConnection.MYSQL_JAVA6.getConnection();
        while (true) {
            final int option = menu();
            switch (option) {
                case 1:
                    createStoreTable(connection);
                    break;
                case 2:
                    insertRowsIntoStoreTable(connection);
                    break;
                case 3:
                    deleteStoreTable(connection);
                    break;
                case 4:
                    showRowsFromStoreTable(connection);
                    break;
                case 5:
                    insertNewRowIntoStoreTable(connection);
                    break;
                case 6:
                    selectByIdFromStoreTable(connection);
                    break;
                case 7:
                    insertIntoResultSet(connection);
                    break;
                case 8:
                    deleteRowFromStoreTable(connection);
                    break;
                case 9:
                    transferPoints(connection);
                    break;
                case 0:
                    return;
            }
        }
    }
}
