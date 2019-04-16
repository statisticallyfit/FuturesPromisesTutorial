package Blogs.FutureExample

/**
  *https://danielwestheide.com/blog/2013/01/09/the-neophytes-guide-to-scala-part-8-welcome-to-the-future.html
  */

import Blogs.FutureExample.CoffeeSequentialExample.GrindingException

import scala.concurrent.future
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random
import scala.util.{Success, Failure}


/**
  * Future[T] is a container type, representing a computation that is supposed to
  * eventually result in a value of type T. The ending completed result may be
  *  a fail (exception) or success.
  *
  *  Future is immutable once it is completed. Interface only allows you to read
  *  the value that has been completed.
  *  Task of writing the computed value is achieved via a Promise.
  */

object CoffeeFutureExample {

	type CoffeeBeans = String
	type GroundCoffee = String

	case class Water(temperature: Int)
	type Milk = String
	type FrothedMilk = String
	type Espresso = String
	type Cappuccino = String

	def grind(beans: CoffeeBeans): Future[GroundCoffee] = Future {
		println("start grinding ...")
		Thread.sleep(Random.nextInt(100)) //putting in random sleep to make it loook like we are computing
		// something
		if(beans == "baked beans") throw GrindingException(s"Got $beans: Are you joking?")
		println("finished grinding ...")

		s"ground coffee of $beans" //thing in the Future container
	}

	def heatWater(water: Water): Future[Water] = Future {
		println("heating the water now")
		Thread.sleep(Random.nextInt(100))
		println("hot, it's hot!")

		water.copy(temperature = 85) //object inside the Future container
	}

	def checkTemperature(water: Water): Future[Boolean] = Future {
		(80 to 85).contains(water.temperature)
	}

	def frothMilk(milk: Milk): Future[FrothedMilk] = Future {
		println("milk frothing system engaged!")
		Thread.sleep(Random.nextInt(100))
		println("shutting down milk frothing system")

		s"frothed milk"
	}

	def brew(coffee: GroundCoffee, heatedWater: Water): Future[Espresso] = Future {
		println("happy brewing :)")
		Thread.sleep(Random.nextInt(100))
		println("it's brewed!")

		"espresso"
	}


	def combine(espresso: Espresso, frothedMilk: FrothedMilk): Cappuccino = "cappuccino"

	//NOTE: apply method requires two arguments:
	/**
	  * body = the computation to be computed asynchronously is passed in as the body parameter
	  * exectx = implicit execution context
	  *
	  * ExecutionContext = something that can execute our future, is available implicitly.
	  *
	  * note: using curly braces instead of paranetheses invokes the Future apply method. to hide the fact
	  * that we are calling an ordinary apply method.
	  */
	/*object Future {
		def apply[T](body: => T)(implicit execctx: ExecutionContext): Future[T]
	}*/


}



object Wait {
	def hangOn[T](future: Future[T], start: Int = 0) = {
		var count = start
		while(!future.isCompleted){
			println(s"Slept $count times")

			Thread.sleep(5)
			count += 1
		}

		/*future.onComplete{
			case s@Success(_) => println(s"$s")
			case f@Failure(_) => println(s"$f")
		}*/
		println(future)

		future
	}
}

object GrindUseCase extends App {

	import CoffeeFutureExample._

	//Example of a callback: what happens if Future completes successfully
	/*grind("arabica beans").onSuccess{
		case groundMsg => println(s"okay got the ground coffee msg: $groundMsg")
	}*/
	println("First coffee!")

	val futureGrind: Future[GroundCoffee] = grind("arabica beans")
	Wait.hangOn(futureGrind)

	// ------------------------------

	println("Second coffee!")

	val futureGrind2: Future[GroundCoffee] = grind("baked beans")
	Wait.hangOn(futureGrind2)

}


object WaterTempUseCase extends App {
	import CoffeeFutureExample._

	val hot: Future[Water] = heatWater(Water(25))

	Wait.hangOn(hot)

	//check water temp is ok after heating
	val temperatureOkay: Future[Boolean] = hot.map{ water =>
		println("we're in the (possible) future!")
		(80 to 85).contains(water.temperature)
	}

	Wait.hangOn(temperatureOkay)

}

object FlatMapFuture extends App {
	import CoffeeFutureExample._


	val hot: Future[Water] = heatWater(Water(25))
	Wait.hangOn(hot)

	val nestedFuture: Future[Future[Boolean]] = hot.map { water =>
		//Wait.hangOn(checkTemperature(water))
		checkTemperature(water)
	}
	Wait.hangOn(nestedFuture)
	//help question: how come when hangOn() has shorter milliseconds, we get the not completed
	//help future for nestedFuture in the console? How come when increasing the miillis time
	// help that the nestedFuture completes?

	val flatFuture: Future[Boolean] = hot.flatMap(water => checkTemperature(water))
	Wait.hangOn(flatFuture)
}



object ForComprehensionFuture extends App {

	import CoffeeFutureExample._

	val acceptable: Future[Boolean] = for {
		hot <- heatWater(Water(25))
		okay <- checkTemperature(hot)
	} yield okay

	Wait.hangOn(acceptable)
	//todo: do we need to wrap each future in the for loop with the hangOn()?

	//println(acceptable)
}

object ForComprehensionFuture_Capp extends App {

	import CoffeeFutureExample._


	// --------- Cappuccino Loop ---------

	def prepareCapuccinoSequentially(): Future[Cappuccino] = {
		for {
			ground <- grind("arabica beans")
			hotWater <- heatWater(Water(20))
			foam <- frothMilk("milk")
			espresso <- brew(ground, hotWater)
		} yield combine(espresso, foam)
	}

	val cap: Future[Cappuccino] = prepareCapuccinoSequentially()
	Wait.hangOn(cap)
}