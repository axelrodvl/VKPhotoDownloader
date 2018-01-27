# Простая утилита, позволяющая загрузить фотографии друзей пользователя VK.com

## Запуск:
- Залогиниться в vk.com
- Создать свое приложение (standalone)
    ``
    https://vk.com/apps?act=manage
    ``
- Перейти в настройки приложения и включить его (Состояние=Приложение включено и видно всем), сохранить сгенерированные ключи (ID приложения и Защищённый ключ) 
- Создать имплементацию класса src/main/java/co/axelrod/vk/config/TokenStorageImpl.java и реализовать получение ключей:
    - ID приложения - APP_ID
    - Защищённый ключ - CLIENT_SECRET
- Открыть браузер, перейти по ссылке и разрешить доступ к запрашиваемым данным (внимание, в середине URL заменить APP_ID на полученный ранее)
    ``
    https://oauth.vk.com/authorize?client_id=APP_ID&display=page&redirect_uri=&scope=photos&response_type=code&v=5.71
    ``
- Открыть строку адреса в браузере и получить значение code
    - Строка адреса после вызова изменится на нечто вроде: 
    ``
    https://oauth.vk.com/blank.html#code=06c721d6ae6921343a
    ``
- Вставить полученный ключ в начало метода VKPhotoDownloader.main и сохранить
- Собрать и запустить проект
    ``
    mvn clean compile exec:java -Dexec.mainClass=co.axelrod.vk.VKPhotoDownloader
    ``
- Фотографии будут загружены в директорию photo в корне проекта 