import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.Executors;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.concurrent.ExecutorService;

public class Server {
    private static final String HANDSHAKE_SECRET = "handshake";
    private static Thread mainThread;

    public static void main(String[] args) {
        try {
            AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(6666));

            System.out.println("Server is listening at port 6666...");

            // Create a thread pool to handle multiple client connections
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
                @Override
                public void completed(AsynchronousSocketChannel clientChannel, Void att) {
                    serverChannel.accept(null, this);
                    executorService.submit(() -> startHandshake(clientChannel));
                }

                @Override
                public void failed(Throwable e, Void att) {
                    System.out.println("Failed to accept a connection");
                    e.printStackTrace();
                }
            });

            mainThread = Thread.currentThread();
            mainThread.join();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void shutdown() {
        if (mainThread != null) {
            mainThread.interrupt();
        }
    }

    private static void startHandshake(AsynchronousSocketChannel clientChannel) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        clientChannel.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer buffer) {
                buffer.flip();
                String secretKey = new String(buffer.array(), 0, result).trim();
                buffer.clear();
                if (HANDSHAKE_SECRET.equals(secretKey)) {
                    System.out.println("Handshake successful. Secret Key: " + secretKey);
                    handleConnection(clientChannel, secretKey);
                } else {
                    System.out.println("Handshake failed. Closing connection.");
                    try {
                        clientChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                System.out.println("Failed to read a message");
                exc.printStackTrace();
            }
        });
    }

    private static void handleConnection(AsynchronousSocketChannel clientChannel, String secretKey) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        clientChannel.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer buffer) {
                try {
                    if (result == -1) {
                        System.out.println("Client disconnected");
                        clientChannel.close();
                    } else {
                        buffer.flip();
                        String decryptedMessage = decryptMessage(buffer.array(), 0, result, secretKey);
                        System.out.println("Decrypted message= " + decryptedMessage);
                        buffer.clear();
                        clientChannel.read(buffer, buffer, this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable e, ByteBuffer buffer) {
                System.out.println("Failed to read a message");
                e.printStackTrace();
            }
        });
    }

    private static String decryptMessage(byte[] encryptedMsg, int offset, int length, String secretKey) throws Exception {
    byte[] encodedMsg = new byte[length];
    System.arraycopy(encryptedMsg, offset, encodedMsg, 0, length);
    byte[] decodedMsg = Base64.getDecoder().decode(encodedMsg);
    SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
    Cipher cipher = Cipher.getInstance("AES");
    cipher.init(Cipher.DECRYPT_MODE, skeySpec);
    byte[] decryptedMsg = cipher.doFinal(decodedMsg);
    return new String(decryptedMsg);
    }
}



