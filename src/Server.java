import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class Server {
    private ServerSocketChannel serverSocket;
    //port hardCodat
    private int port = 14449;
    Selector sel;

    public Server() {

        try {
            this.serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(false);
            serverSocket.bind(new InetSocketAddress("0.0.0.0", this.port));
            this.sel = Selector.open();
            // the selector agrees to listen on serverSocket for any incoming message
            // we will interact with multiple sockets. Please, remember that this is the only socket
            // that we have so far. Its single purpose is to accept new clients (new connections)
            serverSocket.register(sel, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {

        while (true) {
            try {
                sel.select();
                Set<SelectionKey> selectedKeys = sel.selectedKeys();

                for (SelectionKey key : selectedKeys) {
                    if (key.isAcceptable()) {
                        // new client is trying to connect
                        // a fresh new socket is created when accepting the client
                        SocketChannel clientSocket = serverSocket.accept();
                        // non blocking communication
                        clientSocket.configureBlocking(false);
                        // the selector agrees to listen on serverSocket for any incoming message
                        // please, notice that this is the place where new sockets are being created
                        // their purpose is to receive messages from the clients
                        clientSocket.register(sel, SelectionKey.OP_READ);
                    }
                    if (key.isReadable()) {
                        // an existing client is sending a message
                        // the place where the incoming message is stored
                        ByteBuffer buffer = ByteBuffer.allocate(256);
                        SocketChannel client = (SocketChannel) key.channel();

                        try {
                            client.read(buffer);
                        } catch (IOException e) {
                            key.cancel();
                        }
                        String message = new String(buffer.array());

                        Set<SelectionKey> allKeys = sel.keys();

                        for(SelectionKey key1:allKeys)
                        {
                            if(key1.isValid() && key1.isAcceptable() == false) {
                                System.out.println(key1);
                                SocketChannel client1 = (SocketChannel) key1.channel();
                                client1.write(ByteBuffer.wrap(message.getBytes()));
                            }
                        }

                    }
                }
                selectedKeys.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

}
