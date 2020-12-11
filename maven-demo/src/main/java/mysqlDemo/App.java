package mysqlDemo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

//import java.sql.SQLException;
/**
 * Hello world!
 */
public final class App {
    private App() {
    }

    /**
     * Says hello to the world.
     * 
     * @param args The arguments of the program.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        int[] threads= MyUtil.StringToInts(MyUtil.GetProperties("threads"), ",") ;
        int[] loadNums=MyUtil.StringToInts(MyUtil.GetProperties("loadNums"), ",");
        
        System.out.println("hello world");

        // int turnShardingJdbcOn = Integer.parseInt(properties.getProperty("turnShardingJdbcOn")) ;
        // int turnShardingOn = Integer.parseInt(properties.getProperty("turnShardingOn"));
        // SqlUtil.MultiThreadsInsert(16, 1000);
        // SqlUtil.ShardingMultiThreadsInsertOneDatabase(16, 1000);
        // SqlUtil.ShardingMultiThreadsInsert(16, 1000);
        // SqlUtil.Select();
        // SqlUtil.OneSelect();
        // SqlUtil.TwoSelect();
        // SqlUtil.Undate();
        // SqlUtil.OneUndate();
        // SqlUtil.TwoUndate();
        //MyUtil.InsertLog(3, 4, 10, 10000, 100+"", 200+"");

        for(int i=0;i<threads.length;i++){
            for(int j=0;j<loadNums.length;j++){   
                SqlUtil.TruncateTable(1); 
                //不用shardingjdbc
                SqlUtil.MultiThreadsInsert(threads[i], loadNums[j]);
                SqlUtil.Select();
                SqlUtil.Undate();
                System.out.println(threads[i]+"线程,"+"次插入数为"+loadNums[j]+"的原生JDBC测试完成；");                          
            }
        }
        // for(int i=0;i<threads.length;i++){
        //     for(int j=0;j<loadNums.length;j++){   
        //         //不分库
        //         SqlUtil.TruncateTable(2); 
        //         SqlUtil.ShardingMultiThreadsInsertOneDatabase(threads[i],loadNums[j]);
        //         SqlUtil.OneSelect();
        //         SqlUtil.OneUndate();
        //         System.out.println(threads[i]+"线程,"+"次插入数为"+loadNums[j]+"的ShardingJDBC不分片测试完成；");                             
        //     }
        // }
        for(int i=0;i<threads.length;i++){
            for(int j=0;j<loadNums.length;j++){   
                SqlUtil.TruncateTable(3); 
                //启动shardingjdbc 切片分库
                SqlUtil.ShardingMultiThreadsInsert(threads[i],loadNums[j]);
                SqlUtil.TwoSelect();
                SqlUtil.TwoUndate();
                System.out.println(threads[i]+"线程,"+"次插入数为"+loadNums[j]+"的ShardingJDBC分片测试完成；");                          
            }
        }   
        
    }
}


