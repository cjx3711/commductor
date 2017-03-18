# Commductor
Commductor is an Android Application that allows multiple people to play music together. (Yes, that means you need more than one phone)

There will be one **conductor** and one to three **instrumentalists**.

The conductor can use gestures to control some aspects of the instrumentalists' music.

The instrumentalists will simply play music from their device. They will get to choose a digital instrument to play with.

Requirements:
- Min SDK: 15
- Target SDK: 25
- Bluetooth
- Accelerometer


## Bluetooth

This aspect of the application has gotten much more complicated than originally anticipated, so I have dedicated a section of the readme on how to use it.

There are 9 distinct classes and interfaces that work together to achieve Bluetooth networking for the application. What each class does can be found at the top of the file.

These are the important classes for sending and receiving data during the game.
- `BTDataPacket`
- `BTServerManager`
- `BTClientManager`
- `BTPacketCallback`

**`BTDataPacket`**

This is the data packet that gets sent to the remote devices. There is a header to define what the packet is for. The constants for this can be found in `BTPacketHeader` It contains a string, integer and float for you to use. You do not have to use all three.

**`BTServerManager` / `BTClientManager`**

These classes simply allow you to send data to any of the connected clients. The server version keeps a list of `BluetoothSocket` objects while the client version only has one.

To listen for a packet, you simply have to set the callback.

```java
// Create a callback
BTPacketCallback callback = new BTPacketCallback() {
    @Override
    public void packetReceived(BluetoothSocket socket, BTDataPacket packet) {
        Toast.makeText(AppData.getInstance().getApplicationContext(), packet.stringData, Toast.LENGTH_SHORT).show();
    }
};
// Set the callback. Setting this again will override the existing callback.
BTClientManager.getInstance().setCallback(callback);
```

To send a packet:

```java
// Create a packet (Keep the packet below 1kB.)
BTDataPacket packet = new BTDataPacket(BTPacketHeader.STRING_DATA);
packet.stringData = message;
// Send the packet
BTClientManager.getInstance().sendPacket(packet);
```
