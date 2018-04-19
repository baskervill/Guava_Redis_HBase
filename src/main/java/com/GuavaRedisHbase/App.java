package com.GuavaRedisHbase;

import java.util.concurrent.TimeUnit;  
import java.util.concurrent.ExecutionException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Long;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Random;

  import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.coprocessor.Batch;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.ipc.BlockingRpcCallback;
import org.apache.hadoop.hbase.ipc.ServerRpcController;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.ipc.CoprocessorRpcChannel;

import com.GuavaRedisHbase.RedisHbasePro;
import com.GuavaRedisHbase.RedisHbasePro.getValueRequest;
import com.GuavaRedisHbase.RedisHbasePro.getBackResultResponse;
import com.GuavaRedisHbase.RedisHbasePro.RedisHbaseProService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
/**
 * Hello world!
 *
 */
public class App 
{
    //setup the hbase configure
    void createTable(String tableName) 
    {
      try{
        Configuration config = new Configuration();
        HBaseAdmin admin = new HBaseAdmin(config);
        HTableDescriptor tableDesc = new HTableDescriptor(tableName);
        if(admin.tableExists(tableName) == true) {
          admin.disableTable(tableName);
          admin.deleteTable(tableName);
        }
        tableDesc.addFamily(new HColumnDescriptor("c1")); //add column family
        tableDesc.addCoprocessor("com.GuavaRedisHbase.coprocessor.RedisHBaseObserver");
        tableDesc.addCoprocessor("com.GuavaRedisHbase.coprocessor.RedisHbaseEndPoint");
        admin.createTable(tableDesc);
      
      }
      catch(Exception e) {e.printStackTrace();}
    }
 
    void populateTenRows(Guava guava , String tableName, int datanum)
    {
      try{
        Configuration config = new Configuration();
        HConnection conn = HConnectionManager.createConnection(config);
        HTableInterface tbl = conn.getTable(tableName);
        //insert 1000
        for(int i=0; i< datanum; i++)
        {
          String rowkey = "r" + Integer.toString(i);
          Put put = new Put(rowkey.getBytes());
          put.add("c1".getBytes(),"col1".getBytes(),rowkey.getBytes());
          guava.put("r"+i+"_c1_col1","r"+i);
          tbl.put(put);           
        }
      }
      catch(Exception e) {e.printStackTrace();}
    }

    


    public static void main(String[] args ) throws Exception
    {
       
        FileOutputStream fs = new FileOutputStream(new File("guavaredishbase_time.txt"));
        PrintStream p = new PrintStream(fs);
        Guava guava = new Guava(25000);
        String tblName = args[0];
        int testNumber =  Integer.parseInt(args[1]);

        //guava.setMaximumSize(Integer.parseInt(args[2]));
        App app = new App();
        app.createTable(tblName);
        app.populateTenRows(guava ,tblName,50000);
        Configuration config = new Configuration();

        HConnection connection = HConnectionManager.createConnection(config);
        TableName tableName = TableName.valueOf(tblName);
        HTableInterface table = connection.getTable(tableName);
        
       
        
       /* 
        Result hbaseresult = null;
	      String abc =null;
        long startTime1 = System.currentTimeMillis();
        for(int i = 0;i < 500000;i++){
            Random r = new Random();
            int num = r.nextInt(1000000);
            String keys = "r"+Integer.toString(num);
            Get get = new Get(Bytes.toBytes(keys));
            hbaseresult = table.get(get);
             for(KeyValue value:hbaseresult.raw()){
                //System.out.println("cf:"+new String(value.getFamily())+new String(value.getQualifier())+"="+new String(value.getValue()));
                abc = Bytes.toString(value.getValue());
            }
            //p.println("result is　a " + guava.get(table , tblName,"r"+num+"_c1_col1"));
            //guava.get(table , tblName,"r"+num+"_c1_col1");
        }
        long endTime1 = System.currentTimeMillis();
        System.out.println("2500 data time : "+(endTime1-startTime1)+"ms");
        p.println("2500 data time : "+(endTime1-startTime1)+"ms");

        System.out.println(guava.reportStatus());*/
        //guava.invalidateAll(); 
        long startTime1 = System.currentTimeMillis();
        for(int i = 0;i < testNumber;i++){
            Random r = new Random();
            int num = r.nextInt(50000);
            //p.println("result is　a " + guava.get(table , tblName,"r"+num+"_c1_col1"));
            guava.get(table , tblName,"r"+num+"_c1_col1");
        }
        long endTime1 = System.currentTimeMillis();
        System.out.println(Integer.toString(testNumber)+"data time : "+(endTime1-startTime1)+"ms");
        p.println(Integer.toString(testNumber)+"data time : "+(endTime1-startTime1)+"ms");

        System.out.println(guava.reportStatus());
        System.exit(0);
        
    } 
}
