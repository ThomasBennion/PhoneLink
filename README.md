# Linux PhoneLink
PhoneLink is an application that will allow for connectivity between your android phone and a GNU/Linux desktop environment.
Currently, the app established an SSLConnection with the server, and successfully transmits messages and pictures between each other.
With the server successfully receiving and rendering images taken from a MediaProjection service

# TODO
There's a lot TODO, but for now I am focussing on getting the images to be read into an openGL instance on the server. After that I will work on sending and receiving signals between the two.
After this, the desktop should be able to both see and interact with the screen of the android device.

# Getting Started
Currently, a lot of stuff is hardcoded for the purposes of establishing core functionality.
Certicates are still hardcoded into the application (I was smart enough to remove them from the repository). 
I am working on a means to have them read from a file when you start up the application. For now though, please insert the neccessary certificates as follows.

Add the trust anchor (root certificate authority) at:
PhoneApp/app/src/main/res/raw/rootca.pem
Server/rootCA.pem

Add the server certificate at:
PhoneApp/app/src/main/assets/server.pem
Server/server.pem

Add the server key at:
Server/server.key

Add the client certificate at:
PhoneApp/app/src/main/assets/clientcert.pem

Add the client key at:
PhoneApp/app/src/main/assets/client.key

Change the address and port in the main activity file in android by switching the values of
val port = 4655
val address = "192.168.1.100"
to whatever address and port you want to connect to
