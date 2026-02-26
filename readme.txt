обучающий чат:
https://chat.qwen.ai/c/314d58c3-a4bf-46df-bc5c-f2746e96371b

---------------
best practice:
1) для интегр тестов с Testcontainers подключаем
    testImplementation "org.testcontainers:testcontainers:${testcontainersVersion}"
    testImplementation "org.testcontainers:junit-jupiter:${testcontainersVersion}"  //it contains @Testcontainers
    testImplementation "org.testcontainers:postgresql:${testcontainersVersion}"
2) для тестов юзаем assertj
 т.е. import static org.assertj.core.api.Assertions.assertThat;


---------------
 Проблема 1:
    Использование id (суррогатного ключа) в equals/hashCode для сущностей JPA опасно:
    - Новые объекты: У двух новых объектов id == null. По твоей логике они не равны, даже если это один и тот же фильм.
    - Проблема с коллекциями: Если положить новый объект в HashSet, а потом сделать save() (появится ID), хэш-код изменится. Объект "застрянет" в неправильном бакете, и ты его не найдешь или получишь дубликаты.
 Рекомендация для проекта:
    Для учебного проекта используй Business Key (уникальные бизнес-поля). Для фильма это комбинация title + year.