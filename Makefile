rdt: client server
	java Server rdt

udp: client server
	java Server udp

client: Client.java
	javac Client.java

server: Server.java
	javac Server.java

clean:
	rm *.class
