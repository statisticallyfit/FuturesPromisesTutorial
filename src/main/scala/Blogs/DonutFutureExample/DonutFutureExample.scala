package Blogs.DonutFutureExample

/**
  *http://allaboutscala.com/tutorials/chapter-9-beginner-tutorial-using-scala-futures/#futures-introduction
  */


import scala.concurrent.ExecutionContext.Implicits.global //places default thread pool in scope so Future can be
// executed asynchronously.
import scala.concurrent.{Await, Future, Promise, future}
import scala.concurrent.duration._
import scala.util.Random
import scala.util.{Success, Failure}


import util.Wait

object DonutFutureExample extends App {

	case class Donut(name: String, howMany: Int)


}


object Blocking extends App {
	import DonutFutureExample._

	def donutStock(donut: String, count: Int ): Future[Donut] = Future {
		//long running database operation
		Thread.sleep(100)
		println("checking donut stock")
		Donut(donut, count)
	}

	val futureDonut: Future[Donut] = donutStock("vanilla donut", 10)
	val vanillaDonutStock = Await.result(futureDonut, 5 seconds)

	println(futureDonut)
	println(vanillaDonutStock)
}


object NonBlocking extends App {
	import DonutFutureExample._

	import scala.util.{Success, Failure}


	def donutStock(donut: String, count: Int ): Future[Donut] = Future {
		//long running database operation
		Thread.sleep(100)
		println("checking donut stock")
		Donut(donut, count)
	}

	val futureDonut: Future[Donut] = donutStock("vanilla donut", 10)

	//note: this does not count as blocking the Future since the Success/Failure stand
	// as callbacks for the Future.
	futureDonut.onComplete {
		case Success(stock) => println(s"Stock for vanilla donut = $stock")
		case Failure(e) => println(s"Failed to find vanilla donut stock, ${e.getMessage}")
	}

	Thread.sleep(3000) // not done in real life.

}


object FlatMapFuture extends App {
	import DonutFutureExample._


	def donutStock(donut: String, count: Int ): Future[Donut] = Future {
		//long running database operation
		Thread.sleep(100)
		println("checking donut stock")
		Donut(donut, count)
	}

	def buyDonuts(quantity: Int): Future[Boolean] = Future {
		println(s"buying $quantity donuts")
		true
	}


	val donut: Future[Donut] = donutStock("plain donut", 10)
	//Example of chaining futures using flatmap
	val willBuy: Future[Boolean] = donut.flatMap(d => buyDonuts(d.howMany))

	Wait.hangOnS(willBuy)
}




object ForComprehensionFuture extends App {

	import DonutFutureExample._

	def donutStock(donut: String, count: Int ): Future[Donut] = Future {
		//long running database operation
		Thread.sleep(100)
		println("checking donut stock")
		Donut(donut, count)
	}

	def buyDonuts(quantity: Int): Future[Boolean] = Future {
		println(s"buying $quantity donuts")
		true
	}


	val result: Future[Boolean] = for {
		stock <- donutStock("plain donut", 10)
		isSuccess <- buyDonuts(stock.howMany)
	} yield isSuccess //println(s"Buying ${stock.name} was successful = $isSuccess")

	Wait.hangOnS(result)
}


/**
  * Better deal with Future options using monad transformers, for now use for compr
  */
object ForComprehensionFutureOption extends App {

	import DonutFutureExample._

	def donutStock(donutName: String, count: Int ): Future[Option[Donut]] = Future {
		//long running database operation
		Thread.sleep(100)
		println("checking donut stock")

		count match {
			case 0 => None
			case n => Some(Donut(donutName, n))
		}
	}

	def buyDonuts(quantity: Int): Future[Boolean] = Future {
		println(s"buying $quantity donuts")

		if(quantity > 0) true else false
	}

	def from(op: Option[Donut]): Int = op match {
		case Some(d) => d.howMany
		case None => 0
	}


	val result: Future[Boolean] = for {
		optionStock <- donutStock("plain donut", 0)
		isSuccess <- buyDonuts(from(optionStock))
	} yield isSuccess //println(s"Buying ${stock.name} was successful = $isSuccess")

	Wait.hangOnS(result)
	println(result)
}




object MapAndFlatMapFutureOption extends App {

	import DonutFutureExample._

	def donutStock(donutName: String, count: Int ): Future[Option[Donut]] = Future {
		//long running database operation
		Thread.sleep(100)
		println("checking donut stock")

		count match {
			case 0 => None
			case n => Some(Donut(donutName, n))
		}
	}

	def buyDonuts(quantity: Int): Future[Boolean] = Future {
		println(s"buying $quantity donuts")

		if(quantity > 0) true else false
	}

	def from(op: Option[Donut]): Int = op match {
		case Some(d) => d.howMany
		case None => 0
	}

	val donutFuture: Future[Option[Donut]] = donutStock("plain donut", 10)
	Wait.hangOnS(donutFuture)

	println("nested")
	val nested: Future[Future[Boolean]] = donutFuture.map(optionDonut => Wait.hangOn(  buyDonuts(from(optionDonut))  ))
	Wait.hangOnS(nested)


	println("flat")
	val flat: Future[Boolean] = donutFuture.flatMap(optionDonut => Wait.hangOn(  buyDonuts(from(optionDonut))  ))
	Wait.hangOnS(flat)
}


/**
  * Waiting for consecutive results using Sequence
  */
object SequenceFutureOption extends App {

	import DonutFutureExample._

	def donutStock(donutName: String, count: Int ): Future[Option[Donut]] = Future {
		//long running database operation
		Thread.sleep(100)
		println("- checking donut stock ... sleep for a bit")

		count match {
			case 0 => None
			case n => Some(Donut(donutName, n))
		}
	}

	def buyDonuts(quantity: Int): Future[Boolean] = Future {
		println(s"- buying $quantity donuts ... sleep for a bit")

		if(quantity > 0) true else false
	}

	//just to add more complexity
	def processPayment(): Future[Unit] = Future {
		println("- process payment ... sleep for second")
		Thread.sleep(100)
	}




	val futureOperations: List[Future[Any]] = List(
		donutStock("glazed donut", 10),
		buyDonuts(4),
		processPayment()
	)
	futureOperations.map(f => Wait.hangOn(f)) // wait for each of our future values

	// Call future sequence to run the future operations in parllel
	val seq: Future[List[Any]] = Future.sequence(futureOperations)
	Wait.hangOn(seq) //wait for future that is sequence

	println(seq)

	seq.onComplete {
		case Success(results) => println(s"Results $results")
		case Failure(exception) => println(s"Error, ${exception.getMessage}")
	}
}



//Traverse is like sequence but allows you to apply function over the
// future operations
object TraverseFutureOption extends App {

	import DonutFutureExample._

	def donutStock(donutName: String, count: Int ): Future[Option[Donut]] = Future {
		//long running database operation
		Thread.sleep(100)
		println("- checking donut stock ... sleep for a bit")

		count match {
			case 0 => None
			case n => Some(Donut(donutName, n))
		}
	}

	def from(op: Any): Int = {
		op match {
			case Some(d) => d match {
				case Donut(_, q) => q
				case _ => 0 //failsafe 2
			}
			case None => 0
			case _ => 0 //the "any" case, failsafe 1
		}
	}



	val futureOperations: List[Future[Any]] = List(
		donutStock("glazed donut", 10),
		donutStock("sprinkles donut", 1),
		donutStock("chocolate donut", 2)
	)
	futureOperations.map(f => Wait.hangOn(f))


	// Call future traverse
	val trav: Future[List[Int]] = Future.traverse(futureOperations){ optionFuture =>
		optionFuture.map(optionDonut => from(optionDonut))
	}
	Wait.hangOn(trav) //why isn't the hangOnS printing part not working?

	println(trav)

	trav.onComplete {
		case Success(results) => println(s"Results $results")
		case Failure(e) => println(s"Error processing, ${e.getMessage}")
	}
}




//Do foldleft asynchronously from left to right
object FoldLeftFutureOption extends App {

	import DonutFutureExample._

	def donutStock(donutName: String, count: Int ): Future[Option[Donut]] = Future {
		//long running database operation
		Thread.sleep(100)
		println("- checking donut stock ... sleep for a bit")

		count match {
			case 0 => None
			case n => Some(Donut(donutName, n))
		}
	}

	def from(op: Any): Int = {
		op match {
			case Some(d) => d match {
				case Donut(_, q) => q
				case _ => 0 //failsafe 2
			}
			case None => 0
			case _ => 0 //the "any" case, failsafe 1
		}
	}



	val futureOperations: List[Future[Any]] = List(
		donutStock("strawberry vanilla donut", 4),
		donutStock("glazed donut", 10),
		donutStock("sprinkles donut", 1),
		donutStock("chocolate donut", 2)
	)
	futureOperations.map(f => Wait.hangOn(f))


	val foldleft = Future.foldLeft(futureOperations)(0)(
		(accSum, optionDonut) => accSum + from(optionDonut)
	)

	Wait.hangOn(foldleft) //why isn't the hangOnS printing part not working?

	println(foldleft)

	foldleft.onComplete {
		case Success(results) => println(s"Results $results")
		case Failure(e) => println(s"Error processing, ${e.getMessage}")
	}
}