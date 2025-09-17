#!/usr/bin/env python3

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
import toml

class Channel:
    def __init__(self, host, port, context):
        self.closed = False
        self.host = host
        self.socket = None
        self.baseSocket = None
        self.conn = None
        self.context = context
        self.port = port

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
        client_info = ((self.conn.recv(1024).decode()).split("\x00", 1)[1]).split(":")
        #print(client_info,file=sys.stdout)
        self.client_length = int(client_info[0]) // 4
        self.client_width = int(client_info[1]) // 4 
        self.headerSize = int(client_info[2])
        self.client_name = client_info[3]
        print(("[(" + time.strftime("%H:%M:%S", time.localtime()) + ")] Get a connection from " + self.client_name), file=sys.stdout)
        self.conn.send(("Welcome to the channel, " + self.client_name).encode('utf-8'))
        self.RenderData = bytearray(self.client_length * self.client_width * 4)
        
        #self.screenThread = threading.Thread(target=self.RenderScreen.Render_Screen, args=(self.client_width, self.client_length, self.RenderData))
        #self.screenThread.start()
        return None
    
    def send_message(self):
        """
        Use a while loop to get message from stdin and send out the message
        back to the client.
        If the message is "exit", print "[Connection Terminated by the server]"
        to the stdout. Then close the socket and exit with code 0.
        :return: None
        """
        while self.closed == False:
            message = input()
            self.conn.send(message.encode('utf-8'))
            if message == "exit":
                print("[Connection terminated by the server]", file=sys.stdout)
                self.socket.close()
                self.closed = True
        return None
    
    def _receive_and_print_message(self):
        """
        Defined by child classes
        """
        return None

    def receive_and_print_message(self):
        """
        Multithreading
        :return: None
        """
        threading.Thread(target=self._receive_and_print_message).start()
        return None
    
    def run_channel(self):
        self.listen_on_port()
        print(("[(" + time.strftime("%H:%M:%S", time.localtime()) + ")] Waiting for a connection"), file=sys.stdout)
        self.recv_client_connection()
        self.receive_and_print_message()
        self.send_message()
        return None
    

class PhoneChannel(Channel):
    def __init__(self, host, port, context):

        super().__init__(host, port, context)

        self.channel_name = "Phone Channel"
        self.client_name = None

        self.imageNumber = 0
        self.RenderScreen = RenderScreen
        self.RenderData = None
        self.headerSize = 0
        self.client_length = 0
        self.client_width = 0
        self.screenThread = 0
    
    def recv_image(self):
        #print(self.screenThread)
        if self.screenThread == 0 or self.screenThread.is_alive() == False:
            self.screenThread = threading.Thread(target=self.RenderScreen.Render_Screen, args=(self.client_width, self.client_length, self.RenderData))
            self.screenThread.start()

        imageSize = int(self.conn.recv(self.headerSize).decode().rstrip("\x00"))
        if imageSize != None and imageSize != 0:
            # Now, receive the frame data itself
            imageData = b''
            while len(imageData) < imageSize:
                packet = self.conn.recv(512)
                if not packet:
                    break
                imageData += packet
            
            # At this point, you have the complete frame in `frame_data`
            #print(f'Received frame of size: {len(imageData)} bytes')
            
            #imageFile = PIL.Image.frombytes("RGBA", (self.client_width, self.client_length, imageData)
            #imageFile.save(f"Images/Image{self.imageNumber}.png")
            #imageFile.close()
            #self.imageNumber += 1
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
        try:
            while self.closed == False:
                messageType = self.conn.recv(1)[0]
                #print(messageType)
                match messageType:
                    case 0:
                        message = self.conn.recv(1023).decode('utf-8')
                        print("[" + self.client_name + " (" + time.strftime("%H:%M:%S", time.localtime()) + ")] " + message, file=sys.stdout)
                    case 1:
                        self.recv_image()
                    case 2:
                        print("[Connection terminated by the client]", file=sys.stdout)
                        self.closed = True
                    case _:
                        print("Error reading message")

            self.socket.close()
            return None
        except:
            self.socket.close()
            self.closed = True
            return None

    def receive_and_print_message(self):
        """
        Multithreading
        :return: None
        """
        threading.Thread(target=self._receive_and_print_message).start()
        return None

class Server:

    def __init__(self):
        with open('Settings.toml', 'r') as f:
            config = toml.load(f)
        
        self.host = config['server']['host']
        
        self.phone_port = config['PhoneChannel']['port']

        self.context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
        self.context.load_cert_chain(config['certificates']['server'], config['certificates']['key'])
        self.context.verify_mode = ssl.CERT_REQUIRED
        self.context.load_verify_locations(cafile=config['certificates']['root'])
    
    def run_server(self):
        """
        Run the chat server that receives and send information to the client
        :return: None
        """
        try:
            while True:
                phone = PhoneChannel(self.host, self.phone_port, self.context)
                phone.run_channel()
                #phoneThread = threading.Thread(target=phone.run_channel)
                #phoneThread.start()
        except:
            return None

def main():
    try:
        server = Server()
        server.run_server()
    except KeyboardInterrupt:
    
        print("Ctrl + C Pressed. Exiting...")
        os._exit(0)

if __name__ == '__main__':
    main()
