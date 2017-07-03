var term;
// var socket = io(location.origin, {path: '/wetty/socket.io'})
// var addr = $location.protocol() +'://'+$location.host()+':'+'9090';
var ps = location.origin.split(':');
var pp= ps[0]+':'+ps[1]+':9090';
console.log(pp);

function GetQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)","i");
    var r = window.location.search.substr(1).match(reg);
    if (r!=null) return (r[2]); return null;
}

var sn = GetQueryString('Sn');

var socket = io(pp+"?terminal=open&userName=andydev&sn="+sn, {path: '/rest/socketio'});
var buf = '';

console.log("protocol",window.location.href)
// console.log("location",location)


function Wetty(argv) {
    this.argv_ = argv;
    console.log(argv);
    this.io = null;
    this.pid_ = -1;
}

Wetty.prototype.run = function() {
    this.io = this.argv_.io.push();

    this.io.onVTKeystroke = this.sendString_.bind(this);
    this.io.sendString = this.sendString_.bind(this);
    this.io.onTerminalResize = this.onTerminalResize.bind(this);
}

Wetty.prototype.sendString_ = function(str) {
    socket.emit('input', str);
};

Wetty.prototype.onTerminalResize = function(col, row) {
    socket.emit('resize', { col: col, row: row });
};

socket.on('connect', function() {
    lib.init(function() {
        hterm.defaultStorage = new lib.Storage.Local();
        term = new hterm.Terminal();
        window.term = term;
        term.decorate(document.getElementById('terminal'));

        term.setCursorPosition(0, 0);
        term.setCursorVisible(true);
        term.prefs_.set('ctrl-c-copy', true);
        term.prefs_.set('ctrl-v-paste', true);
        term.prefs_.set('use-default-window-copy', true);

        term.runCommandClass(Wetty, document.location.hash.substr(1));
        socket.emit('resize', {
            col: term.screenSize.width,
            row: term.screenSize.height
        });

        if (buf && buf != '')
        {
            term.io.writeUTF16(buf);
            buf = '';
        }
    });
});

socket.on('output', function(data) {
    if (!term) {
        buf += data;
        return;
    }
    console.log("output", data);
    if(data.indexOf('\\7') > 0){
        term.io.writeUTF16(data.substring(0,data.lastIndexOf('\\7')));
    } else {
        term.io.writelnUTF8(data)
    }
});

socket.on('disconnect', function() {
    console.log("Socket.io connection closed");
});
