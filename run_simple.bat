@echo off
echo ========================================
echo    ЗАПУСК ИГРЫ "ЗМЕЙКА"
echo ========================================
echo.

REM Установите путь к JavaFX SDK
REM ЗАМЕНИТЕ ЭТОТ ПУТЬ НА СВОЙ!
set JAVAFX_PATH=C:\путь\к\javafx-sdk-17.0.2\lib

REM Если JavaFX не установлен, скачаем его автоматически
if not exist "%JAVAFX_PATH%\javafx.base.jar" (
    echo JavaFX не найден по пути: %JAVAFX_PATH%
    echo.
    echo Скачать и установить JavaFX автоматически? (Y/N)
    set /p download=
    if /i "%download%"=="Y" (
        echo Скачивание JavaFX...
        powershell -Command "Invoke-WebRequest -Uri 'https://download2.gluonhq.com/openjfx/17.0.2/openjfx-17.0.2_windows-x64_bin-sdk.zip' -OutFile 'javafx-sdk.zip'"
        powershell -Command "Expand-Archive -Path 'javafx-sdk.zip' -DestinationPath '.' -Force"
        set JAVAFX_PATH=%cd%\javafx-sdk-17.0.2\lib
        echo JavaFX установлен в: %JAVAFX_PATH%
    ) else (
        echo.
        echo Для работы игры требуется JavaFX SDK
        echo Скачайте с: https://openjfx.io/
        echo Распакуйте и укажите путь в этом bat-файле
        pause
        exit /b 1
    )
)

echo Проверка Java...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Ошибка: Java не установлена или не добавлена в PATH
    echo Установите Java JDK 11+ с https://adoptium.net/
    pause
    exit /b 1
)

echo.
echo Варианты запуска:
echo 1. Скомпилировать и запустить (рекомендуется)
echo 2. Запустить уже скомпилированный проект
echo 3. Запустить через Maven
echo.
set /p choice="Выберите вариант (1-3): "

if "%choice%"=="1" (
    call :compile_and_run
) else if "%choice%"=="2" (
    call :run_only
) else if "%choice%"=="3" (
    call :run_maven
) else (
    echo Неверный выбор
    pause
    exit /b 1
)

exit /b 0

:compile_and_run
echo.
echo ========================================
echo    КОМПИЛЯЦИЯ ПРОЕКТА
echo ========================================
echo.

REM Очищаем предыдущую компиляцию
if exist out rmdir /s /q out
mkdir out

echo Компиляция исходного кода...
javac -d out --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.graphics ^
    src/main/java/com/snakegame/Main.java ^
    src/main/java/com/snakegame/config/*.java ^
    src/main/java/com/snakegame/core/*.java ^
    src/main/java/com/snakegame/ui/*.java ^
    src/main/java/com/snakegame/utils/*.java

if %errorlevel% neq 0 (
    echo Ошибка компиляции!
    pause
    exit /b 1
)

echo.
echo ========================================
echo    ЗАПУСК ИГРЫ
echo ========================================
echo.
echo Управление в игре:
echo - Интерактивный режим: введите "Хочу играть!"
echo - Неинтерактивный режим: введите "Я наблюдатель"
echo - Выход: введите "выход"
echo.
echo В графическом режиме управление стрелками, пауза - P
echo.

java --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.graphics ^
     -cp "out" com.snakegame.Main

goto :eof

:run_only
echo.
echo ========================================
echo    ЗАПУСК СКОМПИЛИРОВАННОГО ПРОЕКТА
echo ========================================
echo.

if not exist "out" (
    echo Ошибка: папка 'out' не найдена
    echo Сначала скомпилируйте проект (вариант 1)
    pause
    exit /b 1
)

echo Управление в игре:
echo - Интерактивный режим: введите "Хочу играть!"
echo - Неинтерактивный режим: введите "Я наблюдатель"
echo - Выход: введите "выход"
echo.

java --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.graphics ^
     -cp "out" com.snakegame.Main

goto :eof

:run_maven
echo.
echo ========================================
echo    ЗАПУСК ЧЕРЕЗ MAVEN
echo ========================================
echo.

REM Проверяем наличие Maven
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Ошибка: Maven не установлен
    echo Установите Maven с https://maven.apache.org/
    pause
    exit /b 1
)

echo Сборка проекта Maven...
mvn clean compile

if %errorlevel% neq 0 (
    echo Ошибка сборки Maven!
    pause
    exit /b 1
)

echo.
echo Запуск игры...
echo Управление в игре:
echo - Интерактивный режим: введите "Хочу играть!"
echo - Неинтерактивный режим: введите "Я наблюдатель"
echo - Выход: введите "выход"
echo.

REM Запускаем через Maven с JavaFX
set MAVEN_OPTS=--module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.graphics
mvn exec:java -Dexec.mainClass="com.snakegame.Main"

goto :eof