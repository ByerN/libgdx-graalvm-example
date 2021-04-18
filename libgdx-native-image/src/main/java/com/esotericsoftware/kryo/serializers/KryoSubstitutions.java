package com.esotericsoftware.kryo.serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializerGenericsUtil;
import com.esotericsoftware.kryo.serializers.ObjectField;
import com.esotericsoftware.kryo.util.Util;
import com.esotericsoftware.reflectasm.ConstructorAccess;
import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.objenesis.instantiator.ObjectInstantiator;
import org.objenesis.strategy.InstantiatorStrategy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;

import static com.esotericsoftware.kryo.util.Util.className;
import static com.esotericsoftware.minlog.Log.TRACE;
import static com.esotericsoftware.minlog.Log.trace;
/*
@TargetClass(com.esotericsoftware.kryo.serializers.FieldSerializer.class)
abstract class com_esotericsoftware_kryo_serializers_FieldSerializer extends FieldSerializer {

  @Alias
  @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias, isFinal = true)
  Kryo kryo;

  public com_esotericsoftware_kryo_serializers_FieldSerializer(Kryo kryo, Class type) {
    super(kryo, type);
  }

  @Alias
  private FieldSerializer.CachedFieldFactory getObjectFieldFactory (){
    return null;
  }

  @Substitute
  FieldSerializer.CachedField newMatchingCachedField (Field field, int accessIndex, Class fieldClass, Type fieldGenericType,
                                                      Class[] fieldGenerics) {
    FieldSerializer.CachedField cachedField;
    cachedField = getObjectFieldFactory().createCachedField(fieldClass, field, this);
    if (fieldGenerics != null)
      ((ObjectField)cachedField).generics = fieldGenerics;
    else {
      Class[] cachedFieldGenerics = FieldSerializerGenericsUtil.getGenerics(fieldGenericType, kryo);
      ((ObjectField)cachedField).generics = cachedFieldGenerics;
      if (TRACE) trace("kryo", "Field generics: " + Arrays.toString(cachedFieldGenerics));
    }

    return cachedField;
  }
}*/
@TargetClass(com.esotericsoftware.kryo.Kryo.DefaultInstantiatorStrategy.class)
final class com_esotericsoftware_kryo_Kryo_DefaultInstantiatorStrategy {
  @Alias
  private InstantiatorStrategy fallbackStrategy;

  @Substitute
  public ObjectInstantiator newInstantiatorOf(final Class type) {
    // Reflection.
    try {
      Constructor ctor;
      try {
        ctor = type.getConstructor((Class[])null);
      } catch (Exception ex) {
        ctor = type.getDeclaredConstructor((Class[])null);
        ctor.setAccessible(true);
      }
      final Constructor constructor = ctor;
      return new ObjectInstantiator() {
        public Object newInstance () {
          try {
            return constructor.newInstance();
          } catch (Exception ex) {
            throw new KryoException("Error constructing instance of class: " + className(type), ex);
          }
        }
      };
    } catch (Exception ignored) {
    }
    if (fallbackStrategy == null) {
      if (type.isMemberClass() && !Modifier.isStatic(type.getModifiers()))
        throw new KryoException("Class cannot be created (non-static member class): " + className(type));
      else
        throw new KryoException("Class cannot be created (missing no-arg constructor): " + className(type));
    }
    // InstantiatorStrategy.
    return fallbackStrategy.newInstantiatorOf(type);
  }
}

@TargetClass(com.esotericsoftware.kryo.Kryo.class)
final class com_esotericsoftware_kryo_Kryo {

  @Alias
  private InstantiatorStrategy strategy;

  @Substitute
  protected ObjectInstantiator newInstantiator(final Class type) {
    //if you want to log what failed
    //System.out.println("Instantiator for: " + type.getName());
    return strategy.newInstantiatorOf(type);
  }

}

public class KryoSubstitutions {
}
