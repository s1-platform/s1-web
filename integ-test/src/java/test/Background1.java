package test;

import java.util.Date;

/**
 * @author Grigory Pykhov
 */
public class Background1 implements Runnable {

    @Override
    public void run() {
        System.out.println(new Date()+": "+Thread.currentThread().getId());
        try{
            Thread.sleep(120000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
