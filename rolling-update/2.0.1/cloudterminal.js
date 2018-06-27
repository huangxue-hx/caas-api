var t;

if (typeof Object.assign != 'function') {
    // Must be writable: true, enumerable: false, configurable: true
    Object.defineProperty(Object, "assign", {
      value: function assign(target, varArgs) { // .length of function is 2
        'use strict';
        if (target == null) { // TypeError if undefined or null
          throw new TypeError('Cannot convert undefined or null to object');
        }
  
        var to = Object(target);
  
        for (var index = 1; index < arguments.length; index++) {
          var nextSource = arguments[index];
  
          if (nextSource != null) { // Skip over if undefined or null
            for (var nextKey in nextSource) {
              // Avoid bugs when hasOwnProperty is shadowed
              if (Object.prototype.hasOwnProperty.call(nextSource, nextKey)) {
                to[nextKey] = nextSource[nextKey];
              }
            }
          }
        }
        return to;
      },
      writable: true,
      configurable: true
    });
  }

function init(){
    hterm.defaultStorage = new lib.Storage.Memory();
    
    t = new hterm.Terminal("cloudterm");
    
    t.getPrefs().set("send-encoding", "utf-8");
    t.getPrefs().set("receive-encoding", "utf-8");
    
    // t.getPrefs().set("use-default-window-copy", true);
    t.getPrefs().set("clear-selection-after-copy", true);
    t.getPrefs().set("copy-on-select", true);
    t.getPrefs().set("ctrl-c-copy", true);
    t.getPrefs().set("ctrl-v-paste", true);
    // t.getPrefs().set("cursor-color", "black");
    // t.getPrefs().set("background-color", "white");
    // t.getPrefs().set("font-size", 12);
    // t.getPrefs().set("foreground-color", "black");
    // t.getPrefs().set("cursor-blink", false);
    // t.getPrefs().set("scrollbar-visible", true);
    // t.getPrefs().set("scroll-wheel-move-multiplier", 0.1);
    // t.getPrefs().set("user-css", "/afx/resource/?p=css/hterm.css");
    t.getPrefs().set("enable-clipboard-notice", true);
    
    t.onTerminalReady = function () {
    
        app.onTerminalInit();
    
        var io = t.io.push();
    
        io.onVTKeystroke = function (str) {
            app.onCommand(str);
        };
    
        io.sendString = io.onVTKeystroke;
    
        io.onTerminalResize = function (columns, rows) {
            app.resizeTerminal(columns, rows);
        };
    
        t.installKeyboard();
        app.onTerminalReady();
    
    };
}
var params =[];
function UrlSearch() {
    var name,value;
    var str=location.href; //取得整个地址栏
    var num=str.indexOf("?")
    str=str.substr(num+1); //取得所有参数   stringvar.substr(start [, length ]
 
    var arr=str.split("&"); //各个参数放到数组里
     //console.log(arr)
    for(var i=0;i < arr.length;i++){
         num=arr[i].indexOf("=");
         if(num>0){
              value=arr[i].substr(num+1);
              params.push(value);
         }
    }

 }
 UrlSearch();
let ws = new WebSocket("ws://10.10.101.87:30088/wsmars/terminal?scriptType="+params[0]+"&container="+params[1]+"&pod="+params[2]+"&namespace="+params[3]+"&clusterId="+params[4]);

ws.onopen = function() {
    init();
    t.decorate(document.querySelector('#terminal'));
    t.showOverlay("连接成功", 1000);
}

ws.onerror = function() {
    t.showOverlay("Connection error", 3000);
}

ws.onclose = function(){
    t.showOverlay("Connection closed", 3000);
}

ws.onmessage = function(e) {
    let data = JSON.parse(e.data);
    switch (data.type) {
        case "TERMINAL_PRINT":
            t.io.print(data.text);
    }
}

function action(type, data) {
    let action = Object.assign({
        type: type
    }, data);

    return JSON.stringify(action);
}

let app = {
    onTerminalInit: function() {
        ws.send(action("TERMINAL_INIT"));
    },
    onCommand: function(command) {
        ws.send(action("TERMINAL_COMMAND", {
            command: command
        }));
    },
    resizeTerminal: function(columns, rows) {
        ws.send(action("TERMINAL_RESIZE", {
            columns: columns, 
            rows: rows
        }));
    },
    onTerminalReady: function() {
        ws.send(action("TERMINAL_READY"));
    }
};