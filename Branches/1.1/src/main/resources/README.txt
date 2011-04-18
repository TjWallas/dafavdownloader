deviantART Favorites Downloader
Created by Dragoniade

Version 1.1.0

1. Installation
2. Usage
3. Changes log

1. Installation

The software requires the Java Runtime Environment to work. If you already have
a version installed and configured, the software can use it. If it is not 
installed, you can go online and install it at 
http://www.oracle.com/technetwork/java/index.html or use the bundled 
installation if you're using windows



1.1 Windows installer (Stand Alone)

This version is appropriate if you already have a version of Java Runtime 
Environment 1.6 installed.



1.2 Windows installer with bundled Java JRE 1.6

If you don't have the Java Runtime Environment, this version come with a 
stand alone version bundled. The download size is bigger, but you don't need 
to worry about conflict with installed version of Java.



1.3 Zipped package

This version is a plain old zip containing the structure and executable files.
Unzip where you want to install the software. To run, simply double click on 
the main executable jar or use the following command:
javaw -jar da-favorite-*.jar  [replacing the * with the appropriate version].

This version can be used on any OS that support Java (Windows, Linux, Mac, 
etc).



2. Usage

You can configure your profile in the Preference screen. Any profile can be 
saved and loaded should you have need for multiple one.  You can also save 
your profile as 'Default'. Each time the software will start, this profile 
will be reloaded.



2.1 Username

Upon start-up, the downloader assume the current windows username is the same as
the deviation account being downloaded. You can change this behavior in the 
preferences screen.


2.2 Download
After selecting the username, you can either download the favorites or the galleries
from that account. Select which one you want. 

2.3 Location
Upon start-up, the downloader will also download the files into your home folder
( /home/***** on Linux or C:\Documents and Settings\***** on  Windows) under 
the structure \deviantART\%user%\%artist%\%title%\%filename%.

You can change those in the preferences screen to use a better location fitting
your needs. If you have many favorites and plan to regularly synchronize them, 
it is recommended to select a location where the files will remain unchanged. 
The downloaded won't download file that already are up to date.

The downloader allow you to save deviation that are labeled as 'Mature' to a 
different folder. This is convenient if you plan to use your saved deviations
as a screen saver.


2.4 Throttling
The downloader warn when more than 480 deviations have been skipped for 
download. A deviation is skipped when it has been previously and saved on disk.
Should you plan to do synchronization regularly, it is recommend you throttle 
your request. This add a few seconds delay between each search if no files has
been downloaded. This prevent deviantART from blocking you temporarily should 
too many request hammer their server.  A 5 seconds throttle is the minimum default.


3 Changes log

Version 1.1.0  2011/01/23
* Added Support for custom searcher. 
* Added the notion of Document, Images and Video
* Added and set as default the RSS searcher. This is a solution for DA pulling the
Stream Difi and not offering any support.
* Added the ability to download a user gallery rather than only the favorites.
* Forced throttling to 5 seconds minimum and applied it to every 20 requests. This is due
to the RSS feed not supplying filename and having to do a request to retrieve it.


Version 1.0.1  2010/12/05
* Lessen the destination validation for Windows

Version 1.0.0  2010/12/04
* Initial release