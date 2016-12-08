package net.macdidi5.picomservicefire;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.RaspiPin;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

// Raspberry Pi GPIO 管理類別
public class GpioContainer {
    
    // Pi4J GPIO 控制物件
    private final GpioController gpio;
    // 儲存與管理 GPIO 的 Map 物件
    private final Map<String, GpioPinDigitalMultipurpose> pins;
    
    // 建立 GPIO 管理物件
    public GpioContainer(GpioController gpio) {
        this.gpio = gpio;
        pins = new HashMap<>();
    }
    
    // GPIO 物件是否已經存在
    public boolean isExist(String pinName) {
        GpioPinDigitalMultipurpose result = pins.get(pinName);
        return !(result == null);
    }
    
    // 建立或取得保存中的 GPIO 物件
    public GpioPinDigitalMultipurpose getPin(String pinName) {
        // 取得保存中的 GPIO 物件
        GpioPinDigitalMultipurpose result = pins.get(pinName);
        
        // 如果不存在
        if (result == null) {
            // 使用名稱取得 Pin 物件
            Pin pin = RaspiPin.getPinByName(pinName);
            
            if (pin != null) {
                // 建立並加入 GPIO 物件
                result = gpio.provisionDigitalMultipurposePin(
                        pin, PinMode.DIGITAL_OUTPUT);
                pins.put(result.getName(), result);
            }
        }
                
        return result;
    }
    
    // 傳回所有管理的 GPIO 物件
    public Collection<GpioPinDigitalMultipurpose> getGpioPins() {
        return pins.values();
    }
    
}
