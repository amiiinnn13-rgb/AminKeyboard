Amin Keyboard - Cloud Build

این پروژه برای ساخت APK از طریق GitHub Actions آماده شده است تا نیازی به نصب Android SDK روی کامپیوتر شما نباشد.

روش ساخت:
1) ZIP را در GitHub به عنوان یک مخزن جدید بارگذاری کنید (محتویات پوشه، نه خود پوشه).
2) به تب Actions بروید.
3) workflow با نام Build Amin Keyboard APK را انتخاب کنید.
4) روی Run workflow بزنید.
5) پس از سبز شدن اجرای workflow، از صفحه اجرای آن بخش Artifacts فایل AminKeyboard-debug-apk را دانلود کنید.

تنظیمات ساخت: compileSdk 35، build-tools 35.0.0، minSdk 23، targetSdk 35، Java 17، Gradle 8.11.1.

توجه: این پروژه نسخه اولیه و آزمایشی کیبورد است. ساخت ابری هنوز اجرا نشده و APK تا پایان موفق workflow تولیدشده محسوب نمی‌شود. پس از ساخت، باید روی گوشی نصب و رفتار کلیدها و اتصال حروف بررسی شود.
