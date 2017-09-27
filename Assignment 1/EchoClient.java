
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public final class EchoClient {

    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("localhost", 22222)) {
            Scanner in = new Scanner( System.in );
            OutputStream os = socket.getOutputStream();
            PrintStream out = new PrintStream(os, true, "UTF-8");
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            while( true )
            {
              System.out.println( br.readLine() );
              System.out.print( "Client> " );
              String input = in.nextLine();
              if( input.equals("exit") ){
                break;
              }
              out.println(input);
              System.out.print( "Server> " );
            }
        }
    }
}