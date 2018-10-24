import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;

public class Server
{

    private static ArrayList<Datagram> upperLayerBuffer = new ArrayList<>();
    
    public static String checkOrder(int lastReceived, int incomingSeqNumber, int packetNumber) {
        
        for (int i=0; i<Constants.getMaxPacketCount(); i++) {
            if(totalPacketBuffer[i] != null) {
                if (totalPacketBuffer[i].getSequenceNumber() == incomingSeqNumber && totalPacketBuffer[i].getPacketNumber() == packetNumber) {
                    return "Duplicate";
                }
            }
        }
        
        if (lastReceived == -1 || lastReceived == 2*Constants.getWindowSize() - 1) {
            if (incomingSeqNumber == 0) {
                return "Ordered";
            } else {
                return "Not Ordered";
            }
        } else if (lastReceived == (incomingSeqNumber - 1)) {
            return "Ordered";
        } else {
            return "Not Ordered";
        }
    }
    
    private static Datagram[] totalPacketBuffer = new Datagram[Constants.getMaxPacketCount()];
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

            boolean[] udpBuffer = new boolean[Constants.getMaxPacketCount()];
            int packetCounter = 0;
            datagramSocket.setSoTimeout(10000);
            while (true) {
                try {
                    DatagramPacket datagramPacketReceive = new DatagramPacket(receive, receive.length);
                    datagramSocket.receive(datagramPacketReceive);
    
                    Datagram datagram = Constants.byteArrayToDatagram(receive);
                    System.out.println(datagram);
    
                    udpBuffer[datagram.getPacketNumber()] = true;
                } catch (SocketTimeoutException e) {
                    break;
                }
            }

            
            System.out.print("Packets not received: ");

            for (int i=0; i<Constants.getMaxPacketCount(); i++) {
                if (!udpBuffer[i]) {
                    packetCounter += 1;
                    System.out.print(i + ", ");
                }
            }
            System.out.println("Total Received Packets: " + String.valueOf(Constants.getMaxPacketCount() - packetCounter));

        } else if (args[0].equals("rdt")) {
            
            int lastReceived = -1;
            int counter = 0;

            while (true)
            {
                DatagramPacket datagramPacketReceive = new DatagramPacket(receive, receive.length);
                datagramSocket.receive(datagramPacketReceive);
                
                Datagram datagram = Constants.byteArrayToDatagram(receive);

                String ackMessage = "ACK;;" + String.valueOf(datagram.getSequenceNumber()) + ";;" + String.valueOf(Constants.getUpperLayerBufferSize() - upperLayerBuffer.size());

                DatagramPacket datagramPacketSend = new DatagramPacket(ackMessage.getBytes(), ackMessage.getBytes().length, datagramPacketReceive.getAddress(), datagramPacketReceive.getPort());

                String result = checkOrder(lastReceived, datagram.getSequenceNumber(), datagram.getPacketNumber());

                if (result.equals("Duplicate")) {

                    lastReceived = datagram.getSequenceNumber();
                    datagramSocket.send(datagramPacketSend);
                    receive = new byte[65535];
                    System.out.println("[DUPLICATE] #" + String.valueOf(datagram.getPacketNumber()) + ": " + datagram);

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
                    totalPacketBuffer[datagram.getPacketNumber()] = datagram;
    
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
