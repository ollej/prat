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
To install Pet Prat for your UBB.threads forum, follow these instructions.

 1. Modify the js/petprat_config.js file and change the following values:

    // URL to your UBB.threads installation
    BASE_URL: 'http://www.example.com/forum/ubbthreads.php',
    // The cookie prefix from your UBB.threads configuration
    COOKIE_PREFIX: "prefix_",
    // How many messages to keep at once, older messages will be purged from memory.
    MAX_MESSAGES: 50,
    // The name of the application, will be shown in the title bar at the top.
    APP_TITLE: 'Pet Prat'

 2. Create a directory called "petprat" in the directory your forum software
    is installed.

 3. Upload the contents of the "src" directory to the "petprat" directory
    using an FTP client.

 4. Upload the file [ubb/listshouts.inc.php](https://github.com/ollej/prat/blob/master/ubb/listshouts.inc.php) into the "scripts/" directory of your UBB.threads installation.

 5. Once that is done, you should be able to point your iOS device or Chrome browser to the
    URL where you uploaded the code.

    http://www.example.com/forum/petprat

A tip is to add a bookmark for the application on your home screen on your iOS device
for quick access to the application.


License
-------
The MIT License

Copyright (c) 2011 Olle Johansson

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

