Pet Prat
========

An iOS/Android chat app for integration with UBB.threads forum software shoutbox.

An extra server side script is needed for the UBB.threads installation.

Requirements
------------
You need the following to run this chat application:

 * UBB.threads v7.5.6 - Installed and working
 * PHP 5.x - Will not work in PHP4.
 * An iOS device (iPhone/iPad/iPod Touch) or Chrome browser.

Installation
------------
Upload the contents of the src directory to your forum installation.
Upload the file ubb/listshouts.inc.php into the scripts/ directory of your UBB.threads installation.
Modify the js/petprat.js file and change the following values:

    // The name of the application, will be shown in the title bar at the top.
    var appTitle = 'Pet Prat';
    // How many messages to keep at once, older messages will be purged from memory.
    var maxMessages = 50;
    // URL to your UBB.threads installation
    var URL = 'http://www.example.com/forum/ubbthreads.php';
    // The cookie prefix from your UBB.threads configuration (with "ubbt_" suffix)
    var prefix = "prefix_ubbt_"

Once that is done, you should be able to point your iOS device or Chrome browser to the
URL where you uploaded the code.

A tip is to add a bookmark for the application on your home screen on your iOS device 
for quick access to the application.
