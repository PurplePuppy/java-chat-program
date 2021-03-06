/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.system.server;

import chat.system.objects.ChatMessage;
import chat.system.objects.ChatPerson;
import chat.system.objects.ServerMessage;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A server's connection to a client. Runs in background. Listens for client's messages
 *
 * @author jon
 */
public class ClientConnection extends Observable implements Runnable, Observer {

    private ObjectInputStream oIn;
    private ObjectOutputStream oOut;
    private ChatPerson name;

    /**
     * 
     * @param s The socket connected to the client
     * @throws IOException
     * @throws ClassNotFoundException
     */
    ClientConnection(Socket s) throws IOException, ClassNotFoundException {
        //System.out.println("client connection");
        oIn = new ObjectInputStream(s.getInputStream());
        oOut = new ObjectOutputStream(s.getOutputStream());
        while (name == null) {
            Object o = oIn.readObject();
            System.out.println(o);
            if (o instanceof ChatPerson) {
                name = (ChatPerson) o;
            }
        }
        System.out.println("Client initialized");
    }

    @Override
    public void run() {
        while (true) {
            System.gc();
            //System.out.println("run");
            try {
                this.setChanged();
                this.notifyObservers(oIn.readObject());
            } catch (EOFException | SocketException ex) {
                break;
            } catch (IOException ex) {
                Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            System.out.println("client disconnected");
            oIn.close();
            oOut.close();
            this.setChanged();
            this.notifyObservers(new ServerMessage(1, name));
            this.deleteObservers();
        } catch (IOException ex) {
            Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void update(Observable o, Object arg) {
        //System.out.println("update");
        if (o instanceof Server) {
            if (arg instanceof ChatMessage || arg instanceof ChatPerson) {
                try {
                    oOut.writeObject(arg);
                } catch (IOException ex) {
                    Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (arg instanceof ServerMessage) {
                if (((ServerMessage) arg).getServerCode() == 1) {
                    try {
                        oOut.writeObject(arg);
                    } catch (IOException ex) {
                        Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    /**
     * Gets the ChatPerson associated with this connection
     *
     * @return
     */
    public ChatPerson getName() {
        //System.out.println("get name");
        return name;
    }
}
