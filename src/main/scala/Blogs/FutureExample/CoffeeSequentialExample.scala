package Blogs.FutureExample

import scala.util.Try

/**
  * Neophyte part 8: https://danielwestheide.com/blog/2013/01/09/the-neophytes-guide-to-scala-part-8-welcome-to-the-future.html
  */
object CoffeeSequentialExample {

	type CoffeeBeans = String
	type GroundCoffee = String

	case class Water(temperature: Int)
	type Milk = String
	type FrothedMilk = String
	type Espresso = String
	type Cappuccino = String

	// dummy implementations of the individual steps:
	def grind(beans: CoffeeBeans): GroundCoffee = s"ground coffee of $beans"
	def heatWater(water: Water): Water = water.copy(temperature = 85)
	def frothMilk(milk: Milk): FrothedMilk = s"frothed $milk"
	def brew(coffee: GroundCoffee, heatedWater: Water): Espresso = "espresso"
	def combine(espresso: Espresso, frothedMilk: FrothedMilk): Cappuccino = "cappuccino"

	// some exceptions for things that might go wrong in the individual steps
	// (we'll need some of them later, use the others when experimenting
	// with the code):
	case class GrindingException(msg: String) extends Exception(msg)
	case class FrothingException(msg: String) extends Exception(msg)
	case class WaterBoilingException(msg: String) extends Exception(msg)
	case class BrewingException(msg: String) extends Exception(msg)
	// going through these steps sequentially:

	/**
	  * Disadvantage of doing steps sequentially: we are waiting in between each step.
	  * Blocked by each step. One step at a time only. This is waste of resources -
	  * may want to initiate multiple steps and have them execute concurrently.
	  *
	  * Once you see that the water and the ground coffee is ready, you’d start
	  * brewing the espresso, in the meantime already starting the process of
	  * frothing the milk.
	  */
	def prepareCappuccino(): Try[Cappuccino] = for {
		ground <- Try(grind("arabica beans"))
		water <- Try(heatWater(Water(25)))
		espresso <- Try(brew(ground, water))
		foam <- Try(frothMilk("milk"))
	} yield combine(espresso, foam)

}
