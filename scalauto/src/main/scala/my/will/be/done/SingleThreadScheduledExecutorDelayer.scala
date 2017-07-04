package my.will.be.done.scalauto

import scala.concurrent.duration.Duration
import java.util.concurrent.Executors
import scala.concurrent.{Future, Promise}

trait SingleThreadScheduledExecutorDelayer extends Delayer {
  val scheduler = Executors.newSingleThreadScheduledExecutor

  def thread(runImpl: ⇒ Unit): Thread = {
    new Thread {
      override def run {
        runImpl
      }
    }
  }

  override def delay[O](before: Duration,
                        after: Duration,
                        operation: Operation[O]): Future[O] = {
    val promise = Promise[O]()
    scheduler.schedule(thread {
      operation().map { o ⇒
        scheduler.schedule(thread {
          promise.success(o)
        }, after.length, after.unit)
      }
    }, before.length, before.unit)
    promise.future
  }
}
