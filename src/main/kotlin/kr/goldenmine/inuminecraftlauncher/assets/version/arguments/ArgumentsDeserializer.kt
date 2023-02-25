package kr.goldenmine.inuminecraftlauncher.assets.version.arguments

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class ArgumentsDeserializer : JsonDeserializer<Arguments> {
    private val jvmArgumentRulesType = object : TypeToken<Argument>() {}.type

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Arguments {
        val gameObjects = json
            .asJsonObject
            .get("game")
            .asJsonArray
            .map {
                if(it.isJsonPrimitive) {
                    it.asString
                } else {
                    context.deserialize<Argument>(it, jvmArgumentRulesType)
                }
            }

        val jvmObjects = json
            .asJsonObject
            .get("jvm")
            .asJsonArray
            .map {
                if(it.isJsonPrimitive) {
                    it.asString
                } else {
                    context.deserialize<Argument>(it, jvmArgumentRulesType)
                }
            }

        return Arguments(gameObjects, jvmObjects)
    }
}