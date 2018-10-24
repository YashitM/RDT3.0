import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;

public class Client {

    public static boolean checkAcks(Datagram[] datagrams) {
        for (int datagramNumber = 0; datagramNumber < datagrams.length; datagramNumber ++ ) {
            if (!datagrams[datagramNumber].isAcknowledged()) {
                return false;
            } 
        }
        return true;
    }

    // CHECK ALL DATAGRAMS BEFORE THE CURRENT ONE (FOR WHICH ACK IS RECEIVED), TO MAKE SURE ALL OF THEM HAVE ALREADY BEEN ACKED.
    // THIS IS DONE SO AS TO SHIFT THE WINDOW ON ORDERLY RECEIPT OF ACKKNOWLEDGEMENTS FROM THE SERVER.
    // EX: IF 1, 2, 4 ACKS ARE RECEIVED, THIS FUNCTION WILL CHANGE THE WINDOW POSITION TO += 2 AS 1 AND 2 ARE IN ORDER, WHILE
    // SINCE 3 ISN'T ACKED, 4 WILL NOT BE ACCEPTED.
    public static boolean checkAckOrdering(Datagram[] datagrams, int position) {

        for (int datagramNumber = 0; datagramNumber < datagrams[position].getPacketNumber(); datagramNumber++ ) {
            if (!datagrams[datagramNumber].isAcknowledged()) {
                return false;
            }
        }

        return true;
    }

    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.out.println("Usage: java Client <mode: udp/rdt>");
            return;
        }

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        DatagramSocket datagramSocket = new DatagramSocket();

        // System.out.print("Enter the Server IP Address: ");
        // String clientIpAddressString = input.readLine();

        InetAddress inetAddress = InetAddress.getByName("0.0.0.0");

        Datagram[] datagrams = new Datagram[Constants.getMaxPacketCount()];
        int counter = 0;
        int windowPosition = 0;

        for (int i=0; i<Constants.getMaxPacketCount(); i++) {
            Datagram datagram = new Datagram(Constants.getBaseMessage(), i%(Constants.getWindowSize()*2), i);
            datagrams[i] = datagram;
        }

        if (args[0].equals("udp")) {
            for (int datagramNumber=0; datagramNumber < datagrams.length; datagramNumber++) {
                    
                System.out.println("Packet: " + String.valueOf(datagrams[datagramNumber].getPacketNumber()));

                byte[] datagramBytes = Constants.datagramToByteArray(datagrams[datagramNumber]);
                DatagramPacket datagramPacketSend =  new DatagramPacket(datagramBytes, datagramBytes.length, inetAddress, Constants.getPort());
                
                datagramSocket.send(datagramPacketSend);
                datagrams[datagramNumber].setSent(true);
            }            

            System.out.println("Sent " + String.valueOf(datagrams.length) + " Packets using UDP.");
        }
        else if (args[0].equals("rdt")) {

            int receiverBufferFlowControl = -1;            
            // while ((windowPosition + Constants.getWindowSize()) <= datagrams.length)
            while (!checkAcks(datagrams))
            {
                int startWindowPosition = windowPosition;
                
                System.out.println("Sending Window #" + String.valueOf(windowPosition));
                
                if (windowPosition + Constants.getWindowSize() < datagrams.length) {
                    for (int datagramNumber = windowPosition; datagramNumber < (windowPosition + Constants.getWindowSize()); datagramNumber ++ ) {
                        System.out.println("\t Sending Packet #" + String.valueOf(datagrams[datagramNumber].getPacketNumber()));
                        
                        byte[] datagramBytes = Constants.datagramToByteArray(datagrams[datagramNumber]);
                        DatagramPacket datagramPacketSend =  new DatagramPacket(datagramBytes, datagramBytes.length, inetAddress, Constants.getPort());
                        
                        datagramSocket.send(datagramPacketSend);
                        datagrams[datagramNumber].setSent(true);
                        datagrams[datagramNumber].setAcknowledged(false);
                    }
                } else {
                    for (int datagramNumber = windowPosition; datagramNumber < datagrams.length; datagramNumber ++ ) {
                        System.out.println("\t Sending Packet #" + String.valueOf(datagrams[datagramNumber].getPacketNumber()));
                        
                        byte[] datagramBytes = Constants.datagramToByteArray(datagrams[datagramNumber]);
                        DatagramPacket datagramPacketSend =  new DatagramPacket(datagramBytes, datagramBytes.length, inetAddress, Constants.getPort());
                        
                        datagramSocket.send(datagramPacketSend);
                        datagrams[datagramNumber].setSent(true);
                        datagrams[datagramNumber].setAcknowledged(false);
                    }
                }
                
                byte[] acknowledgementData = new byte[1024];
                
                datagramSocket.setSoTimeout(Constants.getTimeout());
                
                ArrayList<Integer> ackList = new ArrayList<>();
                
                while (true) {
                    try {
                        DatagramPacket datagramPacketReceive = new DatagramPacket(acknowledgementData, acknowledgementData.length);
                        datagramSocket.receive(datagramPacketReceive);
                        
                        String ackString = Constants.convertByteArrayToString(acknowledgementData).toString();
                        
                        if (ackString != null) {
                            if (ackString.contains("ACK")) {
                                String[] ackResponse = ackString.split(";;");
                                int seqNumber = Integer.parseInt(ackResponse[1]);
                                receiverBufferFlowControl = Integer.parseInt(ackResponse[2]);
                                int expectedSeqNumber = datagrams[windowPosition].getSequenceNumber();
                                // System.out.println(startWindowPosition);
                                if (expectedSeqNumber == seqNumber) {
                                    ackList.add(seqNumber);
                                    datagrams[windowPosition].setAcknowledged(true);
                                    System.out.println(Arrays.toString(ackResponse)
                                        + ": Acknowledgement for Packet: "
                                        + String.valueOf(windowPosition));
                                    windowPosition += 1;
                                }

                                if (windowPosition == datagrams.length) {
                                    break;
                                }
                            }
                        }

                        if (ackList.size() == Constants.getWindowSize()) {
                            // System.out.println("WINDOW SIZE FULL");
                            break;
                        }
                    }
                    catch (SocketTimeoutException e) {
                        System.out.println("TIMEOUT: " + e.getMessage());
                        break;
                    }
                }

                // if (ackList.size() == Constants.getWindowSize() && checkAcks(windowPosition, datagrams)) {
                if (windowPosition == startWindowPosition + Constants.getWindowSize()) {
                    ackList.clear();
                } else if (checkAcks(datagrams)) {
                    break;
                } else {
                    System.out.println("Retransmitting Window at Position: " + String.valueOf(windowPosition));
                    ackList.clear();
                }

            }
            System.out.println("Sent " + String.valueOf(datagrams.length) + " Packets using UDP+RDT.");
        }

        
        datagramSocket.close();
    }
}
