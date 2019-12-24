# EEGBluetoothApp
---
#### Team, that works on this project:
- ([Taras Rumezhak](https://github.com/tarasrumezhak))
- ([Marian Dubei](https://github.com/MarianDubei))
- ([Denys Ivanenko](https://github.com/LilJohny))
- ([Max Kmet](https://github.com/MaxKmet))
---
## First try
As I have mentioned, our first try was on the first driver for 12V. Here you can view the [result](https://www.youtube.com/watch?v=lqymeNPksB4)
___
## TODO:
|Until the presentation|Until the end of semester|Until the end of the year|
|-|-|-|
|Descrive what we have done| Work on UI of mobile app | |
|Create demo with interaction of mobile app and PSoC|Create set of UI elements that will represent drawn conclusions fully | |
|Prepare and send sample data from PSoC via BLE| Implement algorithms that will take specific user data into account |Implemet tips for improving sleep quality and health state|
| | |Implement tracking of overall health state|
___
## Description of the problem
Main goal of our project is to analyze sleep quality. We use portable electroencephalograph of our own consruction to get electrical signals from human`s head, then we use PSoC to transfer it to mobile device. After this, we analyze recieved data to provide information about sleep quality and overall health condition. 
___
## Documentation for the Mobile App
___
## What we have done
- Implemented scanning nearby BLE devices
- Implemented discovering BLE services
- Implemented READ/WRITE/NOTIFY operation on appropriate characteristics
- Implemented reading numerical data from PSOC via BLE
- Implemented graphing of recieved data
- Implemented Fast Fourier transform
- Implemented Power Spectral density
___
### What is inside:
Current version of mobile app for Android with functions, that are listed above:
___



