import java.util.Scanner;
public class LocalScanner {
    private Scanner sc;
    public LocalScanner(){
        sc = new Scanner(System.in); 
    }
    public String nextToken(){
        String token = sc.nextLine(); 
        return token;
    }
}
