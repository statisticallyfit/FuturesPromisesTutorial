package Blogs.TaxPromiseExample

/**
  * https://danielwestheide.com/blog/2013/01/16/the-neophytes-guide-to-scala-part-9-promises-and-futures-in-practice.html
  */


import scala.concurrent.future
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random
import scala.util.{Success, Failure}


import util.Wait



object TaxPromiseExample {

	/**
	  * Way 1 to create a future: fill in the apply method like in the coffee example
	  * Way 2 to create a future: Promise can complete a Future by putting a value into it (ONLY ONCE)
	  * and then Promise that is completed becomes immutable.
	  *
	  * note: promise instance is linked to one instance of Future.
	  */

	case class TaxCut(reduction: Int)

}

object Ex1 extends App {
	import TaxPromiseExample._

	//either give the type as a type parameter to the factory method:
	val taxcutPromise = Promise[TaxCut]()
	println(taxcutPromise)

	// or give compiler a hint by specifying the type of your val:
	val taxcut2: Promise[TaxCut] = Promise()
	println(taxcut2)

	//Can get the future from the promise by calling .future on the promise instance
	val taxcutFuture: Future[TaxCut] = taxcutPromise.future
	println(taxcutFuture)

}

object SuccessPromiseUseCase extends App {
	import TaxPromiseExample._


	object Government {
		def redeemCampaignPledge(): Future[TaxCut] = {
			val p = Promise[TaxCut]()
			Future {
				println("Starting the new legislative period.")
				Thread.sleep(100)

				//This is how to complete the promise with a success.
				//Now the promise is immutable.
				p.success(TaxCut(20))

				println("We reduced the taxes! You must elect us!")
			}
			p.future
		}
	}


	val taxCutFuture: Future[TaxCut] = Government.redeemCampaignPledge()

	println("Now that they're elected, let's see if they remember their promise.")

	//Wait.hangOn(taxCutFuture)


	taxCutFuture.onComplete{
		case Success(TaxCut(reduction)) => println(s"Miracle! They cut taxes by $reduction percentage.")
		case Failure(exception) => println(s"Liars! ${exception.getMessage}")
	}
}


object BreakPromiseUseCase extends App {
	import TaxPromiseExample._

	case class LameExcuse(msg: String) extends Exception(msg)

	object Government {
		def redeemCampaignPledge(): Future[TaxCut] = {
			val p = Promise[TaxCut]()
			Future {
				println("Starting the new legislative period.")
				Thread.sleep(100)

				//This is how to complete the promise with a success.
				//Now the promise is immutable.
				//p.success(TaxCut(20))
				p.failure(LameExcuse("global economy crisis"))

				println("We didn't fulfill our promises, but surely they'll understand.")
			}
			p.future
		}
	}


	val taxCutFuture: Future[TaxCut] = Government.redeemCampaignPledge()

	println("Now that they're elected, let's see if they remember their promise.")

	Wait.hangOn(taxCutFuture)

	taxCutFuture.onComplete{
		case Success(TaxCut(reduction)) => println(s"Miracle! They cut taxes by $reduction percentage.")
		case Failure(exception) => println(s"Liars! ${exception.getMessage}")
	}
}
