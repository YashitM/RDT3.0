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

char *server_port;

int main(int argc, char *argv[]) { 
	
	if (argc < 2)
    {
        printf("Usage: ./server <port>\n");
        exit(0);
    }

	int sfd, opt = 1; 
	char message_buffer[1024];
	struct sockaddr_in server, client; 
	
	server_port = argv[1];

	sfd = socket(AF_INET, SOCK_DGRAM, 0);

	if (sfd <= 0) 
    {
        printf("Socket couldn't be created.\n");
        return 0;
    }
	
	memset(&server, 0, sizeof(server)); 
	memset(&client, 0, sizeof(client));

	if (setsockopt(sfd, SOL_SOCKET, SO_REUSEADDR | SO_REUSEPORT, &opt, sizeof(opt))) 
    { 
        printf("Error while overriding port.\n");
        return 0;
    }
	
	server.sin_family = AF_INET;
	server.sin_addr.s_addr = INADDR_ANY; 
	server.sin_port = htons(atoi(server_port)); 
	
	if (bind(sfd, (struct sockaddr *)&server, sizeof(server)) != 0)
    {
        printf("Port Busy.\n");
        return 0;
    }
	
	int len, recv_counter = 0, counter = 0;

	while (1)
	{
		memset(&client, 0, sizeof(client));
		int result = recvfrom(sfd, (char *)message_buffer, 1024, MSG_WAITALL, ( struct sockaddr *) &client, &len);
		if (result > 0)
		{
			recv_counter += 1;
		}
		printf("Received %d packets from Client\n", recv_counter);	
	}

	// sendto(sfd, (const char *)hello, strlen(hello), 
	// 	MSG_CONFIRM, (const struct sockaddr *) &client, 
	// 		len); 
	// printf("Hello message sent.\n"); 
	
	return 0; 
} 
