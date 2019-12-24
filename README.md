# EEGBluetoothApp
---
#### Team, that works on this project:
- [Taras Rumezhak](https://github.com/tarasrumezhak)
- [Marian Dubei](https://github.com/MarianDubei)
- [Denys Ivanenko](https://github.com/LilJohny)
- [Max Kmet](https://github.com/MaxKmet)
---
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
An EEG tracks and records brain wave patterns. Small metal discs with thin wires (electrodes) are placed on the scalp, and then send signals to a computer to record the results. Normal electrical activity in the brain makes a recognizable pattern. 
Any synaptic activity generates a subtle electrical impulse. The burst of a single neuron is difficult to reliably detect without direct contact with it. But whenever thouthands of neurons fire in sync, they generate electrical field, which is strong enough to spread through tissue, bone and skull. So, it can be measured on head surface.
We don't need any additional amplifer because we use ADS1299 which already has it.
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
Current version of mobile app for Android with functions, that are listed above.

### Application description:
  - When you open our application you see the home page
  - On the down bar you can choose the sections of oour app
  - When you start using the device, you must go to settings section, turn the Bluetooth on and start scanning
  - You can see available devices and click on the Sleep Monitor based on PSoC
  - Also you can change some basic elements in the settings, for example choose which information and statistics will be shown
  - After this you can start using the device
  - The data from electrodes is collected during the night and sent to the mobile device
  - In our application we use Fourier transform for our EEg data, thus we have the spectrogram
  - After this we implement some algorithms to work with collected data
  - And after this a user can detailed information about his/her sleep
  - When there is the series of interactions (every night usage) the application shows some more general statisctics
___
### Used Sources
- [About EEG and how it shows current human state](https://www.epi.ch/wp-content/uploads/Artikel-Achermann_1-09.pdf)
- Computational Frameworks for the Fast Fourier Transform by Charles Van Loan
### Branches of repository
- legacy branch - Here we have main functionality implemented
- master branch - Here we have main UI work
### Activities in our app:
- CharacteristicOperationActivity provides interface for interacting with BLE characteristics 
- ScanActivity provides interface for scanning nearby BLE devices
- ServiceDiscoveryActivity provides interface for discovering available services in particular BLE device
- Entry activity -  ain’t activity of application, provides interface for all available functions of app
