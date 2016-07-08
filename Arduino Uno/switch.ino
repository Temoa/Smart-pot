#include <SPI.h>
#include <Ethernet.h>
#include <Wire.h>
#include <dht11.h>

#define APIKEY         "your yeelink apikey"
#define DEVICEID       345349
#define SENSORID       388863
String SENSORID_TEMP = "384391";
String SENSORID_HUM  = "384782";
String SENSORID_WATER = "389655";

#define TEMP_PIN  2
#define PUMP_PIN  3
#define HUM_PIN 4
#define WATER_PIN 5

byte mac[] = { 0x00, 0x1D, 0x72, 0x82, 0x35, 0x9D};

EthernetClient client, client2;

char server[] = "api.yeelink.net";

dht11 dht11;

unsigned long lastConnectionTime = 0;
boolean lastConnected = false;
const unsigned long postingInterval = 5 * 1000;
String returnValue = "";
boolean ResponseBegin = false;

int count = 0;
int count2 = 0;

boolean canWatering = false;
boolean ifControl = false;

void setup() {
  delay(2000);

  pinMode(PUMP_PIN, OUTPUT);
  digitalWrite(PUMP_PIN, HIGH);
  pinMode(13, OUTPUT);
  digitalWrite(13, LOW);

  Wire.begin();
  Serial.begin(9600);
  if (Ethernet.begin(mac) == 0) {
    Serial.println("Failed to configure Ethernet using DHCP");
    for (;;)
      ;
  } else {
    Serial.println("Ethernet configuration OK");
  }
  waterBox();
}

void loop() {
  getSwitchState();
}

void getSwitchState() {
  if (!client.connected() && (millis() - lastConnectionTime > postingInterval)) {
    watering();
    getData();

    count++;
    count2++;
    if (count == 100) {
      count = 0;
      sendTempData();
      sendHumData();
      sendWaterData();
    }
    if (count2 == 9) {
      count2 = 0;
      waterBox();
    }
  }

  if (client.available()) {
    char c = client.read();
    if (c == '{')
      ResponseBegin = true;
    else if (c == '}')
      ResponseBegin = false;

    if (ResponseBegin)
      returnValue += c;
  }

  if (returnValue.length() != 0 && (ResponseBegin == false)) {
    if (returnValue.charAt(returnValue.length() - 1) == '1') {
      if (canWatering) {
        ifControl = true;
        Serial.println("yeelink:pump on");
        digitalWrite(PUMP_PIN, LOW);
        delay(2000);
        digitalWrite(PUMP_PIN, HIGH);
        ifControl = false;
        restEthernet();
        sendHumData();
      } else {
        Serial.println("out of water,pump off");
      }
    }
    else if (returnValue.charAt(returnValue.length() - 1) == '0') {
      Serial.println("yeelink:pump off");
      digitalWrite(PUMP_PIN, HIGH);
    }
    returnValue = "";
  }

  if (!client.connected() && lastConnected) {
    Serial.println("client1 disconnecting.");
    Serial.println();
    client.stop();
  }
  lastConnected = client.connected();
}

void getData() {
  // if there's a successful connection:
  if (client.connect(server, 80)) {
    // send the HTTP GET request:
    client.print("GET /v1.0/device/");
    client.print(DEVICEID);
    client.print("/sensor/");
    client.print(SENSORID);
    client.print("/datapoints");
    client.println(" HTTP/1.1");
    client.println("Host: api.yeelink.net");
    client.print("Accept: *");
    client.print("/");
    client.println("*");
    client.print("U-ApiKey: ");
    client.println(APIKEY);
    client.println("Content-Length: 0");
    client.println("Connection: close");
    client.println();
  } else {
    Serial.println("client1 connection failed");
    client.stop();
    restEthernet();
  }
  lastConnectionTime = millis();
}

void sendTempData() {
  int temp = 0;
  if (dht11.read(TEMP_PIN) == DHTLIB_OK) {
    temp = dht11.temperature - 3;
  }
  if (!client2.connected()) {
    Serial.print("temp: ");
    Serial.println(temp);
    sendData(temp, SENSORID_TEMP);
    delay(200);
  }
}

void sendHumData() {
  int hum = analogRead(HUM_PIN);
  int humPer = (int)(hum / 750.0 * 100.0);
  if (humPer < 0) {
    humPer = 0;
  } else if (humPer > 100) {
    humPer = 100;
  }
  Serial.print("hum: ");
  Serial.println(humPer);
  if (!client2.connected()) {
    sendData(humPer, SENSORID_HUM);
    delay(200);
  }
}

//check the waterBox have water
void waterBox() {
  int water = analogRead(WATER_PIN);
  if (water < 600) {
    Serial.println("have water");
    canWatering  = true;
  } else {
    Serial.println("out of water");
    canWatering = false;
  }
}

void sendWaterData() {
  int water = analogRead(WATER_PIN);
  if (water < 600) {
    Serial.println("send 1");
    sendData(1, SENSORID_WATER);
    delay(200);
  } else {
    Serial.println("send 0");
    sendData(0, SENSORID_WATER);
    delay(200);
  }
}

//water for plant
void watering() {
  if (canWatering && !ifControl) {
    int hum = analogRead(HUM_PIN);
    if (hum < 300) {
      client.stop();
      Serial.println("gan,pump on");
      digitalWrite(PUMP_PIN, LOW);
      delay(3000);
      digitalWrite(PUMP_PIN, HIGH);
      restEthernet();
      sendHumData();
    } else {
      Serial.println("shi,pump off");
      digitalWrite(PUMP_PIN, HIGH);
    }
  } else {
    Serial.println("out of water,can not watering");
  }
}

void restEthernet() {
  if (Ethernet.begin(mac) == 1)
    Serial.println("Ethernet Reset");
}

void sendData(int data, String sensorID) {
  if (client2.connect(server, 80)) {
    client2.print("POST /v1.0/device/");
    client2.print(DEVICEID);
    client2.print("/sensor/");
    client2.print(sensorID);
    client2.print("/datapoints");
    client2.println(" HTTP/1.1");
    client2.println("Host: api.yeelink.net");
    client2.print("Accept: *");
    client2.print("/");
    client2.println("*");
    client2.print("U-ApiKey: ");
    client2.println(APIKEY);
    client2.print("Content-Length: ");
    int thisLength = 10 + getLength(data);
    client2.println(thisLength);
    client2.println("Content-Type: application/x-www-form-urlencoded");
    client2.println("Connection: close");
    client2.println();
    client2.print("{\"value\":");
    client2.print(data);
    client2.println("}");
    client2.stop();
  } else {
    Serial.println("client2 disconnect");
    client2.stop();
  }
}

int getLength(int value) {
  int digits = 1;
  int dividend = value / 10;
  while (dividend > 0) {
    dividend = dividend / 10;
    digits++;
  }
  return digits;
}

