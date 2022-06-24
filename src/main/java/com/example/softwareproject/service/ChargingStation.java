package com.example.softwareproject.service;


import com.example.softwareproject.entity.Car;
import com.example.softwareproject.entity.FastChargingPile;
import com.example.softwareproject.entity.RequestInfo;
import com.example.softwareproject.entity.SlowChargingPile;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Data
@Service
public class ChargingStation {
    @Autowired
    WaitingQueue waitingQueue;
    @Autowired
    ChargingField chargingField;

    public void startStation() {
        chargingField.changeChargingPileState("开启");
    }

    public void stopStation() {
        chargingField.changeChargingPileState("关闭");
    }

    public String changeChargePileState(int id) {
        return chargingField.changeChargingPileState(id);
    }

    public Map<String, Object> checkChargingPileService(int id) {
        return chargingField.checkChargingPileService(id);
    }

    public Map<String, Object> checkChargingPile(int id) {
        return chargingField.checkChargingPile(id);
    }

    public Map<String, Object> createReport(int id, Date startTime, Date endTime) {
        return chargingField.createReport(id, startTime, endTime);
    }

    public List<Map<String, Object>> checkChargingPileQueue(int id) {
        return chargingField.checkChargingPileQueue(id);
    }

    public String requestRecharge(RequestInfo requestInfo) {
//        调用join，让传入的信息加入到等待队列
        return waitingQueue.fastJoin(requestInfo);
    }

    public ChargingStation() {
        //设置定时器，用来不断检测等候区来加入到充电区
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (waitingQueue == null)
                    return;
                if (waitingQueue.getFastWaitingQueue().size() > 0) {
                    System.out.println("开始调度fast队列");
                    if(Objects.equals(updateWaitingQueue("fast"), "failed"))
                        System.out.println("调度fast队列失败,当前充电区已经满了或其他原因");
                }
//                else System.out.println("fast等待队列无车辆");
                if (waitingQueue.getSlowWaitingQueue().size() > 0) {
                    System.out.println("开始调度slow队列");
                    if(Objects.equals(updateWaitingQueue("slow"), "failed"))
                        System.out.println("调度fast队列失败,当前充电区已经满了或其他原因");
                }
//                else System.out.println("slow等待队列无车辆");
            }
        };

        timer.schedule(timerTask, 1, 2000);

        Timer timer1=new Timer();
        TimerTask timerTask1 =new TimerTask() {
            @Override
            public void run() {
                if(chargingField!=null) {
                    List<FastChargingPile> fastChargingPiles = chargingField.getFastChargingPiles();
                    for (int i = 0; i < fastChargingPiles.size(); ++i) {
                        FastChargingPile fastChargingPile = fastChargingPiles.get(i);
                        if (Objects.equals(fastChargingPile.getState(), "关闭") && fastChargingPile.getChargingQueue().size() > 0) {
                            fastChargingPile.getTimer().cancel();
                            fastChargingPile.getTimer().cancel();
                            fastChargingPile.getChargingQueue().get(0).setCarState("waitingQueue");
                            List<Car> tmpCars = new LinkedList<>();
                            int max=waitingQueue.fastWaitingQueue.size();
                            for(int j=0;j<max;++j)
                            {
                                tmpCars.add(waitingQueue.fastWaitingQueue.remove(0));
                            }
                            System.out.println(tmpCars);
                            max=fastChargingPile.getChargingQueue().size();
                            for (int j = 0; j < max; ++j) {
                                waitingQueue.fastWaitingQueue.add(fastChargingPile.getChargingQueue().remove(0));
                            }
                            waitingQueue.fastWaitingQueue.addAll(tmpCars);
                            System.out.println(i+"号快充发生故障，正在重新调度"+waitingQueue.fastWaitingQueue);
                        }
                    }
                    List<SlowChargingPile> slowChargingPiles = chargingField.getSlowChargingPiles();
                    for (int i = 0; i < slowChargingPiles.size(); ++i) {
                        SlowChargingPile slowChargingPile = slowChargingPiles.get(i);
                        if (slowChargingPile.getState() == "关闭" && slowChargingPile.getChargingQueue().size() > 0) {
                            slowChargingPile.getTimer().cancel();
                            slowChargingPile.getTimer().cancel();
                            slowChargingPile.getChargingQueue().get(0).setCarState("waitingQueue");
                            List<Car> tmpCars = new LinkedList<>();
                            int max=waitingQueue.slowWaitingQueue.size();
                            for(int j=0;j<max;++j)
                            {
                                tmpCars.add(waitingQueue.slowWaitingQueue.remove(0));
                            }
                            System.out.println(tmpCars);
                            max=slowChargingPile.getChargingQueue().size();
                            for (int j = 0; j < max; ++j) {
                                waitingQueue.slowWaitingQueue.add(slowChargingPile.getChargingQueue().remove(0));
                            }
                            waitingQueue.slowWaitingQueue.addAll(tmpCars);
                            System.out.println(i+"号慢充发生故障，正在重新调度");

                        }

                    }
                }
            }
        };

        timer1.schedule(timerTask1,0,1000);

        Timer timer2=new Timer();
        TimerTask timerTask2=new TimerTask() {
            @Override
            public void run() {
                if(chargingField!=null&&waitingQueue!=null)
                {
                    System.out.println("充电站情况");
                    System.out.println();
                    for (int i = 0; i < 2; ++i) {
                        FastChargingPile fastChargingPile = chargingField.getFastChargingPileById(i);
                        String info = new String();
                        info += "第" + (i+1) + "快充桩:";
                        for (int j = 0; j < fastChargingPile.getChargingQueue().size(); ++j) {
                            Car car = fastChargingPile.getChargingQueue().get(j);
                            info += "id"+car.getId() + " |";
                        }
                        info += "total:" + fastChargingPile.getChargingQueue().size();
                        System.out.println(info);
                    }
                    for (int i = 0; i < 3; ++i) {
                        SlowChargingPile slowChargingPile = chargingField.getSlowChargingPileById(i);
                        String info = new String();
                        info += "第" + (i+1) + "慢充桩:";
                        for (int j = 0; j < slowChargingPile.getChargingQueue().size(); ++j) {
                            Car car = slowChargingPile.getChargingQueue().get(j);
                            info += "id:"+car.getId() + " |";
                        }
                        info += "total:" + slowChargingPile.getChargingQueue().size();
                        System.out.println(info);
                    }

                        String info = new String();

                    info +="快充等待队列:";
                    for(int j=0;j<waitingQueue.fastWaitingQueue.size();++j)
                    {
                        Car car=waitingQueue.fastWaitingQueue.get(j);
                        info += "id:"+car.getId() + " |";
                    }
                    System.out.println(info);
                    info="";
                        info +="慢充等待队列:";
                        for(int j=0;j<waitingQueue.slowWaitingQueue.size();++j)
                        {
                            Car car=waitingQueue.slowWaitingQueue.get(j);
                            info += "id:"+car.getId() + " |";
                        }
                }
            }
        };
        timer2.schedule(timerTask2,0,3000);
    }

    public String updateWaitingQueue(String chargingMode) {

        Car car = waitingQueue.getWaitingQueue(chargingMode);//从等待区队列中对应的充电类型移除第一辆车
        System.out.println(car);
        //获取对应充电类型的所有等待桩队列信息
        //对应匹配充电模式下（快充/慢充），被调度车辆完成充电所需时长（等待时间+自己充电时间）最短。（等待时间=选定充电桩队列中所有车辆完成充电时间之和；自己充电时间=请求充电量/充电桩功率）
        //获取对应充电类型的所有等待桩队列信息，以判断插入哪一桩
        if (chargingMode.equals("fast")) {
            int minPileId = -1;//加入的桩
            float minTime = 10000;//按小时记
            //遍历所有同类型桩
            for (int i = 0; i < chargingField.getMaxFastPileNum(); ++i) {

                float sumTime = 0;
                FastChargingPile pileTemp = chargingField.getFastChargingPileById(i);
                if(Objects.equals(pileTemp.getState(), "关闭"))
                    continue;
                List<Car> chargingQueue = pileTemp.getChargingQueue();
                //当前桩有空位
                if (chargingQueue.size() < pileTemp.maxChargingNum) {
                    //计算本桩已有车辆充电时间之和
                    for (int carNumber = 0; carNumber < chargingQueue.size(); ++carNumber) {
                        sumTime += chargingQueue.get(carNumber).chargingNum / chargingField.getFastPilePower();
                    }
                    //此桩等待时间更短，则加入此桩。更新minPileId和minTime
                    if (sumTime < minTime) {
                        minPileId = i;
                        minTime = sumTime;
                    }
                }
            }
            //要加入的桩即为minPileId对应的桩。将此用户car加入该桩的队列尾
            //保证加入的桩的充电队列一定有空位。
            if (minPileId == -1)
                return "failed";//没有桩有空位。异常请求
            car.carState = "chargingField_fast";//新增，改变汽车的属性

            chargingField.getFastChargingPileById(minPileId).insert(car);//向充电桩中插入Car的信息
        } else {
            int minPileId = -1;
            float minTime = 10000;
            for (int i = 0; i < chargingField.getMaxSlowPileNum(); ++i) {

                float sumTime = 0;
                SlowChargingPile pileTemp = chargingField.getSlowChargingPileById(i);
                if(Objects.equals(pileTemp.getState(), "关闭"))
                    continue;
                List<Car> chargingQueue = pileTemp.getChargingQueue();
                //当前桩有空位
                if (chargingQueue.size() < pileTemp.maxChargingNum) {
                    //计算本桩已有车辆充电时间之和
                    for (int carNumber = 0; carNumber < chargingQueue.size(); ++carNumber) {
                        sumTime += chargingQueue.get(carNumber).chargingNum / chargingField.getSlowPilePower();
                    }
                    //此桩等待时间更短，则加入此桩。更新minPileId和minTime
                    if (sumTime < minTime) {
                        minPileId = i;
                        minTime = sumTime;
                    }
                }
            }
            //要加入的桩即为minPileId对应的桩。将此用户car加入该桩的队列尾
            //保证加入的桩的充电队列一定有空位。
            if (minPileId == -1)
                return "failed";//没有桩有空位。异常请求
            car.carState = "chargingField_slow";//新增，改变汽车的属性
            chargingField.getSlowChargingPileById(minPileId).insert(car);//向充电桩中插入Car的信息
        }
        //schedule(),需要实现调度的工作
        //通过调度获取到要插入的目标充电桩，向充电桩中插入Car的信息
        waitingQueue.updateWaitingQueue(chargingMode);
        return "success";
    }

    public Car removeCarByIdAndMode(long id, String chargingMode) {
        return waitingQueue.removeCarByIdAndMode(id, chargingMode);
    }

}

