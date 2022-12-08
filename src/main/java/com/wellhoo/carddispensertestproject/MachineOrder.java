package com.wellhoo.carddispensertestproject;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 生成发送到卡机的指令 与解析卡机发来的指令
 * 统一指令的格式byte[] bytes这个数组里面实际存放了指令  int len 是bytes中实际指令的长度
 */
public class MachineOrder {
    /*
    起始STX: <
    帧序列号RSCTL: '0'到'9'依次循环 1字节
    控制信息CTL: 标志了这个信息的类型 1字节
    数据域DATA: 不定长
    校验位BCC: [帧序列号,数据域]中的所有字节取异或
    结束ETX: >
     */
    //保存帧序列号的变量,这里的值永远是上一次接收或发送的指令的帧序号
    private static char frameNumber = '0';
    //处理指令
    //----------pc对卡机发送指令-----------------------------------
    //pc主动发送一条初始化信息指令 0x61
    public static int initializeInfo(byte[] msg) {
        int result = initializeInfo(msg, '0');
        addFrameNum(msg);
        return result;
    }

    //出卡信息, data是出卡信息, 0x30默认卡机 ,0x31一号, 0x32二号, 0x33三号, 0x34四号
    public static int dispenseCard(byte[] msg, byte data) {
        addStartFrame(msg);
        addFrameNum(msg);
        addCTLFrame(msg, (byte) 0x62);
        addData(msg, new byte[]{data});
        addBCC(msg, 4);
        addEndFrame(msg, 5);
        return 6;
    }

    //坏卡信息,machineId: 0x30默认卡机 ,0x31一号, 0x32二号, 0x33三号, 0x34四号
    public static int brokenCard(byte[] msg, byte machineId) {
        addStartFrame(msg);
        addFrameNum(msg);
        addCTLFrame(msg, (byte) 0x63);
        addData(msg, new byte[]{machineId});
        addBCC(msg, 4);
        addEndFrame(msg, 5);
        return 6;
    }

    //查询卡机状态
    public static int getMachineInfo(byte[] msg) {
        addStartFrame(msg);
        addFrameNum(msg);
        addCTLFrame(msg, (byte) 0x65);
        addBCC(msg, 3);
        addEndFrame(msg, 4);
        return 5;
    }

    //工位信息设置 TODO: 待完成
    public static int setWorkStationInfo(byte[] msg, byte machineId, byte maxCardCount, byte boxCardCount) {
        return 0;
    }

    /**
     * 通知卡机发送模拟按键取卡消息
     *
     * @param data 0x31上工位 0x32下工位
     */
    public static int notifyDispenserSendSimulateTakeCardMsg(byte[] msg, byte data) {
        addStartFrame(msg);
        addFrameNum(msg);
        addCTLFrame(msg, (byte) 0x6b);
        addData(msg, new byte[]{data});
        addBCC(msg, 4);
        addEndFrame(msg, 5);
        return 6;
    }

    //卡口卡回收信息
    public static int tackBackCard(byte[] msg, byte machineId) {
        addStartFrame(msg);
        addFrameNum(msg);
        addCTLFrame(msg, (byte) 0x6c);
        addData(msg, new byte[]{machineId});
        addBCC(msg, 4);
        addEndFrame(msg, 5);
        return 6;
    }

    /**
     * 卡口伸出信息
     *
     * @param status 0x31上卡口伸出 0x32上下同时伸出 0x33下卡口伸出
     */
    public static int reachOutCardExport(byte[] msg, byte status) {
        addStartFrame(msg);
        addFrameNum(msg);
        addCTLFrame(msg, (byte) 0x6d);
        addData(msg, new byte[]{status});
        addBCC(msg, 4);
        addEndFrame(msg, 5);
        return 6;
    }

    /**
     * 卡口缩回信息
     *
     * @param status 0x31上卡口伸出 0x32上下同时伸出 0x33下卡口伸出
     */
    public static int drawBackCardExport(byte[] msg, byte status) {
        addStartFrame(msg);
        addFrameNum(msg);
        addCTLFrame(msg, (byte) 0x6e);
        addData(msg, new byte[]{status});
        addBCC(msg, 4);
        addEndFrame(msg, 5);
        return 6;
    }

    /**
     * 播报语音
     *
     * @param order 1-欢迎驶入XX高速
     *              2-请按键取卡
     *              3-正在写卡请稍后
     *              4-写卡完成
     *              5-写卡失败
     *              6-请取卡
     *              7-谢谢！一路平安！
     *              8-请按对讲进行求助
     */
    public static int broadcastVoiceMessage(byte[] msg, int order) {
        //order不足10的话,前面补零
        byte[] data = new byte[2];
        data[0] = (byte) (order / 10 + '0');
        data[1] = (byte) (order % 10 + '0');
        addStartFrame(msg);
        addFrameNum(msg);
        addCTLFrame(msg,(byte)0x74);
        addData(msg,data);
        addBCC(msg,5);
        addEndFrame(msg,6);
        return 7;
    }

    /**
     * 费显信息
     * @param order
     */
    public static int displayToll(byte[] msg,int order){
        //order不足10的话,前面补零
        byte[] data = new byte[2];
        data[0] = (byte) (order / 10 + '0');
        data[1] = (byte) (order % 10 + '0');
        addStartFrame(msg);
        addFrameNum(msg);
        addCTLFrame(msg,(byte)0x73);
        addData(msg,data);
        addBCC(msg,5);
        addEndFrame(msg,6);
        return 7;
    }
    //按键取消
    public static int cancelPressButton(byte[] msg){
        addStartFrame(msg);
        addFrameNum(msg);
        addCTLFrame(msg,(byte)0x67);
        addBCC(msg,3);
        addEndFrame(msg,4);
        return 5;
    }
    //----------pc对卡机的应答-------------------------------------

    //正应答 0x30
    public static int positiveResponse(byte[] msg, char frameNumber) {
        addStartFrame(msg);
        addFrameNum(msg, frameNumber);
        addCTLFrame(msg, (byte) 0x30);
        addBCC(msg, 3);
        addEndFrame(msg, 4);
        return 5;
    }

    //负应答 0x31
    public static int negativeResponse(byte[] msg, char frameNumber) {
        addStartFrame(msg);
        addFrameNum(msg, frameNumber);
        addCTLFrame(msg, (byte) 0x31);
        addBCC(msg, 3);
        addEndFrame(msg, 4);
        return 5;
    }

    //初始化信息 0x61
    public static int initializeInfo(byte[] msg, char frameNumber) {
        addStartFrame(msg);
        addFrameNum(msg, frameNumber);
        addCTLFrame(msg, (byte) 0x61);
        byte[] data = new byte[17];
        //[0,2]卡夹计数值,随便填个500
        data[0] = '5';
        data[1] = '0';
        data[2] = '0';
        //[3,16]同步时间,获取系统时间然后填进去
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String stringDateTime = dtf.format(dateTime);
        for (int i = 0; i < stringDateTime.length(); i++) {
            data[3 + i] = (byte) stringDateTime.charAt(i);
        }
        addData(msg, data);
        addBCC(msg, 20);
        addEndFrame(msg, 21);
        return 22;
    }

    //-----------这里往下是拼帧的方法------------------------------------------------------

    //填写起始帧
    private static void addStartFrame(byte[] msg) {
        msg[0] = '<';
    }

    //填写帧序列号 发送时使用
    private static void addFrameNum(byte[] msg) {
        if (frameNumber < '9') {
            frameNumber++;
        } else if (frameNumber == '9') {
            frameNumber = '0';
        }
        msg[1] = (byte) frameNumber;
    }

    //填写帧序列号 回复时使用
    private static void addFrameNum(byte[] msg, char frameNum) {
        msg[1] = (byte) frameNum;
    }

    //填写控制信息
    private static void addCTLFrame(byte[] msg, byte ctl) {
        msg[2] = ctl;
    }

    /**
     * 填写数据域  变长
     *
     * @param msg  消息数组 要存在并且够长
     * @param data 消息内容,它里面的每一位都必须是
     * @return 返回值是添加的DATA区域的最后一位的下标
     * 例如,  发送初始化信息时,DATA数据域占的区域为[3,19] 那么返回值就是19
     */
    private static int addData(byte[] msg, byte[] data) {
        System.arraycopy(data, 0, msg, 3, data.length);
        return data.length + 2;
    }

    //填写异或校验帧
    private static void addBCC(byte[] msg, int bccIndex) {
        byte bcc = msg[1];
        for (int i = 2; i < bccIndex; i++) {
            bcc ^= msg[i];
        }
        msg[bccIndex] = bcc;
    }

    //填写帧尾
    private static void addEndFrame(byte[] msg, int endFrameIndex) {
        msg[endFrameIndex] = '>';
    }

}
