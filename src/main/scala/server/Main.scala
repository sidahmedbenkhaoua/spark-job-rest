package server

import akka.event.Logging
import server.domain.actors.{Supervisor, JobActor, ContextManagerActor}

import scala.concurrent.Await
import akka.actor.ActorRef
import akka.pattern.ask

import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory
import server.domain.actors.timeout

/**
 * Created by raduc on 29/10/14.
 */
object Main {
  def main(args: Array[String]) {

    val defaultConfig = ConfigFactory.load()
    val masterConfig = defaultConfig.getConfig("manager")
    val system = ActorSystem("ManagerSystem", masterConfig)

    val supervisor = system.actorOf(Props(classOf[Supervisor]), "Supervisor")

    val contextManagerActor = createActor(Props(new ContextManagerActor(defaultConfig)), "ContextManager", system, supervisor)
    val jobManagerActor = createActor(Props(new JobActor(defaultConfig, contextManagerActor)), "JobManager", system, supervisor)
    val controller = new Controller(defaultConfig, contextManagerActor, jobManagerActor, system)

  }

  def createActor(props: Props, name: String, customSystem: ActorSystem, supervisor: ActorRef): ActorRef = {
    val actorRefFuture = ask(supervisor, (props, name))
    Await.result(actorRefFuture, timeout.duration).asInstanceOf[ActorRef]
  }
}
