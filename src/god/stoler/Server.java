package god.stoler;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
	// 保存接受的上文件 
	public static String            DEFAULT_STORE_DIR   = System.getProperty("user.home") + File.separator + "_test_" + File.separator; 
	private static final int        DEFAULT_SERVER_PORT = 9999;        // 主机默认端口
    private static final String     storeDir            = DEFAULT_STORE_DIR; 
    private static ExecutorService exeService = Executors.newFixedThreadPool(100);

    public Server() {
        init();
    }

    private void init() {
        try {
        	if (!new File(storeDir).exists()) {
        		new File(storeDir).mkdirs();
        	}
        	
            @SuppressWarnings("resource")
            ServerSocket serverSocket = new ServerSocket(DEFAULT_SERVER_PORT);

            while (true) {
                Socket clientRequest = serverSocket.accept();
                exeService.submit(new StoreThread(clientRequest));
            }
        } catch (Exception e) {
            System.out.println("ServerSocket Start Error on port - " + DEFAULT_SERVER_PORT);
        }
    }

    static class StoreThread extends Thread {
        Socket socket;

        public StoreThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                process();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void process() throws IOException, ClassNotFoundException {
            // 接收即将上传文件的文件名
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8")); 
            String fileName = reader.readLine();
            System.out.println("Received file name - " + fileName);

            // 响应文件名，同意接收
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8")); 
            writer.write("OK - " + fileName + "\r\n");
            writer.flush();
            
            // 接受目标文件并保存
            FileOutputStream out = new FileOutputStream(new File(storeDir + fileName));
            BufferedInputStream in = new BufferedInputStream(socket.getInputStream()); 
            byte[] bytes = new byte[1024]; 
            int byteReaded = -1; 
            while ((byteReaded = in.read(bytes)) != -1) {
                out.write(bytes, 0, byteReaded);
            }
            out.flush();
            out.close();
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        new Server();
    }

}
