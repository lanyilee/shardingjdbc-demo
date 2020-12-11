package mysqlDemo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;

public class MyUtil {

    public static int[] StringToInts(String strs,String a){
        String[] strArray = strs.split(a);
        int[] result = new int[strArray.length];
        int i=0;
        for (String string : strArray) {
            result[i] = Integer.parseInt(string);
            i++;
        }
        return result;
        
    }

    public static String GetProperties(String key){
        // 1.新建属性集对象
        Properties properties = new Properties();
        // 使用InPutStream流读取properties文件
        
        try {
            //BufferedReader bufferedReader = new BufferedReader(new FileReader("maven-demo/db.properties"));
            BufferedReader bufferedReader = new BufferedReader(new FileReader("./db.properties"));
            //BufferedReader bufferedReader = new BufferedReader(new FileReader("/root/mysql/script/insert/java/db.properties"));
            properties.load(bufferedReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
         
        //  Properties properties = new Properties();
        //  //2通过反射，新建字符输入流，读取db.properties文件
        //  InputStream input = JdbcUtil.class.getClassLoader().getResourceAsStream("./db.properties");
        //  //3.将输入流中读取到的属性，加载到properties属性集对象中
        //  try {
        //      properties.load(input);
        //  } catch (IOException e1) {
        //      e1.printStackTrace();
        //  }
         //4.根据键，获取properties中对应的值
         String value = properties.getProperty(key);
         return value;
    }

    public static void InsertLog(int type,int allData,String spentTime,String tps,int thread, int loadNum)
            throws Exception {
        Connection conn = JdbcUtil.getLogConnection();
        String sql ="insert into ShardingLog values(null,?,?,?,?,?,?)";
        try {
            conn.setAutoCommit(true);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, type);       
            ps.setInt(2, allData);
            ps.setString(3, spentTime);
            ps.setString(4, tps);
            ps.setInt(5, thread);
            ps.setInt(6, loadNum);
            int a = ps.executeUpdate();
            System.out.println(a);
            ps.close(); 
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
        finally{
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

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