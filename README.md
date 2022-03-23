# TWA IAP Demo
## Table of Contents
- [Introduction](#introduction)
- [Setup](#setup)
- [Demo Flow](#demo-flow)
- [License](#license)
- [Acknowledgements](#acknowledgements)
- [Questions](#questions)

## Introduction

The purpose of this Android project is to demonstrate that it is possible to integrate an In-App Purchase solution in the native layer together with the web content of a Trusted Web Activity.

This is made by possible by using App Linking to redirect from the web content to the native layer.

- [Huawei IAP](https://developer.huawei.com/consumer/en/hms/huawei-iap/)
- [Project Base - Huawei IAP Demo](https://github.com/HMS-Core/hms-iap-clientdemo-android-studio)
- [Huawei App Linking](https://developer.huawei.com/consumer/en/agconnect/App-linking)
- [Trusted Web Activities (Android Browser Helper)](https://github.com/GoogleChrome/android-browser-helper)

## Setup

### Huawei Developer Account
A Huawei Developer Account is needed to access and configure Huawei IAP. You will need to create an account if you don't already have one. Please refer to this [guide](https://developer.huawei.com/consumer/en/doc/help/registerandlogin-0000001052613847).

### AppGallery Configuration
1.  Create an app in AppGallery Connect and configure the app information. For details, please refer to  [Configuring App Information in AppGallery Connect](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/config-agc-0000001050033072?ha_source=hms1).

2.  Create and configure your products in AppGallery Connect. For details, please refer to  [Configuring Your Products](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/config-product-0000001050033076?ha_source=hms1).
3.  Download the  **agconnect-services.json**  file of your app from AppGallery Connect, and add the file to the app-level directory of the demo.
4. Add the signing certificate and add configurations to the app-level  **build.gradle**  file.
5. Change the package name of the Android project to your desired app package name.
6. Replace  **PUBLIC_KEY**  in the  **CipherUtil**  class with the public key of your app. For details about how to obtain the public key, please refer to  [Querying IAP Information](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/query-payment-info-0000001050166299?ha_source=hms1).
7. Replace the products in this demo with your products.

## Demo Flow
1. MainActivity opens loads demo website via TWA.
2. Purchase button on demo website is a Huawei App Link which redirects to ConsumptionActivity from browser.
3. ConsumptionActivity automatically loads Huawei IAP.
4. IAP result redirects back to demo website via TWA for IAP success or fail.

## License
This Android sample code is licensed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## Acknowledgements
The code in this project has been modified from [Huawei IAP Demo](https://github.com/HMS-Core/hms-iap-clientdemo-android-studio) and [Android Browser Helper](https://github.com/GoogleChrome/android-browser-helper). Their contributions are greatly appreciated.

## Questions
If you have a questions - [Stack Overflow](https://stackoverflow.com/questions/tagged/huawei-mobile-services) is the best place for any programming inquiries. Be sure to include the tag `huawei-mobile-services`.