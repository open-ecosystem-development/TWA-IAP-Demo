"This Project has been archived by the owner, who is no longer providing support.  The project remains available to authorized users on a "read only" basis."

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

This portion is for those that want to setup the demo with their own web host. Please use 'app-release-signed.apk' if you just want to test the demo as is.

### Huawei Developer Account
A Huawei Developer Account is needed to access and configure Huawei IAP and App Linking (optional). You will need to create an account if you don't already have one. Please refer to this [guide](https://developer.huawei.com/consumer/en/doc/help/registerandlogin-0000001052613847).

### AppGallery Configuration
1.  Create an app in AppGallery Connect and configure the app information. For details, please refer to  [Configuring App Information in AppGallery Connect](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/config-agc-0000001050033072?ha_source=hms1).

2.  Create and configure your products in AppGallery Connect. For details, please refer to  [Configuring Your Products](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/config-product-0000001050033076?ha_source=hms1).
3. (Optional) [Create a link](https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-applinking-createlinks-byagc-0000001058988077) for App Linking in AppGallery Connect or *continue using the link already set in the demo*. You will need to configure your **AndroidManifest.xml**, so that intent filter values for host and scheme match with those on your link. Your link will not redirect to the IAP process unless this is set properly.

![277971792_5192230950826954_6569417356395000976_n](https://user-images.githubusercontent.com/40374800/163491077-acfab929-d63c-49ed-b3d3-6d60b4e3a73e.png)
![2022-04-14 15_46_48-278020315_311775591070023_9017516905437510887_n png - IrfanView](https://user-images.githubusercontent.com/40374800/163491092-35fc1283-ed76-43a7-9187-69727c2c434d.png)

4.  Download the  **agconnect-services.json**  file of your app from AppGallery Connect, and add the file to the app-level directory of the demo.
5. Add the signing certificate and add configurations to the app-level  **build.gradle**  file.
6. Change the package name of the Android project to your desired app package name.
7. Replace  **PUBLIC_KEY**  in the  **CipherUtil**  class with the public key of your app. For details about how to obtain the public key, please refer to  [Querying IAP Information](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/query-payment-info-0000001050166299?ha_source=hms1).
8. Replace the products in this demo with your products.

### Web Host Setup
You can [create your own App Linking URL](https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-applinking-createlinks-byagc-0000001058988077) or continue using the current demo link - `https://pictroom.dre.agconnect.link/OPA1?id=123` and add it to your own webhost. This link should still work as long as the intent filter in the **AndroidManifest.xml** remains the same.

If setting up a web host is too difficult, you can alternatively create a localhost with a link pointing to the App Linking URL.

1. `mkdir host && cd host`
2. `npm install http-server`
3. `touch index.html`
4. Add the following code in `index.html`

       <a href="https://pictroom.dre.agconnect.link/OPA1?id=123">
          <button>Purchase</button>
       </a>

5. Run the localhost `./node_modules/http-server/bin/http-server`
6. Change  the values of `twaManifest.hostName` to your http-server localhost IP & port (for example `http://127.0.0.1:8080`) and `twaManifest.launchUrl` to '/' inside `app/build.gradle` (line 24, 25) before building your project.

![2022-04-14 15_45_26-TWA IAP_HMS Ecosystem - Copy – build gradle (_app) Administrator](https://user-images.githubusercontent.com/40374800/163491001-eea2a73b-05e2-4954-924d-024203e718eb.png)

### Complete TWA Setup
Please follow this article by [Sven Budak](https://svenbudak.medium.com/) to set up TWA.

[https://svenbudak.medium.com/this-twa-stuff-rocks-finally-i-got-my-pwa-on-google-play-store-b92fe8dae31f](https://svenbudak.medium.com/this-twa-stuff-rocks-finally-i-got-my-pwa-on-google-play-store-b92fe8dae31f)

You basically need to create a directory and file `.well-known/assetlinks.json` in your web host root with the following information:

```
[{
  "relation": ["delegate_permission/common.handle_all_urls"],
  "target": {
    "namespace": "android_app",
    "package_name": "com.example.app",
    "sha256_cert_fingerprints": [
      "hash_of_app_certificate"
    ]
  }
}]
```

Please refer to the tutorial to get the `sha256_cert_fingerprints` value. Once you have set up your host and built a release APK, the URL bar inside your TWA APK will disappear.

## Demo Flow
1. MainActivity opens loads demo website via TWA.
2. Purchase button on demo website is a Huawei App Link which redirects to ConsumptionActivity from browser.
3. ConsumptionActivity automatically loads Huawei IAP.
4. IAP result redirects back to demo website via TWA for IAP success or fail.

https://user-images.githubusercontent.com/40374800/159623963-25dbd52c-abeb-4283-b809-de3298599c53.mp4

## License
This Android sample code is licensed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## Acknowledgements
The code in this project has been modified from [Huawei IAP Demo](https://github.com/HMS-Core/hms-iap-clientdemo-android-studio) and [Android Browser Helper](https://github.com/GoogleChrome/android-browser-helper). Their contributions are greatly appreciated.

## Questions
If you have a questions - [Stack Overflow](https://stackoverflow.com/questions/tagged/huawei-mobile-services) is the best place for any programming inquiries. Be sure to include the tag `huawei-mobile-services`.
