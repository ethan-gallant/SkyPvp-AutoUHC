package io.skypvp.uhc.jedis;

import io.skypvp.uhc.SkyPVPUHC;
import redis.clients.jedis.JedisPool;

public class UHCJedis {
    
    final SkyPVPUHC main;
    private JedisPool pool;
    
    public UHCJedis(SkyPVPUHC instance) {
        this.main = instance;
        this.pool = null;
    }
    
    public void connect(String host, int port) {
        ClassLoader prevClassLdr = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(UHCJedis.class.getClassLoader());
        pool = new JedisPool(host, port);
        Thread.currentThread().setContextClassLoader(prevClassLdr);
    }
    
    public void close() {
        if(pool != null) {
            pool.destroy();
        }
    }
}
