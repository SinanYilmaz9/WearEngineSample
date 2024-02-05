# WearEngineSample

## Overview

HUAWEI Wear Engine provides the open capabilities of Huawei watches.

It is designed for developers of apps and services running on phones and Huawei smart watches.
By integrating the Wear Engine, it will be possible for an app or service on a phone to send messages and transfer data to a Huawei smart watches. This also works the other way round, which means that an app or service on a Huawei smart watch is able to send messages and transfer data to a phone.
The Wear Engine pools the phone and the watch's resources and capabilities, which include apps and services, creating benefits for consumers and developers alike. It enables users to use their devices in more diversified scenarios and receive more convenient services, with a smoother experience. It also expands the reach of your business, and takes your apps and services to the next level.

This document provides the sample code for integrating the Wear Engine to Android devices. This project contains sample code for calling Android APIs of the Wear Engine to send messages and data between your app on the phone and the watch. It involves only simple methods to call the APIs and is for reference only.

-   :key: Authorize

    Obtain user authorization for the openess of device capabilities.
    The code is stored in  **\\app\\src\\main\\java\\dev\\sinanyilmaz\\wearenginesample\\app\\MainActivity.kt initData\(\)**.

-   :link: Manage device connections

    Allow you to obtain the list of devices that have been connected to the Huawei Health app.
    The code is stored in  **\\app\\src\\main\\java\\dev\\sinanyilmaz\\wearenginesample\\app\\MainActivity.kt getBoundDevices\(View view\)**.

-   :mailbox_with_mail: Manage point-to-point \(P2P\) messaging

    Create an app-to-app communications channel between the phone and the watch to receive and send the customized packet messages and files on third-party apps.

    1. Check whether your app on the watch is running
    The code is stored in  **\\app\\src\\main\\java\\dev\\sinanyilmaz\\wearenginesample\\app\\MainActivity.kt getBoundDevices\(View view\)**.

    2. Send messages or files from your app on the phone to that on the watch in P2P mode
    The code is stored in  **\\app\\src\\main\\java\\dev\\sinanyilmaz\\wearenginesample\\app\\MainActivity.kt sendMessage\(View view\) sendFile\(String sendFilePath\)**.

    3. Receive the messages from your app on the watch
    The code is stored in  **\\app\\src\\main\\java\\dev\\sinanyilmaz\\wearenginesample\\app\\MainActivity.kt getBoundDevices\(View view\)**.

-   :white_check_mark: Monitor and manage device status

    Monitor or query the real-time connection status between the watch and Huawei Health.
    The code is stored in  **\\app\\src\\main\\java\\dev\\sinanyilmaz\\wearenginesample\\app\\MainActivity.kt getBoundDevices\(View view\)**.

-   :bell: Notifications

    Allow you to send notifications to devices that have been connected to the Huawei Health app.
    The code is stored in  **\\app\\src\\main\\java\\dev\\sinanyilmaz\\wearenginesample\\app\\NotificationActivity.kt  sendNotification\(View view\)**.

