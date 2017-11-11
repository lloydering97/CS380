import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.nio.ByteBuffer;
import java.util.Random;
public final class Project5 {

    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("18.221.102.182", 38005)) {
            Scanner in = new Scanner( System.in );
            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            Random random = new Random();
            String version = "0100"; //4
            String hLen = "0101"; //4
            String tos = "00000000"; //8
            String handLength = "0000000000011000"; //16
            String ident = "0000000000000000"; //16
            String flags = "010"; //3
            String offset = "0000000000000"; //13
            String ttl = "00110010"; //8
            String protocol = "00010001"; //8
            String emptyChecksum = "0000000000000000"; //16
            String sourceAddr = "10000110010001111111100110000111"; //32
            String destinationAddr = "00010010110111010110011010110110"; //32
            String headerString = ( version + hLen + tos + handLength + ident + flags + offset + ttl + protocol + emptyChecksum + sourceAddr + destinationAddr );
            byte[] handHeader = new byte[20];
            for( int index = 0; index < 20; ++index ){
                handHeader[index] = (byte)Integer.parseInt( headerString.substring( index * 8, ( index + 1 ) * 8 ), 2 );
            }
            byte[] handshakeChecksum = ByteBuffer.allocate( 2 ).putShort( checksum( handHeader ) ).array();
            handHeader[10] = handshakeChecksum[0];
            handHeader[11] = handshakeChecksum[1];
            byte[] deadBeef = { (byte)0xDE, (byte)0xAD, (byte)0xBE, (byte)0xEF };
            byte[] output = new byte[handHeader.length + deadBeef.length];
            System.arraycopy(handHeader, 0, output, 0, handHeader.length);
            System.arraycopy(deadBeef, 0, output, handHeader.length, deadBeef.length);
            //Handshake here
            os.write( output );
            int response = is.read() << 24 | is.read() << 16 | is.read() << 8 | is.read();
            System.out.printf( "Handshake Response: %s\n" , String.format("0x%08X", response ) );
            if( response != 0xCAFEBABE ){
                throw new Exception( "IT'S NOT CAFEBABE!" );
            }
            int portNumber = is.read() << 8 | is.read();
            String destPort = String.format( "%16s", Integer.toBinaryString( portNumber ) ).replace( ' ', '0' );
            System.out.printf( "Port number received: %d\n\n", portNumber );

            String sourcePort = "0101010110101010"; //16

            for( int index = 1; index <= 12; ++index ){
                // Begin UDP Section
                System.out.printf( "Sending packet with %d bytes of data\n", (int)Math.pow( 2, index) );
                byte[] data = new byte[(int)Math.pow( 2, index )];
                random.nextBytes( data );
                String udpLength = String.format( "%16s", Integer.toBinaryString( (int)Math.pow( 2, index ) + 8 ) ).replace(' ', '0');
                String pseudoHeaderString = sourceAddr + destinationAddr + "00000000" + protocol + udpLength + sourcePort + destPort + udpLength + emptyChecksum;
                byte[] pseudoHeader = new byte[20 + data.length];
                for( int jndex = 0; jndex < 20; ++jndex ){
                    pseudoHeader[jndex] = (byte)Integer.parseInt( pseudoHeaderString.substring( jndex * 8, ( jndex + 1 ) * 8 ), 2 );
                }
                System.arraycopy( data, 0, pseudoHeader, 20, data.length );
                byte[] checksum = ByteBuffer.allocate( 2 ).putShort( checksum( pseudoHeader) ).array();
                String udpPacketString = sourcePort + destPort + udpLength;
                byte[] udpPacket = new byte[8 + data.length];
                for( int jndex = 0; jndex < 6; ++jndex ){
                    udpPacket[jndex] = (byte)Integer.parseInt( udpPacketString.substring( jndex * 8, ( jndex + 1 ) * 8 ), 2 );
                }
                System.arraycopy( checksum, 0, udpPacket, 6, checksum.length );
                System.arraycopy( data, 0, udpPacket, 8, data.length );
                //End udp section w/ udp packet being a full udp packet

                String length = String.format( "%16s", Integer.toBinaryString( 20 + udpPacket.length ) ).replace( ' ', '0' );
                headerString = ( version + hLen + tos + length + ident + flags + offset + ttl + protocol + emptyChecksum + sourceAddr + destinationAddr );
                if( headerString.length() != ( 5 * 32 ) ){
                    throw new Exception( "Header is not predicted 20 bytes long" );
                }
                byte[] header = new byte[20];
                for( int jndex = 0; jndex < 20; ++jndex ){
                    header[jndex] = (byte)Integer.parseInt( headerString.substring( jndex * 8, ( jndex + 1 ) * 8 ), 2 );
                }
                checksum = ByteBuffer.allocate( 2 ).putShort( checksum( header ) ).array();
                header[10] = checksum[0];
                header[11] = checksum[1];
                //Add header and data
                output = new byte[header.length + udpPacket.length];
                System.arraycopy(header, 0, output, 0, header.length);
                System.arraycopy(udpPacket, 0, output, header.length, udpPacket.length);

                os.write( output );
                long beginTime = System.currentTimeMillis();
                System.out.print( "Response: 0x" );
                for( int jndex = 0; jndex < 4; ++jndex ){
                    System.out.printf( "%02X", is.read() );
                }
                System.out.printf( "\nRTT: %dms\n\n", System.currentTimeMillis() - beginTime );
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
