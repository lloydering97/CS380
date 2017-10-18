import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class Ex3Client
{
    public static void main( String[] args ) throws Exception
    {
        try
        {
            System.out.println( "Connected to server" );
            Socket socket = new Socket ( "18.221.102.182" , 38103 );
            InputStream is = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            int messageLength = (int)is.read();
            System.out.printf( "Reading %d bytes\n", messageLength );
            byte[] message = new byte[messageLength];
            for( int index = 0; index < messageLength; ++index )
            {
                message[index] = (byte)is.read();
            }

            System.out.print( "Data Received:" );
            for( int index = 0; index < message.length; ++index )
            {
                if( index % 10 == 0 )
                {
                    System.out.print( "\n\t" );
                }
                System.out.printf( "%02X", message[index] );
            }
            short checkSum = checksum( message );
            System.out.printf( "\nChecksum Calculated: 0x%04X\n", checkSum );

            ByteBuffer output = ByteBuffer.allocate( 2 );
            output.putShort( checkSum );

            byte[] outArray = output.array();

            out.write( outArray );

            if( is.read() == 1 )
            {
                System.out.println( "Response Good!" );
            }
            else
            {
                System.out.println( "Response Bad" );
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
