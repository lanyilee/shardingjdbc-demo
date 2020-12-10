package mysqlDemo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.CountDownLatch;


public class SqlUtil {

    public static void MultiInsert(Integer loadNums) throws Exception {
        long start = System.currentTimeMillis();
        // String sql= "insert into device values(?,?,?,?,?,?,?,?,?)";
        String sql = "insert into device values (null,'xuliehao12345','changshangbiaoshi521','dianxin','5g','xiaojizhan','X521dcb','micro520','tac123')";
        Connection conn = JdbcUtil.getJdbcConnection();
        
        // 每次插入loadNums条数据，循环nums次，数据总量为200万；
        Integer nums = 200000 / loadNums;
        System.out.println("nums: " + nums);
        try {
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement(sql);
            for (int i = 0; i < nums; i++) {
                for (int j=0;j<loadNums;j++){
                    ps.addBatch();
                }
                // 每loadNums条记录插入一次
                ps.executeBatch();
                conn.commit();
                ps.clearBatch();
            }
            // 剩余数量不足
            // ps.executeBatch();
            // conn.commit();
            // ps.clearBatch();
            // String selectSql = "select count(id) as num from device";
            // ResultSet rs = ps.executeQuery(selectSql);
            // while (rs.next()) {
            //     System.out.println("total data counts :" + rs.getString("num"));
            // }
            // rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("耗时：" + (end - start));
        float tps = 200000*1000/(end-start);
        System.out.println("TPS:" + tps);
    }

    //原生插入
    public  static void MultiThreadsInsert(Integer threadNum,Integer loadNum) throws Exception {
        final CountDownLatch cdl= new CountDownLatch(threadNum);//定义线程数量
        final Integer allData = Integer.parseInt(MyUtil.GetProperties("allData")) ;
        long start = System.currentTimeMillis();
        String sql = "insert into device values (?,'xuliehao12345','changshangbiaoshi521','dianxin','5g','xiaojizhan','X521dcb','micro520','tac123',?)";
        
        //每次插入loadNums条数据，循环nums次，数据总量为200万；
        Integer nums = allData/(loadNum*threadNum);
        for(int k=0;k<threadNum;k++){
            String ks = k +"";
            new Thread(new Runnable() {
                @Override
                public void run() {
                    
                    Integer ksNum = Integer.parseInt(ks);
                    int startIndex = allData*ksNum/threadNum;
                    System.out.println("线程"+ ks +"插入数据的初始位置：" + startIndex);
                    try {
                        Connection conn = JdbcUtil.getJdbcConnection();
                        conn.setAutoCommit(false);
                        PreparedStatement ps = conn.prepareStatement(sql);
                        for (int i = 0; i < nums; i++) {
                            for (int j=1;j<=loadNum;j++){
                                int indexId =  startIndex + i*loadNum+j;  
                                ps.setInt(1, indexId);
                                ps.setInt(2, indexId);
                                ps.addBatch();
                            }
                            // 每loadNums条记录插入一次
                            ps.executeBatch();
                            conn.commit();
                            ps.clearBatch();
                        }
                        conn.close();
                        cdl.countDown();    //执行完一个线程，递减1                   
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            },ks).start();
        }
        try{
            cdl.await(); // 前面线程没执行完，其他线程等待，不往下执行
            long spendtime = (System.currentTimeMillis() - start)/1000;
            String log1 = threadNum + "个线程花费时间: " + spendtime+"s"; 
            System.out.println(log1);
            MyUtil.charOutStream("thread_" + threadNum + "_loadNum_" + loadNum, log1);
            float tps = allData/spendtime;
            String log2 = "TPS:" + tps;
            System.out.println(log2);
            MyUtil.charOutStream("thread_"+threadNum+"_loadNum_"+loadNum, log2);
            //插入日志数据库
            MyUtil.InsertLog(1, allData, spendtime+"", tps+"", threadNum, loadNum);
            System.out.println(threadNum + "个线程花费时间:" + spendtime);

        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }


    public static void ShardingMultiInsert(Integer loadNums) throws Exception {
        long start = System.currentTimeMillis();
        // String sql= "insert into device values(?,?,?,?,?,?,?,?,?)";
        String sql = "insert into device values (?,'xuliehao12345','changshangbiaoshi521','dianxin','5g','xiaojizhan','X521dcb','micro520','tac123',?)";
        Connection conn = JdbcUtil.getShardingJdbcConnection();
        
        // 每次插入loadNums条数据，循环nums次，数据总量为200万；
        Integer nums = 200000 / loadNums;
        System.out.println("nums: " + nums);
        try {
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement(sql);
            for (int i = 0; i < nums; i++) {
                for (int j=1;j<=loadNums;j++){
                    //ps.setString(1, i*loadNums+j+"");
                    ps.setInt(1, i*loadNums+j);
                    ps.setInt(2, i*loadNums+j);
                    ps.addBatch();
                }
                // 每loadNums条记录插入一次
                ps.executeBatch();
                conn.commit();
                ps.clearBatch();
            }
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("耗时：" + (end - start));
        float tps = 200000*1000/(end-start);
        System.out.println("TPS：" + tps);
    }

    //切片分库插入
    public  static void ShardingMultiThreadsInsert(Integer threadNum,Integer loadNum) throws Exception {
        final CountDownLatch cdl= new CountDownLatch(threadNum);//定义线程数量
        final Integer allData = Integer.parseInt(MyUtil.GetProperties("allData")) ;
        long start = System.currentTimeMillis();
        String sql = "insert into device values (?,'xuliehao12345','changshangbiaoshi521','dianxin','5g','xiaojizhan','X521dcb','micro520','tac123',?)";
        
        //每次插入loadNums条数据，循环nums次，数据总量为200万；
        Integer nums = allData/(loadNum*threadNum);
        for(int k=0;k<threadNum;k++){
            String ks = k +"";
            new Thread(new Runnable() {
                @Override
                public void run() {
                    
                    Integer ksNum = Integer.parseInt(ks);
                    int startIndex = allData*ksNum/threadNum;
                    System.out.println("线程"+ ks +"插入数据的初始位置：" + startIndex);
                    try {
                        Connection conn = JdbcUtil.getShardingJdbcConnection();
                        conn.setAutoCommit(false);
                        PreparedStatement ps = conn.prepareStatement(sql);
                        for (int i = 0; i < nums; i++) {
                            for (int j=1;j<=loadNum;j++){                              
                                int indexId =  startIndex + i*loadNum+j;                              
                                ps.setInt(1, indexId);
                                ps.setInt(2, indexId);
                                ps.addBatch();
                            }
                            // 每loadNums条记录插入一次
                            ps.executeBatch();
                            conn.commit();
                            ps.clearBatch();
                        }
                        conn.close();
                        cdl.countDown();    //执行完一个线程，递减1                   
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            },ks).start();
        }
        try{
            cdl.await(); // 前面线程没执行完，其他线程等待，不往下执行
            long spendtime = (System.currentTimeMillis() - start)/1000;
            String log1 = threadNum + "个线程花费时间: " + spendtime+"ms";           
            System.out.println(log1);
            LogUtil.charOutStream("thread_"+threadNum+"_loadNum_"+loadNum, log1);
            float tps = allData/spendtime;
            String log2 = "TPS:" + tps;
            System.out.println(log2);
            LogUtil.charOutStream("thread_"+threadNum+"_loadNum_"+loadNum, log2);
            //插入日志数据库
            MyUtil.InsertLog(3,  allData, spendtime+"", tps+"",threadNum, loadNum);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

 
    //切片不分库插入
    public static void ShardingMultiThreadsInsertOneDatabase(Integer threadNum,Integer loadNum) throws Exception{
        final CountDownLatch cdl= new CountDownLatch(threadNum);//定义线程数量
        final Integer allData = Integer.parseInt(MyUtil.GetProperties("allData")) ;
        long start = System.currentTimeMillis();
        String sql = "insert into device values (?,'xuliehao12345','changshangbiaoshi521','dianxin','5g','xiaojizhan','X521dcb','micro520','tac123',?)";
        
        //每次插入loadNums条数据，循环nums次，数据总量为200万；
        Integer nums = allData/(loadNum*threadNum);
        for(int k=0;k<threadNum;k++){
            String ks = k +"";
            new Thread(new Runnable() {
                @Override
                public void run() {
                    
                    Integer ksNum = Integer.parseInt(ks);
                    int startIndex = allData*ksNum/threadNum;
                    System.out.println("线程"+ ks +"插入数据的初始位置：" + startIndex);
                    try {
                        Connection conn = JdbcUtil.getShardingJdbcOneConnection();
                        conn.setAutoCommit(false);
                        PreparedStatement ps = conn.prepareStatement(sql);
                        for (int i = 0; i < nums; i++) {
                            for (int j=1;j<=loadNum;j++){                              
                                int indexId =  startIndex + i*loadNum+j;                              
                                ps.setInt(1, indexId);
                                ps.setInt(2, indexId);
                                ps.addBatch();
                            }
                            // 每loadNums条记录插入一次
                            ps.executeBatch();
                            conn.commit();
                            ps.clearBatch();
                        }
                        conn.close();
                        cdl.countDown();    //执行完一个线程，递减1                   
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            },ks).start();
        }
        try{
            cdl.await(); // 前面线程没执行完，其他线程等待，不往下执行
            long spendtime = (System.currentTimeMillis() - start)/1000;
            String log1 = threadNum + "个线程花费时间: " + spendtime+"ms";           
            System.out.println(log1);
            LogUtil.charOutStream("thread_"+threadNum+"_loadNum_"+loadNum, log1);
            float tps = allData/spendtime;
            String log2 = "TPS:" + tps;
            System.out.println(log2);
            LogUtil.charOutStream("thread_"+threadNum+"_loadNum_"+loadNum, log2);
            //插入日志数据库
            MyUtil.InsertLog(2,  allData, spendtime+"", tps+"",threadNum, loadNum);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }


    //原生jdbc查询
    public static void Select() throws Exception{
        int selectData = 1000;
        long start = System.currentTimeMillis();
        String sql = "select * from  device  where order_id=?";
        try {
            Connection conn = JdbcUtil.getJdbcConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            for(int k=1;k<=selectData;k++){
                ps.setInt(1, k*10);
                ps.executeQuery();
            }
            ps.close();
            conn.close();   
            long spendtime = (System.currentTimeMillis() - start);
            String log1 = "原生jdbc查询花费时间: " + spendtime+"ms"; 
            System.out.println(log1);
            //LogUtil.charOutStream("thread_"+threadNum+"_loadNum_"+loadNum, log1);
            float qps = selectData*1000/spendtime;
            String log2 = "QPS:" + qps;
            System.out.println(log2);
            //LogUtil.charOutStream("thread_"+threadNum+"_loadNum_"+loadNum, log2);
            //插入日志数据库
            MyUtil.InsertLog(4, selectData, spendtime+"", qps+"", 1, 1); 
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //原生jdbc更新
    public static void Undate() throws Exception{
        int selectData = 1000;
        long start = System.currentTimeMillis();
        String sql = "update device set tac = '123abc'  where order_id=?";
        try {
            Connection conn = JdbcUtil.getJdbcConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            for(int k=1;k<=selectData;k++){
                ps.setInt(1, k*10);
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();
            conn.close();   
            long spendtime = (System.currentTimeMillis() - start);
            String log1 = "原生jdbc更新花费时间: " + spendtime+"ms"; 
            System.out.println(log1);
            //LogUtil.charOutStream("thread_"+threadNum+"_loadNum_"+loadNum, log1);
            float qps = selectData*1000/spendtime;
            String log2 = "TPS:" + qps;
            System.out.println(log2);
            //LogUtil.charOutStream("thread_"+threadNum+"_loadNum_"+loadNum, log2);
            //插入日志数据库
            MyUtil.InsertLog(5, selectData, spendtime+"", qps+"", 1, 1); 
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //单库2表shardingjdbc查询
    public static void OneSelect() throws Exception{
        int selectData = 1000;
        long start = System.currentTimeMillis();
        String sql = "select * from  device  where order_id=?";
        try {
            Connection conn = JdbcUtil.getShardingJdbcOneConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            for(int k=1;k<=selectData;k++){
                ps.setInt(1, k*10);
                ps.executeQuery();
            }
            ps.close();
            conn.close();   
            long spendtime = (System.currentTimeMillis() - start);
            String log1 = "Shardingjdbc单库查询花费时间: " + spendtime+"ms"; 
            System.out.println(log1);
            //LogUtil.charOutStream("thread_"+threadNum+"_loadNum_"+loadNum, log1);
            float qps = selectData*1000/spendtime;
            String log2 = "QPS:" + qps;
            System.out.println(log2);
            //LogUtil.charOutStream("thread_"+threadNum+"_loadNum_"+loadNum, log2);
            //插入日志数据库
            MyUtil.InsertLog(4, selectData, spendtime+"", qps+"", 1, 1); 
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //单库2表shardingjdbc更新
    public static void OneUndate() throws Exception{
        int selectData = 1000;
        long start = System.currentTimeMillis();
        String sql = "update device set tac = '123abc'  where order_id=?";
        try {
            Connection conn = JdbcUtil.getShardingJdbcOneConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            for(int k=1;k<=selectData;k++){
                ps.setInt(1, k*10);
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();
            conn.close();   
            long spendtime = (System.currentTimeMillis() - start);
            String log1 = "Shardingjdbc单库更新花费时间: " + spendtime+"ms"; 
            System.out.println(log1);
            //LogUtil.charOutStream("thread_"+threadNum+"_loadNum_"+loadNum, log1);
            float qps = selectData*1000/spendtime;
            String log2 = "TPS:" + qps;
            System.out.println(log2);
            //LogUtil.charOutStream("thread_"+threadNum+"_loadNum_"+loadNum, log2);
            //插入日志数据库
            MyUtil.InsertLog(5, selectData, spendtime+"", qps+"", 1, 1); 
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //2库2表shardingjdbc查询
    public static void TwoSelect() throws Exception{
        int selectData = 1000;
        long start = System.currentTimeMillis();
        String sql = "select * from  device  where order_id=?";
        try {
            Connection conn = JdbcUtil.getShardingJdbcOneConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            for(int k=1;k<=selectData;k++){
                ps.setInt(1, k*10);
                ps.executeQuery();
            }
            ps.close();
            conn.close();   
            long spendtime = (System.currentTimeMillis() - start);
            String log1 = "Shardingjdbc双库查询花费时间: " + spendtime+"ms"; 
            System.out.println(log1);
            //LogUtil.charOutStream("thread_"+threadNum+"_loadNum_"+loadNum, log1);
            float qps = selectData*1000/spendtime;
            String log2 = "QPS:" + qps;
            System.out.println(log2);
            //LogUtil.charOutStream("thread_"+threadNum+"_loadNum_"+loadNum, log2);
            //插入日志数据库
            MyUtil.InsertLog(4, selectData, spendtime+"", qps+"", 1, 1);  
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //2库2表shardingjdbc更新
    public static void TwoUndate() throws Exception{
        int selectData = 1000;
        long start = System.currentTimeMillis();
        String sql = "update device set tac = '123abc'  where order_id=?";
        try {
            Connection conn = JdbcUtil.getShardingJdbcConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            for(int k=1;k<=selectData;k++){
                ps.setInt(1, k*10);
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();
            conn.close();   
            long spendtime = (System.currentTimeMillis() - start);
            String log1 = "Shardingjdbc双库更新花费时间: " + spendtime+"ms"; 
            System.out.println(log1);
            //LogUtil.charOutStream("thread_"+threadNum+"_loadNum_"+loadNum, log1);
            float qps = selectData*1000/spendtime;
            String log2 = "TPS:" + qps;
            System.out.println(log2);
            //LogUtil.charOutStream("thread_"+threadNum+"_loadNum_"+loadNum, log2);
            //插入日志数据库
            MyUtil.InsertLog(5, selectData, spendtime+"", qps+"", 1, 1); 
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void TruncateTable(int num) throws Exception {
        Connection conn = null;
        try {
            if (num==1){
                conn = JdbcUtil.getJdbcConnection();
            }else if (num==2){
                conn = JdbcUtil.getShardingJdbcOneConnection();
            }else{
                conn = JdbcUtil.getShardingJdbcConnection();
            }         
            String sql="truncate table device;";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.execute();
            ps.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

