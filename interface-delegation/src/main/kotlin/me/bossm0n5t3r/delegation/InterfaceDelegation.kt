package me.bossm0n5t3r.delegation

interface SoundSource {
    fun make() : String
}

class DogSound : SoundSource {
    override fun make() = "Woof"
}

class CatSound : SoundSource {
    override fun make() = "Meow"
}

class Animal(sound: SoundSource) : SoundSource by sound

fun main() {
    val dog = Animal(DogSound())
    println(dog.make()) // Woof

    val cat = Animal(CatSound())
    println(cat.make()) // Meow
}
