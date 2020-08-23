import java.sql.*;
import java.util.Scanner;

public class TransactionDemo {
    static void createTableAccount(Connection connection) throws SQLException {
        Statement create = connection.createStatement();
        create.execute("create table account (id integer primary key, points integer);");
        create.executeUpdate("insert into account values (1, 100), (2, 50);");
        create.close();
    }
    static int getPointsFromAccount(Connection connection, int id) throws SQLException {
        PreparedStatement select =
                connection.prepareStatement("select points from account where id = ?");
        select.setInt(1, id);
        ResultSet set = select.executeQuery();
        set.next();
        return set.getInt(1);
    }
    public static void main(String[] args) throws SQLException {
        int pointsToTransfer = 20;
        Connection connection = JdbcConnection.MYSQL_JAVA6.getConnection();
        connection.setAutoCommit(false);
        connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        Savepoint start = connection.setSavepoint("start");
        //createTableAccount(connection);
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
        if (sum == sourcePoints + targetPoints){
            connection.commit();
        } else {
            connection.rollback(start);
        }
        System.out.println("Stan konta 1 po transferze: " + sourcePoints);
        System.out.println("Stan konta 2 po transferze: " + targetPoints);

    }
}
