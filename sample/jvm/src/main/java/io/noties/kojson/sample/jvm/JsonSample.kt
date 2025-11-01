package io.noties.kojson.sample.jvm

import io.noties.kojson.api.Json

object JsonSample {
    data class MyDataClass(
        val id: String,
        val name: String,
        val description: String?,
        val age: Int?,
        val hasAddon: Boolean
    ) {
        companion object {
            // can decide what to do:
            //  - to throw an exception when required fields are not found
            //  - return null
            fun fromJson(json: Json): MyDataClass {
                // from our requirements, id is a must, so we throw an error
                val id = json["id"].string ?: error("`id` is missing in:$json")
                // and name is also required
                val name = json["name"].string ?: error("`name` is missing in:$json")
                // but description is optional (like the rest of the fields)
                return MyDataClass(
                    id = id,
                    name = name,
                    description = json["description"].string,
                    age = json["age"].int,
                    // boolean is not very convenient to have as null,
                    //  try to limit its usage to true/false, for example
                    //  by calling `booleanValue` we will always receive a boolean and nothing else
                    //  internally it tries to fallback to different known usages,
                    //  like sending "1" for true, or "true" as quoted string. so, after
                    //  even all checks are failed a default value will be returned
                    hasAddon = json["hasAddon"].booleanValue
                )
            }

            fun dynamicOrUnStableApi(
                json: Json,
                verifyId: (String) -> Boolean
            ) {
                data class Another(
                    val id: String,
                    val name: String,
                    val description: String?
                )

                val id = json["id"].stringValue
                    .takeIf { it.isNotEmpty() }
                    ?.takeIf(verifyId)
                    ?: error("required `id` is missing")

                // sometimes due to instability of responses, it is convenient to
                //  use `value` for optional fields along with additional local verification
                //  so, for example, empty string can be always considered absent value
                val another = Another(
                    id = id,
                    name = json["name"].stringValue,
                    // by using `*Value` suffixed property all json-strings
                    //  will be returned if they exists, but additionally all json-primitives
                    //  would be converted to a string (1 => "1"), in other cases
                    //  it would return empty string
                    description = json["description"].stringValue.takeIf { it.isNotEmpty() }
                )
            }
        }
    }
}
