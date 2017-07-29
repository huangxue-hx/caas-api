package com.harmonycloud.common.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by anson on 17/7/28.
 */
public class NewCachedThreadPool{

    private ExecutorService executor;

    private static NewCachedThreadPool threadPool;

    private NewCachedThreadPool(){
        executor = Executors.newCachedThreadPool();
    }

    public static NewCachedThreadPool init(){
        if(threadPool==null)
            threadPool=new NewCachedThreadPool();
        return threadPool;
    }

    public void execute(Runnable t){
        executor.execute(t);
    }

}
