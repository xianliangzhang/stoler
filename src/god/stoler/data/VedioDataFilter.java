package god.stoler.data;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class VedioDataFilter extends DataFilter {
    
    private Set<String> acceptTypes = new HashSet<String>(Arrays.asList(new String[]{".rm", ".rmvb", "avi"})); 
    private long minSize = 1024 * 1024; 
    private long maxSize = 1024 * 1024 * 1024; 
    
    @Override
    public boolean filter(File file) {
        String fileName = file.getName();
        if (fileName == null || "".equals(fileName) || !fileName.contains(".")) {
            return false; 
        }
        
        if (!acceptTypes.contains(fileName.substring(fileName.lastIndexOf(".")))) {
            //System.out.println("Illeagl file type - " + file.getAbsolutePath());
            return false; 
        }
        
        if (file.length() < minSize || file.length() > maxSize) {
            //System.out.println("Illegal file length - " + file.getAbsolutePath());
            return false; 
        }
        
        return true; 
    }

}
