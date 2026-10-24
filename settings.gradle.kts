rootProject.name = "modl-minecraft"

include("api")
include("core")
include("bridge-core")
include("platforms:spigot")
include("platforms:spigot-sv")
include("platforms:velocity")
include("distribution")

includeBuild("build-logic")