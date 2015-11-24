package com.zving.advertise.ui;

import com.zving.framework.User;
import com.zving.framework.ui.control.StopThreadException;
import com.zving.framework.utility.StringUtil;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 生成首页shtml 的线程 抽象类
 */
public abstract class PublishIndexTask  extends Thread {
   //任务列表
    private List<Integer> taskList=null;

    //编号
    private int serialNum=0;

    //线程开始时间
    private long startTime=0;



    public PublishIndexTask(List<Integer> taskList,int serialNum){
        this.taskList=taskList;
        this.serialNum=serialNum;
        this.startTime=System.currentTimeMillis();
    }

    public abstract void execute();

    @Override
    public void run() {
        execute();
    }


    public int getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(int serialNum) {
        this.serialNum = serialNum;
    }

    public synchronized Integer getTaskId(){
        if(!taskList.isEmpty()){
            return taskList.remove(0);
        }else{
            return null;
        }


    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /*
    * 判断线程运行是否超时
    * @param timeMills 表示超时时间间隔 单位为毫秒
    * */
    public boolean isTimeOut(long timeMillis){
        long currTime=System.currentTimeMillis();

        if((currTime-this.getStartTime())> timeMillis){
            return true;
        }else{
            return false;
        }
    }
}
