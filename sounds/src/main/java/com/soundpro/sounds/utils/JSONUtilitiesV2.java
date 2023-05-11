package com.soundpro.sounds.utils;

import java.beans.Introspector;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public final class JSONUtilitiesV2 {

    private Gson gson;
    private GsonBuilder gsonBuilder;
    private Boolean parseExpose;
    private Boolean parseDate;
    private Boolean parseLocalDate;
    private Boolean parseLocalDateTime;
    private Boolean parseXMLGregorianCalendar;
    private Boolean parseByteArray;
    private Boolean parseEnum;

    public static enum BuildType {
        EAGER,
        LAZY;
    }

    private JSONUtilitiesV2() {
        this.parseExpose = Boolean.FALSE;
        this.parseDate = Boolean.FALSE;
        this.parseLocalDate = Boolean.FALSE;
        this.parseLocalDateTime = Boolean.FALSE;
        this.parseXMLGregorianCalendar = Boolean.FALSE;
        this.parseByteArray = Boolean.FALSE;
        this.parseEnum = Boolean.FALSE;
        this.gson = new Gson();
    }

    public static synchronized JSONUtilitiesV2 create() {
        return new JSONUtilitiesV2();
    }

    public Gson gson() {
        return this.gson;
    }

    public String toJson(Object src) {
        return this.gson.toJson(src);
    }

    public <T> T fromJson(String json, Class<T> classOfT) {
        return this.gson.fromJson(json, classOfT);
    }

    public <T> T fromJson(String json, Type type) {
        return this.gson.fromJson(json, type);
    }

    public Map<String, Object> jsonToMap(String json) {
        return this.gson.fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());
    }

    public JSONUtilitiesV2 builder() {
        return this.configGsonBuilder();
    }

    private JSONUtilitiesV2 configGsonBuilder() {
        this.gsonBuilder = new GsonBuilder();
        if (this.parseExpose) {
            this.gsonBuilder = this.gsonBuilder
                                   .excludeFieldsWithoutExposeAnnotation();
        }
        if (this.parseDate) {
            this.gsonBuilder = this.gsonBuilder
                                   .registerTypeAdapter(Date.class, new DateSerializer())
                                   .registerTypeAdapter(Date.class, new DateDeserializer());
        }
        if (this.parseLocalDate) {
            this.gsonBuilder = this.gsonBuilder
                                   .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
                                   .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
        }
        if (this.parseLocalDateTime) {
            this.gsonBuilder = this.gsonBuilder
                                   .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                                   .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer());
        }
        if (this.parseXMLGregorianCalendar) {
            this.gsonBuilder = this.gsonBuilder
                                   .registerTypeAdapter(XMLGregorianCalendar.class, new XMLGregorianCalendarSerializer())
                                   .registerTypeAdapter(XMLGregorianCalendar.class, new XMLGregorianCalendarDeserializer());
        }
        if (this.parseByteArray) {
            this.gsonBuilder = this.gsonBuilder
                                   .registerTypeHierarchyAdapter(byte[].class, new ByteArraySerializer())
                                   .registerTypeHierarchyAdapter(byte[].class, new ByteArrayDeserializer())
                                   .disableHtmlEscaping();
        }
        this.gsonBuilder = this.gsonBuilder.registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeSerializer());
        this.gsonBuilder = this.gsonBuilder.registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeDeserializer());

        if (this.parseEnum) {
            this.gsonBuilder = this.gsonBuilder
                                   .registerTypeAdapterFactory(new EnumAdapterFactory());
        }
        this.gson = this.gsonBuilder.create();
        return this;
    }

    public JSONUtilitiesV2 noParseExpose() {
        return this.noParseDate(BuildType.EAGER);
    }

    public JSONUtilitiesV2 noParseExpose(BuildType buildType) {
        this.parseExpose = Boolean.FALSE;
        if (BuildType.EAGER.equals(buildType)) {
            return this.configGsonBuilder();
        }
        return this;
    }

    public JSONUtilitiesV2 noParseDate() {
        return this.noParseDate(BuildType.EAGER);
    }

    public JSONUtilitiesV2 noParseDate(BuildType buildType) {
        this.parseDate = Boolean.FALSE;
        if (BuildType.EAGER.equals(buildType)) {
            return this.configGsonBuilder();
        }
        return this;
    }

    public JSONUtilitiesV2 withParseDate() {
        return this.noParseDate(BuildType.EAGER);
    }

    public JSONUtilitiesV2 withParseDate(BuildType buildType) {
        this.parseDate = Boolean.TRUE;
        if (BuildType.EAGER.equals(buildType)) {
            return this.configGsonBuilder();
        }
        return this;
    }

    public JSONUtilitiesV2 noParseLocalDate() {
        return this.noParseLocalDate(BuildType.EAGER);
    }

    public JSONUtilitiesV2 noParseLocalDate(BuildType buildType) {
        this.parseLocalDate = Boolean.FALSE;
        if (BuildType.EAGER.equals(buildType)) {
            return this.configGsonBuilder();
        }
        return this;
    }

    public JSONUtilitiesV2 withParseLocalDate() {
        return this.withParseLocalDate(BuildType.EAGER);
    }

    public JSONUtilitiesV2 withParseLocalDate(BuildType builderType) {
        this.parseLocalDate = Boolean.TRUE;
        if (BuildType.EAGER.equals(builderType)) {
            return this.configGsonBuilder();
        }
        return this;
    }

    public JSONUtilitiesV2 noParseLocalDateTime() {
        return this.noParseLocalDateTime(BuildType.EAGER);
    }

    public JSONUtilitiesV2 noParseLocalDateTime(BuildType buildType) {
        this.parseLocalDateTime = Boolean.FALSE;
        if (BuildType.EAGER.equals(buildType)) {
            return this.configGsonBuilder();
        }
        return this;
    }

    public JSONUtilitiesV2 withParseLocalDateTime() {
        return this.withParseLocalDate(BuildType.EAGER);
    }

    public JSONUtilitiesV2 withParseLocalDateTime(BuildType builderType) {
        this.parseLocalDateTime = Boolean.TRUE;
        if (BuildType.EAGER.equals(builderType)) {
            return this.configGsonBuilder();
        }
        return this;
    }

    public JSONUtilitiesV2 noParseXMLGregorianCalendar() {
        return this.noParseXMLGregorianCalendar(BuildType.EAGER);
    }

    public JSONUtilitiesV2 noParseXMLGregorianCalendar(BuildType builderType) {
        this.parseXMLGregorianCalendar = Boolean.FALSE;
        if (BuildType.EAGER.equals(builderType)) {
            return this.configGsonBuilder();
        }
        return this;
    }

    public JSONUtilitiesV2 withParseXMLGregorianCalendar() {
        return this.withParseXMLGregorianCalendar(BuildType.EAGER);
    }

    public JSONUtilitiesV2 withParseXMLGregorianCalendar(BuildType builderType) {
        this.parseXMLGregorianCalendar = Boolean.TRUE;
        if (BuildType.EAGER.equals(builderType)) {
            return this.configGsonBuilder();
        }
        return this;
    }

    public JSONUtilitiesV2 noParseByteArray() {
        return this.noParseByteArray(BuildType.EAGER);
    }

    public JSONUtilitiesV2 noParseByteArray(BuildType builderType) {
        this.parseByteArray = Boolean.FALSE;
        if (BuildType.EAGER.equals(builderType)) {
            return this.configGsonBuilder();
        }
        return this;
    }

    public JSONUtilitiesV2 withParseByteArray() {
        return this.withParseByteArray(BuildType.EAGER);
    }

    public JSONUtilitiesV2 withParseByteArray(BuildType builderType) {
        this.parseByteArray = Boolean.TRUE;
        if (BuildType.EAGER.equals(builderType)) {
            return this.configGsonBuilder();
        }
        return this;
    }

    public JSONUtilitiesV2 noParseEnum() {
        return this.noParseEnum(BuildType.EAGER);
    }

    public JSONUtilitiesV2 noParseEnum(BuildType builderType) {
        this.parseEnum = Boolean.FALSE;
        if (BuildType.EAGER.equals(builderType)) {
            return this.configGsonBuilder();
        }
        return this;
    }

    public JSONUtilitiesV2 withParseEnum() {
        return this.withParseEnum(BuildType.EAGER);
    }

    public JSONUtilitiesV2 withParseEnum(BuildType builderType) {
        this.parseEnum = Boolean.TRUE;
        if (BuildType.EAGER.equals(builderType)) {
            return this.configGsonBuilder();
        }
        return this;
    }

    public static class DateSerializer implements JsonSerializer<Date> {

        @Override
        public JsonElement serialize(Date date, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(date));
        }
    }

    public static class DateDeserializer implements JsonDeserializer<Date> {

        @Override
        public Date deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
            try {
                if (jsonElement instanceof JsonPrimitive) {
                    return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(jsonElement.getAsString());
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static class LocalDateSerializer implements JsonSerializer<LocalDate> {

        @Override
        public JsonElement serialize(LocalDate localDate, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
    }

    public static class LocalDateDeserializer implements JsonDeserializer<LocalDate> {

        @Override
        public LocalDate deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
            try {
                if (jsonElement instanceof JsonPrimitive) {
                    return LocalDate.parse(jsonElement.getAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
                JsonObject jsonObject = (JsonObject) jsonElement;
                return LocalDate.parse(  jsonObject
                                         .get("year")
                                         .getAsString()
                                         .concat("-")
                                         .concat(String.format(  "%02d"
                                                               , Integer
                                                                 .parseInt(jsonObject
                                                                           .get("monthValue")
                                                                           .getAsString())))
                                         .concat("-")
                                         .concat(String.format(  "%02d"
                                                               , Integer
                                                                 .parseInt(jsonObject
                                                                           .get("dayOfMonth")
                                                                           .getAsString())))
                                       , DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {

        @Override
        public JsonElement serialize(LocalDateTime localDate, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        }
    }

    public static class LocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {

        @Override
        public LocalDateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
            try {
                if (jsonElement instanceof JsonPrimitive) {
                    return LocalDateTime.parse(jsonElement.getAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
    
    public static class OffsetDateTimeSerializer implements JsonSerializer<OffsetDateTime> {

        @Override
        public JsonElement serialize(OffsetDateTime offsetLocalDate, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(offsetLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ")));
        }
    }

    public static class OffsetDateTimeDeserializer implements JsonDeserializer<OffsetDateTime> {

        @Override
        public OffsetDateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
            try {
                if (jsonElement instanceof JsonPrimitive) {
                    return OffsetDateTime.parse(jsonElement.getAsString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }


    public static class XMLGregorianCalendarSerializer implements JsonSerializer<XMLGregorianCalendar> {

        @Override
        public JsonElement serialize(XMLGregorianCalendar xmlGregorianCalendar, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(xmlGregorianCalendar.toXMLFormat());
        }
    }

    public static class XMLGregorianCalendarDeserializer implements JsonDeserializer<XMLGregorianCalendar> {

        @Override
        public XMLGregorianCalendar deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
            try {
                if (jsonElement instanceof JsonPrimitive) {
                    return DatatypeFactory.newInstance().newXMLGregorianCalendar(jsonElement.getAsString());
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private static class ByteArraySerializer implements JsonSerializer<byte[]> {

        @Override
        public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(Base64.getMimeEncoder().encodeToString(src));
        }
    }

    private static class ByteArrayDeserializer implements JsonDeserializer<byte[]> {

        @Override
        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return Base64.getMimeDecoder().decode(json.getAsString());
        }
    }

    public static class EnumAdapterFactory implements TypeAdapterFactory {

        @Override
        public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
            Class<? super T> rawType = type.getRawType();
            if (rawType.isEnum()) {
                return new EnumTypeAdapter<>();
            }
            return null;
        }

        public class EnumTypeAdapter<T> extends TypeAdapter<T> {

            @Override
            public void write(JsonWriter out, T value) throws IOException {
                if (value == null || !value.getClass().isEnum()) {
                    out.nullValue();
                    return;
                }
                try {
                    out.beginObject();
                    out.name("name");
                    out.value(value.toString());
                    Arrays
                    .stream(Introspector
                            .getBeanInfo(value.getClass())
                            .getPropertyDescriptors())
                    .filter(pd ->    pd.getReadMethod() != null
                                  && !"class".equals(pd.getName())
                                  && !"declaringClass".equals(pd.getName()))
                    .forEach(pd -> {
                            try {
                                out.name(pd.getName());
                                out.value(String.valueOf(pd.getReadMethod().invoke(value)));
                            } catch (IllegalAccessException | InvocationTargetException | IOException e) {
                                e.printStackTrace();
                            }
                        });
                    out.endObject();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
                }
            }

            @Override
            public T read(JsonReader in) throws IOException {
                try {
                    in.beginObject();
                    String text = null;
                    while (in.hasNext()) {
                        String name = in.nextName();
                        if (name.equals("value")) {
                            text = in.nextString();
                        } else {
                            in.skipValue();
                        }
                    }
                    in.endObject();
                    return (T) text;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
                }
            }
        }
    }
}