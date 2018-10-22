all: client server

client: 2016123_client.c
	gcc -pthread -o client 2016123_client.c

server: 2016123_server.c
	gcc -pthread -o server 2016123_server.c

clean:
	sudo rm server client

runc:
	./client 192.168.65.73 9095

runs:
	./server 9095