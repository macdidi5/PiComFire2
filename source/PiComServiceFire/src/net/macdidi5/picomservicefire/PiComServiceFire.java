package net.macdidi5.picomservicefire;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseReference.CompletionListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;

import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;
import net.macdidi5.rpi.sensor.dht11.DHT11SensorReader;

public class PiComServiceFire {

    static {
        // 載入使用 C 撰寫的 DHT11 模組
        System.loadLibrary("dht11sensor");
    }
    
    // DHT11 使用的 Pin 編號(Pi4J)
    public static final int DHT11_PIN = 29;
    
    // Firebase app
    public static final String FIREBASE_URL
            = "https://picom.firebaseio.com/";
    
    // Firebase 節點名稱：控制、監聽、照片、伺服馬達與溫、濕度
    public static final String CHILD_CONTROL = "control";
    public static final String CHILD_LISTENER = "listener";
    public static final String CHILD_IMAGE = "image";
    public static final String CHILD_SERVO = "servo";
    public static final String CHILD_MONITOR = "monitor";
    public static final String CHILD_DEVICE = "device";
    
    // Firebase 節點物件：app、控制、監聽、照片、伺服馬達與溫、濕度
    private static DatabaseReference firebaseRef;
    private static DatabaseReference childControl;
    private static DatabaseReference childListener;
    private static DatabaseReference childImage;
    private static DatabaseReference childServo;
    private static DatabaseReference childMonitor;
    private static DatabaseReference childDevice;
    
    // Firebase 資料監聽物件
    private static ValueEventListener valueEventListener;
    
    // GPIO 管理物件
    private static GpioContainer gpioContainer;
    // 儲存 Pi4J GPIO 監聽物件
    private static Set<String> listeners;
    // 是否正在加入 Pi4J GPIO 監聽物件
    private static boolean startAddListener = false;
    
    // 工作目錄
    public static final String WORK_DIR = "/home/pi/picomwork";
    
    // 是否照相
    private static boolean takePicture = true;
    // 相片儲存位置與檔名
    public static final String IMAGE_FILE = 
            WORK_DIR + "/picon.jpg";
    // 照相指令
    public static final String TAKE_COMM = 
            "raspistill -o " + IMAGE_FILE + 
            " -q 50 -w 480 -h 320 -t 1000ms -n -ex auto -awb auto";
    
    // 感應設備設定檔
    public static final String DEVICE_FILE =
            WORK_DIR + "/picdevice.txt";
        
    // 是否結束應用程式
    private static boolean exit = false;
    
    // 轉換 MCP3008 數位值的比例
    public static final double RATE = 100D / 1023;
    
    public static void main(String[] args) throws Exception { 
        // 檢查工作目錄
        File workDir = new File(WORK_DIR);
        
        if (!workDir.exists()) {
            workDir.mkdir();
        }
        
        // 檢查感應設備設定檔
        checkDeviceFile();
        
        // 是否拍照
        if (args.length > 0) {
            takePicture = false;
        }

        // 建立 Pi4J GPIO 控制物件
        final GpioController gpio = GpioFactory.getInstance();
        
        // 建立 MCP3008 針腳物件
        final GpioPinDigitalInput serialDataOutput = 
                gpio.provisionDigitalInputPin(RaspiPin.GPIO_22);
        final GpioPinDigitalOutput serialDataInput = 
                gpio.provisionDigitalOutputPin(RaspiPin.GPIO_23);
        final GpioPinDigitalOutput serialClock = 
                gpio.provisionDigitalOutputPin(RaspiPin.GPIO_21);
        final GpioPinDigitalOutput chipSelect = 
                gpio.provisionDigitalOutputPin(RaspiPin.GPIO_24);
        
        // 建立 MCP3008 物件
        final MCP3008 mcp3008 = new MCP3008(
                serialClock, serialDataOutput, serialDataInput, chipSelect);
        
        // 建立儲存 Pi4J GPIO 監聽物件的 HashSet 物件
        listeners = new HashSet<>();
        
        InputStream input = PiComServiceFire.class.getResourceAsStream(
                "PICOM-af91563d98c6.json");
        
        System.out.println("Init Firebase...");
        
        FirebaseOptions options = new FirebaseOptions.Builder()
            .setServiceAccount(input)
            .setDatabaseUrl(FIREBASE_URL)
            .build();
        FirebaseApp.initializeApp(options);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        
        // 建立 Firebase 物件：app、控制、監聽、照片、伺服馬達與溫、濕度
        firebaseRef = database.getReference();
        childControl = firebaseRef.child(CHILD_CONTROL);  
        childListener = firebaseRef.child(CHILD_LISTENER);
        childImage = firebaseRef.child(CHILD_IMAGE);
        childServo = firebaseRef.child(CHILD_SERVO);
        childMonitor = firebaseRef.child(CHILD_MONITOR);
        childDevice = firebaseRef.child(CHILD_DEVICE);
        
        // 建立 GPIO 管理物件
        gpioContainer = new GpioContainer(gpio);
        
        for (PiGPIO pg : PiGPIO.values()) {
            // 加入 GPIO 物件
            gpioContainer.getPin(pg.getName());
        }
        
        // 移除原來的感應設備節點
        childDevice.removeValue();
        
        // 建立 Firebase 資料改變監聽物件
        valueEventListener = new ValueEventListener(){
            
            // 資料改變
            @Override
            public void onDataChange(DataSnapshot ds) {
                // 讀取 GPIO 名稱
                String pinName = PiGPIO.valueOf(ds.getKey()).getName();
                System.out.println(pinName + ":" + ds.getValue());
                // 取得 GPIO 物件
                GpioPinDigitalMultipurpose pin = 
                        gpioContainer.getPin(pinName);
                // 讀取雲端資料
                boolean status = (Boolean) ds.getValue();
                // 設定 GPIO
                pin.setState(status);
            }

            @Override
            public void onCancelled(DatabaseError de) { }
        };
        
        // 建立與註冊 Firebase 監聽節點事件
        childListener.addChildEventListener(new ChildEventListenerAdapter() {

            // 新增節點
            @Override
            public void onChildAdded(DataSnapshot ds, String previousChildKey) {
                // 如果不是正在加入 Pi4J GPIO 監聽物件
                if (!startAddListener) {
                    System.out.println("PiComServiceFire add listener...");
                    startAddListener = true;
                    // 執行控制設定
                    processControl();
                }
                
                // 讀取針腳名稱
                String pinName = PiGPIO.valueOf(ds.getKey()).getName();
                // 讀取 Firebase 資料
                String notifyStatus = (String) ds.getValue();
                String[] nsa = notifyStatus.split(",");
                // 移除控制節點的監聽物件
                childControl.child(PiGPIO.fromName(pinName).name())
                        .removeEventListener(valueEventListener);
                // 新增監聽的 GPIO
                listeners.add(pinName);
                addListener(pinName, nsa[0], nsa[1]);
                System.out.println("Add listener: " + pinName + "," + notifyStatus);
            }
        });
        
        // 建立與註冊 Firebase 伺服馬達節點事件
        childServo.addChildEventListener(new ChildEventListenerAdapter() {

            @Override
            public void onChildChanged(DataSnapshot ds, String previousChildKey) { 
                // 讀取 Firebase 節點名稱與資料
                String key = ds.getKey();
                boolean status = (Boolean) ds.getValue();
                
                // 控制伺服馬達
                if (status) {
                    servoControl(key, 70, 180, 10);
                }
                else {
                    servoControl(key, 180, 70, 10);
                }
            }
        });
        
        // 建立與註冊 Firebase 應用程式結束節點事件
        firebaseRef.child("exit").addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot ds) {
                        // 讀取 Firebase 資料
                        boolean value = (Boolean) ds.getValue();
                        
                        // 如果結束應用程式
                        if (value) {
                            firebaseRef.child("exit").setValue(false);
                            exit = true;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError de) {
                        System.out.println(de.toString());
                    }
                }
        );
        
        System.out.println("Init Firebase done...");
        
        // 讀取感應設備設定檔
        List<Device> devices = getDevices(DEVICE_FILE);
        
        // 加入溫濕度感應設備
        Device deviceTemp = new Device(
                String.format("D%02d", devices.size()),
                "Temperature", 0, true, 1, 0);
        Device deviceHum = new Device(
                String.format("D%02d", devices.size() + 1),
                "Humidity", 0, true, 1, 0);

        // 儲存溫、濕度前一次讀取資料
        int oldTemp = 0, oldHum = 0;
        
        // 儲存感應設備前一次讀取資料
        int[] oldValue = new int[devices.size()];
        Arrays.fill(oldValue, -99);
        
        while (!exit) {
            // 讀取溫、濕度
            float[] data = readDHT11Data(DHT11_PIN);
            int temp = (int)data[0];
            int hum = (int)data[1];
            
            // 如果溫度變化
            if (Math.abs(temp - oldTemp) > 1) {
                // 設定 Firebase 溫度資料
                childMonitor.child("m001").setValue(temp);
                deviceTemp.setValue(temp);
                childDevice.child(deviceTemp.getId()).setValue(deviceTemp);
                oldTemp = temp;
            }
            
            // 如果濕度變化
            if (Math.abs(hum - oldHum) > 1) {
                // 設定 Firebase 濕度資料
                childMonitor.child("m002").setValue(hum);
                deviceHum.setValue(hum);
                childDevice.child(deviceHum.getId()).setValue(deviceHum);
                oldHum = hum;
            }
            
            // 感應設備
            for (int i = 0; i < devices.size(); i++) {
                Device dev = devices.get(i);
                // 讀取 MCP3008 指定的 channel 值
                int adcValue = mcp3008.read(dev.getChannel());
                int value = (int)(adcValue * RATE);
                
                // 轉換為遞增值
                if (!dev.isIsIncrememt()) {
                    value = 100 - value;
                }
                
                if (Math.abs(value - oldValue[i]) > dev.getAccuracy()) {
                    oldValue[i] = value;

                    dev.setValue(value);
                    childDevice.child(dev.getId()).setValue(dev);
                }
            }        
                        
            delay(1000);
        }
        
        System.out.println("PiComServiceFire Bye...");
        
        servoControl("p1-18", 180, 70, 110);
        
        gpio.shutdown();
        System.exit(0);
    }
    
    // 加入監聽
    //   String pinName         GPIO 名稱
    //   final String isHigh    高電壓的說明
    //   String isLow           低電壓的說明
    private static void addListener(final String pinName, 
                                    final String isHigh, 
                                    final String isLow) {
        // 取得 GPIO 物件並設定為輸入模式
        GpioPinDigitalMultipurpose gpioPin = 
                gpioContainer.getPin(pinName);
        gpioPin.setMode(PinMode.DIGITAL_INPUT);
        gpioPin.setPullResistance(PinPullResistance.PULL_DOWN);
        gpioPin.setDebounce(2000);

        // 建立與註冊 Pi4J GPIO 監聽物件
        gpioPin.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(
                    GpioPinDigitalStateChangeEvent gpdsce) {
                String nodeName = PiGPIO.fromName(pinName).name();
                // 是否需要上傳照片
                boolean uploadPicture = 
                        (isHigh.equals("true") && gpdsce.getState().isHigh()) ||
                        (isLow.equals("true") && gpdsce.getState().isLow());
                        
                if (takePicture && uploadPicture) {
                    try {
                        // 執行照相指令
                        Runtime.getRuntime().exec(TAKE_COMM);
                        // 等候完成照相指令
                        delay(2000);

                        // 讀取照片檔案
                        BufferedImage img = ImageIO.read(new File(IMAGE_FILE));
                        // 轉換照片檔為字串
                        String imgstr = encodeToString(img, "jpg");

                        // 傳送照片資料到 Firebase
                        childImage.setValue(imgstr, new CompletionListener() {

                            @Override
                            public void onComplete(DatabaseError de, DatabaseReference dr) {
                                if (de != null) {
                                    System.out.println("Picture upload failure... " + de.getMessage());
                                }
                                else {
                                    System.out.println("Picture upload successfully.");
                                    // 儲存 Firebase 節點資料
                                    childControl.child(nodeName).setValue(gpdsce.getState().isHigh());
                                }
                            }
                        });
                    }
                    catch (IOException e){
                        System.out.println(e);
                    }
                }
                else {
                    // 儲存 Firebase 節點資料
                    childControl.child(nodeName).setValue(gpdsce.getState().isHigh());
                }
                
                System.out.println("Listener: " + gpdsce.getState());
            }
        });
    }
    
    // 執行控制設定
    private static void processControl() {
        delay(3000);
        System.out.println("PiComServiceFire process control...");
        
        int length = PiGPIO.length();
        
        // 註冊 Firebase 資料改變事件
        for (int i = 0; i < length; i++) {
            String pinName = PiGPIO.fromOrdinal(i).getName();
            
            if (! listeners.contains(pinName)) {
                childControl.child(PiGPIO.fromOrdinal(i).name())
                        .addValueEventListener(valueEventListener);
            }
        }
        
        System.out.println("PiComServiceFire running...");
    }

    // 暫停參數指定的時間（ms）
    public static void delay(int ms) {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException e) {
            System.out.println(e.toString());
        }
    }    
    
    // 轉換照片檔為字串
    //   BufferedImage image    照片
    //   String type            類型
    public static String encodeToString(BufferedImage image, String type) {
        String imageString = null;    

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();) {
            ImageIO.write(image, type, bos);
            byte[] imageBytes = bos.toByteArray();
            Encoder encoder =  Base64.getEncoder();
            imageString = encoder.encodeToString(imageBytes);
        } 
        catch (IOException e) {
            System.out.println(e);
        }
        
        return imageString;
    }    
    
    
    // 使用指定GPIO針腳控制伺服馬達
    //   String Pin     GPIP針腳（P1-GPIO針腳編號）
    //   String value   設定值  
    public static void set(String pin, String value) {
        try (OutputStream out = new FileOutputStream("/dev/servoblaster");
             OutputStreamWriter writer = new OutputStreamWriter(out)) {
            writer.write(pin + "=" + value + "\n");
            writer.flush();
        }
        catch (IOException e) {
            System.out.println("================= " + e);
        }
    }
    
    // 控制伺服馬達
    //   String pin     伺服馬達連接的針腳
    //   int start      開始
    //   int end        結束
    //   int interval   間隔
    public static void servoControl(String pin, int start, int end, int interval) {
        if (end > start) {
            for (int i = start; i <= end; i += interval) {
                set(pin, Integer.toString(i));
                delay(50);
            }
        }
        else {
            for (int i = start; i >= end; i -= interval) {
                set(pin, Integer.toString(i));
                delay(50);
            }
        }
    }
    
    // 讀取溫、濕度
    //    int pin   DHT11 連接的針腳編號
    public static float[] readDHT11Data(int pin) {
        float[] data = DHT11SensorReader.readData(pin);
        int stopCounter = 0;
        
        while (!isValid(data)) {
            stopCounter++;
            
            if (stopCounter > 10) {
                System.out.println("Sensor return invalid data 10 times:" + data[0] + ", " + data[1]);
                return new float[]{0.0F, 0.0F};
            }
            
            data = DHT11SensorReader.readData(pin);
        }
        
        return data;
    }
    
    // 檢查讀取的溫、濕度資料是否正確
    private static boolean isValid(float[] data) {
        return data[0] > 0 && data[0] < 100 && data[1] > 0 && data[1] < 100;
    }
    
    // 讀取感應設備設定檔
    private static List<Device> getDevices(String fileName) {
        ArrayList<Device> result = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(
             new FileReader(fileName))) {
            String line;
            
            while ((line = br.readLine()) != null) {
                // 0:代碼, 1:名稱, 2:遞增, 3:精確度, 4:通道
                String[] items = line.split(",");
                
                if (items != null && items.length == 5) {
                    Device device = new Device();
                    device.setId(items[0]);
                    device.setName(items[1]);
                    device.setIsIncrememt(Boolean.parseBoolean(items[2]));
                    device.setAccuracy(Integer.parseInt(items[3]));
                    device.setChannel(Integer.parseInt(items[4]));
                    result.add(device);
                }
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("Device file '/home/pi/picdevice.txt' not found!");
        }
        catch (IOException e) {
            System.out.println(e);
        }
        
        return result;
    }
    
    private static void checkDeviceFile() {
        System.out.println("Check device file...");
        
        File file = new File(DEVICE_FILE);
        
        if (file.exists()) {
            return;
        }
        
        try (BufferedWriter bw = new BufferedWriter(
             new FileWriter(file))) {
             bw.write("D00,可變電阻,true,1,0");
             bw.newLine();
             bw.write("D01,光敏電阻,false,3,0");
             bw.newLine();
             bw.write("D02,搖桿X,true,1,0");
             bw.newLine();
             bw.write("D03,搖桿Y,true,1,0");
             bw.newLine();
             bw.write("D04,搖桿按鈕,true,1,0");
             bw.newLine();
             bw.write("D05,瓦斯,true,1,0");
             bw.newLine();
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
        
}
