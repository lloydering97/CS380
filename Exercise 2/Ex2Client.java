import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.zip.CRC32;
import java.nio.ByteBuffer;

public class Ex2Client
{
    public static void main( String[] args ) throws Exception
    {
        try
        {
            Socket socket = new Socket ( "18.221.102.182" , 38102 );
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader in = new BufferedReader(isr);
            OutputStream os = socket.getOutputStream();
            PrintStream out = new PrintStream( os, true, "UTF-8" );

            byte[] input = new byte[100];
            for( int index = 0; index < input.length; ++index )
            {
                byte inputFirst = (byte)in.read();
                byte inputSecond = (byte)in.read();
                input[index] = (byte)( inputFirst << 4 | inputSecond );
            }

            System.out.print( "Received bytes:" );

            for( int index = 0; index < input.length; ++index )
            {
                if( index % 10 == 0 )
                {
                    System.out.print( "\n\t" );
                }
                System.out.printf( "%02X", input[index] );
            }

            CRC32 crc = new CRC32();
            crc.update( input );
            int res = (int)crc.getValue();
            System.out.printf( "\nGenerated CRC32: %08X\n", res );

            ByteBuffer output = ByteBuffer.allocate( 4 );
            output.putInt( res );

            byte[] outArray = output.array();

            out.write( outArray );

            if( in.read() == 1 )
            {
                System.out.println( "Success!" );
            }
            else
            {
                System.out.println( "Failure" );
            }
        }
        catch( Exception e )
        {
            System.out.println( "Critical Failure" );
            e.printStackTrace();
        }
    }
}
