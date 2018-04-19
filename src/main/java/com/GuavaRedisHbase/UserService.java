package com.GuavaRedisHbase;

      
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.*;
  
import com.google.common.cache.CacheBuilder;  
import com.google.common.cache.CacheLoader;  
import com.google.common.cache.LoadingCache;  
  
public class UserService  
{  
    private final LoadingCache<String, String> cache;  
      
    public UserService()  
    {  
        /** 
         * 5秒自动过期 
         */  
        cache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS).build(new CacheLoader<String, String>() {  
            public String load(String id) throws Exception  
            {  
                System.out.println("method inovke");  
                //这里执行查询数据库，等其他复杂的逻辑  
                return "User:" + id;  
            }  
        });  
    }  
      
    public String getUserName(String id) throws Exception  
    {  
        return cache.get(id);  
    }  
}  
