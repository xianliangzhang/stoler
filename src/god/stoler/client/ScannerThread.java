package god.stoler.client;

import god.stoler.data.DataFilter;

import java.io.File;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class ScannerThread extends Thread {
    // 如果保存到本机，忽略保存目录 	
	private static final String IGNORE_STORE_DIR = System.getProperty("user.home") + File.separator + "_test_";

    private final String rootDirectory;
    private Set<DataFilter> dataFilters;
    private BlockingQueue<File> dataQueue;

    public ScannerThread(String rootDir, Set<DataFilter> dataFilters, BlockingQueue<File> dataQueue) {
        this.rootDirectory = rootDir;
        this.dataFilters = dataFilters;
        this.dataQueue = dataQueue;
    }

    @Override
    public void run() {
        scan(new File(rootDirectory));
    }

    private void scan(File dir) {
        File[] files = dir.listFiles();

        for (int i = 0; files != null && i < files.length; i++) {
            if (files[i].isDirectory() && !files[i].getName().startsWith(IGNORE_STORE_DIR)) {
                scan(files[i]);
            } else {
                processFile(files[i]);
            }
        }
    }

    private void processFile(File file) {
        boolean filterResult = false;
        for (DataFilter filter : dataFilters) {
            if (filter.filter(file)) {
                filterResult = true;
                break;
            }
        }
        if (filterResult) {
            dataQueue.offer(file);
            System.out.println("Found target file - " + dataQueue.size() + " - " + file.getAbsolutePath());
        } else {
        	System.out.println("IGNORE - " + file.getAbsolutePath());
        }
    }

}
