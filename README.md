# AndroidApplication
This application allows an Android device to connect to an electronic card through Bluetooth, then control and modify the lighting level (0 to 100) of a led on the card.

Card info - oscilloscope:
The operating part is a two-channel acquisition board equipped with a Microchip dsPIC33EP64GS502 microcontroller, MCP6S22 programmable gain amplifiers, and an MCP4922 digital analogue converter designed to generate an offset on each channel.
It allows the acquisition of voltages in the 10V range at sampling frequencies of a few hundred kHz, which, associated with an an Android terminal, it can be used as an oscilloscope.
Its parameterization and the reading of the acquired data are carried out through a Bluetooth link using a RN42 module (standard 2.1 EDR, class 2) via an SPP profile parameterized at 230kbauds.

Communication protocol:
![image](https://user-images.githubusercontent.com/66179360/216107909-e4f57ef1-c752-4999-92ef-5dfc9c5424e6.png)

*the values 0x05 and 0x04 indicate respectively the HEADER and TAIL of a frame.
<br>
*the size of the PAYLOAD elements of the frame is coded on the 2 bytes following the header (LENGTHH and LENGTHL).
<br>
*the CTRL control field is set as the 2's complement of the sum of the bytes contained in PAYLOAD and PAYLOAD and LENGTH modulo 256.
<br>
*the values 0x04 and 0x05 being reserved as markers, they must not appear in the rest of the frame: consequently, if the PAYLOAD, LENGTH or CTRL field contains this value, it is preceded by an escape (ESC = 0x06) and transmitted as value + ESC. For the same same reason, if the payload shows the value 0x06, 0x06 will be transmitted followed by 0x0C. The processing of escapes is carried out after the calculation of the length and the control.


eg. for 200us/div
![image](https://user-images.githubusercontent.com/66179360/216108112-a6a50a38-f9ad-497a-917e-f762946307e2.png)

UML description of the application : 
![image](https://user-images.githubusercontent.com/66179360/216110481-e08f7324-0de9-44b5-bb34-3b5c59862fc3.png)

Video showing how the app works :
https://user-images.githubusercontent.com/66179360/216111037-d8a4eb29-9681-46db-b926-6336dbaae084.mp4



