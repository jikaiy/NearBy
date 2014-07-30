NearBy
======

An android mobile application involved with Location Based Service and Wi-Fi Direct.

Users could upload his/her location to remote server and look for his/her friends' location. Google Map API is used to display locations.

The highlight here is that users who have no cellular data plan or Wi-Fi access could still upload and look for locations in this app.

We employed Wi-Fi Direct technique to implement above feature:

  1). User A without direct network access will search for a user B with network access nearby and request a Wi-Fi Direct connection to B. 
  
  2). Once Wi-Fi Direct connection between A and B built, A will send his/her location to B. B relays A location to remote server.

The project code includes two components: application code running in android mobile phone and php api running in remote server to support connections between mobiles and database.

Here are some users snapshots:

Basic upload and look for with network access
![alt tag](https://github.com/jikaiy/NearBy/blob/master/snapshot/initpintu_1.jpg)

Without direct network access, user use Wi-Fi Direct to upload his/her location
![alt tag](https://github.com/jikaiy/NearBy/blob/master/snapshot/initpintu_2.jpg)

Without direct network access, user use Wi-Fi Direct to look for his/her friends' location
![alt tag](https://github.com/jikaiy/NearBy/blob/master/snapshot/initpintu_3.jpg)
