package cn.nancy.scheduling_of_rfl;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 用于终端工作线程的类
 */
public class TimeOutTask extends TimerTask {
    private Thread t;
    private Timer timer;

    TimeOutTask(Thread t, Timer timer) {
        this.t = t;
        this.timer = timer;
    }

    // 用于结束工作进程
    public void run() {
        if (t != null && t.isAlive()) {
            t.interrupt();
            timer.cancel();
            System.out.println("线程已中断。");
        }
    }
}
