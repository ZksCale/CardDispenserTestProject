package com.wellhoo.carddispensertestproject;

public class StateTable {
    //工位号
    private int id;
    //工位的工作状态  正常/故障
    private boolean workingState;
    //有无卡夹
    private boolean clipState;
    //卡夹编号
    private int clipId;
    //卡数
    private int cardCount;
    //天线数
    private boolean antennaState;
}
