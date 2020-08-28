import java.sql.*;
import java.util.Scanner;

public class TransactionDemo {
    static void createTableAccount(Connection connection) throws SQLException {
        Statement create = connection.createStatement();
        create.execute("drop table if exists account;");
        create.execute("create table if not exists account (id integer primary key, points integer);");
        create.executeUpdate("insert into account values (1, 10), (2, 50);");
        create.close();
    }

    static int getPointsFromAccount(Connection connection, int id) throws SQLException {
        PreparedStatement select =
                connection.prepareStatement("select points from account where id = ?");
        select.setInt(1, id);
        ResultSet set = select.executeQuery();
        set.next();
        int result = set.getInt(1);
        set.close();
        return result;
    }

    public static void main(String[] args) throws SQLException {
        int pointsToTransfer = 20;
        Connection connection = JdbcConnection.MYSQL_JAVA6.getConnection();
        createTableAccount(connection);
        //wyłączamy autocommit, od tego punktu zaczyna się transakcja
        connection.setAutoCommit(false);
        //ustawiamy poziom izolacji na serializable, całkowita izolacja jednoczesnych transacji, transakcje będą szeregowane, jedna po drugiej
        connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        //ustawiamy savepoint, będzie można przywrócić stan bazy do tego punktu
        Savepoint start = connection.setSavepoint("start");
        //Pobieramy z konta o id = 1 liczbę punktów  pointsToTransfer
        int sourcePoints = getPointsFromAccount(connection, 1);
        int targetPoints = getPointsFromAccount(connection, 2);
        System.out.println("Stan konta 1: " + sourcePoints);
        System.out.println("Stan konta 2: " + targetPoints);
        PreparedStatement transfer =
                connection.prepareStatement("update account set points = ?" +
                        " where id = ?");
        transfer.setInt(1, sourcePoints - pointsToTransfer);
        transfer.setInt(2, 1);
        transfer.executeUpdate();
        transfer.close();
        //symulujemy zatrzymanie transakcji, można w tym czasie w innym kliencie wykonać druga transakcję
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        //Dodajemy do konta o id = 2 liczbę punktów w pointsToTransfer
        PreparedStatement transferTo =
                connection.prepareStatement("update account set points = ?" +
                        " where id = ?");
        transferTo.setInt(1, pointsToTransfer + targetPoints);
        transferTo.setInt(2, 2);
        transferTo.executeUpdate();
        transferTo.close();
        int sum = sourcePoints + targetPoints;
        sourcePoints = getPointsFromAccount(connection, 1);
        targetPoints = getPointsFromAccount(connection, 2);
        //testujemy, czy suma punktów obu kont jest taka sama przed i po transakcji i czy na koncie źródłowym zostały punkty
        if (sum == sourcePoints + targetPoints && sourcePoints >= 0) {
            connection.commit();
            System.out.println("Transakcja wykonana");
        } else {
            //odwołujemy transakcję do punktu savepon'tu start
            connection.rollback(start);
            System.out.println("Transakcja odwołana. Za mała punktów na koncie 1");
        }
        connection.setAutoCommit(true);
        System.out.println("Obecny stan konta 1: " + getPointsFromAccount(connection, 1));
        System.out.println("Obecny stan konta 2: " + getPointsFromAccount(connection, 2));
        connection.close();

    }
}
