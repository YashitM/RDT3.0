import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

public class Server
{

    private static ArrayList<Datagram> upperLayerBuffer = new ArrayList<>();

    public static String checkOrder(int lastReceived, int incomingSeqNumber) {        

        if (lastReceived == -1) {
            if (incomingSeqNumber == 0) {
                return "Ordered";
            } else {
                return "Not Ordered";
            }
        } else if (lastReceived == (incomingSeqNumber - 1)) {
            return "Ordered";
        } else if (lastReceived == )
        
        } else {
            return "Not Ordered";
        }
        // } else if (lastReceived == Constants.getWindowSize() - 1) {
        //     if (incomingSeqNumber == 0) {
        //         // RETRANSMISSION
        //         return "Duplicate";
        //     } else if (incomingSeqNumber == Constants.getWindowSize()) {
        //         // WINDOW RECEIVED CORRECTLY, NEXT ONE BEING TRANSMITTED
        //         return "Ordered";
        //     } else {
        //         return "Not Ordered";
        //     }
        // } else if (lastReceived == (2*Constants.getWindowSize() - 1)) {
        //     if (incomingSeqNumber == Constants.getWindowSize()) {
        //         // RETRANSMISSION
        //         return "Duplicate";
        //     } else if (incomingSeqNumber == 0) {
        //         // WINDOW RECEIVED CORRECTLY, NEXT ONE BEING TRANSMITTED
        //         return "Ordered";
        //     } else {
        //         return "Not Ordered";
        //     }
    }

    public static void main(String[] args) throws IOException
    {
        if (args.length != 1) {
            System.out.println("Usage: java Server <mode: udp/rdt>");
            return;
        }

        DatagramSocket datagramSocket = new DatagramSocket(Constants.getPort());
        InetAddress inetAddress = InetAddress.getLocalHost();

        byte[] receive = new byte[65535];

        if (args[0].equals("udp")) {

            int packetCounter = 0;

            while (true) {
                DatagramPacket datagramPacketReceive = new DatagramPacket(receive, receive.length);
                datagramSocket.receive(datagramPacketReceive);

                Datagram datagram = Constants.byteArrayToDatagram(receive);
                System.out.println(datagram);
                packetCounter += 1;
                System.out.println("Received " + packetCounter + " packets from Client.");
            }
        } else if (args[0].equals("rdt")) {
            
            int lastReceived = -1;
            int counter = 0;
            
            int duplicateCounter = 0;

            while (true)
            {
                DatagramPacket datagramPacketReceive = new DatagramPacket(receive, receive.length);
                datagramSocket.receive(datagramPacketReceive);
                
                Datagram datagram = Constants.byteArrayToDatagram(receive);

                String ackMessage = "ACK;;" + String.valueOf(datagram.getSequenceNumber());

                DatagramPacket datagramPacketSend = new DatagramPacket(ackMessage.getBytes(), ackMessage.getBytes().length, datagramPacketReceive.getAddress(), datagramPacketReceive.getPort());

                String result = checkOrder(lastReceived, datagram.getSequenceNumber());

                if (result.equals("Duplicate") || duplicateCounter != 0) {

                    if (result.equals("Duplicate")) {
                        duplicateCounter = Constants.getWindowSize();
                    }

                    lastReceived = datagram.getSequenceNumber();
                    datagramSocket.send(datagramPacketSend);
                    receive = new byte[65535];
                    System.out.println("[DUPLICATE] #" + String.valueOf(datagram.getPacketNumber()) + ": " + datagram);
                    
                    if (duplicateCounter >= 1)
                        duplicateCounter--;

                    if (datagram.getPacketNumber() == Constants.getMaxPacketCount() - 1 ) {
                        System.out.println("Transfer Complete.");
                        System.out.println(counter);
                        break;
                    }
                } else if (result.equals("Ordered")) {
                    counter += 1;

                    lastReceived = datagram.getSequenceNumber();
                    datagramSocket.send(datagramPacketSend);
                    receive = new byte[65535];
                    System.out.println("#" + String.valueOf(datagram.getPacketNumber()) + ": " + datagram);

                    // ADDING DATAGRAM TO RECEIVER BUFFER TO SEND TO THE NEXT LAYER
                    
                    upperLayerBuffer.add(datagram);
    
                    if (upperLayerBuffer.size() == Constants.getUpperLayerBufferSize()) {
                        upperLayerBuffer.clear();
                        System.out.println("Receiver Buffer Full. Sending data to Upper Layer and clearing buffer.");
                    }

                    if (datagram.getPacketNumber() == Constants.getMaxPacketCount() - 1 ) {
                        System.out.println("Transfer Complete.");
                        System.out.println(counter);
                        break;
                    }
                } else if (result.equals("Not Ordered")) {
                    System.out.println("[UNORDERED] #" + String.valueOf(datagram.getPacketNumber()) + ": " + datagram);
                }
            }
        }
        datagramSocket.close();
    }
}
