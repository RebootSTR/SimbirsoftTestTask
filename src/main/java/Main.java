import java.util.Scanner;

public class Main {
    final static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Enter URL");
        SitesUtils utils = new SitesUtils(scanner.nextLine());
        try {
            System.out.println(utils.getAllWords());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
