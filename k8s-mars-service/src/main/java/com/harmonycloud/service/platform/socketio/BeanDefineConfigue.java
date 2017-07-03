package com.harmonycloud.service.platform.socketio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.harmonycloud.service.platform.socketio.server.SocketIOConfig;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by andy on 17-2-22.
 */
@Component("BeanDefineConfigue")
public class BeanDefineConfigue  implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    SocketIOConfig socketIOConfig;

    //执行时间，时间单位为毫秒
    private static Long cacheTime = 300000l;
    //延迟时间，时间单位为毫秒
    private static Integer delay = 3000;
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                //启动socket监听
                try{
                    if(socketIOConfig.getServer() == null){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
//                                try {
//                                    socketIOConfig.startServer();
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
                            }
                        }).start();
                    }
                }catch(Exception e){
                }
            }
        }, delay,cacheTime);// 这里设定将延时每天固定执行

    }
}
