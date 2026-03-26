package me.bossm0n5t3r.delegation.standard

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class InterfaceDelegationTest {

    @Test
    fun `Animal with DogSound should bark`() {
        val dog = Animal(DogSound())
        assertEquals("Woof", dog.make())
    }

    @Test
    fun `Animal with CatSound should meow`() {
        val cat = Animal(CatSound())
        assertEquals("Meow", cat.make())
    }

    @Test
    fun `Animal can override delegated methods`() {
        class SilentAnimal(sound: SoundSource) : SoundSource by sound {
            override fun make() = "..."
        }

        val silentDog = SilentAnimal(DogSound())
        assertEquals("...", silentDog.make())
    }
}
