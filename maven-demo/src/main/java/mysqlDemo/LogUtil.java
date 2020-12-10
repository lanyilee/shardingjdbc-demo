package mysqlDemo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class LogUtil {
    public static void charOutStream(String fileName, String log) {
        // 1：利用File类找到要操作的对象
        File file = new File("." + File.separator + fileName + ".txt");





        
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        // 2：准备输出流
        Writer out;
        try {
            out = new FileWriter(file,true);
            out.write(log);
            out.write("\n");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

            
    }
    
}