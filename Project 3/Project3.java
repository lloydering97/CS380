import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.nio.ByteBuffer;

public final class Project3 {

    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("18.221.102.182", 38003)) {
            Scanner in = new Scanner( System.in );
            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String version = "0100"; //4
            String hLen = "0101"; //4
            String tos = "00000000"; //8
            //length
            String ident = "0000000000000000"; //16
            String flags = "010"; //3
            String offset = "0000000000000"; //13
            String ttl = "00110010"; //8
            String protocal = "00000110"; //8
            //checksum
            String sourceAddr = "10000110010001111111100110000111"; //32
            String destinationAddr = "00010010110111010110011010110110"; //32
            //data
            for( int index = 1; index <= 12; ++index ){
                System.out.printf( "Data Length: %d\n", (int)Math.pow( 2, index) );
                byte[] data = new byte[(int)Math.pow( 2, index )];
                String length = Integer.toBinaryString( (int)Math.pow( 2, index ) + 20 );
                while( length.length() != 16 ){
                    length = "0" + length;
                }
                String emptyChecksum = "0000000000000000"; //16
                String headerString = ( version + hLen + tos + length + ident + flags + offset + ttl + protocal + emptyChecksum + sourceAddr + destinationAddr );
                if( headerString.length() != ( 5 * 32 ) ){
                    throw new Exception( "Header is not predicted 20 bytes long" );
                }
                byte[] header = new byte[20];
                for( int jndex = 0; jndex < 20; ++jndex ){
                    header[jndex] = (byte)Integer.parseInt( headerString.substring( jndex * 8, ( jndex + 1 ) * 8 ), 2 );
                }
                byte[] checksum = ByteBuffer.allocate( 2 ).putShort( checksum( header ) ).array();
                header[10] = checksum[0];
                header[11] = checksum[1];
                //Add header and data
                byte[] output = new byte[header.length + data.length];
                System.arraycopy(header, 0, output, 0, header.length);
                System.arraycopy(data, 0, output, header.length, data.length);
                os.write( output );
                System.out.println( "Response Get!: " + br.readLine() );
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    public static short checksum(byte[] bi)
    {
        int sum = 0;
        byte[] b;
        if( bi.length % 2 == 1 )
        {
            b = new byte[bi.length + 1];
            for( int i = 0; i < bi.length; ++i )
            {
                b[i] = bi[i];
            }
            b[b.length-1] = 0;
        }
        else
        {
            b = bi;
        }

        for( int index = 0; ( index + 1 ) < b.length; index += 2 )
        {
            int first = b[index];
            if( first < 0 )
                first = first ^ 0xFFFFFF00;
            int second = b[index + 1];
            if( second < 0 )
                second = second ^ 0xFFFFFF00;
            first = first << 8;
            sum += ( first ^ second );

            if( ( sum & 0xFFFF0000 ) != 0 )
            {
                sum = sum & 0xFFFF;
                sum++;
            }
        }
        short output = (short)~( sum & 0xFFFF );
        return output;
    }
}
