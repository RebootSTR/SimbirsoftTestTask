package rafikov.uniqueWords;

import rafikov.uniqueWords.exceptions.DataBaseException;
import rafikov.uniqueWords.exceptions.SiteConnectException;

import java.util.Scanner;

public class Main {
    final static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        println("Enter the URL");
        UniqueWords uniqueWords = new UniqueWords(scanner.nextLine());
        try {
            uniqueWords.loadPage();
            uniqueWords.parsePage();
            uniqueWords.printUniqueWords(Main::println);
        } catch (SiteConnectException ex) {
            println(ex.getMessage());
        } catch (DataBaseException ex) {
            println("Problem with DataBase: " + ex.getMessage());
        }
    }

    public static void println(Object toPrint) {
        System.out.println(toPrint);
    }
}
