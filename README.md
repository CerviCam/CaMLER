# Camera-Machine Learning Environment Research (CaMLER) Project
![Codename](https://img.shields.io/badge/Codename-CaMLER-blue)
![Status](https://img.shields.io/badge/Status-Active-green)

![Schema](screenshot01.jpg)

### Introductions
Nowadays, machine learning library such as TensorFlow has evolve rapidly so it can enabled of mobile or smartphone doing their own machine learning tasks independently. However, due to the complexities, not all of (machine learning) models can run in smartphone. Instead of doing model simplification so it can run in smartphone with the risk in reducing the quality of the outputs, we will run the model in the server (cloud). In this research project, we will create an environment research where client (application) can send the image classification request to the server and the server will doing the classification and sent back the result to the client.

## Client
![Application schema](screenshot02.jpg)

### Introductions
Instead of deploying the model in the client itself, we will create an application (client) that have ability to take the picture and send the request to the server and wait for the result or feedback. 

### Constraints
- Android application, native, written in Kotlin.
- Minimum Android SDK 21 (5.0 a.k.a Jelly Bean)
- Compiled and target SDK is 29
- Image captured should be saved in media folder<br>
    ```
  /Android/media/domain.applicationdomainname.applicationsubdomainname/applicationname/images/
    ```
- Image filename should written in format of `yyyy-mm-dd hh:mm` and saved in format of `*.jpg`
- Canceled image should be removed from the media folder
- Allowed dependencies,<br>
  - Android standard dependencies
  - Android JetPack dependency (CameraX)
  - Android Kotlin Coroutines library
  - Google Materials
  - Image: Picasso or Glide
  - Network: GSon, KTOR, OkHTTP
  - Asynchronous: ReactiveX (rxJava, rxKotlin)
- Please use the latest version for all dependencies that used in this project
- Dependencies other than in those list should have discussed first

## Server
To be discussed later.

## Contributors
- **PIC**<br>
  Nandhika Prayoga
- **Supervisor**<br>
  Arief Purnama Muharram