import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class Client {
    private static final String HANDSHAKE_SECRET = "handshake";

    public static void main(String[] args) {
        try {
            AsynchronousSocketChannel clientChannel = AsynchronousSocketChannel.open();
            clientChannel.connect(new InetSocketAddress("localhost", 6666), null, new CompletionHandler<Void, Void>() {
                @Override
                public void completed(Void result, Void att) {
                    startHandshake(clientChannel);
                }

                @Override
                public void failed(Throwable e, Void att) {
                    System.out.println("Failed to connect to server");
                    e.printStackTrace();
                }
            });

            Thread.currentThread().join();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static void startHandshake(AsynchronousSocketChannel clientChannel) {
        ByteBuffer buffer = ByteBuffer.wrap(HANDSHAKE_SECRET.getBytes());
        clientChannel.write(buffer, buffer, createWriteCompletionHandler(clientChannel, buffer));
    }

    private static CompletionHandler<Integer, ByteBuffer> createWriteCompletionHandler(AsynchronousSocketChannel clientChannel, ByteBuffer buffer) {
        return new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer buffer) {
                handleServerResponse(clientChannel, HANDSHAKE_SECRET);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer buffer) {
                System.out.println("Failed to send handshake");
                exc.printStackTrace();
            }
        };
    }

    private static void handleServerResponse(AsynchronousSocketChannel clientChannel, String secretKey) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        clientChannel.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer buffer) {
                try {
                    if (result == -1) {
                        System.out.println("Server disconnected");
                        clientChannel.close();
                    } else {
                        buffer.flip();
                        String decryptedMessage = decryptMessage(buffer.array(), 0, result, secretKey);
                        System.out.println("Decrypted message= " + decryptedMessage);
                        buffer.clear();
                        clientChannel.read(buffer, buffer, this);

                        // Send an encrypted message to the server
                        String message = "Hello Server";
                        sendEncryptedMessage(clientChannel, message, secretKey);
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

    private static void sendEncryptedMessage(AsynchronousSocketChannel clientChannel, String message, String secretKey) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encryptedMsg = Base64.getEncoder().encode(cipher.doFinal(message.getBytes()));
        ByteBuffer buffer = ByteBuffer.wrap(encryptedMsg);

        clientChannel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer buffer) {
                // Message sent
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                System.out.println("Failed to send a message");
                exc.printStackTrace();
            }
        });
    }
}




