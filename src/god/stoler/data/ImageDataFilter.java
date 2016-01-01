package god.stoler.data;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ImageDataFilter extends DataFilter  {
    
    private Set<String> acceptType = new HashSet<String>(Arrays.asList(new String[]{".png", ".jpg", "gif"})); 
    private long minSize = 1024 * 100;           // 100KB
    private long maxSize = 1024 * 1024 * 1024;   // 1024MB
            
    @Override
    public boolean filter(File file) {
        String fileName = file.getName(); 
        if (fileName == null || "".equals(fileName) || !fileName.contains(".")) {
            return false;
        }
        
        String suffix = fileName.substring(fileName.lastIndexOf(".")); 
        if (!acceptType.contains(suffix.toLowerCase())) {
            //System.out.println("Illegal file type - " + file.getAbsolutePath());
            return false;
        }
        if (file.length() < minSize || file.length() > maxSize) {
            //System.out.println("Illegal file size - " + file.getAbsolutePath());
            return false; 
        }
        return true;
    }

}
