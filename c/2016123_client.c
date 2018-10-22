#include <stdio.h>
#include <string.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <pthread.h>
#include <regex.h>
#include <stdlib.h>

#include "2016123_constants.h"

struct sockaddr_in server;
char *server_ip;
char *server_port;
int packet_count;

int main(int argc, char *argv[]) { 
	int sfd; 
	char message[4096]; 
	char *to_server = "Testing"; 

    if (argc < 4)
    {
        printf("Usage: ./client <server_ip_address> <port> <packet_count>\n");
        exit(0);
    }

    server_ip = argv[1];
    server_port = argv[2];
    packet_count = atoi(argv[3]);

    sfd = socket(AF_INET, SOCK_DGRAM, 0);

	if (sfd <= 0) 
    {
        printf("Socket couldn't be created.\n");
        return 0;
    }

	memset(&server, 0, sizeof(server)); 
	
	server.sin_family = AF_INET; 
	server.sin_port = htons(atoi(server_port));
	server.sin_addr.s_addr = inet_addr(server_ip); 
	
	int n, len; 

	int counter = 0;

	while(counter < packet_count)
	{
		sendto(sfd, (const char *)to_server, strlen(to_server), MSG_CONFIRM, (const struct sockaddr *) &server, sizeof(server));
		counter += 1;
	}

	printf("Sent %d packets to Server\n", packet_count); 
	
	// n = recvfrom(sfd, (char *)message, 4096, MSG_WAITALL, (struct sockaddr *) &server, &len); 
	// message[n] = '\0'; 


	close(sfd); 
	return 0; 
} 
