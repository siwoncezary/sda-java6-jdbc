

import com.sun.rowset.JdbcRowSetImpl;

import javax.sql.rowset.JdbcRowSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateTable {
  public static void main(String[] args) throws SQLException {
    Connection connection = JdbcConnection.MYSQL_JAVA6.getConnection();
    Statement createTable = connection.createStatement();
    JdbcRowSet set = new JdbcRowSetImpl(connection);
    set.setCommand("select * from kursanci");
    Statement dropTable = connection.createStatement();
    //execute służy do tworzenia obiektów: tabel, wyzwalaczy, widoków itd.
    dropTable.execute("drop table kursanci;");
    boolean result = createTable.execute("create table kursanci(email varchar(25) primary key, nick varchar(15));");
    System.out.println("Czy polecenie zwróciło ResultSet? " + result);
    Statement insertRow = connection.createStatement();
    //executeUpdate służy do operacji wstawiania, usuwania lub modyfikacji rekordów
    int count = insertRow.executeUpdate("insert into kursanci values('karol@op.pl','karolek')," +
            "('ewa@gmail.com','ewa'),('marek@sda.pl','marek');");
    System.out.println("Liczba wstawionych rekordów: " + count);
  }
}
