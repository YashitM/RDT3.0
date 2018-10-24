import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class Constants {
    private static int port = 12349;
    private static int windowSize = 5;
    private static int timeout = 500; //ms
    private static int maxPacketCount = 1000;
    private static int upperLayerBufferSize = 20;
    private static String baseMessage = "hi";
    
    public static int getPort() {
        return port;
    }
    
    public static int getWindowSize() {
        return windowSize;
    }
    
    public static int getTimeout() {
        return timeout;
    }
    
    public static int getMaxPacketCount() {
        return maxPacketCount;
    }
    
    public static String getBaseMessage() {
        return baseMessage;
    }
    
    public static int getUpperLayerBufferSize() {
        return upperLayerBufferSize;
    }

    public static Datagram byteArrayToDatagram(byte[] datagramArray) {
        
        ByteArrayInputStream bis = new ByteArrayInputStream(datagramArray);
        ObjectInput in = null;
        Datagram datagram = null;
        try {
            in = new ObjectInputStream(bis);
            Object object = in.readObject();
            datagram = (Datagram) object;
            if (in != null) {
                in.close();
            }
        } catch (Exception e) {}

        return datagram;
    }

    public static byte[] datagramToByteArray(Datagram datagram) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutput out = null;
        byte[] byteArray = null;

        try {
            out = new ObjectOutputStream(outputStream);   
            out.writeObject(datagram);
            out.flush();
            byteArray = outputStream.toByteArray();
            outputStream.close();
        } catch (Exception ex) {}
        
        return byteArray;

    }

    public static String convertByteArrayToString(byte[] byteArray)
    {
        String byteString = "";

        for (byte character : byteArray)
        {
            if (character == 0) {
                break;
            }
            byteString += ((char) character);
        }

        return byteString;
    }
}