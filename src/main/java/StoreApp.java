import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class StoreApp {
    static Scanner scanner = new Scanner(System.in);
    static void createStoreTable(Connection connection) throws SQLException {
        Statement create = connection.createStatement();
        create.execute("create table store " +
                "(" +
                " id integer primary key," +
                " name varchar(25)," +
                " category enum(beer, whisky, vodka, wine)," +
                " voltage decimal(4,1)" +
                " capacity decimal(4,3)" +
                ");"
        );
        create.close();
    }
    static void insertRowsIntoStoreTable(Connection connection) throws SQLException {
        Statement insert = connection.createStatement();
        insert.executeUpdate("insert into store values(1, 'kasztelan', 'beer', 5.5, 0.5);");
        insert.close();
    }

    static void deleteStoreTable(Connection connection) throws SQLException {
        Statement drop = connection.createStatement();
        drop.execute("drop table store");
        drop.close();
    }
    static int menu(){
        System.out.println("1. Utwórz tabelę");
        System.out.println("2. Dodaj kilka rekodrów");
        System.out.println("3. Usuń tabelę");
        System.out.println("0. Wyjście z programu");
        while(!scanner.hasNextInt()){
            System.out.println("Wpisz numer polecenia z menu!!!");
            scanner.nextLine(); //wyczyszczenie zawartości bufora klawiatury
        }
        return scanner.nextInt();
    }
    public static void main(String[] args) {
        while(true){
            final int option = menu();
            switch(option){
                case 1:

                    break;
                case 2:

                    break;
                case 3:

                    break;
                case 0:
                    return;
            }
        }
    }
}
