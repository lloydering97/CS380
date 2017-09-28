
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public final class EchoServer {

    private static Socket clientSocket;

    public static void main(String[] args) throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(22222)) {
            while (true) { 
                    Runnable session = () -> {   
                        try{  
                            Socket socket = clientSocket;
                            String address = socket.getInetAddress().getHostAddress();
                            InputStream is = socket.getInputStream();
                            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                            BufferedReader br = new BufferedReader(isr);
                            System.out.printf("Client connected: %s%n", address);
                            OutputStream os = socket.getOutputStream();
                            PrintStream out = new PrintStream(os, true, "UTF-8");
                            out.printf("Hi %s, thanks for connecting!%n", address);
                            while( true ){
                                String input = br.readLine();
                                if( input == null ){
                                    System.out.printf("Client disconnected: %s%n", address);
                                    break;
                                }
                                out.println( input );
                            }
                        }
                        catch(Exception e)
                        {}
                    };
                    clientSocket = serverSocket.accept();
                    Thread newSession = new Thread(session);
                    newSession.start();
            }
        }
    }
}