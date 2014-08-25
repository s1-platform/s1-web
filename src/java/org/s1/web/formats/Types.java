/*
 * Copyright 2014 Grigory Pykhov
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.s1.web.formats;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Type helper
 *
 * @author Grigory Pykhov
 */
public class Types {

    private Types(){}

    /**
     * Copy object
     *
     * @param orig Source object
     * @param <T> Type
     * @return Copy of object
     */
    public static <T> T copy(T orig) {
        if (orig == null)
            return null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(orig);
            oos.flush();
            ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bin);
            return (T) ois.readObject();
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * Cast object to type
     *
     * @param obj Object
     * @param type Type
     * @param <T> Type
     * @return Casted object
     */
    public static <T> T cast(Object obj, Class<T> type) {
        if (type == String.class) {
            if (obj == null)
                obj = "";
            obj = obj.toString();
        } else if (type == BigInteger.class) {
            if (obj == null || (""+obj).isEmpty())
                obj = "0";
            obj = new BigInteger("" + obj);
        } else if (type == BigDecimal.class) {
            if (obj == null || (""+obj).isEmpty())
                obj = "0";
            obj = new BigDecimal("" + obj);
        } else if (type == Integer.class || type == int.class) {
            if (obj == null || (""+obj).isEmpty())
                obj = "0";
            if(obj instanceof Integer){

            }else
                obj = new BigDecimal(""+obj).intValue();
        } else if (type == Long.class || type==long.class) {
            if (obj == null || (""+obj).isEmpty())
                obj = "0";
            if(obj instanceof Long){

            }else
                obj = new BigDecimal(""+obj).longValue();
        } else if (type == Float.class || type==float.class) {
            if (obj == null || (""+obj).isEmpty())
                obj = "0";
            if(obj instanceof Float){

            }else
                obj = new BigDecimal(""+obj).floatValue();
        } else if (type == Double.class || type==double.class) {
            if (obj == null || (""+obj).isEmpty())
                obj = "0";
            if(obj instanceof Double){

            }else
                obj = new BigDecimal(""+obj).doubleValue();
        } else if (type == Boolean.class || type==boolean.class) {
            if (obj == null || (""+obj).isEmpty())
                obj = "false";
            if(obj instanceof Number){
                obj = ((Number) obj).intValue()!=0;
            }else
                obj = Boolean.parseBoolean("" + obj);
        } else if (type == Date.class) {
            if((""+obj).isEmpty())
                obj = null;
            if (obj != null) {
                if (obj instanceof String) {
                    obj = new Date(Long.parseLong("" + obj));
                } else if (obj instanceof Long) {
                    obj = new Date((Long) obj);
                }
            }
        } else if(type.isEnum()){
            if(obj != null && obj instanceof String){
                obj = Enum.valueOf((Class<? extends Enum>) type, ((String) obj));
            }
        }

        return (T) obj;
    }

    /**
     *
     * @param obj Object
     * @return Is null or empty
     */
    public static boolean isNullOrEmpty(Object obj) {
        if (obj == null)
            return true;
        if (obj instanceof String) {
            return (((String) obj).isEmpty());
        }
        if (obj instanceof Map) {
            return ((Map) obj).isEmpty();
        }
        if (obj instanceof List) {
            return ((List) obj).isEmpty();
        }
        if (obj instanceof Set) {
            return ((Set) obj).isEmpty();
        }
        return false;
    }

    /**
     * Returns true if objects are equals by value
     *
     * @param o1 Object1
     * @param o2 Object2
     * @return True if equals
     */
    public static boolean equals(Object o1, Object o2) {
        if (o1 == o2)
            return true;
        if (o1 == null && o2 == null)
            return true;
        if (o1 == null || o2 == null)
            return false;
        //number
        if (!o1.getClass().equals(o2.getClass()) && (o1 instanceof Number || o2 instanceof Number)) {
            try {
                BigDecimal b1 = cast(o1, BigDecimal.class);
                BigDecimal b2 = cast(o2, BigDecimal.class);
                return b1.compareTo(b2)==0;
            } catch (Throwable e) {
                return false;
            }
        }
        //map
        if (o1 instanceof Map && o2 instanceof Map) {
            return Maps.diff((Map<String, Object>) o1, (Map<String, Object>) o2).size() == 0;
        } else if (o1 instanceof List && o2 instanceof List) {
            Map<String, Object> m1 = Maps.newHashMap("list", o1);
            Map<String, Object> m2 = Maps.newHashMap("list", o2);
            return Maps.diff(m1, m2).size() == 0;
        } else {
            return o1.equals(o2);
        }
    }
}
