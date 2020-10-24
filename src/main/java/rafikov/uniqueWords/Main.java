package rafikov.uniqueWords;

import java.util.Scanner;

public class Main {
    final static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        UniqueWords uniqueWords = null;
        try {
            println("Enter the URL");
            new UniqueWords(scanner.nextLine());
            uniqueWords.loadPage();
            uniqueWords.parsePage();
            println(uniqueWords.calculateUniqueWords());
        } catch (IllegalArgumentException | SiteConnectException ex) {
            ex.printStackTrace();
        }
    }

    public static void println(Object toPrint) {
        System.out.println(toPrint);
    }
}
