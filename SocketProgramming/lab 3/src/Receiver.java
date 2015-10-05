import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Receiver {
    HashSet<Byte> outstanding = new HashSet();
    byte winSz;
    byte maxSeq;
    byte base;
    byte curPack;
    DatagramSocket receiverSocket;
    public Receiver() {
        try {
            this.receiverSocket = new DatagramSocket(9876);
        } catch (SocketException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void printWindow(){
        System.out.print("[");
        for(byte j = base;j < (base+ winSz); j++ ){
            if(j < maxSeq){
                System.out.print(j);
                if(outstanding.contains(j)){
                    System.out.print("#");
                }
            }else{
                System.out.print("-");
            }   
            if(j < base + winSz - 1 ){
                System.out.print(",");
            }
        }
        System.out.print("]");
    }
    public static void main(String[] args) {
            Receiver receive = new Receiver();
            Scanner scan = new Scanner(System.in);
            System.out.print("Enter the window’s size on the sender: ");
            receive.winSz = scan.nextByte();
            System.out.println();
            System.out.print("Enter the maximum sequence number on the sender: ");
            receive.maxSeq = scan.nextByte();
            System.out.println();
            receive.base = 0;
            for(byte i =0;i <receive.maxSeq;i++){
                byte[] rec = receive.recieveSetup();
                if(rec[0]!=receive.base)
                    receive.outstanding.add(rec[0]);
                else{
                    receive.base++;
                    while(receive.outstanding.contains(receive.base)){
                        receive.base++;}
                }   
                System.out.print("Packet " + rec[0] + " is received,send Ack"+ rec[0] +", window " );
                receive.printWindow();
                System.out.println();
                receive.sendAck();
                
            }
            receive.receiverSocket.close();
      
    }

    
    static class Ack {
        public byte ack;
    }
    
    public void sendData(byte[] sendData, DatagramSocket senderSocket) {
        try {
            
            InetAddress IPAddress = InetAddress.getByName("localhost");
            DatagramPacket sendPkt = new DatagramPacket(sendData, sendData.length, IPAddress, 9874);
            senderSocket.send(sendPkt);
        } catch (SocketException ex) {
            System.out.println("Opening socket 9874 failed.");
        } catch (UnknownHostException ex) {
            System.out.println("Hostname does not exist.");
        } catch (IOException ex) {
            System.out.println("Data transfer failed.");
        }
    }

    public byte[] getData(byte[] rcvData) {
        try {
            DatagramPacket rcvPkt = new DatagramPacket(rcvData, rcvData.length);
            receiverSocket.receive(rcvPkt);
            InetAddress IPAddress = rcvPkt.getAddress();
            int port = rcvPkt.getPort();
        } catch (SocketException ex) {
            System.out.println("Opening socket 9876 failed.");
        } catch (IOException ex) {
            System.out.println("Data transfer failed.");
        }
        return rcvData;
    }
    
    public byte[] recieveSetup() {
        byte[] rcvData = new byte[1];
        getData(rcvData);
        curPack = rcvData[0];
        return rcvData;
    }
    
    public void sendAck() {
        sendData(new byte[]{curPack},receiverSocket);
    }

    public void confirmSetup(DatagramSocket receiverSocket) {
        byte[] rcvData = new byte[10];
        getData(rcvData);
    }
}

    

