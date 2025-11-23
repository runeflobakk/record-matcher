package no.rune.record.matcher;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static java.lang.reflect.Modifier.isPrivate;

final class ScanHelper {

    static boolean isAccessibleFromSamePackage(Type type) {
        if (type instanceof Class<?> cls) {
            if (cls.isLocalClass()) {
                return false;
            }
            if (cls.isArray()) {
                return isAccessibleFromSamePackage(cls.getComponentType());
            }
            for (var c = cls; c.getDeclaringClass() != null; c = c.getDeclaringClass()) {
                if (isPrivate(c.getModifiers())) {
                    return false;
                }
            }
            return true;
        } else if (type instanceof ParameterizedType parameterizedType) {
            if (!isAccessibleFromSamePackage(parameterizedType.getRawType())) {
                return false;
            }
            for (var typeArg : parameterizedType.getActualTypeArguments()) {
                if (!isAccessibleFromSamePackage(typeArg)) {
                    return false;
                }
            }
            return true;
        } else if (type instanceof GenericArrayType genericArrayType) {
            return isAccessibleFromSamePackage(genericArrayType.getGenericComponentType());
        } else {
            return false;
        }
    }

    private ScanHelper() {
    }
}
