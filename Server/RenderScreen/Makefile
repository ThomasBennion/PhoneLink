obj-m += RenderScreen.o

RenderScreen:
	gcc RenderScreen.c -o RenderScreen -lglfw -lGL

Python3.13:
	gcc -I/usr/include/python3.13 -Wall -shared -o RenderScreen.so -fPIC RenderScreen.c $(python3.13-config --cflags --ldflags) -lglfw -lGL -lGLEW

clean:
	rm RenderScreen
	rm RenderScreen.so
