package dk.brics.jwig.boost.rendering.uicomponents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dk.brics.jwig.FormField;
import dk.brics.jwig.Parameters;
import dk.brics.jwig.TextField;
import dk.brics.jwig.persistence.Persistable;
import dk.brics.jwig.server.RequestManager;

public class MapDeserializer {
    public static class ListClass<E> {
        private final Class<E> element;

        public ListClass(Class<E> element) {
            this.element = element;
        }

        @SuppressWarnings("rawtypes")
        public Class<List> getCollection() {
            return List.class;
        }

        public Class<E> getElement() {
            return element;
        }

    }

    /**
     * A HashMap modification which does not return null as default on keys
     * which are not present. This is done to prevent spurious null-values.
     * 
     * If in doubt about the existence of a key, use
     * {@link #containsKey(Object)}.
     * 
     * @param <T>
     *            as the key-type
     * @param <S>
     *            as the value-type
     * @throws IllegalArgumentException
     *             ff {@link #get(Object)} is called with a non-key, an
     */
    @SuppressWarnings("serial")
    public class NoDefaultValueHashMap<T, S> extends HashMap<T, S> {
        @Override
        public S get(Object key) {
            if (!super.containsKey(key)) {
                throw new IllegalArgumentException(
                        "No such key present in this map: " + key);
            }
            return super.get(key);
        }
    }

    /**
     * Class for computing various information about a type.
     */
    public class TypeInfo {
        private final boolean array;
        private final boolean collection;
        private Class<?> collectionType;
        private Class<?> type;

        @SuppressWarnings("unchecked")
        public TypeInfo(Class<?> type) {
            // logic copied from requestmangager.buildmethodarg
            array = type.isArray();
            collection = Collection.class.isAssignableFrom(type);
            collectionType = null;
            if (array) {
                this.type = type.getComponentType();
            } else if (collection) {
                collectionType = type
                        .asSubclass(Collection.class);
                // this.type = ParameterNamer.getListType(method, param);
                throw new UnsupportedOperationException(
                        "No hooks into JWIG to extract the information needed to construct a collection");
            } else {
                this.type = type;
            }
        }

        public TypeInfo(ListClass<?> listClass) {
            array = false;
            collection = true;
            collectionType = listClass
                    .getCollection();
            type = listClass.getElement();
        }

        Class<?> getCollectionType() {
            return collectionType;
        }

        Class<?> getType() {
            return type;
        }

        boolean isArray() {
            return array;
        }

        boolean isCollection() {
            return collection;
        }

    }

    private final RequestManager requestManager;

    public MapDeserializer() {
        requestManager = new RequestManager();
    }

    private Object[] convertFieldListToObjectArray(List<FormField> values) {
        List<Object> arrayList = new ArrayList<>();
        for (FormField formField : values) {
            arrayList.add(formField.getValue());
        }
        return arrayList.toArray();
    }

    private Object deserialize(Object[] value, Class<?> type,
            String variableName) {
        TypeInfo typeInfo = new TypeInfo(type);
        return deserializeArgument(value, variableName, typeInfo);
    }

    private Object deserialize(Object[] value, ListClass<?> listClass,
            String variableName) {
        TypeInfo typeInfo = new TypeInfo(listClass);
        return deserializeArgument(value, variableName, typeInfo);
    }

    @SuppressWarnings("unchecked")
    private Object deserializeArgument(Object[] value, String variableName,
            TypeInfo typeInfo) {
        return requestManager.deserializeArgument(value, typeInfo.getType(),
                variableName, typeInfo.isArray(), typeInfo.isCollection(),
                (Class<? extends Collection<?>>) typeInfo.getCollectionType());
    }

    private <T, S> Map<T, S> deserializeMap(Map<String, Object[]> params,
            String mapName, Class<T> keyType, Class<S> valueType) {
        Map<String, Object[]> relatorId2valueIds = makeRelator2ids(params,
                mapName);

        Map<T, S> map = new NoDefaultValueHashMap<>();

        for (Entry<String, Object[]> entry : relatorId2valueIds.entrySet()) {
            @SuppressWarnings("unchecked")
            final T key = (T) deserialize(new Object[] { entry.getKey() },
                    keyType, mapName);
            @SuppressWarnings("unchecked")
            S value = (S) deserialize(entry.getValue(), valueType, mapName);
            map.put(key, value);
        }
        return map;
    }

    private <T, S> Map<T, S> deserializeMap(Map<String, Object[]> params,
            String mapName, Class<T> keyType, ListClass<?> listClass) {
        Map<String, Object[]> relatorId2valueIds = makeRelator2ids(params,
                mapName);

        Map<T, S> map = new NoDefaultValueHashMap<>();

        for (Entry<String, Object[]> entry : relatorId2valueIds.entrySet()) {
            @SuppressWarnings("unchecked")
            final T key = (T) deserialize(new Object[] { entry.getKey() },
                    keyType, mapName);
            @SuppressWarnings("unchecked")
            S value = (S) deserialize(entry.getValue(), listClass, mapName);
            map.put(key, value);
        }
        return map;
    }

    /**
     * Deserializes the parameters of a webmethods-call to a convenient map.
     * 
     * The parameters to be put in the map is expected to have names of the
     * form: <code>mapName:key</code>, where <code>key</code> the id of a
     * {@link Persistable}-object.
     * 
     * The returned map will <emph>not</emph> contain <code>null</code> for
     * values which are not present as keys. An error will be thrown whenever a
     * non-existing key is used.
     * 
     * If in doubt about the existence of a key, use
     * {@link #containsKey(Object)}.
     * 
     * @param <S>
     *            as the key-type of the map
     * @param <T>
     *            as the value-type of the map
     * @param parameters
     *            as the {@link Parameters} argument of the webmethod
     * @param mapName
     *            as the suffix of the names of the values to put into the map
     * @param keyType
     *            as the key-type of the map
     * @param valueType
     *            as the value-type of the map
     * @return
     * 
     */
    public <S, T> Map<T, S> deserializeMap(Parameters parameters,
            String mapName, Class<T> keyType, Class<S> valueType) {
        return deserializeMap(fixParams(parameters), mapName, keyType,
                valueType);
    }

    /**
     * 
     */
    public <S, T> Map<T, List<S>> deserializeMap(Parameters parameters,
            String mapName, Class<T> keyType, ListClass<S> listClass) {
        return deserializeMap(fixParams(parameters), mapName, keyType,
                listClass);
    }

    /**
     * Converts {@link Parameters} to Map<String, Object[]>, as they appear in
     * the {@link RequestManager}.
     * 
     * Workaround when we can't plug directly into the {@link RequestManager}.
     * 
     * Is not as expressive as it could be: only {@link TextField} and are
     * expressive in {@link Parameters}.
     */
    private Map<String, Object[]> fixParams(Parameters parameters) {
        Map<String, Object[]> paramMap = new HashMap<>();
        Map<String, List<FormField>> parameterMap = parameters.getMap();
        for (Entry<String, List<FormField>> entry : parameterMap.entrySet()) {
            paramMap.put(entry.getKey(),
                    convertFieldListToObjectArray(entry.getValue()));
        }
        return paramMap;
    }

    private Map<String, Object[]> makeRelator2ids(Map<String, Object[]> params,
            String mapName) {
        Map<String, Object[]> relatorId2valueIds = new HashMap<>();
        for (Entry<String, Object[]> entry : params.entrySet()) {
            final String key = entry.getKey();
            String[] split = key.split(":");
            if (split.length == 2) {
                if (mapName.equals(split[0])) {
                    relatorId2valueIds.put(split[1], entry.getValue());
                }
            }
        }
        return relatorId2valueIds;
    }
}
