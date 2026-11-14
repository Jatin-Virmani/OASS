/* Live deadline countdown.
   Any element like <span class="countdown" data-deadline="1767225540000">Due in 2h</span>
   is refreshed every second. Under 2 hours left it turns red ("time-urgent"). */
(function () {
    function update() {
        document.querySelectorAll('.countdown').forEach(function (el) {
            var ms = Number(el.dataset.deadline) - Date.now();
            el.classList.remove('time-normal', 'time-warning', 'time-urgent', 'time-overdue');

            if (ms <= 0) {
                el.textContent = 'Overdue';
                el.classList.add('time-overdue');
                return;
            }
            var totalSeconds = Math.floor(ms / 1000);
            var days    = Math.floor(totalSeconds / 86400);
            var hours   = Math.floor((totalSeconds % 86400) / 3600);
            var minutes = Math.floor((totalSeconds % 3600) / 60);
            var seconds = totalSeconds % 60;

            var text = (days > 0 ? days + 'd ' : '') + hours + 'h ' + minutes + 'm ' + seconds + 's';
            var twoHours = 2 * 3600 * 1000;
            if (ms < twoHours) {
                el.textContent = 'Due soon: ' + text;
                el.classList.add('time-urgent');
            } else {
                el.textContent = 'Due in ' + text;
                el.classList.add(ms < 24 * 3600 * 1000 ? 'time-warning' : 'time-normal');
            }
        });
    }
    update();
    setInterval(update, 1000);
})();
