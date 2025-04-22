// theme-switcher.js
document.addEventListener('DOMContentLoaded', () => {
    const themeToggle = document.getElementById('theme-toggle');
    const currentTheme = localStorage.getItem('theme') || 'dark';

    // Встановлюємо початкову тему
    if (currentTheme === 'light') {
        document.body.classList.add('light-theme');
        themeToggle.textContent = '🌞';
    } else {
        themeToggle.textContent = '🌙';
    }

    // Обробник кліку на перемикач
    themeToggle.addEventListener('click', () => {
        document.body.classList.toggle('light-theme');
        const isLightTheme = document.body.classList.contains('light-theme');
        localStorage.setItem('theme', isLightTheme ? 'light' : 'dark');
        themeToggle.textContent = isLightTheme ? '🌞' : '🌙';
    });
});