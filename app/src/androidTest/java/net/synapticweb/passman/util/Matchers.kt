package net.synapticweb.passman.util

import android.os.IBinder
import android.view.View
import android.view.WindowManager
import androidx.test.espresso.Root
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher


//https://stackoverflow.com/a/34286462
fun hasTextInputLayoutErrorText(expectedErrorText: String): Matcher<View?>? {
    return object : TypeSafeMatcher<View?>() {

        override fun matchesSafely(view: View?): Boolean {
            if (view !is TextInputLayout) {
                return false
            }
            val error = view.error ?: return false
            val hint = error.toString()
            return expectedErrorText == hint
        }

        override fun describeTo(description: Description?) {}
    }
}

//https://stackoverflow.com/a/33387980
fun isToast(): Matcher<Root?>? {
    return object : TypeSafeMatcher<Root>() {
        override fun describeTo(description: Description) {
            description.appendText("is toast")
        }

        override fun matchesSafely(root: Root): Boolean {
            val type: Int = root.windowLayoutParams.get().type
            if (type == WindowManager.LayoutParams.TYPE_TOAST) {
                val windowToken: IBinder = root.decorView.windowToken
                val appToken: IBinder = root.decorView.applicationWindowToken
                if (windowToken === appToken) {
                    // windowToken == appToken means this window isn't contained by any other windows.
                    // if it was a window for an activity, it would have TYPE_BASE_APPLICATION.
                    return true
                }
            }
            return false
        }
    }
}