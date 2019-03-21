![](https://github.com/OpenLiberty/open-liberty/blob/master/logos/logo_horizontal_light_navy.png)

# Reactive Extensions Backpressure Sample
## A simple data store application using RxJava reactive programming with JAX-RS 2.1 Reactive Extensions

This sample is intended to help understand how to write reactive programming applications that use backpressure to control the amount of data stored.
In this sample, a new customer is created causing the data store creation and the data collected to be stored.
Backpressure is used to determine the maximum amount of data collected.
JAX-RS 2.1 Reactive Extension Flowable is used to retreive the collected data.

To run this sample, first [download](https://github.com/OpenLiberty/sample-reactive-extensions/archive/master.zip) or clone this repo - to clone:
```
git clone git@github.com:OpenLiberty/sample-reactive-extensions.git
```

From inside the sample-reactive-extensions directory, build and start the application in Open Liberty with the following command:
```
mvn clean package liberty:run-server
```

The server will listen on port 9080 by default.  You can change the port (for example, to port 9081) by adding `mvn clean package liberty:run-server -DtestServerHttpPort=9081` to the end of the Maven command.

After the server has started, you should see some output like this:
```
CWWKF0011I: The server sampleAppServer is ready to run a smarter planet.
```

At this point, you can enter the following URL into your browser:
```
http://localhost:9080/reactive/test
```

After the test is complete, you shoud see output like this on your browser:
```
ReactiveTestResource Test was successful
```

Press `Ctrl-C` from the command line to stop the server.

Please take a look at the source code that makes this possible.  If you run into any problems with the sample, please let us know by opening a new issue.

Thanks!

