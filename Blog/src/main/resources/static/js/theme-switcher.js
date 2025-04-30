
document.addEventListener('DOMContentLoaded', () => {
    const themeToggle = document.getElementById('theme-toggle');

    const isInitiallyLight = document.documentElement.classList.contains('light-theme');


    if (isInitiallyLight) {
        themeToggle.textContent = '🌞';
    } else {
        themeToggle.textContent = '🌙';
    }

    //  клік
    themeToggle.addEventListener('click', () => {
        // перемкнути клас на <html>
        document.documentElement.classList.toggle('light-theme');

        // збереження нової теми
        const isNowLight = document.documentElement.classList.contains('light-theme');
        localStorage.setItem('theme', isNowLight ? 'light' : 'dark');


        themeToggle.textContent = isNowLight ? '🌞' : '🌙';
    });
});