package com.example.phoneapp.ui.theme

import android.util.Size
import org.chromium.base.Promise
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManagerFactory

/** SSLClient
 *
 * Handles the client socket and the application's connection to the server
 * Performs mTLS to create an SSLSocket with the server
 *
 * @param hostSocket InetSocketAddress that represents the server the client will default to
 * connecting when opening the app.
 * address when first running the server
 * @param clientTrustManager TrustManagerFactory that is used by the client
 * to establish trust with the server (Server Credentials)
 * @param clientKeyManager KeyManagerFactory that is used by the server
 * to establish trust with the client (Client Credentials)
 *
 * @author Thomas Bennion
 */
class SSLClient (
    hostSocket: InetSocketAddress,
    private val clientTrustManager: TrustManagerFactory,
    private val clientKeyManager: KeyManagerFactory
): Thread()
{
    private val clientContext: SSLContext = SSLContext.getInstance("TLSv1.3")
    private val startAddress = hostSocket
    private lateinit var hostAddress: InetAddress
    private var port: Int = 4655
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream
    private lateinit var socket: SSLSocket

    private val MESSAGE_BYTE: ByteArray = ByteArray(1) { 0 }
    private val IMAGE_BYTE: ByteArray = ByteArray(1) { 1 }
    private val SIGNAL_BYTE: ByteArray = ByteArray(1) { 2 }

    /** CheckConnection
     *
     * Checks if the socket connection is alive.
     *
     * @return true if the connection is alive, else false
     *
     * @author Thomas Bennion
     */
    fun checkConnection(): Boolean {
        return try {
            socket.isConnected
        } catch (e: Exception) {
            false
        }
    }

    /** WriteMessage
     *
     * Sends byteArray.length + 1 bytes to the server
     * Should only be used for strings (can be decoded in utf-8)
     *
     * Message requests are recognised by the server via the packet starting with a 0 / MESSAGE_BYTE
     *
     * @param byteArray The data to be sent to the server
     * @throws IOException If an I/O error occurs when writing to the socket
     *
     * @author Thomas Bennion
     */
    fun WriteMessage(byteArray: ByteArray) {

        try {
            println("Sending...")
            this.outputStream.write(MESSAGE_BYTE + byteArray)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun WriteImage(byteArray: ByteArray, displaySize: Size) {
        try {
            //println("Width: " + displaySize.width + " Height: " + displaySize.height)
            val arraySize = byteArray.size.toString().toByteArray()
            println("Value:" + byteArray.size + "Size:" + arraySize.size + "Total:" + (IMAGE_BYTE + arraySize + byteArray).size)
            this.outputStream.write((IMAGE_BYTE + arraySize + byteArray))
            this.outputStream.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /** read
     *
     * Reads buffer.length bytes from the server and stores them in the buffer
     * The number of bytes actually read is returned as an integer.
     *
     * If the length of b is zero, or some error occurs, then no bytes are read and 0 is returned.
     *
     * If no byte is available because the stream is at the end of the file,
     * the value -1 is returned.
     *
     * Otherwise, at least one byte is read, the first byte read is stored into element buffer[0],
     * the next one into b[1], and so on.
     *
     * If the number of bytes read is less than b.length, then buffer will store the bytes in
     * buffer[0..(n-1)] where n is the number of bytes read. The rest of the buffer is unaffected.
     *
     * @param buffer the buffer into which the data is read
     *
     * @return the total number of bytes read into the buffer,
     * or -1 if there is no more data because the end of the stream has been reached.
     *
     * @throws IOException If the first byte cannot be read
     * for any reason other than the end of the file, if the input stream has been closed,
     * or if some other I/O error occurs.
     *
     * @author Thomas Bennion
     */
    fun read(buffer: ByteArray): Int {
        return try {
            inputStream.read(buffer)
        } catch(e: IOException) {
            e.printStackTrace()
            0
        } catch (e: Exception) {
            0
        }
    }

    override fun run() {
        try {
            this.clientContext.init(
                clientKeyManager.keyManagers,
                clientTrustManager.trustManagers,
                SecureRandom()
            )
            this.connect(this.startAddress)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** connect
     *
     * Initialises the clients connection to the server.
     * If successful, it will send a message stating "Android" to the server.
     *
     * Remember to call SSLClient.disconnect() first, as the client is already connected
     * to the default server on startup
     *
     * @param socketAddress InetSocketAddress representing the server
     *
     * @throws Exception if an error occurs in initialisation
     *
     * @author Thomas Bennion
     */
    fun connect(socketAddress: InetSocketAddress) {
        try {
            this.hostAddress = socketAddress.address
            this.port = socketAddress.port
            println("Connecting...")
            socket = clientContext.socketFactory.createSocket(hostAddress, port) as SSLSocket

            socket.useClientMode = true

            println("Handshaking...")
            socket.startHandshake()

            if (socket.isConnected) {
                this.inputStream = socket.inputStream
                this.outputStream = socket.outputStream
                println("Connected")
                this.WriteMessage("Android".encodeToByteArray())
            } else {
                println("Failed to connect")
            }
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    /** disconnect
     *
     * Handles shutdown process of the clients' connection to the server
     *
     * @return Promise of a Boolean. true if the client was shutdown successfully, false if an
     * error occurred
     *
     * @throws IOException If an I/O error occurs
     *
     * @author Thomas Bennion
     */
    fun disconnect(): Promise<Boolean> {
        val returnValue = Promise<Boolean>()
        try {
            inputStream.close()
            outputStream.close()
            socket.close()
            returnValue.fulfill(true)

        } catch (e: IOException) {
            e.printStackTrace()
            returnValue.fulfill(false)
        }
        return returnValue
    }
}