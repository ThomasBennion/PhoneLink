import socket
import sys
import time
import threading
import ssl
import PIL
import PIL.Image
from PIL.Image import Image
import PIL.ImageMode
from RenderScreen import RenderScreen
import ctypes
import os

class ChatServer:

    def __init__(self):
        self.host = "0.0.0.0"
        self.client_name = None
        self.port = None
        self.socket = None
        self.baseSocket = None
        self.conn = None
        self.context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
        self.context.load_cert_chain("server.pem", "server.key")
        
        self.context.verify_mode = ssl.CERT_REQUIRED
        self.context.load_verify_locations(cafile="rootCA.pem")
        
        self.imageNumber = 0
        self.RenderScreen = RenderScreen
        self.RenderData = None

    def read_port_number(self):
        """
        Read the port number from argument, store it to self.port.
        Exit with code 1 if invalid argument is provided.
        :return: None
        """
        if len(sys.argv) < 1 or str.isdigit(sys.argv[1]) != True:
            exit(1)
        self.port = int(sys.argv[1])
        return None

    def listen_on_port(self):
        """
        Create a socket listens on the specified port.
        Store the socket object to self.socket.
        :return: None
        """
        self.baseSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM, 0)
        self.baseSocket.bind((self.host, self.port))
        self.baseSocket.listen(5)
        self.socket = self.context.wrap_socket(self.baseSocket, server_side=True)
        return None

    def recv_client_connection(self):
        """
        Accept a client connection and store the new
        accepted connection to self.conn.
        Get and store the client name in self.client_name.
        Print the get connection message to the stdout.
        Send the welcome message to the connected client.
        :return: None
        """
        self.conn, addr = self.socket.accept()
        self.client_name = self.conn.recv(1024).decode()
        print(("[(" + time.strftime("%H:%M:%S", time.localtime()) + ")] Get a connection from " + self.client_name), file=sys.stdout)
        self.conn.send(("Welcome to the channel, " + self.client_name).encode('utf-8'))
        #self.RenderScreen.Render_Screen((1440 // 4), (2920 // 4), self.RenderData)
        self.RenderData = bytearray(4204800)
        #self.RenderData = bytearray(1074560)
        threading.Thread(target=self.RenderScreen.Render_Screen, args=(1440 // 2, 2920 // 2, self.RenderData)).start()
        return None
    
    def recv_image(self):
        imageSize = int(self.conn.recv(7).decode())
        print(imageSize)
        if imageSize != None and imageSize != 0:
            # Now, receive the frame data itself
            imageData = b''
            while len(imageData) < imageSize:
                packet = self.conn.recv(8192)
                if not packet:
                    break
                imageData += packet
            
            # At this point, you have the complete frame in `frame_data`
            #print(f'Received frame of size: {len(imageData)} bytes')
            
            #imageFile = PIL.Image.frombytes("RGBA", (1440 // 2, 2920 // 2), imageData)
            #imageFile.save(f"Images/Image{self.imageNumber}.png")
            #imageFile.close()
            self.imageNumber += 1
            self.RenderData[:] = imageData
        else:
            print("Error receiving image", file=sys.stdout)

    def _receive_and_print_message(self):
        """
        Use a while loop to receive TCP packets from the client and print
        messages to stdout.
        If the message is "exit", print "[Connection Terminated by the client]"
        to stdout. Then close the socket and exit wit code 0.
        :return: None
        """
        while True:
            messageType = self.conn.recv(1)[0]
            print(messageType)
            if messageType == 0:
                message = self.conn.recv(1023).decode('utf-8')
                print("[" + self.client_name + " (" + time.strftime("%H:%M:%S", time.localtime()) + ")] " + message, file=sys.stdout)
            elif messageType == 1:
                self.recv_image()
            elif messageType == 2:
                print("[Connection terminated by the client]", file=sys.stdout)
                self.socket.close()
                exit(0)
            else:
                print("Error reading message")
        return None

    def receive_and_print_message(self):
        """
        Multithreading
        :return: None
        """
        threading.Thread(target=self._receive_and_print_message).start()
        return None

    def send_message(self):
        """
        Use a while loop to get message from stdin and send out the message
        back to the client.
        If the message is "exit", print "[Connection Terminated by the server]"
        to the stdout. Then close the socket and exit with code 0.
        :return: None
        """
        while True:
            message = input()
            self.conn.send(message.encode('utf-8'))
            if message == "exit":
                print("[Connection terminated by the server]", file=sys.stdout)
                self.socket.close()
                exit(0)
        return None
    
    def run_chat_server(self):
        """
        Run the chat server that receives and sends messages to the client
        :return: None
        """
        self.read_port_number()
        self.listen_on_port()
        print(("[(" + time.strftime("%H:%M:%S", time.localtime()) + ")] Waiting for a connection"), file=sys.stdout)
        self.recv_client_connection()
        self.receive_and_print_message()
        self.send_message()
        return None


if __name__ == '__main__':
    chat_server = ChatServer()
    chat_server.run_chat_server()

