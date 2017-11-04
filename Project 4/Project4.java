import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.nio.ByteBuffer;

import java.util.*;

public final class Project4 {

    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("18.221.102.182", 38004)) {
            Scanner in = new Scanner( System.in );
            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String version = "0110"; //4
            String trafficClass = "00000000"; //8
            String flowLabel = "00000000000000000000"; //20
            //payloadLength
            String nextHeader = "00010001"; //8
            String hopLimit = "00010100"; //8
            String sourceAddr = "00000000000000000000000000000000000000000000000000000000000000000000000000000000111111111111111110000110010001111111100110000111"; //128
            String destinationAddr = "00000000000000000000000000000000000000000000000000000000000000000000000000000000111111111111111100010010110111010110011010110110"; //128
            //data
            for( int index = 1; index <= 12; ++index ){
                System.out.printf( "Data Length: %d\n", (int)Math.pow( 2, index) );
                byte[] data = new byte[(int)Math.pow( 2, index )];
                String payloadLength = Integer.toBinaryString( (int)Math.pow( 2, index ));
                while( payloadLength.length() != 16 ){
                    payloadLength = "0" + payloadLength;
                }
                String headerString = ( version + trafficClass + flowLabel + payloadLength + nextHeader + hopLimit + sourceAddr + destinationAddr );
                if( headerString.length() != ( 10 * 32 ) ){
                    throw new Exception( "Header is not predicted 40 bytes long" );
                }
                byte[] header = new byte[40];
                for( int jndex = 0; jndex < header.length; ++jndex ){
                    header[jndex] = (byte)Integer.parseInt( headerString.substring( jndex * 8, ( jndex + 1 ) * 8 ), 2 );
                }
                byte[] output = new byte[header.length + data.length];
                System.arraycopy(header, 0, output, 0, header.length);
                System.arraycopy(data, 0, output, header.length, data.length);
                //System.out.println( Arrays.toString( output ) );
                os.write( output );
                for( int jndex = 0; jndex < 4; ++jndex ){
                    System.out.printf( "%02X", is.read() );
                }
                System.out.printf("\n");
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}
