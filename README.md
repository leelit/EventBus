# EventBus

Lightweight EventBus:

post support：

- post
- postDelay
- postSticky

register threadmode:

- main thread
- sub thread
- post thread

read the library package for more detail


## **ScreenShot**

download the apk, and run...
you also can see the art.gif, but it is a little bit different from the latest version


## **Usage**

just copy the library pacakge into your project, then

1、register events

```
EventBus.register(EventType.MAIN_ACTIVITY_EVENT, this); // callback in main thread
EventBus.register(EventType.OTHER_EVENT, this, ThreadMode.SUB_THREAD);  // callback in sub thread
```

2、define your call back

```
@Override
public void onBusCall(Event msg) {
    switch (msg.type) {
        case EventType.MAIN_ACTIVITY_EVENT:
            msg.data; // get your data
            break;
       
        case EventType.OTHER_EVENT:
          msg.data; // get your data
          break;
}
```

3、post event anywhere

```
Event event = EventBus.createEvent(EventType.MAIN_ACTIVITY_EVENT,data);
EventBus.post(event);

Event event = EventBus.createEvent(EventType.OTHER_EVENT,data);
EventBus.post(event);
```

read the demo for more detail

## **LICENSE**
**MIT**

