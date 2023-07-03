# Java VPN Project
A simple Virtual Private Network (VPN) built in Java that showcases the basics of networking and encryption. While the project is just a demonstration of principles and not suitable for use in a real-world secure communication system, it provides a solid foundation for learning about these topics.

### Features
- The VPN uses the AsynchronousSocketChannel class in Java's NIO (Non-blocking I/O) package to handle network communication.
- The server accepts multiple client connections concurrently, with each connection handled in its own thread using a thread pool.
- The client and server perform a simple handshake using a shared secret key, after which they can exchange encrypted messages.
- All messages are encrypted using the AES (Advanced Encryption Standard) algorithm with a secret key.
- The VPN handles messages of varying sizes by dynamically managing buffer sizes.

### Usage 
First, start the server:
'''java Server'''

The server will start listening for connections on port 6666.

Next, start the client:

'''java Client'''

The client will connect to the server and send an encrypted message.
### Limitations
While this VPN demonstrates the basics of encrypted communication over a network, it is not secure against many types of attacks and should not be used in a real-world application. For a secure VPN, use a well-tested library or software package.

### Future Improvements
Here are a few ideas for ways this project could be improved or expanded:

- Implement a secure handshake protocol that doesn't require a shared secret key, such as the Diffie-Hellman key exchange.
- Use a more secure method of message encryption, such as AES with a mode of operation that includes an integrity check.
- Add error handling and recovery mechanisms, such as reconnecting if the connection is lost.
- Implement a graphical user interface (GUI) for the client and server.

I hope this project provides a helpful starting point for learning about networking and encryption in Java!
