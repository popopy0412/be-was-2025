package webserver;

import java.io.*;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.FileUtil;
import util.HttpRequestParser;
import util.HttpRequestUtil;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private static final String RESOURCE_PATH = "./src/main/resources/static";

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8")); // InputStream => InputStreamReader => BufferedReader
            String startLine = HttpRequestParser.parseRequest(br);

            DataOutputStream dos = new DataOutputStream(out);
            String url = HttpRequestUtil.getUrl(startLine); // 분할한 토큰들 중 URL을 추출

            if (url == null) {
                return;
            }

            String path = RESOURCE_PATH + url;
            path = HttpRequestUtil.buildPath(path);
            String type = HttpRequestUtil.getType(path); // 파일 유형 별로 Content-Type 할당

            byte[] body = FileUtil.fileToByteArray(path); // 해당 파일의 경로를 byte로 전달

            if (body == null) {
                response404Header(dos);
            } else {
                response200Header(dos, type, body.length);
                responseBody(dos, body);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, String type, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + type + ";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void response404Header(DataOutputStream dos) throws IOException {
        dos.writeBytes("HTTP/1.1 404 Not Found \r\n");
        dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
        dos.writeBytes("\r\n");
        dos.writeBytes("<h1>404 Not Found</h1>");
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
