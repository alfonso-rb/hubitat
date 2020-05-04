# Hubitat iAquaLink driver - customized

If you only need to control one device you can use "aqualink-driver" in the OLD section,  if you want to control more devices, color lights and get temperature data install all drivers in the ParentChild folder.

## Usage example

I modified mikec85's code heavily since it didn't pull a lot of relevant data from my Jandy when I tried to use his.
Just load these as custom drivers in your hubitat. Once you do that, then you can add a virtual device using the parent driver.
It found my child devices which for me are 3, one of them being an Air Blower for the Spa.

Enter the __Userid - Email__ & __User Password__ and it should pull the data.

I am not experienced in Groovy coding at all so I fumbled through a lot of this. Please feel free to improve the code and add functionality if you can!

## Completed so far

- Pool pump on and off
- Toggle Solar Heating On/Off
- Toggle Spa Mode On/Off
- Identify if Solar Heater is On Heating vs. On vs. Off
- Freeze Protection State
- Toggle Gas Heating On/Off
- Identify if Gas Heater is On Heating vs. On vs. Off
- Air blower (child device) is working

## To Do

- Add my hardware info to this README so if you want to try it you can compare
  - Jandy Model #
  - iAqualink Model #
  - Pool pump is a single speed DC pump, so I can't test if this works with variable speed pump.
  - Child devices I have are
    - Pool light (not attached, I'm using an Inovelli smart dimmer instead)
    - Air blower
    - Aux3 (not attached)
- See if I can hide the password field in the Preferences for the device
- Verify that attributes and buttons can be setup in a dashboard (will just update here if so)
  - pool temperature?
  - spa temperature
  - filter pump on/off
  - Solar heating on/off
  - Gas heating on/off?
  - Spa mode on/off
  - Air blower on/off

- Confirm whether I can integrate it with Alexa from this. The Alexa app was very limited in what information it provided.

  - Can Alexa report the temperature?
  - Can Alexa turn on and off the filter pump
  - Can Alexa turn on and off solar heating (not really critical)?
  - Can Alexa turn on and off the gas heating?
  - Can Alexa turn on and off spa mode?
  - Can Alexa turn on and off the Air blower?
