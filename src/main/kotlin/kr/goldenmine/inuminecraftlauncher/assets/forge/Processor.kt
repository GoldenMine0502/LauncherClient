package kr.goldenmine.inuminecraftlauncher.assets.forge

import com.google.gson.annotations.SerializedName

//        private List<String> sides;
//        private Artifact jar;
//        private Artifact[] classpath;
//        private String[] args;
//        private Map<String, String> outputs;

class Processor(
    @SerializedName("sides") val sides: List<String>,
    @SerializedName("jar") val jar: String,
    @SerializedName("classpath") val classpath: List<String>,
    @SerializedName("args") val args: List<String>,
    @SerializedName("outputs") val outputs: Map<String, String>?,
) {
}