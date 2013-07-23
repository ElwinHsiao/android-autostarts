This is a branch of Android-Autostarts.
The main difference is this one speed up the load process and add newly installed package notification. 
the original project is at https://github.com/miracle2k/android-autostarts

The feature added and will add in future is below(with prefix "*" means already added, "+" means haven't): 
* Speed up load process by change the sort algorithm.
* Notify user when there is newly installed package, so that user can disable some receiver immediately
* The view option can select hide disabled component or enabled component, this will usefull when user 
  rummaged through a devices that have so many package to looking the particular component to disable it.
* Sort the component by last update time when group by application.
+ separate the system package and user package by tabs. Only show the user package tab when launch, this 
  meaning only parser the user package when launch, avoid to parser system package every time. Because 
  in common case, user don't need to change system app.
+ Add a swith(android.widget.swith) to each item, so user can change the component directly.
+ Operate component at background.
+ Gather together all actions within the same receiver when showing.
+ Also manage the receiver which register to ActivityManager dynamic.
