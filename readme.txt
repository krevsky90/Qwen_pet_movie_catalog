обучающий чат:
https://chat.qwen.ai/c/314d58c3-a4bf-46df-bc5c-f2746e96371b

info:
    MapStruct https://mapstruct.org/documentation/stable/reference/html/?spm=a2ty_o01.29997173.0.0.2cda5171R6uw0E
    Spring validation Guide https://docs.spring.io/spring-framework/reference/core/validation.html
---------------
best practice:
1) для интегр тестов с Testcontainers подключаем
    testImplementation "org.testcontainers:testcontainers:${testcontainersVersion}"
    testImplementation "org.testcontainers:junit-jupiter:${testcontainersVersion}"  //it contains @Testcontainers
    testImplementation "org.testcontainers:postgresql:${testcontainersVersion}"
2) для тестов юзаем assertj
 т.е. import static org.assertj.core.api.Assertions.assertThat;
3) аннотация @Transactional используется во всех методах сервиса, к-ые меняют данные
4) добавить валидацию:
    @Valid для параметра метода в контроллере
    всякие @NotBlank и пр в Model-классе
    NOTE: при нарушении ограничения бросается MethodArgumentNotValidException
        НО по умолчанию исключение обрабатывается с помощью Spring Boot's Default Handler (BasicErrorController),
        к-ый возвращает стандартный JSON без деталей валидации:
                { "status": 400, "error": "Bad Request", ... }

---------------
 Проблема 1:
    Использование id (суррогатного ключа) в equals/hashCode для сущностей JPA опасно:
    - Новые объекты: У двух новых объектов id == null. По твоей логике они не равны, даже если это один и тот же фильм.
    - Проблема с коллекциями: Если положить новый объект в HashSet, а потом сделать save() (появится ID), хэш-код изменится. Объект "застрянет" в неправильном бакете, и ты его не найдешь или получишь дубликаты.
 Рекомендация для проекта:
    Для учебного проекта используй Business Key (уникальные бизнес-поля). Для фильма это комбинация title + year.