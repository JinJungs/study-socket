package com.example.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class DemoClient {

    public static void main(String[] args) throws IOException {

        // 1. 소켓 테스트
        // String message = "apple";
        // 2. 웹서버 테스트
        String message = "GET 1.txt";


        for (int i=0; i<10; i++){

            // 여러 클라이언트에서 들어온다는 가정하에 쓰레드로 처리
            // 단순 반복문으로 처리하니 요청이 순차적으로 들어와서 쓰레드가 정상작동하는지 확인이 어려움
            Thread thread = testThread(message);
            thread.start();

        }

    }

    private static Thread testThread(String message) {
        return new Thread( new Runnable() {

            @Override
            public void run() {
                // 필요한 객체 생성
                OutputStream os = null;
                InputStream is = null;
                Socket socket = null;

                try{

                    // 1. 소켓 생성
                    socket = new Socket("127.0.0.1", 5050);
                    System.out.println(":::::: connected ::::::");


                    // 2. 데이터 전송
                    byte[] data = message.getBytes(); // byte로 변환
                    os = socket.getOutputStream();  // 서버로 보낼 출력 스트림
                    os.write(data); // 출력 스트림에 데이터를 쓰고
                    os.flush();   // 보냄
                    System.out.println(":::::: send Message : " + message);


                    // 3． 데이터 수신
                    int maxBufferSize = 2000; // 수신 버퍼의 최대 사이즈
                    byte[] recvBuffer = new byte[maxBufferSize]; // 버퍼 생성

                    is = socket.getInputStream(); // 서버로부터 받기 위한 입력 스트림
                    int readSize = is.read(recvBuffer); // 버퍼를 인자로 넣어 반환 값과 사이즈를 받음
                    String recvMessage = new String(recvBuffer, 0, readSize); // 받아온 byte를 String으로 변환

                    // 받아온 값이 0보다 클 때
                    if(readSize > 0){
                        System.out.println(":::::: receive Message : " + recvMessage);
                    }

                }catch (IOException e) {
                    e.printStackTrace();

                }finally {
                    try {
                        // 끝난 후에는 반드시 close
                        os.close();
                        is.close();
                        socket.close();
                        System.out.println(":::::::::::::::::: ended ::::::::::::::::::\n");

                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }



}
