
To setup it up as a kotlin multiplatform library is very complicated. Random things just stop working.
Not sure what causing it too - maybe adding `kotlin.test.*` to commonMain has a special meaning to KMP?
Because it fails with unresolved reference to `@Test` annotation. Then it is not clear if they even
going to run the tests defined in a shared/library module

So, instead of trying (it does not mean I didn't try, on the contrary) to make it work as
a gradle (meh) module, it is just sources that other modules can include. More or less
normal modules work, but if you create a KMP test module, it is not being included, nothing is working.

Kotlin (MP) is very time consuming.