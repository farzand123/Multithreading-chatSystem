# Multithreading-chatSystem

Abstraction:
The chat system we are going to design will have two classes: ChatServer & ChatHandler. The ChatServer class will be responsible for accepting the connections from clients and providing each client with a dedicated thread for messaging. The ChatHandler is an extension of class Thread. This class is responsible for receiving client messages and broadcasting those messages to other clients. In ChatServer class there will be only one main method, which will have 3 variables including port, serversocket and socket. The port variable will store the port on which the server will listen for new connections. Then there will coding for the server connections including different try and catch blocks. If anything goes away with either the server socket or client socket an I/O Exception will be thrown to overcome the bug. In ChatHandler class the constructor we assign the socket the handler we will use and construct the socket input and output streams. The BufferedReader and PrintWriter classes are used for handling user I/O. The Reader classes enable proper handling of bytes and characters. For example, the BufferedReader class method readLine() will properly convert 8-bit bytes to 16-bit UNICODE characters. If an exception is thrown or the user sends a "/quit" message, we attempt to close the I/O streams and the socket, and finally remove the current handler from the list.




Requirement: 

•	Java JDK 11 or above
•	Java JDK or Open JDK
•	Java SE Development Kit

Working:

1)To start the main server of chatting you need to first open command prompt and type the following steps:
•	Cd documents(or wherever the files are stored)
•	javac Server.java
•	java Server 1233 
This will start the server on socket 1233

2)Then we will start the client server to chat by following steps:
•	Cd documents(or wherever the files are stored)
•	javac Client.java
•	java Client localhost 1233 
we will do these steps as many times as the amount of users we want in the chatroom

