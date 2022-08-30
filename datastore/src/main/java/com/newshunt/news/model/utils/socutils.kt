/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.utils

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.ObjectStreamClass
import java.io.OutputStream
import java.io.Serializable
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


// copied from https://raw.githubusercontent.com/apache/commons-lang/master/src/main/java/org/apache/commons/lang3/SerializationUtils.java
// converted using android studio java-kotlin converter


/**
 *
 * Assists with the serialization process and performs additional functionality based
 * on serialization.
 *
 *
 *  * Deep clone using serialization
 *  * Serialize managing finally and IOException
 *  * Deserialize managing finally and IOException
 *
 *
 *
 * This class throws exceptions for invalid `null` inputs.
 * Each method documents its behaviour in more detail.
 *
 *
 * #ThreadSafe#
 * @since 1.0
 */
/**
 *
 * SerializationUtils instances should NOT be constructed in standard programming.
 * Instead, the class should be used as `SerializationUtils.clone(object)`.
 *
 *
 * This constructor is public to permit tools that require a JavaBean instance
 * to operate.
 * @since 2.0
 */
class SerializationUtils {

  /**
   *
   * Custom specialization of the standard JDK [java.io.ObjectInputStream]
   * that uses a custom  `ClassLoader` to resolve a class.
   * If the specified `ClassLoader` is not able to resolve the class,
   * the context classloader of the current thread will be used.
   * This way, the standard deserialization work also in web-application
   * containers and application servers, no matter in which of the
   * `ClassLoader` the particular class that encapsulates
   * serialization/deserialization lives.
   *
   *
   * For more in-depth information about the problem for which this
   * class here is a workaround, see the JIRA issue LANG-626.
   */
  internal class ClassLoaderAwareObjectInputStream
  /**
   * Constructor.
   * @param in The `InputStream`.
   * @param classLoader classloader to use
   * @throws IOException if an I/O error occurs while reading stream header.
   * @see java.io.ObjectInputStream
   */
  @Throws(IOException::class)
  constructor(`in`: InputStream, private val classLoader: ClassLoader) : ObjectInputStream(`in`) {

    /**
     * Overridden version that uses the parameterized `ClassLoader` or the `ClassLoader`
     * of the current `Thread` to resolve the class.
     * @param desc An instance of class `ObjectStreamClass`.
     * @return A `Class` object corresponding to `desc`.
     * @throws IOException Any of the usual Input/Output exceptions.
     * @throws ClassNotFoundException If class of a serialized object cannot be found.
     */
    @Throws(IOException::class, ClassNotFoundException::class)
    override fun resolveClass(desc: ObjectStreamClass): Class<*> {
      val name = desc.name
      try {
        return Class.forName(name, false, classLoader)
      } catch (ex: ClassNotFoundException) {
        try {
          return Class.forName(name, false, Thread.currentThread().contextClassLoader)
        } catch (cnfe: ClassNotFoundException) {
          val cls = primitiveTypes[name]
          if (cls != null) {
            return cls
          }
          throw cnfe
        }

      }

    }

    companion object {
      private val primitiveTypes = HashMap<String, Class<*>>()

      init {
        primitiveTypes["byte"] = Byte::class.javaPrimitiveType!!
        primitiveTypes["short"] = Short::class.javaPrimitiveType!!
        primitiveTypes["int"] = Int::class.javaPrimitiveType!!
        primitiveTypes["long"] = Long::class.javaPrimitiveType!!
        primitiveTypes["float"] = Float::class.javaPrimitiveType!!
        primitiveTypes["double"] = Double::class.javaPrimitiveType!!
        primitiveTypes["boolean"] = Boolean::class.javaPrimitiveType!!
        primitiveTypes["char"] = Char::class.javaPrimitiveType!!
        primitiveTypes["void"] = Void.TYPE
      }
    }

  }

  companion object {

    // Clone
    //-----------------------------------------------------------------------
    /**
     *
     * Deep clone an `Object` using serialization.
     *
     *
     * This is many times slower than writing clone methods by hand
     * on all objects in your object graph. However, for complex object
     * graphs, or for those that don't support deep cloning this can
     * be a simple alternative implementation. Of course all the objects
     * must be `Serializable`.
     *
     * @param <T> the type of the object involved
     * @param object  the `Serializable` object to clone
     * @return the cloned object
     * @throws SerializationException (runtime) if the serialization fails
    </T> */
    fun <T : Serializable> clone(`object`: T?): T? {
      if (`object` == null) {
        return null
      }
      val objectData = serialize(`object`)
      val bais = ByteArrayInputStream(objectData)

      return try {
        `object`.javaClass.classLoader?.let {
          ClassLoaderAwareObjectInputStream(bais, it).use { `in` ->
            /*
           * when we serialize and deserialize an object,
           * it is reasonable to assume the deserialized object
           * is of the same type as the original serialized object
           */
            return `in`.readObject() as T

          }
        }
      } catch (ex: ClassNotFoundException) {
        throw SerializationException("ClassNotFoundException while reading cloned object data", ex)
      } catch (ex: IOException) {
        throw SerializationException("IOException while reading or closing cloned object data", ex)
      }

    }

    /**
     * Performs a serialization roundtrip. Serializes and deserializes the given object, great for testing objects that
     * implement [Serializable].
     *
     * @param <T>
     * the type of the object involved
     * @param msg
     * the object to roundtrip
     * @return the serialized and deserialized object
     * @since 3.3
    </T> */
    // OK, because we serialized a type `T`
    fun <T : Serializable> roundtrip(msg: T): T {
      return deserialize<Any>(serialize(msg)) as T
    }

    // Serialize
    //-----------------------------------------------------------------------
    /**
     *
     * Serializes an `Object` to the specified stream.
     *
     *
     * The stream will be closed once the object is written.
     * This avoids the need for a finally clause, and maybe also exception
     * handling, in the application code.
     *
     *
     * The stream passed in is not buffered internally within this method.
     * This is the responsibility of your application if desired.
     *
     * @param obj  the object to serialize to bytes, may be null
     * @param outputStream  the stream to write to, must not be null
     * @throws IllegalArgumentException if `outputStream` is `null`
     * @throws SerializationException (runtime) if the serialization fails
     */
    fun serialize(obj: Serializable, outputStream: OutputStream?) {
      Validate.isTrue(outputStream != null, "The OutputStream must not be null")
      try {
        ObjectOutputStream(outputStream).use { out -> out.writeObject(obj) }
      } catch (ex: IOException) {
        throw SerializationException(ex)
      }

    }

    /**
     *
     * Serializes an `Object` to a byte array for
     * storage/serialization.
     *
     * @param obj  the object to serialize to bytes
     * @return a byte[] with the converted Serializable
     * @throws SerializationException (runtime) if the serialization fails
     */
    fun serialize(obj: Serializable): ByteArray {
      val baos = ByteArrayOutputStream(512)
      serialize(obj, baos)
      return baos.toByteArray()
    }

    // Deserialize
    //-----------------------------------------------------------------------
    /**
     *
     *
     * Deserializes an `Object` from the specified stream.
     *
     *
     *
     *
     * The stream will be closed once the object is written. This avoids the need for a finally clause, and maybe also
     * exception handling, in the application code.
     *
     *
     *
     *
     * The stream passed in is not buffered internally within this method. This is the responsibility of your
     * application if desired.
     *
     *
     *
     *
     * If the call site incorrectly types the return value, a [ClassCastException] is thrown from the call site.
     * Without Generics in this declaration, the call site must type cast and can cause the same ClassCastException.
     * Note that in both cases, the ClassCastException is in the call site, not in this method.
     *
     *
     * @param <T>  the object type to be deserialized
     * @param inputStream
     * the serialized object input stream, must not be null
     * @return the deserialized object
     * @throws IllegalArgumentException
     * if `inputStream` is `null`
     * @throws SerializationException
     * (runtime) if the serialization fails
    </T> */
    fun <T> deserialize(inputStream: InputStream?): T {
      Validate.isTrue(inputStream != null, "The InputStream must not be null")
      try {
        ObjectInputStream(inputStream).use { `in` ->
          return `in`.readObject() as T
        }
      } catch (ex: ClassNotFoundException) {
        throw SerializationException(ex)
      } catch (ex: IOException) {
        throw SerializationException(ex)
      }

    }

    /**
     *
     *
     * Deserializes a single `Object` from an array of bytes.
     *
     *
     *
     *
     * If the call site incorrectly types the return value, a [ClassCastException] is thrown from the call site.
     * Without Generics in this declaration, the call site must type cast and can cause the same ClassCastException.
     * Note that in both cases, the ClassCastException is in the call site, not in this method.
     *
     *
     * @param <T>  the object type to be deserialized
     * @param objectData
     * the serialized object, must not be null
     * @return the deserialized object
     * @throws IllegalArgumentException
     * if `objectData` is `null`
     * @throws SerializationException
     * (runtime) if the serialization fails
    </T> */
    fun <T> deserialize(objectData: ByteArray?): T {
      Validate.isTrue(objectData != null, "The byte[] must not be null")
      return deserialize(ByteArrayInputStream(objectData!!))
    }
  }

}

internal object Validate {
  fun isTrue(boolean: Boolean, message: String) = assert(boolean, { message })
}

class SerializationException(message: String, val exception: java.lang.Exception) : Exception
(message) {
  constructor(exception: java.lang.Exception) : this(exception.message ?: "", exception)
}



/**
 * Safely handles observables from LiveData for testing.
 */
object LiveDataTestUtil {

  /**
   * Gets the value of a LiveData safely.
   */
  @Throws(InterruptedException::class)
  fun <T> getValue(liveData: LiveData<T>, sec : Long = 2): T? {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
      override fun onChanged(o: T?) {
        data = o
        latch.countDown()
        liveData.removeObserver(this)
      }
    }
    liveData.observeForever(observer)
    latch.await(sec, TimeUnit.SECONDS)

    return data
  }
}
