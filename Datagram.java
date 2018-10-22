import java.io.Serializable;

public class Datagram implements Serializable{

    private static final long serialVersionUID = 6969L;

    private String message;
    private int sequenceNumber;
    private boolean sent;
    private int packetNumber;
    private boolean acknowledged;

    public Datagram() {}
    
    public Datagram(String message, int sequenceNumber, int packetNumber) {
        this.message = message;
        this.sequenceNumber = sequenceNumber;
        this.packetNumber = packetNumber;
        this.sent = false;
        this.acknowledged = false;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getPacketNumber() {
        return this.packetNumber;
    }

    public void setPacketNumber(int packetNumber) {
        this.packetNumber = packetNumber;
    }

    public int getSequenceNumber() {
        return this.sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public boolean isSent() {
        return this.sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public boolean isAcknowledged() {
        return this.acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    @Override
    public String toString() {
        return "{" +
            " message='" + getMessage() + "'" +
            ", sequenceNumber='" + getSequenceNumber() + "'" +
            // ", sent='" + isSent() + "'" +
            ", packetNumber='" + getPacketNumber() + "'" +
            "}";
    }

    // FUNCTIONS

    public void computeChecksum() {
        
    }
}