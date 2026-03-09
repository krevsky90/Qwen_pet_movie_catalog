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
    testImplementation "org.springframework.boot:spring-boot-testcontainers:${springBootVersion}"   //to use @ServiceConnection

    в рамках тестируемого класса создаем контейнеры:
    @Container
    @ServiceConnection
        static PostgreSQLContainer<?> postgreSQLContainer = ...
    или
        static GenericContainer<?> redisContainer = ...
    NOTE: если нужен один контейнер на весь класс - то static.

    @ServiceConnection - автоматически настраивает подключение к внешним сервисам
        (БД, брокерам сообщений) в тестах, использующих Testcontainers,
        устраняя необходимость ручного указания JDBC URL, логинов и паролей)
    НО если нужны доп параметры - пишем рядом метод с аннотацией @DynamicPropertySource
        Пример: MovieRepositoryTest или MovieServiceIntegrationTest

    Best practice:
        для тестов с постгрес над тестовыми методами ставим @Transactional
        а c redis @Transactional не работает. поэтому юзаем
            @BeforeEach
            void clearCache() {
                redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
            }

    ПРОБЛЕМА:
        инъекция через конструктор не работает в интегр спрингбут тестах
        т.к. junit сначала пытается создать инстанс класса (и падает), не доходя до обнаружения спринга.
    РЕШЕНИЕ:
        юзать @Autowired


2) для тестов юзаем assertj
    т.е. import static org.assertj.core.api.Assertions.assertThat;
2.2) для юнит-тестов правило:
    если сервис модифицирует объект перед сохранением (например, обогащает данные) - всегда проверяйте состояние этого объекта через ArgumentCaptor
    см MovieServiceTest # createMovie_shouldHandleExternalApiError_Gracefully
3) аннотация @Transactional используется во всех методах сервиса, к-ые меняют данные
4) добавить валидацию:
    @Valid для параметра метода в контроллере
    всякие @NotBlank и пр в Model-классе
    NOTE: при нарушении ограничения бросается MethodArgumentNotValidException
        НО по умолчанию исключение обрабатывается с помощью Spring Boot's Default Handler (BasicErrorController),
        к-ый возвращает стандартный JSON без деталей валидации:
                { "status": 400, "error": "Bad Request", ... }
    РЕШЕНИЕ: добавить класс, к-ый вернет понятный JSON: GlobalExceptionHandler с аннотацией @RestControllerAdvice
        тогда ответ будет:
            {
              "timestamp": "2026-02-26T22:57:15.6403065",
              "status": 400,
              "error": "Validation failed",
              "errors": {
                "year": "Year too old",
                "title": "Title is required"
              },
              "message": "Check the 'errors' field for details"
            }
5) Для тестирования слоев приложения: см инфо https://chat.qwen.ai/c/a5b62189-5e51-409d-b9c6-c3339bcd4d1e
    repository-слоя: @DataJpaTest
    веб-слоя: @WebMvcTest
            загружает только веб-слой (контроллеры, конфиги безопасности, конвертеры), игнорируя сервисы и базу данных (слайс-тест).
            Не поднимает реальный HTTP-сервер. Использует MockMvc для эмуляции запросов внутри JVM
        Для чего используется:
            Тестирование логики контроллеров.
            Проверка маппинга URL, HTTP-методов, статус-кодов.
            Проверка валидации входных данных (@Valid).
            Проверка сериализации/десериализации JSON.
        зависимости мокаем через @MockBean
    service-слоя:
        обычные unit-тесты (JUnit 5 и Mockito), БЕЗ спринга, т.к. бизнес-логика не должна зависеть от фреймворка.
            НО тогда не проверить @Transactional, AOP-прокси
        тесты с контекстом Spring (для тестирования фич спринга типа @Transactional, AOP-прокси, SpEL выражения в аннотациях, кэширование (@Cacheable), асинхронность (@Async))
            загружаем @SpringBootTest, но мокаем репозиторий через @MockBean
    всех слоев: @SpringBootTest
            загружает полный ApplicationContext приложения,
            поднимает реальный (embedded) HTTP-сервер
        Для чего используется:
            End-to-End (E2E) тесты.
            Проверка взаимодействия всех слоев (Контроллер -> Сервис -> Репозиторий -> БД).
            Проверка корректности конфигурации приложения.
6) для кеширования на уровне сервиса подключают Redis
    детальное объяснение тут: https://chat.qwen.ai/c/c015d832-5b0a-493a-b299-1ee1afbd1269
    и в общем чате https://chat.qwen.ai/c/314d58c3-a4bf-46df-bc5c-f2746e96371b

    АРХИТЕКТУРНО: если объект создается/меняестся, то надежнее инвалидировать кеши, содержащие этот объект,
            чем апдейтить кеш прямо в методе создания/апдейта объекта!

    6.1) если
        spring.cache.redis.key-prefix=movie:
        и
        @Cacheable(value = "movies", key = "#id")
        public Movie getMovie(Long id) { ... }
        то
        итоговый ключ в редисе movie:movies::55, где movie - key-prefix, movie - cacheName

    6.2) внимание:
        @Transactional should be BEFORE @Caching!
        По умолчанию @CacheEvict срабатывает ПОСЛЕ выполнения метода (beforeInvocation = false по умолчанию),
            так что если методы create/update упадут, то кеш не будет очищен

    6.3) TTL надо задавать в проперти-файле
    6.4) имена кешей надо задавать Java-константой (внимание! @CacheEvict и @Cacheable в MovieService НЕ умеют парсить SpEl из проперти-файла!)
    6.5) конфигурации для конкретных кешей надо наследовать от базовой (см CacheConfig)

7) если хотим использовать Redis напрямую (командами/java-кодом, а не кеш-аннотациями), то
    полезно смотреть в redis-cli. команда для случая запуска в существующем локальном докер-контейнере редиса:
        docker exec -it <container_name> redis-cli
    7.1) cache should store DTO (JSON response), but not Entity!
    7.2) если надо искать множество ключей, то НЕ делать redisTemplate.keys(..),
        потому что keys блокирует весь редис.
        Надо писать redisTemplate.scan(..) (см RedisCacheHelper # cacheEvictByPattern), к-ый будет читать ключи пачками по count.
        Опять-таки, чем больше count, тем надольше редис блокируется. Нужен баланс
    7.3) все команды касательно всего контента редиса тут:
        redisTemplate.getConnectionFactory().getConnection().serverCommands()

8) для улучшения перфоманса лучше ставить @Transactional(readOnly = true) над сервисом

9) для выноса пропертей из application.properties в бин
       a) можно внедрить через @Value - типа 	@Value("${external.omdb.api-key}")
           НО тогда это надо будет делать во всех классах, где будет юзаться это значение
       b) создать record OmdbProperties c
           @ConfigurationProperties(prefix = "external.omdb") - указывает, какие проперти буду читаться из application.properties
           @EnableConfigurationProperties(OmdbProperties.class) - говорит спрингу зарегистрировать этот класс как бин
           и внедрять его как бин

Архитектурно:
1) НЕ создаем статич классов. Создаем интерфейсы + их имплементацию-бины с @Component. так проще работать и тестить (замокать интерфейс)
---------------
 Проблема 1:
    Использование id (суррогатного ключа) в equals/hashCode для сущностей JPA опасно:
    - Новые объекты: У двух новых объектов id == null. По твоей логике они не равны, даже если это один и тот же фильм.
    - Проблема с коллекциями: Если положить новый объект в HashSet, а потом сделать save() (появится ID), хэш-код изменится. Объект "застрянет" в неправильном бакете, и ты его не найдешь или получишь дубликаты.
 Рекомендация для проекта:
    Для учебного проекта используй Business Key (уникальные бизнес-поля). Для фильма это комбинация title + year.

