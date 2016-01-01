package god.stoler.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

public class TransferThread extends Thread {

    private File file;     // 待上传的文件 
    private String host;   // 服务器主机IP
    private int port;      // 服务器接收文件端口
    private boolean showTransferDetail;   // 是否打印上传明细
    
    public TransferThread(File file, String host, int port, boolean showTransferDetail) {
        this.file = file; 
        this.host = host; 
        this.port = port; 
        this.showTransferDetail = showTransferDetail;
        if (this.showTransferDetail) {
        	System.out.println("transfer - " + file.getName());
        }
    }
    
    @Override
    public void run() {
        try {
            Socket socket = new Socket(host, port);
            transfer(socket, file); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 将目标文件上传到服务器 
     * @param socket 客户端与服务器端的 Socket 连接
     * @param file   待上传的文件 
     * @throws IOException 传输失败异常
     */
    private void transfer(Socket socket, File file) {
        try {
            // 传送文件名，将等待服务器响应，以确定是否接受此文件 
            Writer writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
            writer.write(file.getName());
            writer.write("\r\n"); 
            writer.flush(); 
            
            // 接受服务器响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8")); 
            String response = reader.readLine(); 
            System.out.println(response);
            
            // 传送目标文件
            OutputStream out = socket.getOutputStream(); 
            @SuppressWarnings("resource")
            FileInputStream in = new FileInputStream(file); 
            byte[] bytes = new byte[1024]; 
            int readBytes = -1; 
            while ((readBytes = in.read(bytes)) != -1) {
                out.write(bytes, 0, readBytes);
            }
            out.flush();
            out.close();
        } catch (IOException ioe) {
            System.out.println("Send Request Error - " + file.getName());
        }
    }
    
    public static void main(String[] args) {
    	String file = "/Users/Willer/lab/IMG_0051.JPG"; 
        new TransferThread(new File(file), "localhost", 9999, true).start();
    }

}
