# HandlerBus

## **ScreenShot**

![](https://github.com/leelit/HandlerBus/blob/master/art.gif)


## **Usage**

just copy the library pacakge into your project, then

1、register events

```
HandlerBus.register(EventType.MAIN_ACTIVITY_EVENT, this); // callback in main thread
HandlerBus.register(EventType.OTHER_EVENT, this, false);  // callback in sub thread
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
Event event = HandlerBus.createEvent(EventType.MAIN_ACTIVITY_EVENT,data);
HandlerBus.post(event);

Event event = HandlerBus.createEvent(EventType.OTHER_EVENT,data);
HandlerBus.post(event);
```

read the demo for more detail

## **LICENSE**
**MIT**

