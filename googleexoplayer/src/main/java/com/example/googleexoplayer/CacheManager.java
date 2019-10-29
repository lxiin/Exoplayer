package com.example.googleexoplayer;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.database.DatabaseProvider;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class CacheManager {

    private static CacheManager instance;

    private Cache cache;

    private Set<Object> tokens = new HashSet<>();

    private final Context context;

    private CacheManager(Context context){
        this.context = context;
    }

    public synchronized static CacheManager getInstance(Context context){
        if (instance == null){
            instance = new CacheManager(context.getApplicationContext());
        }
        return instance;
    }

    public synchronized Cache getCache(Object token){
        if (cache == null){
            cache = new SimpleCache(new File(context.getExternalCacheDir(),"exoplayer_cache"),new LeastRecentlyUsedCacheEvictor(1024 * 1024 * 100), (DatabaseProvider) null);
        }
        tokens.add(token);
        return cache;
    }

    private synchronized void performRelease() {
        if (cache != null) {
            cache.release();
        }
        instance = null;
    }


    public synchronized void releaseCache(@NonNull Object token) {
        tokens.remove(token);
        if (tokens.isEmpty()) {
            performRelease();
        }
    }








}
