package ru.poker.sportpoker.validate.errors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Класс с кодами ошибок валидации.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorCodes {

    /**
     * Ошибки, возникающие, если проверяемый id является null.
     */
    public static final String ID_IS_NULL = "ID_IS_NULL";
    /**
     * Ошибки, возникающие, если проверяемая коллекция id является null.
     */
    public static final String ID_COLLECTION_IS_NULL = "ID_COLLECTION_IS_NULL";
    /**
     * Ошибки, возникающие, если необходимая сущность в БД не существует.
     */
    public static final String NONEXISTENT_ENTITY = "ENTITY_NOT_FOUND";

    /**
     * Ошибки, возникающие, если нарушена уникальность поля сущности
     */
    public static final String ENTITY_ALREADY_EXISTS = "ENTITY_ALREADY_EXISTS";

    /**
     * Ошибки, возникающие, если проверяемый объект является null.
     */
    public static final String OBJECT_IS_NULL = "OBJECT_IS_NULL";
    /**
     * Ошибки, связанные с нарушениями правил согласованности бизнес-типов баз данных домена.
     */
    public static final String DATABASE_BUSINESS_TYPE_VIOLATION = "DATABASE_BUSINESS_TYPE_VIOLATION";
    /**
     * Ошибки, связанные с получением 422 статуса в запросе, отправленном во время валидации.
     */
    public static final String SERVICE_CALL_VALIDATION = "HTTP_REQUEST_TO_ANOTHER_SERVICE_HAS_FAILED_WITH_422";
    /**
     * Ошибки, связанные с нарушениями уникальности значения, которое должно быть уникальным.
     */
    public static final String NOT_UNIQUE_IDENTIFIER = "IDENTIFIER_IS_NOT_UNIQUE";
    /**
     * Ошибки, возникающие, если элемент коллекции имеет значение null, когда это запрещено.
     */
    public static final String COLLECTION_ELEM_IS_NULL = "COLLECTION_ELEM_IS_NULL";
    /**
     * Ошибки, возникающие, если элемент коллекции является пустой строкой, коллекцией, словарём и тд, когда это запрещено.
     * Данное ограничение включает в себя {@link #COLLECTION_ELEM_IS_NULL}.
     */
    public static final String COLLECTION_ELEM_IS_EMPTY = "COLLECTION_ELEM_IS_EMPTY";
    /**
     * Ошибки, возникающие, если элемент коллекции является пробельной строкой, когда это запрещено. Применимо для
     * элементов коллекций состоящих из строк. Данное ограничение включает в себя {@link #COLLECTION_ELEM_IS_EMPTY}.
     *
     * @see String#isBlank()
     * @see Character#isWhitespace(int)
     */
    public static final String COLLECTION_ELEM_IS_BLANK = "COLLECTION_ELEM_IS_BLANK";
    /**
     * Ошибки, возникающие, если значение поля объекта null, когда это запрещено.
     */
    public static final String FIELD_IS_NULL = "FIELD_IS_NULL";
    /**
     * Ошибки, возникающие, если поле является пустой строкой, коллекцией, словарём и тд, когда это запрещено.
     * Данное ограничение автоматически накладывает запрет на null-значение поля.
     */
    public static final String FIELD_IS_EMPTY = "FIELD_IS_EMPTY";
    /**
     * Ошибки, возникающие, если значение поля является пробельной строкой, когда это запрещено.
     * Данное ограничение автоматически накладывает запрет на empty-значение поля.
     *
     * @see String#isBlank()
     * @see Character#isWhitespace(int)
     */
    public static final String FIELD_IS_BLANK = "FIELD_IS_BLANK";

    /**
     * Ошибки, возникающие, при связи родительской сущности с самой собой или с одним из ее наследников.
     */
    public static final String CYCLIC_DEPENDENCE = "CYCLIC_DEPENDENCE";

    /**
     * Ошибки, возникающие, при не совпадении домена текущего пользователя и домена указываемого при создании сущности.
     */
    public static final String CROSS_REALM_REQUEST = "CROSS_REALM_REQUEST";

    /**
     * Ошибки, возникающие, при попытке удалить сущность, имеющую иерерахическую связь с другой сущностью.
     */
    public static final String HIERARCHICAL_DEPENDENCY = "HIERARCHICAL_DEPENDENCY";
    /**
     * Ошибки, возникающие, если значение поля не соответствует заданному значению или какому-либо значению из заданного
     * набора значений.
     */
    public static final String FIELD_IS_NOT_EQUAL = "FIELD_IS_NOT_EQUAL";
    /**
     * Ошибки, возникающие, если поле не является корректным dns-именем хоста, когда это необходимо. Применимо к строкам.
     */
    public static final String NOT_VALID_DNS_HOSTNAME = "NOT_VALID_DNS_HOSTNAME";
    /**
     * Ошибки, возникающие, если поле не является корректным номером порта, то есть не принадлежит отрезку [0, 65535],
     * когда это необходимо. Применимо к целым числам.
     */
    public static final String NOT_VALID_PORT = "NOT_VALID_PORT";
    /**
     * Ошибки, возникающие, если поле не является корректной строкой вида хост:порт, когда это необходимо. Применимо к строкам.
     *
     * @see #NOT_VALID_DNS_HOSTNAME
     * @see #NOT_VALID_PORT
     */
    public static final String NOT_HOST_PORT_STRING = "NOT_HOST_PORT_STRING";

    /**
     * Ошибки, возникающие при указании не поддерживаемого типа аутентификации
     */
    public static final String UNSUPPORTED_AUTH_TYPE = "UNSUPPORTED_AUTH_TYPE";
    /**
     * Ошибки, возникающие в случае если поле не равно null когда это необходимо
     */
    public static final String FIELD_SHOULD_BE_NULL = "FIELD_SHOULD_BE_NULL";

    /**
     * Ошибки, возникающие при попытке провести изменение или удаление используемого Сервера данных
     */
    public static final String DATA_SERVER_IN_USE = "DATA_SERVER_IN_USE";

    /**
     * Ошибки, возникающие, если значение поля объекта не положительное, когда это запрещено.
     */
    public static final String VALUE_IS_NONPOSITIVE = "VALUE_IS_NONPOSITIVE";


    /**
     * Не валидное поле. Должно соответствовать паттерну. Применимо к строкам.
     */
    public static final String FIELD_SHOULD_MATCH_PATTERN = "FIELD_SHOULD_MATCH_PATTERN";

    /**
     * Длина поля превышает максимально-допустимое значение.
     */
    public static final String FIELD_TOO_LONG = "FIELD_TOO_LONG";

    /**
     * Длина поля меньше минимально-допустимого значения значения
     */
    public static final String FIELD_TOO_SHORT = "FIELD_TOO_SHORT";


    /**
     * Попытка заблокировать уже заблокированного пользователя
     */
    public static final String USER_ALREADY_BLOCKED = "USER_ALREADY_BLOCKED";


    /**
     * Попытка разблокировать уже разблокированного пользователя
     */
    public static final String USER_ALREADY_UNBLOCKED = "USER_ALREADY_UNBLOCKED";

    /**
     * Пользователь не аутентифицирован
     */
    public static final String USER_NOT_AUTHENTICATED = "USER_NOT_AUTHENTICATED";

    /**
     * Ошибки возникающие при попытке работать с заблокированным пользователем
     */
    public static final String DISABLED_USER = "DISABLED_USER";

    /**
     * Ошибки, возникающие, если поле не является корректным форматом даты и времени, когда это необходимо. Применимо в т.ч. и к строкам.
     */
    public static final String INVALID_DATETIME_FORMAT = "INVALID_DATETIME_FORMAT";


    /**
     * Ошибки, возникающие при создании шага типа параметры, если для строкового параметра указать значение не входящее в допустимые,
     * или для числово превысить максимальное или минимальное значение
     */
    public static final String VALUE_CONSTRAINT_VIOLATION = "VALUE_CONSTRAINT_VIOLATION";
}
