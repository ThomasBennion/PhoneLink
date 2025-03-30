# Linux PhoneLink
PhoneLink is an application that will allow for connectivity between your android phone and a GNU/Linux desktop environment.
Currently, the app established an SSLConnection with the server, and successfully transmits messages and pictures between each other.
With the server successfully receiving and rendering images taken from a MediaProjection service

# TODO
There's a lot TODO, but for now I am getitng touch commands to be written and read between the server and the phone so the phone can be controlled remotely

# Getting Started
Currently, a lot of stuff is hardcoded for the purposes of establishing core functionality.
Certicates are still hardcoded into the application (I was smart enough to remove them from the repository). 
I am working on a means to have them read from a file when you start up the application. For now though, please insert the neccessary certificates as follows.

Add the trust anchor (root certificate authority) at:
PhoneApp/app/src/main/res/raw/rootca.pem
Server/certs/rootCA.pem

Add the server certificate at:
PhoneApp/app/src/main/assets/server.pem
Server/certs/server.pem

Add the server key at:
Server/certs/server.key

Add the client certificate at:
PhoneApp/app/src/main/assets/clientcert.pem

Add the client key at:
PhoneApp/app/src/main/assets/client.key

Change the address and port in the main activity file in android by switching the values of
val port = 4655
val address = "192.168.1.100"
to whatever address and port you want to connect to
