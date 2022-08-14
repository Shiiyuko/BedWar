package org.sobadfish.bedwar.thread;

import org.sobadfish.bedwar.manager.ThreadManager;

/**
 * @author SoBadFish
 * 2022/1/7
 */
public abstract class BaseTimerRunnable extends ThreadManager.AbstractBedWarRunnable{
    private int time = 0;

    private int end;


    public BaseTimerRunnable(int end){
        this.end = end;
    }

    public void cancel(){
        this.isClose = true;
    }

    @Override
    public void run() {
        while (!isClose){
            if(time < end){
                time++;
                onRun();
            }else{
                callback();
                isClose = true;
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void onRun(){}

    /**
     * 当倒计时结束的回调
     * */
    abstract protected void callback();
}
