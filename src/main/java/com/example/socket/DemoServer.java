package com.example.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class DemoServer {
    public static void main(String[] args) throws IOException {
        DemoServer demoServer = new DemoServer();
        demoServer.run();
    }

    public void run() throws IOException {
        try{
            int port = 5050;

            // 서버소켓을 생성
            ServerSocket ssk = new ServerSocket(port);

            while (true) {
                // 1. 연결
                System.out.println("****** 접속대기중 ******");
                Socket socket = ssk.accept();
                System.out.println(ssk.getInetAddress() +" 로부터 연결 요청이 들어옴");

                // 2. 데이터 받기
                InputStream is = socket.getInputStream();
                byte[] bytes = new byte[2000];

                int readByteCount = is.read(bytes);

                if (readByteCount > 0) {
                    System.out.println("클라이언트로부터 데이터 수신");
                    sendData(bytes, socket);
                }
                System.out.println("****** 재전송완료 ******");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendData(byte[] bytes, Socket socket) throws IOException {
        try {
            OutputStream os = socket.getOutputStream();
            os.write(bytes);
            os.flush();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
