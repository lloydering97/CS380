
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public final class ChatClient {

  private static PrintStream out;
  private static BufferedReader br;
  private static Socket socket;
  private static String username;
  private static Scanner in;

  public static void main(String[] args) throws Exception {
    try {
      socket = new Socket("18.221.102.182", 38001);
      in = new Scanner( System.in );
      OutputStream os = socket.getOutputStream();
      out = new PrintStream(os, true, "UTF-8");
      InputStream is = socket.getInputStream();
      InputStreamReader isr = new InputStreamReader(is, "UTF-8");
      br = new BufferedReader(isr);
      System.out.print( "Enter Username: " );
      username = in.nextLine();
      out.println( username );
      Thread.sleep( 500 ); //Wait for server response
      if( br.ready() ){
        String response = br.readLine();
        System.out.println( response );
        return;
      }
    }
    catch( Exception e )
    {
      System.out.println( "Error: " + e.toString() );
    }
    Runnable sender = () -> {
      while( true )
      {
        out.println( in.nextLine() );
      }
    };
    Runnable receiver = () -> {
      try{
        while( true )
        {
          System.out.println( br.readLine() );  
        }
      }
      catch( Exception e )
      {
        System.out.println( "Error: " + e.toString() );
      }
    };
    Thread sendSession = new Thread( sender );
    Thread receiveSession = new Thread( receiver );
    sendSession.start();
    receiveSession.start();
  }
}