package computerdatabase

import io.gatling.commons.stats.{KO, OK}
import io.gatling.core.Predef._
import io.gatling.core.action.{ExitableAction, Action}
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.message.ResponseTimings
import io.gatling.core.structure.ScenarioContext
import io.gatling.core.util.NameGen
import java.io

class S3PutSimulation extends Simulation {

  val scn = scenario("Scenario Name") // A scenario is a chain of requests and pauses
    .exec(new CustomActionBuilder(() => {
    println("***hoge***")
    Thread.sleep((1000 * Math.random()).toInt)
    (OK, Some("ok"))
  }))

  setUp(scn.inject(atOnceUsers(5)))
}

class CustomActionBuilder(func: () => (OK.type, Some[String])) extends ActionBuilder {
  override def build(ctx: ScenarioContext, next: Action): Action = new CustomAction(ctx.coreComponents.statsEngine, func, next)
}

class CustomAction(val statsEngine: StatsEngine, func: () => (OK.type, Some[String]), val next: Action) extends ExitableAction with NameGen {
  override def name: String = genName("Hoge")

  override def execute(session: Session) = {
    val start = System.currentTimeMillis

    val result = try {
      func()
    } catch {
      case _: Throwable => (KO, Some("ng"))
    }


    val end = System.currentTimeMillis
    val timings = ResponseTimings(start, end)
    statsEngine.logResponse(session, name, timings, result._1, result._2, None)
    next ! session
  }
}