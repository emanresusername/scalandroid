package my.will.be.done.scalauto

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

trait Delayer {
  implicit val executionContext: ExecutionContext
  type Operation[O] = () ⇒ Future[O]

  def delay[O](before: Duration,
               after: Duration,
               operation: Operation[O]): Future[O]
  def delayAfter[O](duration: Duration, operation: Operation[O]): Future[O] = {
    delay(before = Duration.Zero, after = duration, operation)
  }
  def delayBefore[O](duration: Duration, operation: Operation[O]): Future[O] = {
    delay(before = duration, after = Duration.Zero, operation)
  }
  def delayBetween[O](
      duration: Duration,
      operations: Seq[Operation[O]],
      initialDelay: Duration = Duration.Zero): Future[Seq[O]] = {
    val (first, rest) = operations.splitAt(1)
    rest.foldLeft(Future.traverse(first)(op ⇒ delayBefore(initialDelay, op))) {
      (futureOs, operation) ⇒
        for {
          os ← futureOs
          o  ← delayBefore(duration, operation)
        } yield {
          os :+ o
        }
    }
  }
}
