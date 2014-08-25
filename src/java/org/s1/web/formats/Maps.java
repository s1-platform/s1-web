package org.s1.web.formats;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Map helper. Contains useful methods
 *
 * @author Grigory Pykhov
 */
public class Maps {

    private Maps() {
    }

    /**
     * Get value from path (null is default) and cast to type
     *
     * @param type Type
     * @param data Map data
     * @param path Path
     * @param <T>  Type
     * @return Object
     */
    public static <T> T get(Class<T> type, Map<String, Object> data, String path) {
        return get(type, data, path, null);
    }

    /**
     * Get value from path and cast to type
     *
     * @param type Type
     * @param data Map data
     * @param path Path
     * @param def  Default
     * @param <T>  Type
     * @return Object
     */
    public static <T> T get(Class<T> type, Map<String, Object> data, String path, T def) {
        return Types.cast(get(data, path, def), type);
    }

    /**
     * Get value from path (null is default)
     *
     * @param data Map data
     * @param path Path
     * @param <T>  Type
     * @return Object
     */
    public static <T> T get(Map<String, Object> data, String path) {
        return get(data, path, null);
    }

    /**
     * Get value from path
     *
     * @param data Map data
     * @param path Path
     * @param def  Default
     * @param <T>  Type
     * @return Object
     */
    public static <T> T get(Map<String, Object> data, String path, T def) {
        Object ret = def;
        try {
            String[] parts = tokenizePath(path);
            Object o = data;
            for (int i = 0; i < parts.length; i++) {
                int[] j = getNumber(parts[i]);
                String name = getLocalName(parts[i]);
                o = ((Map) o).get(name);
                if (j != null) {
                    for (int k = 0; k < j.length; k++) {
                        o = ((List) o).get(j[k]);
                    }
                }
            }
            if (o != null)
                ret = o;
            else
                ret = def;
        } catch (Throwable e) {
        }
        return (T) ret;
    }


    /**
     * Set value to key identified by path
     *
     * @param data Map data
     * @param path Path
     * @param val  Value
     */
    public static void set(Map<String, Object> data, String path, Object val) {
        String[] parts = tokenizePath(path);
        Map<String, Object> o = data;
        for (int i = 0; i < parts.length; i++) {
            int[] j = getNumber(parts[i]);
            String name = getLocalName(parts[i]);
            if (i == parts.length - 1) {
                if (j != null) {
                    if (!o.containsKey(name)) {
                        o.put(name, new ArrayList());
                    }
                    List<Object> o1 = (List<Object>) o.get(name);

                    for (int k = 0; k < j.length; k++) {
                        if (o1.size() <= j[k]) {
                            for (int ii = 0; ii <= j[k] - o1.size(); ii++)
                                o1.add(null);
                            if (k == j.length - 1) {
                                o1.set(j[k], new HashMap());
                            } else {
                                o1.set(j[k], new ArrayList());
                            }
                        }
                        if (k == j.length - 1) {
                            o1.set(j[k], val);
                        } else {
                            o1 = (List<Object>) o1.get(j[k]);
                        }
                    }
                } else {
                    o.put(name, val);
                }
            } else {

                if (j != null) {
                    if (!o.containsKey(name)) {
                        o.put(name, new ArrayList());
                    }
                    List<Object> o1 = (List<Object>) o.get(name);

                    for (int k = 0; k < j.length; k++) {
                        if (o1.size() <= j[k]) {
                            for (int ii = o1.size(); ii <= j[k]; ii++) {
                                o1.add(null);
                            }
                            if (k == j.length - 1) {
                                o1.set(j[k], new HashMap());
                            } else {
                                o1.set(j[k], new ArrayList());
                            }
                        }
                        if (k == j.length - 1) {
                            o = (Map<String, Object>) o1.get(j[k]);
                        } else {
                            o1 = (List<Object>) o1.get(j[k]);
                        }
                    }
                } else {
                    if (!o.containsKey(name)) {
                        o.put(name, new HashMap());
                    }
                    o = (Map<String, Object>) o.get(name);
                }
            }
        }
    }

    /**
     *
     * @param args Elements
     * @param <T> Type
     * @return ArrayList
     */
    public static <T> List<T> newArrayList(T... args) {
        List<T> list = new ArrayList<T>();
        for (T t : args) {
            list.add(t);
        }
        return list;
    }

    /**
     * @param args Key, Value array
     * @return HashMap
     */
    public static Map<String, Object> newSOHashMap(Object... args) {
        return newHashMap(args);
    }

    /**
     * @param args Key, Value array
     * @param <K>  Key type
     * @param <V>  Value type
     * @return HashMap
     */
    public static <K, V> Map<K, V> newHashMap(Object... args) {
        Map<K, V> m = new HashMap<K, V>();
        for (int i = 0; i < args.length; i += 2) {
            m.put((K) args[i], i + 1 >= args.length ? null : (V) args[i + 1]);
        }
        return m;
    }

    private static String[] tokenizePath(String path) {
        String s = path;
        s = s.replace("&", "&amp;");
        s = s.replace("\\\\", "&backslash;");
        s = s.replace("\\.", "&dot;");
        String[] p = s.split("\\.");
        String[] p2 = new String[p.length];
        for (int i = 0; i < p.length; i++) {
            p2[i] = p[i]
                    .replace("&dot;", ".")
                    .replace("&backslash;", "\\\\")
                    .replace("&amp;", "&");
        }
        return p2;
    }

    private static int[] getNumber(String name) {
        String s = name;
        s = s.replace("&", "&amp;");
        s = s.replace("\\\\", "&backslash;");
        s = s.replace("\\[", "&open;");
        s = s.replace("\\]", "&close;");
        if (s.indexOf("[") < s.indexOf("]")) {
            String s1 = s.substring(s.indexOf("[") + 1, s.lastIndexOf("]"));
            String[] s2 = s1.split("\\]\\[");
            int[] r = new int[s2.length];
            for (int i = 0; i < s2.length; i++) {
                r[i] = Integer.parseInt(s2[i]);
            }
            return r;
        }
        return null;
    }

    private static String getLocalName(String name) {
        String s = name;
        s = s.replace("&", "&amp;");
        s = s.replace("\\\\", "&backslash;");
        s = s.replace("\\[", "&open;");
        s = s.replace("\\]", "&close;");
        String s1 = s;
        if (s.indexOf("[") < s.indexOf("]")) {
            s1 = s.substring(0, s.indexOf("["));
        }
        name = s1.replace("&open;", "[").replace("&close;", "]")
                .replace("&backslash;", "\\")
                .replace("&amp;", "&");
        //name = name.replace("\\[", "[").replace("\\]", "]");
        return name;
    }

    public static <T> T iterate(Object o, IterateFunction closure) {
        return (T) iterateNamedObjectFromLeaf(null, "", o, closure);
    }

    private static Object iterateNamedObjectFromLeaf(String name, String path, Object o, IterateFunction closure) {
        if (o instanceof Map) {
            final Map<String, Object> m1 = new HashMap<String, Object>();
            Map<String, Object> m = (Map<String, Object>) o;
            for (Map.Entry<String, Object> e : m.entrySet()) {
                m1.put(e.getKey(),
                        iterateNamedObjectFromLeaf(e.getKey(), (!path.isEmpty() ? path + "." + e.getKey() : e.getKey()), e.getValue(), closure));
            }
            return closure.call(name, m1, path);
        } else if (o instanceof List) {
            List l = new ArrayList();
            for (int i = 0; i < ((List) o).size(); i++) {
                l.add(iterateNamedObjectFromLeaf(null, path + "[" + i + "]", ((List) o).get(i), closure));
            }
            return closure.call(name, l, path);
        } else {
            return closure.call(name, o, path);
        }
    }

    /**
     *
     */
    public static abstract class IterateFunction {
        public abstract Object call(String name, Object value, String path);
    }

    /**
     * @param beanType Bean type
     * @param map      Map
     * @param <T>      Bean Type
     * @return Bean instance
     */
    public static <T> T convertMapToBean(Class<T> beanType, Map<String, Object> map) {
        T bean = null;
        try {
            bean = beanType.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot instantiate bean of class " + beanType, e);
        }
        BeanInfo info = null;
        try {
            info = Introspector.getBeanInfo(bean.getClass());
        } catch (IntrospectionException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        //setters
        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            Method writer = pd.getWriteMethod();
            if (writer != null && !"class".equals(pd.getName())) {
                try {
                    Object o = map.get(pd.getName());
                    //o = convertMapTypeToBeanType(pd.getPropertyType(), o);
                    o = convertMapTypeToBeanType(pd.getWriteMethod().getParameters()[0].getParameterizedType(), o);
                    writer.invoke(bean, o);
                } catch (Exception e) {
                }
            }
        }

        //public fields
        Class cls = bean.getClass();
        while (cls != null && cls != Object.class) {
            Field[] declaredFields = cls.getDeclaredFields();
            for (Field field : declaredFields) {
                //if(field.isAccessible())
                try {
                    Object o = map.get(field.getName());
                    o = convertMapTypeToBeanType(field.getGenericType(), o);
                    field.set(bean, o);
                } catch (Exception e) {
                }
            }
            cls = cls.getSuperclass();
        }

        return bean;
    }

    /**
     * @param type Type
     * @param val  Source value
     * @param <T>  Type
     * @return Value converted for bean field
     */
    public static <T> T convertMapTypeToBeanType(Class<T> type, Object val) {
        return convertMapTypeToBeanType((Type)type,val);
    }

    /**
     *
     * @param type Type
     * @param val Source value
     * @param <T> Type
     * @return Value converted for bean field
     */
    public static <T> T convertMapTypeToBeanType(Type type, Object val) {
        if(type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)type;
            Class cls = (Class)pt.getRawType();
            if (Map.class.isAssignableFrom(cls)) {
                Class<?> tp = (Class<?>) pt.getActualTypeArguments()[0];
                //try convert map keys to beans
                Map l = new HashMap();
                if (val instanceof Map) {
                    Map v = (Map) val;
                    for (Object k : v.keySet()) {
                        l.put(k, convertMapTypeToBeanType(tp, v.get(k)));
                    }
                }
                return (T) l;
            } else if (List.class.isAssignableFrom(cls)) {
                Class<?> tp = (Class<?>) pt.getActualTypeArguments()[0];
                //try convert List elements to beans
                List l = new ArrayList();
                if (val instanceof List) {
                    for (Object o : (List) val) {
                        l.add(convertMapTypeToBeanType(tp, o));
                    }
                } else if (val instanceof Set) {
                    for (Object o : (Set) val) {
                        l.add(convertMapTypeToBeanType(tp, o));
                    }
                }
                return (T) l;
            } else if (Set.class.isAssignableFrom(cls)) {
                Class<?> tp = (Class<?>) pt.getActualTypeArguments()[0];
                //try convert Set elements to beans
                Set l = new HashSet();
                if (val instanceof List) {
                    for (Object o : (List) val) {
                        l.add(convertMapTypeToBeanType(tp, o));
                    }
                } else if (val instanceof Set) {
                    for (Object o : (Set) val) {
                        l.add(convertMapTypeToBeanType(tp, o));
                    }
                }
                return (T) l;
            }
        }else if(type instanceof Class){
            Class cls = (Class) type;

            if (val instanceof Map && !Map.class.isAssignableFrom(cls)) {
                return (T)convertMapToBean(cls, (Map) val);
            }

            return (T)Types.cast(val,cls);
        }
        return (T)val;
    }

    /**
     * @param bean Bean
     * @return Map
     */
    public static Map<String, Object> convertBeanToMap(Object bean) {
        Map<String, Object> result = new HashMap<String, Object>();
        BeanInfo info = null;
        try {
            info = Introspector.getBeanInfo(bean.getClass());
        } catch (IntrospectionException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        //getters
        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            Method reader = pd.getReadMethod();
            if (reader != null && !"class".equals(pd.getName()))
                try {
                    result.put(pd.getName(), convertBeanTypeToMapType(reader.invoke(bean)));
                } catch (Exception e) {
                }
        }

        //fields
        Class cls = bean.getClass();
        while (cls != null && cls != Object.class) {
            Field[] declaredFields = cls.getDeclaredFields();
            for (Field field : declaredFields) {
                //if(field.isAccessible())
                try {
                    result.put(field.getName(), convertBeanTypeToMapType(field.get(bean)));
                } catch (Exception e) {
                }
            }
            cls = cls.getSuperclass();
        }
        return result;
    }

    /**
     * @param val Value from bean field
     * @param <T> Type
     * @return Value for Map
     */
    public static <T> T convertBeanTypeToMapType(Object val) {
        if (val instanceof Map) {
            Map l = (Map) val;
            for (Object k : l.keySet()) {
                l.put(k, convertBeanTypeToMapType(l.get(k)));
            }
            return (T)l;
        } else if (val instanceof List) {
            List l = (List) val;
            for (int i = 0; i < l.size(); i++) {
                l.set(i, convertBeanTypeToMapType(l.get(i)));
            }
            return (T)l;
        } else if (val instanceof Set) {
            List l = new ArrayList();
            for (Object o : (Set) val) {
                l.add(convertBeanTypeToMapType(o));
            }
            return (T)l;
        } else if (val instanceof Integer || val instanceof String || val instanceof Date || val instanceof Boolean || val instanceof Long
                || val instanceof Double || val instanceof Float || val instanceof BigInteger || val instanceof BigDecimal
                || val.getClass() == int.class || val.getClass() == long.class || val.getClass() == boolean.class ||
                val.getClass() == float.class || val.getClass() == double.class) {
            return (T)val;
        } else if (val instanceof Enum) {
            return (T)val.toString();
        } else if (val instanceof Object) {
            return (T)convertBeanToMap(val);
        }
        return (T)val;
    }

    /**
     * Get difference between two maps
     *
     * @param object1 Object1
     * @param object2 Object2
     * @return List of differences
     */
    public static List<DiffBean> diff(Map<String, Object> object1, Map<String, Object> object2) {
        if (object1 == null)
            object1 = newHashMap();
        if (object2 == null)
            object2 = newHashMap();
        object1 = Types.copy(object1);
        object2 = Types.copy(object2);
        final List<DiffBean> diff = newArrayList();

        diffTwoMaps(diff, object1, object2, true);
        diffTwoMaps(diff, object2, object1, false);

        return diff;
    }

    private static void diffTwoMaps(final List<DiffBean> diff, final Map<String, Object> m1,
                                    final Map<String, Object> m2, final boolean m1Old) {
        iterate(m1, new IterateFunction() {
            @Override
            public Object call(String name, Object value, String path) {
                Object o = value;
                if (o instanceof Map) {

                } else if (o instanceof List) {

                } else {
                    Object oo = null;
                    if (!Types.isNullOrEmpty(path))
                        oo = get(m2, path, null);
                    if (m1Old) {
                        if (!Objects.equals(o, oo)) {
                            DiffBean res = new DiffBean(path, o, oo);
                            boolean b = false;
                            for (DiffBean db : diff) {
                                if (res.getPath().equals(db.getPath())) {
                                    b = true;
                                }
                            }
                            if (!b) {
                                diff.add(res);
                            }
                        }
                    } else {
                        if (!Objects.equals(o, oo)) {
                            DiffBean res = new DiffBean(path, oo, o);
                            boolean b = false;
                            for (DiffBean db : diff) {
                                if (res.getPath().equals(db.getPath())) {
                                    b = true;
                                }
                            }
                            if (!b) {
                                diff.add(res);
                            }
                        }
                    }
                }
                return o;
            }
        });
    }

    /**
     * Map diff result bean
     */
    public static class DiffBean {
        private String path;
        private Object value1;
        private Object value2;

        public DiffBean(String path, Object value1, Object value2) {
            this.path = path;
            this.value1 = value1;
            this.value2 = value2;
        }

        public String getPath() {
            return path;
        }

        public Object getValue1() {
            return value1;
        }

        public Object getValue2() {
            return value2;
        }
    }


}
