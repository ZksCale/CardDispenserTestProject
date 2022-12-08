package com.wellhoo.carddispensertestproject;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;


/**
 * 汇豪发卡机测试软件
 * javaFX项目
 * 使用JNA加载dll
 * 使用jSerialComm读写串口
 * <p>
 * 张克松 22/12/6
 */
public class DispenserApplication extends Application {

    private TableView table = new TableView();

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        AnchorPane root = new AnchorPane();
        //场景分辨率600 * 600,可调整
        Scene scene = new Scene(root, 800, 600);
        //1 下面开始添加组件 左边占2/3 右边占1/3
        //左上 标题区 高度占1/7
        /* 下面的代码添加一个矩形,然后将它的大小与位置和场景绑定起来 */
        Rectangle titleArea = new Rectangle();
        titleArea.setFill(Color.ORANGE);
        titleArea.widthProperty().bind(scene.widthProperty().divide(3).multiply(2));
        titleArea.heightProperty().bind(scene.heightProperty().divide(7));
        titleArea.setLayoutX(0);
        titleArea.setLayoutY(0);
        //左中 显示工位与卡片信息区域 高度占4/7
        Rectangle infoArea = new Rectangle();
        infoArea.setFill(Color.GRAY);
        infoArea.widthProperty().bind(scene.widthProperty().divide(3).multiply(2));
        infoArea.heightProperty().bind(scene.heightProperty().divide(7).multiply(4));
        //组件位置的相对绑定,这里需要用双向绑定才行
        infoArea.layoutXProperty().bindBidirectional(titleArea.layoutXProperty());
        infoArea.layoutYProperty().bindBidirectional(titleArea.heightProperty());
        //左下 表格区域 高度占2/7
        Rectangle tableArea = new Rectangle();
        tableArea.setFill(Color.DARKGRAY);
        tableArea.widthProperty().bind(scene.widthProperty().divide(3).multiply(2));
        tableArea.heightProperty().bind(scene.heightProperty().divide(7).multiply(3));
//        tableArea.setLayoutY(infoArea.getLayoutY() + infoArea.getHeight());
        tableArea.setLayoutX(0);
        infoArea.layoutYProperty().addListener(((observableValue, oldValue, newValue) -> {
            tableArea.setLayoutY(infoArea.getLayoutY() + infoArea.getHeight());
        }));
//        tableArea.layoutYProperty().bind(scene.heightProperty());
        //右上 单独测试按钮区域 高度占1/3
        Rectangle buttonsArea = new Rectangle();
        buttonsArea.setFill(Color.YELLOW);
        buttonsArea.widthProperty().bind(scene.widthProperty().divide(3));
        buttonsArea.heightProperty().bind(scene.heightProperty().divide(3));
        buttonsArea.layoutXProperty().bindBidirectional(titleArea.widthProperty());
        buttonsArea.setLayoutY(0);
        //右下 日志区域 高度占2/3
        Rectangle recordArea = new Rectangle();
        recordArea.setFill(Color.DARKKHAKI);
        recordArea.widthProperty().bind(scene.widthProperty().divide(3));
        recordArea.heightProperty().bind(scene.heightProperty().divide(3).multiply(2));
        recordArea.layoutXProperty().bindBidirectional(titleArea.widthProperty());
        recordArea.layoutYProperty().bindBidirectional(buttonsArea.heightProperty());

        root.getChildren().addAll(titleArea, infoArea, tableArea, buttonsArea,recordArea);

        stage.setTitle("发卡机测试软件");
        stage.setScene(scene);
        stage.show();
        //卡机通讯
        String machinePortName="COM2";
        SerialCommunicate.createComPortInstance(machinePortName);
        SerialCommunicate.configComPort(57600,8,1,0,0);
        SerialCommunicate.initReadThread();
    }
}
