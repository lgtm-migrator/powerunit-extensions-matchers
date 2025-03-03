/**
 * Powerunit - A JDK1.8 test framework
 * Copyright (C) 2014 Mathieu Boretti.
 *
 * This file is part of Powerunit
 *
 * Powerunit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Powerunit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Powerunit. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.powerunit.extensions.matchers;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be used on a java class, to mark this class as supporting
 * generation of hamcrest matcher.
 * <p>
 * <b>This annotation is not supported on interface and enum. A error will be
 * generated in this case.</b>
 * <p>
 * This annotation is processed by an annotation processor, in order to generate
 * :
 * <ul>
 * <li>One class for each annotated classes, that will contains Hamcrest
 * Matchers for the class.</li>
 * <li>In case the annotation processor parameter
 * "{@code ch.powerunit.extensions.matchers.provideprocessor.ProvidesMatchersAnnotationsProcessor.factory}"
 * is set, this value define the fully qualified name of a interface that will
 * be generated and will contains all <i>start method</i> allowing to create
 * instance of the various matchers.</li>
 * </ul>
 * <p>
 * <i>The generated classes are related with the hamcrest framework ; This
 * library will be required in the classpath in order to compile or run the
 * generated classes.</i>
 * <p>
 * <b>Concept regarding the generated Matchers</b>
 * <p>
 * Hamcrest Matchers can be used, for example, with test framework (JUnit,
 * PowerUnit, etc.) to validate expectation on object. Hamcrest provides several
 * matchers to validate some information of an object (is an instance of, is, an
 * array contains some value, etc.), but can't provide ready to use matcher for
 * your own object. When trying to validate properties of object, no syntaxic
 * sugar (ie. autocompletion) are available and only the generic method can be
 * used.
 * <p>
 * With this annotation, it is possible to provide <i>builder-like</i> method,
 * based on hamcrest, to validate fields of an object. To do so, the annotation
 * processor do the following :
 * <ul>
 * <li>For each public field or public method starting with {@code get} or
 * {@code is} without any argument, generated a private matcher based on the
 * {@code org.hamcrest.FeatureMatcher} for this field ; this will provide a way
 * to validate the value of one specific <i>property</i>.</li>
 * <li>Generate an interface and the related implementation of a matcher (which
 * is also a builder) on the annotated classes itself, which will validate all
 * of the <i>properties</i>.</li>
 * <li>Generate various methods, with a name based on the annotated class, to
 * start the creation of the matcher.</li>
 * </ul>
 * The annotation processor will also generate javadoc and try to retrieve from
 * the javadoc of the annotated element the information regarding generic
 * attribute.
 * <p>
 * The processor will also try, for field which are not based on generics, to
 * link the generated matchers between them, in order to provide chaining of the
 * Fields.
 * <p>
 * <i>First example</i>
 * <p>
 * Let's assume the following class, containing one single field, will be
 * processed by the annotation processor :
 * 
 * <pre>
 * package ch.powerunit.extensions.matchers.samples;
 *
 * import ch.powerunit.extensions.matchers.ProvideMatchers;
 *
 * &#64;ProvideMatchers
 * public class SimplePojo {
 * 	public String oneField;
 * }
 * </pre>
 * 
 * In this case a class named {@code SimplePojoMatchers} will be generated. As a
 * public interface, the following methods will be available :
 * <ul>
 * <li>{@code public static SimplePojoMatcher simplePojoWith()}: This will
 * return a matcher (see below), which by default matches any instance of the
 * SimplePojo class.</li>
 * <li>
 * {@code public static SimplePojoMatcher simplePojoWithSameValue(SimplePojo  other)}
 * : This will return a matcher, which by default matches an instance of the
 * SimplePojo having the field {@code oneField} matching (Matcher {@code is} of
 * hamcrest) of the reference object.</li>
 * </ul>
 * The returned interface is already a correct hamcrest matcher. This interface
 * provide method that set the expectating on the various fields. As in this
 * case, where is only one field, the returned interface ensure that once the
 * expected is defined, it is not possible to modify it. Depending of the type
 * of the field, various methods are generated to define the expectation :
 * <ul>
 * <li>Two standards methods are defined for all type of fields : {@code Matcher
 * <SimplePojo> oneField(Matcher<? super java.lang.String> matcher)} and
 * {@code Matcher<SimplePojo> oneField(String value)}. The second one is a
 * shortcut to validate the field with the {@code is} Matcher and the first one
 * accept another matcher ; The method with matcher parameter ensures that it is
 * possible to combine any other matcher provided by hamcrest or any others
 * extensions.
 * <li>As the field is a String, others special expectation (shortcut) are
 * provided, for example : {@code oneFieldComparesEqualTo},
 * {@code oneFieldLessThan}, {@code oneFieldStartsWith}, etc.</li>
 * </ul>
 * <i>Second example</i>
 * <p>
 * In case the annotated contains several fields, the generated <i>DSL</i>
 * provide chained methods, for example
 * {@code TwoFieldsPojoMatcher firstField(Matcher<? super String> matcher)} and
 * {@code TwoFieldsPojoMatcher secondField(Matcher<? super String> matcher)}.
 * 
 * Also, depending on the class, other <i>with</i> methods may be provided.
 * <p>
 * <i>Usage example</i>
 * <p>
 * Assuming powerunit as a test framework, the usage of the matcher will look
 * like :
 * 
 * <pre>
 * &#64;Test
 * public void testOKMatcherWithComparable() {
 * 	Pojo1 p = new Pojo1();
 * 	p.msg2 = "12";
 * 	assertThat(p).is(Pojo1Matchers.pojo1With().msg2ComparesEqualTo("12"));
 * }
 * 
 * </pre>
 * 
 * Assuming the {@code msg2} is change to the value {@code 11}, the resulting
 * unit test error will look like (the Pojo1 classes contains several fields) :
 * 
 * <pre>
 * expecting an instance of ch.powerunit.extensions.matchers.samples.Pojo1 with
 * [msg2 a value equal to "12"]
 * [msg3 ANYTHING]
 * [msg4 ANYTHING]
 * [msg5 ANYTHING]
 * [msg6 ANYTHING]
 * [msg7 ANYTHING]
 * [msg8 ANYTHING]
 * [msg9 ANYTHING]
 * [msg12 ANYTHING]
 * [msg1 ANYTHING]
 * [myBoolean ANYTHING]
 * [oneBoolean ANYTHING]
 * but [msg2 "11" was less than "12"]
 * 
 * </pre>
 * 
 * <hr>
 * <p>
 * <b>Overriding the way the matchers are generated</b>
 * <ul>
 * <li>The attribute {@link #matchersClassName() matchersClassName} may be used
 * to change the simple name (<b>NOT THE FULLY QUALIFIED NAME</b>) of the
 * generated class.</li>
 * <li>The attribute {@link #matchersPackageName() matchersPackageName} may be
 * used to change the package name of the generated class.</li>
 * </ul>
 * <hr>
 * <p>
 * <b>Extensions</b>
 * <p>
 * The framework, since version 0.1.0, is able to detect others library and use
 * it :
 * <ul>
 * <li>If <a href="https://github.com/exparity/hamcrest-date">Hamcrest Date</a>
 * is available, additional DSL method are added for the Java 8 Date objects.
 * </li>
 * <li>If <a href="https://github.com/NitorCreations/matchers">Hamcrest 1.3
 * Utility Matchers</a> is available, additional DSL method are added for the
 * collections.</li>
 * <li>If <a href="https://github.com/spotify/java-hamcrest">Spotify Hamcrest
 * (jackson)</a> is available and the {@link #JSON_EXTENSION} is used on
 * {@link #extensions()}, then method to validate String as json are added.</li>
 * <li>If <a href="https://github.com/orien/bean-matchers">Bean Matchers</a> is
 * available additional method to validate Class field.</li>
 * </ul>
 * <hr>
 * <p>
 * <b>Linking between matchers</b>
 * <p>
 * The annotation processor try to link the generated matchers. The goal is that
 * when a field or the super class also have a matchers, this one is used. It is
 * not always possible to link everything but the concept is the following :
 * <ul>
 * <li>If the super class is compiled at the same time and is annotated with
 * this annotation, the annotation processor may add a control on the parent and
 * generated chaining method.</li>
 * <li>If a field class is compiled at the same time and is annotated with this
 * annotation, the annotation processor may add chaining and dedicated control
 * that use this matcher.</li>
 * <li>If a matcher, following the convention of this framework is detected for
 * the super class or a field (already compiled for example), the annotation
 * processor may also apply the previous rules by using this detected
 * matcher.</li>
 * <li>The annotation processor may also try to find matcher for element of Map,
 * List, Array.</li>
 * </ul>
 * <hr>
 * <p>
 * <b>Ignore field in <i>WithSameValue</i> matchers</b>
 * <p>
 * Since version 0.3.0, it is possible to pass a list of field names to be
 * ignored when constructing a <i>WithSameValue</i> matcher. The generated
 * matcher tries to also apply in cascade this ignore feature to included
 * object. The syntax to ignore fields of another fields is to use
 * <code>fieldName.fieldNameInside</code>.
 * <p>
 * The generated matcher may not be able to apply the ignore feature in all the
 * included fields of the fields.
 * <hr>
 * <p>
 * <b>Cycle detection</b>
 * <p>
 * Since version 0.3.0, the generated <i>WithSameValue</i> matchers try to
 * detect the cycle in the object to be compared. Cycle may be included for
 * example when working with bidirectional link. The matcher is <b>not</b> able
 * to detect all cycles, but when detected, replace the control by a same
 * instance control.
 * 
 * @author borettim
 *
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.TYPE })
@Inherited
public @interface ProvideMatchers {
	/**
	 * This attribute may be used to override the default class name that will
	 * contains the generated matchers.
	 * <p>
	 * <i>By default, this attribute is an empty string, which indicate to use the
	 * default construction pattern.</i>
	 * <p>
	 * By default, the Matchers class name is the name of the annotated class,
	 * followed by {@code Matchers}.
	 * <p>
	 * <i>Using this attribute may make the matcher undetectable when working with
	 * separated compilation.</i>
	 * 
	 * @return the name of the matchers class or an empty string if this is not
	 *         overloaded.
	 */
	String matchersClassName() default "";

	/**
	 * This attribute may be used to override the default package name that will be
	 * used for the generated matchers.
	 * <p>
	 * <i>By default, this attribute is an empty string, which indicate to use the
	 * default construction pattern.</i>
	 * <p>
	 * By default, the Matchers package name is the same that the annotated class.
	 * <p>
	 * <i>Using this attribute may make the matcher undetectable when working with
	 * separated compilation.</i>
	 * 
	 * @return the name of the matchers package or an empty string if this is not
	 *         overloaded.
	 */
	String matchersPackageName() default "";

	/**
	 * This attribute may be used to set a comments that will be passed inside the
	 * {@link javax.annotation.Generated#comments() comments} attribute of the
	 * {@link javax.annotation.Generated @Generated} annotation.
	 * 
	 * @return the comments or an empty string if ignored.
	 */
	String comments() default "";

	/**
	 * This attribute may be used to generate additional exposition methods for the
	 * object.
	 * <p>
	 * By default, only the standard method are generated.
	 * 
	 * @return additional method to be generated or an empty array by default.
	 * @since 0.1.0
	 */
	ComplementaryExpositionMethod[] moreMethod() default {};

	/**
	 * This attribute may be used to enable some extensions for this objects.
	 * <p>
	 * By default, no extension are enabled.
	 * 
	 * @return the extension to be used or an empty array by default.
	 * @since 0.1.0
	 * @see #JSON_EXTENSION An extension to add a validation on String, as JSON,
	 *      based on the <a href="https://github.com/spotify/java-hamcrest">Spotify
	 *      Matchers</a>.
	 */
	String[] extensions() default {};

	/**
	 * This attribute may be used in case when it is not possible to create a
	 * <i>sure</i> <code>WithSameValue</code> matcher. This may be the case when the
	 * annotated class extends a not annotated class or when the parent class is
	 * already compiled.
	 * <p>
	 * By default, this mode is set to false.
	 * <p>
	 * In case this option is set to true and the <code>WithSameValue</code> matcher
	 * is considered as weak, a warning will be produced.
	 * 
	 * @return to create (or not) a <code>WithSameValue</code> matcher even if it is
	 *         not possible to ensure the control of the field of the parent class.
	 * @since 2.0.0
	 */
	boolean allowWeakWithSameValue() default false;

	/**
	 * May be use as part of {@link #extensions()} to enable, if available, an
	 * extension an each String to be validated by using a json validation.
	 * 
	 * 
	 * @since 0.1.0
	 */
	public static final String JSON_EXTENSION = "json-extension";
	
	/**
	 * May be used to disable generation of the factory.
	 * 
	 * @return true to disable the generation of the factory
	 * @since 0.4.0
	 */
	boolean disableGenerationOfFactory() default false;
}