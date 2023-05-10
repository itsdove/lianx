var hqyzm = document.getElementById('hqyzm');

var email = document.getElementById('email');

hqyzm.addEventListener('click', function() {
    var xhr = new XMLHttpRequest();
    var param=email.value;
    xhr.open('GET', '/community/getyzm?email='+param);
    xhr.onload = function() {
        // 处理响应
    };
    xhr.send();
});
//
// czmm.addEventListener('click', function() {
//     var xhr = new XMLHttpRequest();
//     xhr.open('POST', '/community/forget');
//     xhr.setRequestHeader('Content-Type', 'application/json');
//     xhr.onload = function() {
//         // 处理响应
//     };
//     var requestData = { email: email.value,verifycode:verifycode.value, password:password.value};
//     xhr.send(JSON.stringify(requestData));
//
// });