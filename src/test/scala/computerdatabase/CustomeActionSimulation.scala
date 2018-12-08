package computerdatabase

import io.gatling.commons.stats.{KO, OK}
import io.gatling.core.Predef._
import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.message.ResponseTimings
import io.gatling.core.structure.ScenarioContext
import io.gatling.core.util.NameGen
import java.util.concurrent.atomic.AtomicInteger

class CustomeActionSimulation extends Simulation {
  var counter = new AtomicInteger

  val scn = scenario("Scenario Name")
    .exec(
      new CustomActionBuilder(
        "hoge",
        () => {
          val num = counter.getAndAdd(1).toInt
          println(s"***hoge$num start***")
          Thread.sleep((3000 * Math.random()).toInt)
          println(s"***hoge$num end***")
          OK
        }
      )
    )

  setUp(scn.inject(constantUsersPerSec(5) during (5)))
}

class CustomActionBuilder(name: String, func: () => Status)
    extends ActionBuilder {
  override def build(ctx: ScenarioContext, next: Action): Action =
    new CustomAction(name, ctx.coreComponents.statsEngine, func, next)
}

class CustomAction(val name: String,
                   val statsEngine: StatsEngine,
                   func: () => Status,
                   val next: Action)
    extends Action {

  override def execute(session: Session): Unit = {
    val start = System.currentTimeMillis

    val status = try {
      func()
    } catch {
      case _: Throwable => KO
    }

    val end = System.currentTimeMillis
    statsEngine.logResponse(session, name, start, end, status, None, None)
    next ! session
  }
}
