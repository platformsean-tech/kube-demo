package gusl.launcher;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * Factory Class to create object mapper.
 *
 * Object Mappers are thread safe
 *
 * @author dhudson
 */
public class ObjectMapperFactory {

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final TimeZone UTC_TZ = TimeZone.getTimeZone("UTC");

    public static final String KEBAB_CASE = "KEBAB_CASE";
    // This is the one used by Casanova
    public static final String CASANOVA_DEFAULT = KEBAB_CASE;

    public static final HashMap<String, ObjectMapper> OBJECT_MAPPERS = new HashMap<>();

    static {
        OBJECT_MAPPERS.put(KEBAB_CASE, createObjectMapper(PropertyNamingStrategy.KEBAB_CASE));
    }

    private ObjectMapperFactory() {
    }

    /**
     * Return the standard configured object mapper.
     *
     * Object Mappers are thread safe, so there on no need to create more than
     * one unless you would like different features.
     *
     * @return the common object mapper.
     */
    public static ObjectMapper getDefaultObjectMapper() {
        return getObjectMapperWithNameing(CASANOVA_DEFAULT);
    }

    /**
     * Return an Shared Object Mapper, with of the naming strategy.
     *
     * @param nameingStrategy
     * @return
     */
    public static ObjectMapper getObjectMapperWithNameing(String nameingStrategy) {
        return OBJECT_MAPPERS.get(nameingStrategy);
    }

    /**
     * Create a Object Mapper with lots of options set.
     *
     * If you modify any properties, you should always create a new one.
     *
     *
     * @param namingStrategy
     * @return a mapper with the default options set.
     */
    public static ObjectMapper createObjectMapper(PropertyNamingStrategy namingStrategy) {
        ObjectMapper mapper = new ObjectMapper();
        setOptions(mapper, namingStrategy);
        return mapper;
    }

    /**
     * Return an Object mapper with the default naming strategy set.
     *
     * @return
     */
    public static ObjectMapper createDefaultObjectMapper() {
        return createObjectMapper(PropertyNamingStrategy.KEBAB_CASE);
    }

    /**
     * Create a mapper with a given factory.
     *
     * @param factory
     * @param namingStrategy
     * @return
     */
    public static ObjectMapper createObjectMapper(JsonFactory factory, PropertyNamingStrategy namingStrategy) {
        ObjectMapper mapper = new ObjectMapper(factory);
        setOptions(mapper, namingStrategy);
        return mapper;
    }

    private static void setOptions(ObjectMapper mapper, PropertyNamingStrategy namingStrategy) {
        mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);

        mapper.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
        // can be replaced by this ...
        //mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        // Configure deserializer
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        mapper.configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
                DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        mapper.enable(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME);
        mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE,
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT));
        mapper.setPropertyNamingStrategy(namingStrategy);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper.setTimeZone(UTC_TZ);
    }

    public static <T> CharSequence prettyPrint(T value, ObjectMapper mapper) throws JsonProcessingException {
        if (value == null) {
            return "--null--";
        }

        StringBuilder builder = new StringBuilder(100);
        builder.append("[");
        builder.append(value.getClass().getSimpleName());
        builder.append("] ");
        builder.append(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value));
        return builder.toString();
    }

    // Use the default Kebab Case mapper
    public static <T> CharSequence prettyPrint(T value) throws JsonProcessingException {
        return prettyPrint(value, getDefaultObjectMapper());
    }
}
