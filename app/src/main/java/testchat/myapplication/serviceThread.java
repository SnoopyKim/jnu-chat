package testchat.myapplication;


import android.os.Handler;

/**
 * Created by th on 2017-06-14.
 */

public class serviceThread extends Thread {
    Handler handler;
    boolean isRun = true;
    public serviceThread(Handler handler){
        this.handler=handler;
    }
    public void stopForever(){
        synchronized (this){
            this.isRun = false;
        }
    }
    public void run(){
        while(isRun){
            handler.sendEmptyMessage(0);
            try{
                Thread.sleep(3000);
            }catch(Exception e) {}
        }
    }
}
