package rafikov.uniqueWords;

import org.apache.log4j.xml.DOMConfigurator;
import rafikov.uniqueWords.exceptions.DataBaseException;
import rafikov.uniqueWords.exceptions.SiteConnectException;

import javax.xml.parsers.FactoryConfigurationError;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    final static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // loading logger xml file
        DOMConfigurator.configure("./resources/log4j.xml");
        println("Enter the URL");
        UniqueWords uniqueWords = new UniqueWords(scanner.nextLine());
        try {
            uniqueWords.loadPage();
        } catch (SiteConnectException ex) {
            println(ex.getMessage());
            println("Parsing local saved copy");
        }
        try {
            uniqueWords.parsePage();
            println("Words wrote on database. Count=" + uniqueWords.getCountWords());
            println("Do you want a print it here? (y or yes)");
            if (scanner.next().matches("y|yes")) {
                uniqueWords.printUniqueWords(Main::println);
            }
        } catch (DataBaseException ex) {
            println("Problem with DataBase: " + ex.getMessage());
        } catch (IOException ex) {
            println("Problems with open local copy page");
        } catch (Exception unknownException) {
            unknownException.printStackTrace();
        }
        uniqueWords.close();
    }

    public static void println(Object toPrint) {
        System.out.println(toPrint);
    }
}
