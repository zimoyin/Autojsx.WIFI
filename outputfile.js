'use strict';

function s() {
    function g(a, e) {
        var b = a.bounds(), c = b.left, h = b.top, k = b.right, l = b.bottom, p = k - c, q = l - h;
        b = "  ".repeat(e);
        d += b + '\x3cnode id\x3d"' + a.id() + '" className\x3d"' + a.className() + '" description\x3d"' + a.desc() + '" text\x3d"' + a.text() + '" ';
        d += 'left\x3d"' + c + '" top\x3d"' + h + '" right\x3d"' + k + '" bottom\x3d"' + l + '" width\x3d"' + p + '" height\x3d"' + q + '" depth\x3d"' + e + '"\x3e\n';
        a = a.children();
        for (c = 0; c < a.length; c++) g(a[c], e + 1);
        d += b + "\x3c/node\x3e\n"
    }

    for (var m = find(), d = '\x3c?xml version\x3d"1.0" encoding\x3d"UTF-8"?\x3e\n\x3cnodes\x3e\n',
             f = 0; f < m.length; f++) {
        var n = m[f];
        n.parent() || g(n, 1)
    }
    d += "\x3c/nodes\x3e";
    log(d)
}

s();