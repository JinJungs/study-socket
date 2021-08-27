package com.example.socket;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DemoServer {

    public static void main(String[] args) throws IOException {

        // 초기화
        ServerSocket ssk = null;
        int seq = 0;
        int port = 5050;

        try{

            // 서버소켓을 생성 - bind와 listen을 포함
            ssk = new ServerSocket(port);

            // 요청이 들어올 때마다 반복 수행
            while (true) {

                // 1. 연결
                System.out.println("****** 접속대기중 ******");
                Socket socket = ssk.accept();
                System.out.println(socket.getInetAddress() +" 로부터 연결 요청이 들어옴");

                // 데이터 받기 및 전송은 쓰레드에서 처리
                // 1. 소켓 테스트
                //Thread thread = testSocket(seq, socket);
                // 2. 웹서버 테스트
                //Thread thread = testWebServer(seq, socket);
                // 3. Http 테스트
                Thread thread = testHttp(seq, socket);

                thread.start();

                seq++;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        finally {
            try {
                ssk.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

    }

    // 1. 소켓 테스트
    private static Thread testSocket(int seq, Socket socket) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(seq + " thread start");
                try {

                    Thread.sleep(1000);

                    // 2. 데이터 받기
                    String recvMessage = readData(socket);

                    // 3. 데이터 재전송
                    sendData(socket, recvMessage);

                }catch(Exception e) {
                    e.printStackTrace();
                }
                System.out.println(seq+" thread end.");
            }
        });
    }


    // 2. 웹서버 테스트
    private static Thread testWebServer(int seq, Socket socket) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(seq + " thread start");
                try {

                    Thread.sleep(1000);

                    /*
                     * 1. GET 으로 왔는지 형식 확인 -> 아니면 400 return
                     * 2. 파일이 존재하는지 확인 -> 아니면 404 return
                     * 3. 파일이 존재하면 내용을 읽고 return
                     * */

                    // 1. 데이터 받기
                    String recvMessage = readData(socket);

                    // message null 체크
                    if (recvMessage == null) {
                        sendData(socket, "400");
                        return;
                    }

                    // 받은 메세지를 GET 과 받은 파일이름으로 구분
                    String[] headerArr = recvMessage.split(" ");
                    String httpMethod = headerArr[0];
                    String recvFileName = headerArr[1];

                    // (1) GET 으로 왔는지 형식 확인
                    if (!"GET".equals(httpMethod)) {
                        sendData(socket, "400");
                        return;
                    }

                    // (2) 파일 이름 null 체크
                    if (recvFileName == null | "".equals(recvFileName)) {
                        sendData(socket, "404");
                        return;
                    }

                    // (3) 파일이 존재하는지 확인
                    String dirPath = "C:\\source\\ToyProjectWorkspace\\toy_socket\\src\\main\\resources";
                    File dir = new File(dirPath);
                    File[] files = dir.listFiles();
                    boolean isFileExist = false;
                    for (File file : files) {
                        if (recvFileName.equals(file.getName())) {
                            isFileExist = true;
                            break;
                        }
                    }

                    // (4) 파일 내용 읽기
                    if (isFileExist) {
                        byte[] fileBytes = Files.readAllBytes(Paths.get(dirPath + "\\" + recvFileName));
                        sendData(socket, new String(fileBytes));
                        return;
                    }

                    // (5) 파일이 없다면 404 전송
                    sendData(socket, "404");
                    return;


                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println(seq + " thread end.");
            }

        });
    }

    // 3. HTTP 통신 테스트
    private static Thread testHttp(int seq, Socket socket) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(seq + " thread start");
                try{
                    // 1. 데이터 받기
                    InputStream is = socket.getInputStream();

                    String recvMessage = readData(socket);

                    // \r\n 을 기준으로 split - 마지막에 \r\n이 두번나오면 stop
                    String[] headerArr = recvMessage.split("\r\n");
//                    for(String s : headerArr) {
//                        System.out.println("데이터 : " + s);
//                    }

                    // 제일 첫번째 줄에서 정보를 꺼냄
                    String[] infos = headerArr[0].split(" ");
                    String httpMethod = infos[0];
                    String recvFileName = infos[1].substring(1);    // 맨앞에 '/' 제거
                    String httpVersion = infos[2];


                    // (3) 파일이 존재하는지 확인
                    String dirPath = "C:\\source\\ToyProjectWorkspace\\toy_socket\\src\\main\\resources";
                    File dir = new File(dirPath);
                    File[] files = dir.listFiles();
                    boolean isFileExist = false;
                    for (File file : files) {
                        if (recvFileName.equals(file.getName())) {
                            isFileExist = true;
                            break;
                        }
                    }

                    // (4) 파일 내용 읽기
                    String fileContents = null;
                    if (isFileExist) {
                        byte[] fileBytes = Files.readAllBytes(Paths.get(dirPath + "\\" + recvFileName));
                        fileContents = new String(fileBytes);
                    }

                    sendHttpResponse(socket, fileContents);

                }catch (Exception e) {
                    e.printStackTrace();
                }

            }});
    }

    ///////////////////////////////////////////////// 내부 메서드 ////////////////////////////////////////////////////////

    // 데이터 수신
    private static String readData(Socket socket) throws IOException {
        InputStream is = socket.getInputStream();

        byte[] bytes = new byte[2000];

        // 읽은 byte의 크기 반환
        int readByteCount = is.read(bytes);

        // 수신한 데이터 - byte 크기만큼 자른다.
        String recvMessage = new String(bytes, 0, readByteCount);

        System.out.println("클라이언트로부터 데이터 수신 :\n" + recvMessage);

        return recvMessage;
    }

    // 테이터 전송
    private static void sendData(Socket socket, String sendMessage) throws IOException {
        OutputStream os = socket.getOutputStream();
        os.write(sendMessage.getBytes());
        os.flush();

        System.out.println("클라이언트로 보낸 메세지 : " + sendMessage);
        System.out.println("****************** 전송완료 ******************\n");

    }

    // http로 데이터 전송
    private static void sendHttpResponse(Socket socket, String fileMessage) throws IOException {
        int contentLength = fileMessage.length();
        String sendMessage =
                "HTTP/1.1 200 OK" + "\r\n"
                        +"Date: Fri, 18 Aug 2021 15:36:25 GMT" + "\r\n"
                        +"X-UA-Compatible: IE=10" + "\r\n"
                        +"Cache-Control: no-cache, no-store, must-revalidate" + "\r\n"
                        +"Content-Type: text/plain;charset=UTF-8" + "\r\n"
                        +"Content-Language: ko-KR" + "\r\n"
                        +"Vary: Accept-Encoding" + "\r\n"
                        +"Content-Encoding: identity" + "\r\n"
                        +"X-UA-Device-Type: pc" + "\r\n"
                        +"Content-Length: " +contentLength+ "\r\n"
                        +"Connection: close" + "\r\n"
                        +"" + "\r\n"
                        +fileMessage + "\r\n"
                ;

        OutputStream os = socket.getOutputStream();
        os.write(sendMessage.getBytes());
        os.flush();

        System.out.println("클라이언트로 보낸 메세지 : " + sendMessage);
        System.out.println("****************** 전송완료 ******************\n");

    }

}
