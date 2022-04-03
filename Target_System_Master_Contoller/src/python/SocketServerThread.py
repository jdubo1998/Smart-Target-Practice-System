import threading
import socket as Socket
import re
import os

class SocketServerThread:
    isConnected = False
    startSession = False
    endSession = False

    code = None
    host = ""
    port = 0
    socket = None
    conn = None

    def __init__(self, host, port):
        self.host = host
        self.port = int(port)

        self.socket = Socket.socket(Socket.AF_INET, Socket.SOCK_STREAM)

        try:
            self.socket.bind((self.host, self.port ))
        except:
            self.socket.close()
            print("Exception.")
        # except Socket.error as error:
        #     if error == errno.ECONNREFUSED:
        #         print(os.strerror(error.errno))
        #     else:
        #         raise

    def listen(self):
        try:
            self.socket.listen(1)
            print("Waiting for a conn.")
            self.conn, address = self.socket.accept()
            self.conn.settimeout(60)
            print("Connected to: " + address[0] + ":" + str(address[1]))
            self.isConnected = True

            threading.Thread(target = self.listenToconn).start()
        except KeyboardInterrupt :
            self.socket.close()
            os._exit(0)

    def listenToconn(self):
        size = 1024
        while True:
            try:
                # print("Waiting for response... ", end="")
                data = self.conn.recv(size)
                # print("Received repsonse: ", end="")
                if data:
                    message = data.decode('utf-8')
                    message = re.sub("\n", "", message)

                    print("D/SocketServerThread: " + message)

                    if re.search("start;", message):
                        self.code = message
                        self.startSession = True

                    if re.search("exit", message):
                        self.conn.shutdown(Socket.SHUT_RDWR)
                        self.conn.close()
                        self.isConnected = False
                        self.endSession = True
                        break
                else:
                    raise error('Connection disconnected')
            except:
                self.conn.close()
                self.isConnected = False
                return False

    def sendMessage(self, message):
        if (self.isConnected):
            message += "\n"
            print("Sending: " + message, end="")
            self.conn.sendall(str.encode(message))

    def closeSocket():
        self.socket.close()
