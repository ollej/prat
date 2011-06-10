/*
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
*/
console.log('Calling Ext.setup()');

Ext.setup({
    tabletStartupScreen: 'images/tablet_startup.png',
    phoneStartupScreen: 'images/phone_startup.png',
    icon: 'images/icon.png',
    glossOnIcon: true,
    onReady: function() {
        console.log('onReady');
        // Setup configuration from external config file.
        var appTitle = PETPRAT.APP_TITLE;
        var maxMessages = PETPRAT.MAX_MESSAGES;
        var URL = PETPRAT.BASE_URL;
        var prefix = PETPRAT.COOKIE_PREFIX + "ubbt_";
        if (!PETPRAT.BASE_URL) {
            Ext.Msg.alert("Error", "No configuration found!");
            return;
        }

        var startId = 0;
        var running = false;
        var user = {};
        var getUnixtime = function() {
            var d = new Date;
            var unixtime_ms = d.getTime();
            var unixtime = parseInt(unixtime_ms / 1000);
            return unixtime;
        };
        var helperFunctions = {
            compiled: true,
            formatMessageBody: function(values) {
                var text = values.body;
                text = linkify(text);
                var newtext = text.replace(/^\/me /, '* ' + values.username + ' ');
                if (text != newtext) {
                    return '<span class="me">' + newtext + '</span>';
                } else {
                    return text;
                }
            },
            formatTime: function(t) {
                return Date.parseDate(t, 'U').format('H:i:s');
            },
            showAvatar: function(t) {
                if (t && t != 'http://') {
                    return t;
                } else {
                    return "images/noavatar.png";
                }
            },
            showUserColor: function(c) {
                return c ? " style='color: " + c + ";'" : '';
            },
            getUserColor: function(values) {
                var idx = userStore.findExact('user_id', values['user_id']);
                //console.log('getuserColor rec:', userStore, idx, values['user_id']);
                if (!idx || idx === -1) return;
                var rec = userStore.getAt(idx);
                //console.log('rec:', rec);
                if (rec) {
                    //console.log(this, rec, rec.get('namecolor'), idx, this.showUserColor(rec.get('namecolor')));
                    return this.showUserColor(rec.get('namecolor'));
                } else {
                    return '';
                }
            },
            highlightUser: function(values) {
                if (!user.username || !values.body) {
                    return;
                }
                var pos = values.body.toLowerCase().indexOf(user.username.toLowerCase());
                if (pos >= 0) {
                    return "highlightUser";
                }
            }
        }
        var readCookie = function(name) {
            var nameEQ = prefix + name + "=";
            var ca = document.cookie.split(';');
            for(var i=0;i < ca.length;i++) {
                var c = ca[i];
                while (c.charAt(0)==' ') c = c.substring(1,c.length);
                if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
            }
            return null;
        };
        var highlightMessages = function() {
	    //console.log('highlightMessages called', user);
            if (!user.username) { return; }
            var els = Ext.select("div[class*=x-list-item]", messageList.getEl().dom);
            if (!els) {
		console.log('No messages found.');
		return;
	    }
            els.each(function(el, c, idx) {
		//console.log('checking element:', el);
                var msgbody = Ext.select("div[class=message-text]", el.dom).first();
		var msguser = Ext.select("div[class=message-username]", el.dom).first();
                //console.log("checking body:", msgbody, 'msguser', msguser);
                if (!msgbody || !msguser) { return; }
		var slashme = Ext.select("span[class=me]", msgbody.dom).first();
                var bodytext = msgbody.getHTML().toLowerCase();
                var username = user.username.toLowerCase();
                //console.log('bodytext:', bodytext, 'username:', username);
                if (bodytext.indexOf(username) >= 0 &&
		    !(slashme && msguser.getHTML() == user.username)) {
                    el.addCls("highlightUser");
                } else {
                    el.removeCls('highlightUser');
                }
            });
        };
        var updateUser = function(id, data) {
            //console.log('updateUser - user', dump(user), 'data', dump(data));
            user.id = id;
            user = Ext.apply(user, data);
            var topToolbar = panel.getDockedComponent(0);
            var btn = topToolbar.getComponent('loginButton');
            if (user.username && btn) {
                btn.setText('Logged in as: ' + user.username);
            }

            // Highlight messages by this user
            highlightMessages();
        };
        var dump = function(obj) {
            var str = "";
            for (var i in obj) if (obj.hasOwnProperty(i)) {
                str += i + " = " + obj[i] + "\n";
            }
            return str;
        };
        var loadUserInfo = function(id) {
            // TODO: Use userStore.load instead
            //console.log('loadUserInfo', id);
            userStore.load({
                addRecords: true,
                params: { id: id },
                callback: function(recs, op, success) {
                    if (success && recs.length > 0) {
                        updateUser(id, recs[0].data);
                        console.log('userStore:', userStore);
                    }
                }
            }, true);
        };
        var loginUser = function() {
            // if user/pass stored, login directly, otherwise open login panel
            loginPanel.show();
        };
        var checkUser = function() {
            //console.log('checkUser');
            if (user.username) {
                return;
            }
            var id = readCookie('myid');
            if (id > 0) {
                user.id = id;
                loadUserInfo(id);
            } else {
                loginUser();
            }
        };
        var playPauseHandler = function() {
            var topToolbar = panel.getDockedComponent(0);
            var btn = topToolbar.getComponent('btnPlayPause');
            if (this.running) {
                console.log('stop loading', btn);
                btn.addCls('x-button-pressed');
                stopLoading();
            } else {
                console.log('refresh messages', btn);
                btn.removeCls('x-button-pressed');
                refreshMessages();
            }
        };
        var refreshMessages = function() {
            startId = 0;
            loadMessages();
        };
        var loadMessages = function() {
            this.running = true;
            msgreader.cancel();
            msgStore.load();
        };
        var stopLoading = function() {
            msgreader.cancel();
            this.running = false;
        };
        var msgreader = new Ext.util.DelayedTask(loadMessages);
        var delayMessageReading = function() {
            this.running = true;
            msgreader.delay(1000);
        }
        var parseLinkFromText = function(text) {};
        var ajaxReq = function(url, params, cb, opts) {
            //params = Ext.apply({}, params, { format: 'json' });
            opts = opts || {};
            Ext.Ajax.request({
                url: url,
                method: opts.method || 'GET',
                params: params,
                failure: opts.failure || function(response, opts) {
                    console.log('Failed loading:', response, opts);
                    //Ext.getBody().unmask();
                },
                success: function(response, opts) {
                    var data = response.responseText;
                    if (data && opts.params.format == "json") {
                        //console.log("Decoding json");
                        data = Ext.util.JSON.decode(data);
                    }
                    if (cb) {
                        //console.log('received data:', data);
                        cb(data, response, opts);
                    }
                }
            });
        };
        var updateStartValue = function() {
            var last = msgStore.last();
            if (last) {
                var lastId = last.get('id');
                if (lastId > startId) {
                    startId = lastId;
                }
            }
        };
        var scrollToBottom = function(animate) {
            messageList.scroller.updateBoundary();
            messageList.scroller.scrollTo({x: 0, y:messageList.scroller.size.height}, animate);
        };
        var pruneMessages = function(store) {
            //console.log('pruneMessages');
            var count = store.getCount();
            if (count > maxMessages) {
                var purgeItems = store.getRange(0, count - maxMessages - 1);
                console.log("Purging old messages:", purgeItems);
                store.remove(purgeItems);
            }
        };
        var updateMessages = function(users) {
            var userids = [];
            for (var i in users) if (users.hasOwnProperty(i)) {
                userids.push(users[i].get('user_id'));
            }
            var msgs = messageList.queryBy(function(rec, id) {
                if (users.indexOf(rec.get('user_id')) >= 0) {
                    rec.commit();
                }
            });
        };
        var onAddUser = function(store, recs, idx) {
            purgeOldUsers();
            updateMessages(recs);
        };
        var purgeOldUsers = function() {
            //var msgids = getAllUsers(messageList.getRange());
            console.log('Purging old users.');
            userStore.each(function(rec) {
                var userid = rec.get('user_id');
                //if (msgids.indexOf(rec.get('user_id')) === -1) {
                if (messageStore.findExact('user_id', rec.get('user_id')) === -1) {
                    console.log('Removing old user:', rec);
                    userStore.remove(rec);
                }
            });
        };
        var getAllUsers = function(msgs) {
            var ids = [];
            for (var i in msgs) if (msgs.hasOwnProperty(i)) {
                var uid = msgs[i].get('user_id');
                if (ids.indexOf(uid) === -1) {
                    var urec = userStore.findExact('user_id', uid);
                    if (urec === -1) {
                        ids.push(uid);
                    }
                }
            }
            return ids;
        };
        var addUsers = function(msgs) {
            var ids = getAllUsers(msgs);
            if (ids.length > 0 ) {
                userStore.load({
                    addRecords: true,
                    params: { id: ids.join(',') },
                    callback: function(recs, op, success) {
                        console.log('loaded users.', recs);
                    }
                }, true);
            }
        };
        var onMessageUpdate = function(store, data, success) {
            //console.log('onMessageUpdate');
            updateStartValue();
            pruneMessages(store);
            scrollToBottom(false);
            highlightMessages();
            addUsers(data);
        }

        var addUsername = function(username) {
            var tbar = panel.getDockedComponent(1);
            var msgField = tbar.getComponent('messageField');
            var text = msgField.getValue();
            if (text) {
                text = text + ' ' + username;
            } else {
                text = username + ': ';
            }
            msgField.setValue(text);
            msgField.focus();
        };

        var onSwipeMessage = function(dv, idx, itm, ev) {
            console.log('swipe item', ev);
        };

        var onSwipeUser = function(dv, idx, itm, ev) {
            console.log('swipe user', ev);
        };

        var onTapItem = function(dv, idx, itm, ev) {
            console.log('item tapped:', dv, idx, itm, ev);
            var rec = dv.getRecord(itm);
            if (!rec) {
                console.log("No record at tapped id", idx);
                return;
            }
            console.log('item rec:', rec);
            var username = rec.get('username');
            addUsername(username);

            // Find link in message
            var link = parseLinkFromText(rec.body);
            if (link) {
                // Do something
            }
        };

        var sendMessage = function() {
            var message = textField.getValue();
            ajaxReq(
                URL,
                { ubb: 'shoutit', shout: message },
                function(data, resp, opts) {
                    console.log('Message sent ok.', data, resp, opts);
                    textField.reset();
                },
                {
                    method: 'POST',
                    failure: function(resp, opts) {
                        Ext.Msg.alert(
                            'Error posting',
                            "Couldn't post message.",
                            function() {
                                loginPanel.show();
                            }
                        );
                    }
                }
            );
        };

        var loginUserViaForm = function(form) {
            var vals = form.getValues();
            ajaxReq(
                URL,
                { ubb: 'start_page', Loginname: vals.Loginname, Loginpass: vals.Loginpass, rememberme: '1', firstlogin: '1', buttlogin: 'Logga in' },
                function(data, resp, opts) {
                    //console.log('login returned:', resp, opts, resp.getAllResponseHeaders());
                    var cookieuser = readCookie("myid");
                    //console.log("Cookie myid=", cookieuser);
                    if (cookieuser > 0) {
                        // When backend api has login functionality, it should return user info directly.
                        form.hide();
                        updateUser(cookieuser, {
                            //username: vals.Loginname,
                            password: vals.Loginpass
                        });
                        loadUserInfo(cookieuser);
                    } else {
                        Ext.Msg.alert("Login error", "There was an error logging in. Please try again.");
                    }
                    //ubb7_ubbt_myid
                },
                {
                    method: 'POST',
                    failure: function(resp, opts) {
                        Ext.Msg.alert("Login error", "There was an error logging in. Please try again.");
                    }
                }
            );
            return false;
        };

        Ext.regModel('Message', {
            fields: ['id', 'user_id', 'username', 'body', 'time', 'avatar']
        });
        var msgStore = new Ext.data.Store({
            model: 'Message', autoLoad: true, scroll: 'vertical',
            sorters: [ { property: 'id', direction: 'ASC' } ],
            load: function(opts) {
                var store = this;
                ajaxReq(
                    URL, 
                    { ubb: 'listshouts', start: startId, format: 'json', showlocal: '1', longpoll: '1' },
                    function(data) {

                        // Update the message list
                        if (data && data.length > 0) {
                            var append = startId ? true : false;
                            store.loadData(data, append);
			    //console.log('firing load event');
                            store.fireEvent('load', store, data, true);
                            //store.fireEvent('add', store, data, true);
                        }
                        checkUser();
                        delayMessageReading();
                    },
                    {
                        failure: function(response, opts) {
                            console.log('Failed loading shout messages.', response, opts);
                            delayMessageReading();
                        }
                    }
                );
            },
            listeners: {
                load: onMessageUpdate, add: function() { console.log('addmsgstore', arguments); }
            }
        });

        Ext.regModel('User', {
            fields: ['user_id', 'username', 'avatar', 'title', 'namecolor', 'email', 'homepage',
            'occupation', 'location', 'hobbies'],
        });
        var userStore = new Ext.data.Store({
            model: 'User', scroll: 'vertical',
            proxy: {
                type: 'ajax', url: URL,
                extraParams: { 'ubb': 'listshouts', 'action': 'showuser', 'format': 'json' },
                reader: { type: 'json', root: 'users', idProperty: 'user_id' }
            },
            sorters: [{
                property: 'username', direction: 'ASC',
                sorterFn: function(o1, o2) {
                    var v1 = this.getRoot(o1)[this.property].toLowerCase(),
                        v2 = this.getRoot(o2)[this.property].toLowerCase();
                    return v1 > v2 ? 1 : (v1 < v2 ? -1 : 0);
                },
            }],
            listeners: {
                add: onAddUser,
                scope: this
            }
        });

        // Main panels
        var loginPanel = new Ext.form.FormPanel({
            url: URL, baseParams: { ubb: 'login', action: 'send', rememberme: 1 },
            fullscreen: false, cardSwitchAnimation: 'pop', frame: true, submitOnAction: true,
            autoRender: true, floating: true, modal: true, centered: true, title: 'Enter credentials',
            hideOnMaskTap: false, scroll: 'vertical', monitorValid: true, standardSubmit: false,
            items: [
                { xtype: 'textfield', label: 'Username', name: 'Loginname', allowBlank: false,
                  autoCapitalize: false, useClearIcon: true, autoCorrect: false,
                  autoComplete: false },
                { xtype: 'passwordfield', label: 'Password', name: 'Loginpass', allowBlank: false }
            ],
            dockedItems: {
                xtype: 'toolbar', dock: 'bottom',
                items: [
                    { xtype: 'spacer' },
                    { xtype: 'button', text: 'Close',
                        handler: function() {
                            loginPanel.hide();
                        }
                    },
                    { xtype: 'button', ui: 'confirm', text: 'Login',
                        handler: function() {
                            loginUserViaForm(loginPanel);
                        }
                    }
                ]
            },
            listeners: { beforesubmit: loginUserViaForm }
        });

        var messageList = new Ext.List({
            title: 'Messages', scroll: 'vertical', itemSelector: 'div[class*=messageItem]',
            store: msgStore, multiSelect: false, singleSelect: false,
            pinHeaders: false, indexBar: false, pressedCls: '',
	    cls: 'messageList',
            listeners: { 'itemtap': onTapItem, scope: this },
            itemTpl: new Ext.XTemplate(
                '<div class="messageItem">',
                    '<div class="avatar">',
                        '<img src="{avatar:this.showAvatar}" />',
                    '</div>',
                    //'<div class="message-body {[ this.highlightUser(values) ]}">',
                    '<div class="message-body">',
                        '<div class="message-row">',
                            '<div class="message-username" {[ this.getUserColor(values) ]}>{username}</div>',
                            '<div class="message-info">',
                                '<span class="message-time">{time:this.formatTime}</span>',
                            '</div>',
                        '</div>',
                        '<div class="message-row">',
                            '<div class="message-text">{[ this.formatMessageBody(values) ]}</div>',
                        '</div>',
                    '</div>',
                '</div>',
                helperFunctions
            )
        });

        var userList = new Ext.List({
            title: 'Users', scroll: 'vertical', itemSelector: 'div[class=user]',
            store: userStore, multiSelect: false, singleSelect: false, //width: 150,
            pinHeaders: false, indexBar: false, cls: 'userList',
            //listeners: { 'itemswipe': onSwipeUser, scope: this },
            //fields: ['user_id', 'username', 'avatar', 'title', 'namecolor', 'email', 'homepage',
            //'occupation', 'location', 'hobbies'],
            itemTpl: new Ext.XTemplate(
                '<div class="user">',
                    '<div class="avatar">',
                        '<img src="{avatar:this.showAvatar}" />',
                    '</div>',
                    '<div class="user-body">',
                        '<div class="user-row">',
                            '<div class="user-username" {namecolor:this.showUserColor}>{username}</div>',
                        '</div>',
                        '<div class="user-row">',
                            '<div class="user-text">',
                                '<tpl if="title">',
                                    '<span class="user-label">Titel:</span> {title} ',
                                '</tpl>',
                                '<tpl if="location">',
                                    '<span class="user-label">Hemort:</span> {location} ',
                                '</tpl>',
                                '<tpl if="occupation">',
                                    '<span class="user-label">Uppehälle:</span> {occupation} ',
                                '</tpl>',
                                '<tpl if="hobbies">',
                                    '<span class="user-label">Hobbyer:</span> {hobbies} ',
                                '</tpl>',
                            '</div>',
                        '</div>',
                    '</div>',
                '</div>',
                helperFunctions
            )
        });

        // Top Dock
        var loginButton = {
            xtype: 'button', text: 'Login', ui: 'small', cls: 'btnLogin', itemId: 'loginButton',
            handler: function() {
                loginPanel.show();
                loginPanel.doLayout();
            }
        };
        var refreshButton = {
            xtype: 'button', ui: 'small', cls: 'btnPlayPause', itemId: 'btnPlayPause',
            handler: playPauseHandler
        };
        var textField = new Ext.form.Text({
            autoComplete: true, autoCapitalize: true, autoCorrect: true, placeHolder: 'Skriv meddelande...',
            itemId: 'messageField', flex: 1,
            listeners: { 'action': { 'fn': sendMessage, 'scope': this } }
        });

        var panel = new Ext.Carousel({
            fullscreen: true, cardSwitchAnimation: 'slide', direction: 'horizontal', //flex: 1,
            layout: { type: 'fit', align: 'stretch' },
            defaults: { cls: 'card', flex: 1, style: 'position: absolute' }, activeItem: 0, 
            dockedItems: [
                { xtype: 'toolbar', dock: 'top', title: appTitle,
                  items: [loginButton, { xtype: 'spacer' }, refreshButton] },
                { xtype: 'toolbar', dock: 'bottom', 
                  layout: { type: 'hbox', align: 'stretch' },
                  items: [textField] }
            ],
            items: [messageList, userList]
        });
        panel.on('orientationchange', function() {
            this.el.parent().setSize(window.innerWidth, window.innerHeight);
        });

        // Listen to application cache updates.
        window.applicationCache.addEventListener('updateready', function(e) {
            if (window.applicationCache.status == window.applicationCache.UPDATEREADY) {
                // Browser downloaded a new app cache.
                // Swap it in and reload the page to get the new hotness.
                window.applicationCache.swapCache();
                Ext.Msg.confirm(
                    'Reload application?',
                    'A new version of this application is available. Do you want to load it?',
                    function(btn) {
                        if (btn == 'yes') {
                            window.location.reload();
                        }
                    }
                );
            } else {
                // Manifest didn't changed. Nothing new to server.
            }
        }, false);
        window.applicationCache.update();

    }
});

