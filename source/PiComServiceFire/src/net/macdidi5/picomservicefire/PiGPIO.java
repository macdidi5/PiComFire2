package net.macdidi5.picomservicefire;

// Pi4J GPIO 成員
public enum PiGPIO {
    
    GPIO_00("GPIO 0"), GPIO_01("GPIO 1"),
    GPIO_02("GPIO 2"), GPIO_03("GPIO 3"),
    GPIO_04("GPIO 4"), //GPIO_05("GPIO 5"),
    GPIO_06("GPIO 6"), GPIO_07("GPIO 7"),
    //GPIO_08("GPIO 8"), GPIO_09("GPIO 9"),
    GPIO_10("GPIO 10"), GPIO_11("GPIO 11"),
    GPIO_12("GPIO 12"), GPIO_13("GPIO 13"),
    GPIO_14("GPIO 14"), GPIO_15("GPIO 15"),
    GPIO_16("GPIO 16");
    
    // GPIO 名稱
    private String name;
    
    private PiGPIO(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    private static PiGPIO[] ps = values();
    
    // 傳回指定編號的 GPIO 成員
    public static PiGPIO fromOrdinal(int ordinal) {
        return ps[ordinal];
    }
    
    // 傳回數量
    public static int length() {
        return ps.length;
    }
    
    // 傳回指定名稱的 GPIO 成員
    public static PiGPIO fromName(String name) {
        for (PiGPIO p : ps) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        
        return null;
    }
    
}
