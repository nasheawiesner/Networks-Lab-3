
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.HashSet;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author newfrontier2
 */
public class Sender {

    HashSet<Byte> outstanding = new HashSet();
    HashSet<Byte> completed = new HashSet();
    HashSet<Byte> drop = new HashSet();
    byte winSz;
    byte maxSeq;
    byte base;
    DatagramSocket senderSocket;
    Timer packet;
    TimerTask timerTask = new TimerTask() 
    {
         @Override
         public void run() 
        {
          new Thread(timerTask).start(); //do something
         }
     } ;

    public Sender() {
        try {
            this.senderSocket = new DatagramSocket(9874);
        } catch (SocketException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void printWindow(){
        System.out.print("[");
        for(byte j = base;j < (base+ winSz); j++ ){
            if(j < maxSeq){
                System.out.print(j);
                if(outstanding.contains(j)){
                    System.out.print("*");
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
    
        public void sendSetup() {
        sendData(new byte[]{(byte)base, (byte)winSz});
    }

    public void ackPacket() {
        byte[] rcvData = new byte[1];
        getData(rcvData);      
        if (rcvData[0] == base) {
            base++;
            while(completed.contains(base)){
                base++;
            }
        } else {
            outstanding.remove(rcvData[0]);
            completed.add(rcvData[0]);
        }
        System.out.print("Ack" + rcvData[0] + " is received, window");
        printWindow();
        System.out.println();
    }
    
    public void ackSetup(){
        byte[] rcvData = new byte[1];
        getData(rcvData);
        System.out.println("Receive confirmation from the receiver");
    }

    public void sendData(byte[] sendData) {
        try {
            
            InetAddress IPAddress = InetAddress.getByName("localhost");
            // byte[] sendData = new byte[1024];
            DatagramPacket sendPkt = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
            senderSocket.send(sendPkt);
            //senderSocket.close();
        } catch (SocketException ex) {
            System.out.println("Opening socket 9876 failed.");
        } catch (UnknownHostException ex) {
            System.out.println("Hostname does not exist.");
        } catch (IOException ex) {
            System.out.println("Data transfer failed.");
        }
    }

    public byte[] getData(byte[] rcvData) {
        try {
            
            // byte[] rcvData = new byte[1024];
            DatagramPacket rcvPkt = new DatagramPacket(rcvData, rcvData.length);
            senderSocket.receive(rcvPkt);
            InetAddress IPAddress = rcvPkt.getAddress();
            int port = rcvPkt.getPort();
            //receiverSocket.close();
        } catch (SocketException ex) {
            System.out.println("Opening socket 9874 failed.");
        } catch (IOException ex) {
            System.out.println("Data transfer failed.");
        }
        return rcvData;
    }
    public static void main(String[] args) {
            Sender send = new Sender();
            Scanner scan = new Scanner(System.in);
            System.out.print("Enter the window’s size on the sender: ");
            send.winSz = scan.nextByte();
            System.out.println();
            System.out.print("Enter the maximum sequence number on the sender: ");
            send.maxSeq = scan.nextByte();
            System.out.println();
            System.out.print("Select the packet(s) that will be dropped: ");
            Arrays.asList(scan.next().split(",")).forEach((packetToDrop) -> send.drop.add(Byte.parseByte(packetToDrop)));
            System.out.println();
            send.base = 0;
            boolean print = true; 
            send.sendData(new byte[] {send.winSz,send.maxSeq});
            System.out.println("Send window's size and maximum seq.number to the receiver");
            send.ackSetup();
            for(byte i =0;i <send.maxSeq;i++){//run till all packets are sent
              
                for(byte j = send.base; j<send.base + send.winSz;j++){//send all unsent packets in window
                     if(j<send.maxSeq){
                         if(send.outstanding.contains(j)){
                           //do nothing
                         }else if (!send.outstanding.contains(j) && !send.completed.contains(j)){ //packet has not been sent yet
                              if(send.drop.contains(j)){ //packet is to be dropped
                                  if(print){
                                    send.outstanding.add(j);  
                                    System.out.print("Packet " + j + " is sent, window");
                                    send.printWindow();
                                    System.out.println();
                                    print = false;
                                  }
                              }else{ //send packet
                                send.sendData(new byte[]{j});
                                send.outstanding.add(j);
                                send.packet = new Timer(Byte.toString(j));
                                send.packet.schedule(send.timerTask, 120000);//start timer
                                System.out.print("Packet " + j + " is sent, window");
                                send.printWindow();
                                System.out.println();
                              }
                        }
                     }    
                        
                }    
                
                send.ackPacket();
               send.timerTask.cancel();
                //stop timer
                
            }

        }
        
    }
    

