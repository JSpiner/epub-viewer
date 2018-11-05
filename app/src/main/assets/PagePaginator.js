var body = document.body.innerHTML;
var deviceHeight = window.innerHeight;
var words = body.split(' ');

function search(startIndex, min, max, current) {
    if (current == max) return current - 1;
    if (current == min) return current;

    document.body.innerHTML = words.slice(startIndex, current).join(' ');;

    var heightDiff = document.body.scrollHeight - deviceHeight;

    if (heightDiff <= 0) {
        return search(
            startIndex,
            current,
            max,
            Math.min(parseInt(current - (heightDiff / 3)), max - 1)
        );
    }
    else {
        return search(
            startIndex,
            min,
            current,
            Math.max(parseInt(current - (heightDiff / 3)), min + 1)
        );
    }
}
var lastIndex = 0;
var lastDiff = 150;
var result = [];
while (lastIndex != words.length) {
    var pagingIndex = search(pagingIndex + 1, lastIndex, words.length, lastIndex + lastDiff);
    if (pagingIndex == words.length - 1) pagingIndex = words.length;
    result.push(pagingIndex);
    lastDiff = pagingIndex - lastIndex;
    lastIndex = pagingIndex;
}
AndroidFunction.result(result.join(","));