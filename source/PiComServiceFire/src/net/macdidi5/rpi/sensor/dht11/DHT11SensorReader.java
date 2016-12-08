package net.macdidi5.rpi.sensor.dht11;

// DHT11 溫、濕度感應器類別
public class DHT11SensorReader {
    // 使用 C 撰寫的方法
    public static native float[] readData(int pin);
}
