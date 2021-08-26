package com.example.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class DemoClient {

    public static void main(String[] args) throws IOException {
        DemoClient demoClient = new DemoClient();
        demoClient.run();
    }

    void run() throws IOException {
        System.out.println(":::::: Democlient Start ::::::");

        // 소켓 생성
        Socket socket = new Socket();

        // 주소 생성
        SocketAddress address = new InetSocketAddress("127.0.0.1", 5050);

        // 주소에 해당하는 서버와 연결
        socket.connect(address);
        System.out.println(":::::: connected ::::::");

        try{
            // 메세지 전송
            send(socket);
            System.out.println(":::::: send Message ::::::");

            // 메세지 받기
            receive(socket);
            System.out.println(":::::: receive Message ::::::");

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void send(Socket socket) throws IOException {

        // 전달할 String 생성
        String a = "hello";

        // byte로 변환
        byte[] data = a.getBytes(StandardCharsets.UTF_8);

        // 서버로 보낼 출력 스트림
        OutputStream os = socket.getOutputStream();
        
        // 출력 스트림에 데이터를 쓰고
        os.write(data);
        
        // 보냄
        os.flush();

    }

    public static void receive(Socket socket) throws IOException {
        // 수신 버퍼의 최대 사이즈
        int maxBufferSize = 5;
        // 버퍼 생성
        byte[] recvBuffer = new byte[maxBufferSize];
        // 서버로부터 받기 위한 입력 스트림
        InputStream is = socket.getInputStream();
        // 버퍼를 인자로 넣어 반환 값을 받음
        int readSize = is.read(recvBuffer);

        // 받아온 값이 0보다 클 때
        if(readSize > 0){
            // 받아온 byte를 String으로 변환
            String a1 = new String(recvBuffer);
            System.out.println(">>>>>> 응답받은 메세지 : " + a1 + " >>>>>>");
        }

    }

}
