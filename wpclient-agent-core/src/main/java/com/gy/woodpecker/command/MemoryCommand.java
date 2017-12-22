package com.gy.woodpecker.command;

import com.gy.woodpecker.command.annotation.Cmd;
import com.gy.woodpecker.command.annotation.IndexArg;
import com.gy.woodpecker.command.annotation.NamedArg;
import com.gy.woodpecker.log.LoggerFacility;
import com.gy.woodpecker.textui.TKv;
import com.gy.woodpecker.textui.TTable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.management.MemoryType.HEAP;
import static java.lang.management.MemoryType.NON_HEAP;

/**
 * @author guoyang
 * @Description: TODO
 * @date 2017/12/21 下午1:38
 */
@Slf4j
@Cmd(name = "memory", sort = 17, summary = "Display memory information",
        eg = {
                "memory",
                "memory -a",
                "memory -t 10",
                "memory -ta 10"
        })
public class MemoryCommand extends AbstractCommand{
    @IndexArg(index = 0, name = "times",isRequired = false ,summary = "timing interval(s)")
    private String times;

    @NamedArg(name = "t", summary = "is timer")
    private boolean isTimer = false;

    @NamedArg(name = "a", summary = "Display all")
    private boolean isAll = false;

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    @Override
    public boolean excute(Instrumentation inst) {

        if(isTimer){
            if(StringUtils.isBlank(times)){
                ctxT.writeAndFlush("not timer!\n");
                return false;
            }
            final int timer = Integer.valueOf(times);
            long delay = timer;
            long initDelay = 0;
            executor.scheduleAtFixedRate(
                    new Runnable() {
                        public void run() {
                            System.out.println("print memory!!!");
                            printMemory();
                        }
                    },
                    initDelay,
                    delay,
                    TimeUnit.SECONDS);

            //等待结果
            super.isWait = true;
            return true;
        }else{
            printMemory();
            return true;
        }
    }

    private void printMemory() {
        final TTable tTable = new TTable(new TTable.ColumnDefine[]{
                new TTable.ColumnDefine()
        });
        List<MemoryPoolMXBean> mps = ManagementFactory.getMemoryPoolMXBeans();
        for(MemoryPoolMXBean mp : mps){
            final TKv tKv = new TKv(
                    new TTable.ColumnDefine(TTable.Align.RIGHT),
                    new TTable.ColumnDefine(TTable.Align.LEFT));
            if(isAll){
                tKv.add("name",mp.getName());
                tKv.add("CollectionUsage",mp.getCollectionUsage());
                tKv.add("type", mp.getType());
                tTable.addRow(tKv.rendering());
            }else {
                if (mp.getType() == HEAP) {
                    tKv.add("name", mp.getName());
                    tKv.add("CollectionUsage", mp.getCollectionUsage());
                    tKv.add("type", mp.getType());
                    tTable.addRow(tKv.rendering());
                }
            }
        }

        ctxT.writeAndFlush(tTable.rendering());
    }

    @Override
    public void destroy(){
        executor.shutdownNow();
    }
}