package com.GuavaRedisHbase.coprocessor;

import java.io.IOException;  
import java.util.ArrayList;  
import java.util.List;  

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CoprocessorEnvironment;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.coprocessor.BaseRegionObserver;
import org.apache.hadoop.hbase.coprocessor.CoprocessorException;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.regionserver.wal.WALEdit;
import org.apache.hadoop.hbase.util.Bytes;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Jedis;
import com.GuavaRedisHbase.RedisCluster;

import java.io.IOException;
import java.util.List;
import java.util.NavigableMap;

/**
 * Created by 汤飞 on 2017/6/3.
 */
public class RedisHBaseObserver extends BaseRegionObserver {

    private static final Log LOG = LogFactory.getLog(RedisHBaseObserver.class);
    RegionCoprocessorEnvironment env;

    public static  RedisCluster redisCluster = null;

    @Override
    public void start(CoprocessorEnvironment e) throws IOException {
        super.start(e);
        if (e instanceof RegionCoprocessorEnvironment) {
            env = (RegionCoprocessorEnvironment) e;
        } else {
            throw new CoprocessorException("Must be loaded on a table region!");
        }
        //init the redis
		List<HostAndPort> list = new ArrayList<HostAndPort>();
        list.add(new HostAndPort("127.0.0.1", 7000));
		list.add(new HostAndPort("127.0.0.1", 7001));
		initRedisCluster(list);

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
    public void postPut(ObserverContext<RegionCoprocessorEnvironment> e, Put put, WALEdit edit, Durability durability) throws IOException {
        super.postPut(e, put, edit, durability);

        //Jedis jedis = new Jedis("localhost");
        NavigableMap<byte[], List<Cell>> familyCells = put.getFamilyCellMap();
        for (byte[] key : familyCells.keySet()) {
            List<Cell> cells = familyCells.get(key);
            for (Cell cell : cells) {
                String rowKey = Bytes.toString(cell.getRowArray(), cell.getRowOffset(), cell.getRowLength());
                String columnFamily = Bytes.toString(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength());
                String qualifier = Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                String value = Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
                String newKey = rowKey+"_"+columnFamily+"_"+qualifier;
                //System.out.println("put data to redis");
                redisCluster.set(newKey, value);
            }
        }
    }

    @Override
    public void stop(CoprocessorEnvironment e) throws IOException {
        super.stop(e);
    }
}
