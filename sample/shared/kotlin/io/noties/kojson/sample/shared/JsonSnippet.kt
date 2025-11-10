package io.noties.kojson.sample.shared

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class JsonSnippet(
    val originalFileName: String = ""
)

// only one allowed @JsonSnippet annotation
// obtain imports to understand which api/components are used (automatically)
// check by file hash if it has changed
// provide description + title + possible tags?
// remove from the store what was not found + add new
// date added/changed
//  actually, how can we identify it?

// id by file name
//  if changed -> check the `originalFileName` to match