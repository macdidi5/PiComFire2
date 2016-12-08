#include <wiringPi.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include "DHT11SensorReader.h"

#define MAXTIMINGS 85

int dht11_dat[5] = {0, 0, 0, 0, 0};

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    // 檢查是否已經安裝 WiringPi
    if (wiringPiSetup() == -1) {
        exit(1);
    }
    
    return JNI_VERSION_1_6;
}

// 讀取溫、濕度
//   int dhtPin     WiringPi GPIO 編號
//   float *temp_p  溫度
//   float *hum_p   濕度
int readDHT(int dhtPin, float *temp_p, float *hum_p) {
    uint8_t laststate = HIGH;
    uint8_t counter = 0;
    uint8_t j = 0, i;
    dht11_dat[0] = dht11_dat[1] = dht11_dat[2] = dht11_dat[3] = dht11_dat[4] = 0;

    // 設定為輸出模式
    pinMode(dhtPin, OUTPUT);
    // 輸出低電壓 18ms
    digitalWrite(dhtPin, LOW);
    delay(18);
    
    // 輸出高電壓 40ms
    digitalWrite(dhtPin, HIGH);
    delayMicroseconds(40);
    
    // 準備讀取資料
    pinMode(dhtPin, INPUT);

    // 偵測針腳的變化與讀取資料
    for (i = 0; i < MAXTIMINGS; i++) {
        counter = 0;
        
        while (digitalRead(dhtPin) == laststate) {
            counter++;
            delayMicroseconds(1);
            
            if (counter == 255) {
                break;
            }
        }
        
        laststate = digitalRead(dhtPin);

        if (counter == 255) break;

        // 忽略前三次的狀態轉換
        if ((i >= 4) && (i % 2 == 0)) {
            dht11_dat[j / 8] <<= 1;
            
            if (counter > 16)
                dht11_dat[j / 8] |= 1;
            j++;
        }
    }

    // 檢查讀取的 40 個位元資料
    if ((j >= 40) && 
            (dht11_dat[4] == 
            ((dht11_dat[0]+dht11_dat[1]+dht11_dat[2]+dht11_dat[3])&0xFF))
       ) {
        *temp_p = (float) dht11_dat[2];
        *hum_p = (float) dht11_dat[0];
        return 1;
    } else {
        return 0;
    }
}

JNIEXPORT jfloatArray JNICALL Java_net_macdidi5_rpi_sensor_dht11_DHT11SensorReader_readData
(JNIEnv *env, jobject obj, jint gpio_pin) {
    jfloatArray j_result = (*env)->NewFloatArray(env, 2);
    jfloat result[2];
    float t, h;
    int pin = (int) gpio_pin;
    int rs = 0;
    int errorCounter = 0;
    
    do {
        rs = readDHT(pin, &t, &h);        
        if (errorCounter > 10) {
            t = -999;
            h = -999;
            break;
        }else{
            errorCounter++;
        }        
    } while (rs == 0);
    
    result[0] = (jfloat) t;
    result[1] = (jfloat) h;
    (*env)->SetFloatArrayRegion(env, j_result, 0, 2, result);
    
    return j_result;
}