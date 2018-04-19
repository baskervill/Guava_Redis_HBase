package com.GuavaRedisHbase;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

public class RedisCluster {
	
	private JedisCluster jcCluster = null;
	private List<HostAndPort> hostList = null;
	private Set<HostAndPort> jedisClusterNodes = null;
	private static int DEFAULT_EXPIRE_TIME = 3600; //1 HOUR
	
	public RedisCluster(List<HostAndPort> hosts){
		this.hostList = hosts;
		this.jedisClusterNodes = new HashSet<HostAndPort>();
		for(HostAndPort host: this.hostList){
			this.jedisClusterNodes.add(host);
		}
		this.jcCluster = new JedisCluster(this.jedisClusterNodes);
	}
	
	/**
	 * get kv
	 * @param key
	 * @return
	 */
	public String get(String key){
		if(this.jcCluster == null){
			System.out.println("please init cluster");
			return null;
		}
		return this.jcCluster.get(key);
	}

	public void delete(String key){
		if(this.jcCluster == null){
			System.out.println("please init cluster");
			return ;
		}
		this.jcCluster.del(key);
	}

	public void deleteAll(){
		if(this.jcCluster == null){
			System.out.println("please init cluster");
			return ;
		}
		this.jcCluster.flushAll();
	}
	
	/**
	 * set kv
	 * @param key
	 * @param value
	 */
	public void set(String key, String value, int expireTime){
		if(this.jcCluster == null){
			System.out.println("please init cluster");
			return ;
		}
		this.jcCluster.setex(key, expireTime, value);
	}
	/**
	 * 使用默认的过期时间(1 hour)
	 * @param key
	 * @param value
	 */
	public void set(String key, String value){
		set(key, value, RedisCluster.DEFAULT_EXPIRE_TIME);
	}
	

	
}
