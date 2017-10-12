import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.zip.CRC32;
import java.nio.ByteBuffer;
import java.util.*;

public class Project2
{
    public static void main( String[] args ) throws Exception
    {
        try
        {
            int[] preamble = new int[64];
            int[] message = new int[320];
            Socket socket = null;
            boolean valid = true;
            InputStream is;
            do
            {
                socket = new Socket ( "18.221.102.182" , 38002 );
                is = socket.getInputStream();
                for( int index = 0; index < preamble.length; ++index )
                {
                    preamble[index] = (int)is.read();
                }
                for( int index = 0; index < message.length; ++index )
                {
                    message[index] = (int)is.read();
                }

                for( int value: preamble )
                {
                    if( value > 255 )
                    {
                        valid = false;
                        break;
                    }
                    valid = true;
                }
                for( int value: message )
                {
                    if( value > 255 )
                    {
                        valid = false;
                        break;
                    }
                    valid = true;
                }

                if( !valid )
                {
                    socket.close();
                }
            }
            while( !valid );

            double baseLine;
            int averageSum = 0;
            for( int value: preamble )
            {
                averageSum += value;
            }
            baseLine = (double)averageSum/64;
            System.out.printf("Baseline established from preamble: %f\n", baseLine );

            String encodedMessage = "";
            for( int value: message )
            {
                if( value > baseLine )
                {
                    encodedMessage += "1";
                }
                else if( value < baseLine )
                {
                    encodedMessage += "0";
                }
                else
                {
                    throw new Exception( "Ambiguous signal in message" );
                }
            }
            //System.out.printf( "\nEncoded Message: %s\n", encodedMessage );

            String decodedMessage = decodeNRZI( encodedMessage );
            //System.out.printf( "Decoded Message: %s\n", decodedMessage );

            String fourBMessage = "";
            for( int index = 0; index < decodedMessage.length(); index += 5 )
            {
                fourBMessage += convertFiveToFour( decodedMessage.substring( index, index + 5 ) );
            }
            //System.out.printf( "4B Message: %s\n", fourBMessage );

            int[] output = new int[32];
            for( int index = 0, jdex = 0; index < fourBMessage.length(); index += 8, ++jdex )
            {
                output[jdex] = (Integer.parseInt( fourBMessage.substring( index, index + 4 ), 2 ) << 4 | Integer.parseInt( fourBMessage.substring( index + 4, index + 8 ), 2 ) );
            }

            System.out.printf("Received 32 bytes: ");

            byte[] outputBytes = new byte[32];

            for( int index = 0; index < outputBytes.length; ++index )
            {
                outputBytes[index] = (byte)output[index];
                System.out.printf( "%02X ", outputBytes[index] );
            }

            OutputStream os = socket.getOutputStream();
            os.write( outputBytes );
            int response = (int)is.read();
            if( response == 1 )
            {
                System.out.println( "\nResponse Good!" );
            }
            else
            {
                System.out.println( "\nResponse Bad." );
            }
            socket.close();
            System.out.println( "Disconnected from server" );
        }
        catch( Exception e )
        {
            System.out.println( "Critical Failure" );
            e.printStackTrace();
        }
    }

    public static String convertFiveToFour( String input ) throws Exception
    {
        if( input.equals( "11110" ) )
            return "0000";
        else if( input.equals( "01001" ) )
            return "0001";
        else if( input.equals( "10100" ) )
            return "0010";
        else if( input.equals( "10101" ) )
            return "0011";
        else if( input.equals( "01010" ) )
            return "0100";
        else if( input.equals( "01011" ) )
            return "0101";
        else if( input.equals( "01110" ) )
            return "0110";
        else if( input.equals( "01111" ) )
            return "0111";
        else if( input.equals( "10010" ) )
            return "1000";
        else if( input.equals( "10011" ) )
            return "1001";
        else if( input.equals( "10110" ) )
            return "1010";
        else if( input.equals( "10111" ) )
            return "1011";
        else if( input.equals( "11010" ) )
            return "1100";
        else if( input.equals( "11011" ) )
            return "1101";
        else if( input.equals( "11100" ) )
            return "1110";
        else if( input.equals( "11101" ) )
            return "1111";
        else
            throw new Exception("Unknown 5B binary parsed: " + input);
    }

    public static String decodeNRZI( String input )
    {
        char previousBit = '0';
        String output = "";
        for( char currentBit : input.toCharArray() )
        {
            if( currentBit == previousBit )
            {
                output += "0";
            }
            else
            {
                output += "1";
            }
            previousBit = currentBit;
        }
        return output;
    }
}

