package com.wellhoo.carddispensertestproject;

import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * 通过串口与卡机进行通讯的类
 * 按照协议与卡机进行通讯
 * 例如，获取卡机状态、控制伸缩装置、播报语音等
 */
public class SerialCommunicate {
    private static SerialPort comPort = null;
    private static Thread readThread = null;
    private static boolean readThreadBoolean = false;

    private static int offset = 0;
    private static final Logger log = LoggerFactory.getLogger(ReadWriter.class);

    private SerialCommunicate() {
    }

    //创建串口对象
    public static void createComPortInstance(String portName) {
        if (comPort == null) {
            comPort = SerialPort.getCommPort(portName);
        } else {
            log.error("已存在串口对象时试图新建串口对象! 原串口对象:{} 新串口名:{}",
                    comPort, portName);
        }
    }

    //获取串口对象
    public static SerialPort getComPort() {
        return comPort;
    }

    //设置串口对象的参数
    public static void configComPort(int baudRate, int numDataBits, int stopBits, int parity, int flowControl) {
        if (comPort == null) {
            log.error("试图对空的串口对象设置参数");
            return;
        }
        log.info("设置{}的参数,波特率={},数据位={},停止位={},奇偶校验={},流控制={}"
                , comPort.getSystemPortName(), baudRate, numDataBits, stopBits, parity, flowControl);
        comPort.setBaudRate(baudRate);
        comPort.setNumDataBits(numDataBits);
        comPort.setNumStopBits(stopBits);
        comPort.setParity(parity);
        comPort.setFlowControl(flowControl);
    }

    //开启一个线程,用来从串口读取
    public static void initReadThread() {
        if (readThread != null) {
            readThread.interrupt();
        }
        readThreadBoolean = true;
        readThread = new Thread(() -> {
            byte[] buffer = new byte[400];


            log.info("开始监听{}", comPort.getSystemPortName());
            while (readThreadBoolean) {
                if (!comPort.isOpen()) {
                    comPort.openPort();
                }
                int readCount = comPort.readBytes(buffer, 200, offset);
                if (readCount > 0) {
                    //读到数据了,buffer中的数据是否完整
                    //有效数据的部分应该是[0, offset+readCount)
                    //判断buffer[0]是否是起始标志,如果不是的话说明这不是正常的指令
                    offset += readCount;
                    if (buffer[0] != '<') {
                        offset = 0;
                        Arrays.fill(buffer, (byte) 0);
                        continue;
                    }
                    if(buffer[offset-1]=='>'){
                        //完整的一条指令
                        //暂时先打印到日志上,
                        log.info("读取到一条信息:{}", new String(buffer).trim());
                        //清空
                        offset=0;
                        Arrays.fill(buffer,(byte)0);
                    }
                }

            }
        });
        readThread.start();
    }
    //向串口写指令,返回成功写入的字节数
    public static int writeBytes(byte[] buffer,long bytesToWrite){
        if(comPort==null){
            log.error("试图在comPort为空时写入数据!");
            return -1;
        }
        return comPort.writeBytes(buffer, bytesToWrite);
    }
    //关闭串口
    public static boolean closeComPort(){
        return comPort.closePort();
    }

}
