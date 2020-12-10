package mysqlDemo;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;

//获取到db.properties文件中的数据库信息
public class JdbcUtil {

    public static Connection getJdbcConnection() throws Exception {
        
        Connection conn = null;
        BasicDataSource bds = null;
        try {
            //4.根据键，获取properties中对应的值
            String driver = MyUtil.GetProperties("driver");
            String user = MyUtil.GetProperties("user");
            String password = MyUtil.GetProperties("password");
            String url = MyUtil.GetProperties("url");
            //连接池
            bds = new BasicDataSource();
            bds.setDriverClassName(driver);
            bds.setUrl(url);
            bds.setUsername(user);
            bds.setPassword(password);
            conn = bds.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return conn;
    }

    // 返回数据库连接
    public static Connection getShardingJdbcConnection() throws Exception {
        //4.根据键，获取properties中对应的值
        String user = MyUtil.GetProperties("user");
        String password = MyUtil.GetProperties("password");
        String mysqlHost = MyUtil.GetProperties("mysqlHost");
        
        Map<String,DataSource> dataSourceMap = new HashMap<>();
        // 配置第一个数据源
        BasicDataSource dataSource1 = new BasicDataSource();
        dataSource1.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource1.setUrl("jdbc:mysql://"+ mysqlHost +"/sbtest0");
        dataSource1.setUsername(user);
        dataSource1.setPassword(password);
        dataSourceMap.put("sbtest0", dataSource1);
        // 配置第二个数据源
        //org.apache.commons.dbcp.BasicDataSource
        BasicDataSource dataSource2 = new BasicDataSource();
        dataSource2.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource2.setUrl("jdbc:mysql://"+ mysqlHost +"/sbtest1");
        dataSource2.setUsername(user);
        dataSource2.setPassword(password);
        dataSourceMap.put("sbtest1", dataSource2);
        // 配置device表规则
        TableRuleConfiguration deviceTableRuleConfig = new TableRuleConfiguration("device","sbtest${0..1}.device${0..1}");
        // 配置分库策略
        deviceTableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("id", "sbtest${id % 2}") );
        // 配置分表策略
        deviceTableRuleConfig.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id", "device${order_id % 2}"));
        
        // 配置分片规则
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(deviceTableRuleConfig);
        
        //
        try {
            DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, 
                    new Properties());
            Connection connection = dataSource.getConnection();
            //返回数据库连接
            return connection;
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    //不分库连接
    public static Connection getShardingJdbcOneConnection() throws Exception {
        String user = MyUtil.GetProperties("user"); 
        String password = MyUtil.GetProperties("password");
        String mysqlHost = MyUtil.GetProperties("mysqlHost");
        
        Map<String,DataSource> dataSourceMap = new HashMap<>();
        // 配置第一个数据源
        BasicDataSource dataSource1 = new BasicDataSource();
        dataSource1.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource1.setUrl("jdbc:mysql://"+ mysqlHost +"/sbtest");
        dataSource1.setUsername(user);
        dataSource1.setPassword(password);
        dataSourceMap.put("sbtest", dataSource1);
        // 配置第二个数据源
        //org.apache.commons.dbcp.BasicDataSource
        // BasicDataSource dataSource2 = new BasicDataSource();
        // dataSource2.setDriverClassName("com.mysql.cj.jdbc.Driver");
        // dataSource2.setUrl("jdbc:mysql://"+ mysqlHost +"/sbtest");
        // dataSource2.setUsername(user);
        // dataSource2.setPassword(password);
        // dataSourceMap.put("sbtest1", dataSource2);
        // 配置device表规则
        TableRuleConfiguration deviceTableRuleConfig = new TableRuleConfiguration("device","sbtest.device${0..1}");
        // 配置分库策略
        // deviceTableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("id", "sbtest${id % 2}") );
        // 配置分表策略
        deviceTableRuleConfig.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id", "device${order_id % 2}"));
        
        // 配置分片规则
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(deviceTableRuleConfig);
        
        //
        try {
            DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, 
                    new Properties());
            Connection connection = dataSource.getConnection();
            //返回数据库连接
            return connection;
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }


    //日志数据库
    public static Connection getLogConnection() throws Exception {
        Connection conn = null;
        BasicDataSource bds = null;
        try {
            
            String driver = MyUtil.GetProperties("driver");
            String user = MyUtil.GetProperties("user");
            String password = MyUtil.GetProperties("password");
            String logHost = MyUtil.GetProperties("logHost");
            String url = "jdbc:mysql://"+ logHost +"/log";
            //连接池
            bds = new BasicDataSource();
            bds.setDriverClassName(driver);
            bds.setUrl(url);
            bds.setUsername(user);
            bds.setPassword(password);
            conn = bds.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } 
        return conn;
    }
}