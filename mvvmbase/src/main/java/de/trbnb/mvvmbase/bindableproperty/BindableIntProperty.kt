package de.trbnb.mvvmbase.bindableproperty

import android.databinding.BaseObservable
import de.trbnb.mvvmbase.BR
import de.trbnb.mvvmbase.BaseViewModel
import kotlin.reflect.KProperty

/**
 * Delegate property that invokes [BaseObservable.notifyPropertyChanged] after a value is set.
 * The getter is not affected.
 *
 * @param fieldId ID of the field as in the BR.java file. A `null` value will cause automatic detection of that field ID.
 * @param defaultValue Value that will be used at start.
 */
class BindableIntProperty<R : BaseViewModel> (
        private var fieldId: Int?,
        defaultValue: Int
) : BindablePropertyBase() {

    override val isBoolean = false

    /**
     * Gets or sets the stored value.
     */
    private var value = defaultValue


    /**
     * Gets or sets a function that will be invoked if a new value is about to be set.
     * The first parameter is the old value and the second parameter is the new value.
     *
     * This function will not be invoked if [BindablePropertyBase.distinct] is true and the new value
     * is equal to the old value.
     */
    internal var beforeSet: (R.(Int, Int) -> Unit)? = null

    /**
     * Gets or sets a function that will validate a newly set value.
     * The first parameter is the old value and the second parameter is the new value.
     * The returned value will be the new stored value.
     *
     * If this function is null validation will not happen and the new value will simply be set.
     */
    internal var validate: (R.(Int, Int) -> Int)? = null

    /**
     * Gets or sets a function that will be invoked if a new value was set and
     * [BaseObservable.notifyPropertyChanged] was invoked.
     * The first parameter is the old value and the second parameter is the new value.
     */
    internal var afterSet: (R.(Int) -> Unit)? = null

    operator fun getValue(thisRef: R, property: KProperty<*>) = value

    operator fun setValue(thisRef: R, property: KProperty<*>, value: Int) {
        if (fieldId == null) {
            fieldId = resolveFieldId(property)
        }

        if (distinct && this.value == value) {
            return
        }

        beforeSet?.invoke(thisRef, this.value, value)
        this.value = validate?.invoke(thisRef, this.value, value) ?: value
        thisRef.notifyPropertyChanged(fieldId ?: BR._all)
        afterSet?.invoke(thisRef, this.value)
    }
}

/**
 * Creates a new [BindableIntProperty] instance.
 *
 * @param defaultValue Value of the property from the start.
 * @param fieldId ID of the field as in the BR.java file. A `null` value will cause automatic detection of that field ID.
 */
fun <R : BaseViewModel> R.bindableInt(defaultValue: Int = 0, fieldId: Int? = null): BindableIntProperty<R> {
    return BindableIntProperty(fieldId, defaultValue)
}

/**
 * Sets [BindableIntProperty.beforeSet] of a [BindableIntProperty] instance to a given function and
 * returns that instance.
 */
fun <R : BaseViewModel> BindableIntProperty<R>.beforeSet(action: R.(Int, Int) -> Unit) = apply { beforeSet = action }

/**
 * Sets [BindableIntProperty.validate] of a [BindableIntProperty] instance to a given function and
 * returns that instance.
 */
fun <R : BaseViewModel> BindableIntProperty<R>.validate(action: R.(Int, Int) -> Int) = apply { validate = action }

/**
 * Sets [BindableIntProperty.afterSet] of a [BindableIntProperty] instance to a given function and
 * returns that instance.
 */
fun <R : BaseViewModel> BindableIntProperty<R>.afterSet(action: R.(Int) -> Unit) = apply { afterSet = action }
