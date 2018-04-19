    package com.GuavaRedisHbase.coprocessor;  
      
    import java.io.IOException;  
    import java.util.ArrayList;  
    import java.util.List;  
    
    import org.apache.commons.logging.Log;
    import org.apache.commons.logging.LogFactory;

    import org.apache.hadoop.hbase.KeyValue;
    import org.apache.hadoop.hbase.client.Result;
    import org.apache.hadoop.hbase.client.ResultScanner;

    import org.apache.hadoop.hbase.Coprocessor;  
    import org.apache.hadoop.hbase.CoprocessorEnvironment;  
    import org.apache.hadoop.hbase.client.Scan; 
    import org.apache.hadoop.hbase.client.Get; 
    import org.apache.hadoop.hbase.coprocessor.CoprocessorException;  
    import org.apache.hadoop.hbase.coprocessor.CoprocessorService;  
    import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;  
    import org.apache.hadoop.hbase.filter.CompareFilter;  
    import org.apache.hadoop.hbase.filter.Filter;  
    import org.apache.hadoop.hbase.filter.FilterList;  
    import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;  
    import org.apache.hadoop.hbase.protobuf.ResponseConverter;  
    import org.apache.hadoop.hbase.regionserver.InternalScanner;  
    import org.apache.hadoop.hbase.util.Bytes;  
    import org.apache.hadoop.hbase.Cell;  
    import org.apache.hadoop.hbase.CellUtil;  
      
    import com.google.protobuf.RpcCallback;  
    import com.google.protobuf.RpcController;  
    import com.google.protobuf.Service;  
   
    import com.GuavaRedisHbase.RedisHbasePro;
	
	
	import redis.clients.jedis.HostAndPort;
	import redis.clients.jedis.JedisCluster;

	import com.GuavaRedisHbase.RedisCluster;
      
    public class RedisHbaseEndPoint extends RedisHbasePro.RedisHbaseProService  
            implements Coprocessor, CoprocessorService {  
       
        private static final Log LOG = LogFactory.getLog(RedisHbaseEndPoint.class);
        private RegionCoprocessorEnvironment env;  
		public static  RedisCluster redisCluster = null;
		
      
        @Override  
        public void start(CoprocessorEnvironment env) throws IOException {  
            if (env instanceof RegionCoprocessorEnvironment) {  
                this.env = (RegionCoprocessorEnvironment) env;  
            } else {  
                throw new CoprocessorException("Must be loaded on a table region!");  
            }  
			//init the redis
			List<HostAndPort> list = new ArrayList<HostAndPort>();
			list.add(new HostAndPort("127.0.0.1", 7000));
			list.add(new HostAndPort("127.0.0.1", 7001));
			initRedisCluster(list);

        }  
      
        @Override  
        public void stop(CoprocessorEnvironment arg0) throws IOException {  
      
        }  
      
        @Override  
        public Service getService() {  
            return this;  
        }  
		//init the redis
		private void initRedisCluster(List<HostAndPort> list){
			if(list == null){
				System.err.println("make the list first!");
				System.exit(0);
			}
			this.redisCluster = new RedisCluster(list);
		}
      
        @Override  
        public void getVauleFromCo(RpcController controller, RedisHbasePro.getValueRequest request, RpcCallback<RedisHbasePro.getBackResultResponse> done) {  
             
            // String userkey = request.getUserKey();
             
             String rowKey = request.getRowKey();
             String family = request.getFamily();
             String column = request.getColumn();
             String BackResult = rowKey+family+column;
			 String val = null;
			 String key = rowKey+"_"+family+"_"+column;
             RedisHbasePro.getBackResultResponse.Builder responseBuilder = RedisHbasePro.getBackResultResponse.newBuilder(); 
             InternalScanner scanner = null;
             Result hbaseresult = null;
             Get get = new Get(Bytes.toBytes(request.getRowKey()));
             
             //Scan scan = new Scan();
             get.addFamily(Bytes.toBytes(request.getFamily()));
             get.addColumn(Bytes.toBytes(request.getFamily()), Bytes.toBytes(request.getColumn()));
			 
			  
			 //find key in redis 
			 		 
			 try
             {
			
				if(redisCluster != null)
					val = redisCluster.get(key);
				if(val != null){
				 BackResult = val;
				 responseBuilder.setBackResult(BackResult);
				 //System.out.println("read data from redis");
				}
				else{
					hbaseresult = env.getRegion().get(get);
					 if(hbaseresult.isEmpty())
					 {
						responseBuilder.setBackResult("no data be hitted");
					 }
					 else
					 {
						for (KeyValue kv : hbaseresult.list()) {
							//System.out.println("read data from hbase instead of redis");
							BackResult = Bytes.toString(kv.getValue());
						}

						//put the value in redis

						redisCluster.set(key,BackResult);
						//System.out.println("put the kv into redis and read data from hbase!");
						responseBuilder.setBackResult(BackResult);
					 }
				}
                 
             }
             catch(IOException ioe) {
                
                ResponseConverter.setControllerException(controller, ioe);
             }
             finally {
                if (scanner != null) {
                    try {
                        scanner.close();
                    } catch (IOException ignored) {}
                }
             }
             
             done.run(responseBuilder.build());
            
        }  
      
    }  