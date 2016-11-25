package com.lawsofnature.sso.repo

import com.lawsofnature.connection.{DBComponent, MySQLDBImpl}

import scala.concurrent.Future

/**
  * Created by fangzhongwei on 2016/11/23.
  */
trait SessionRepository extends Tables {
  this: DBComponent =>

  import profile.api._

  def createSession(tmSessionRow: TmSessionRow): Future[Int] = db.run {
    TmSession += tmSessionRow
  }

  def selectSessionByToken(token: String):Future[Option[TmSessionRow]] = db.run{
    TmSession.filter(_.token === token).result.headOption
  }

  def getNextSessionId():Future[Seq[(Long)]]={
    val tab = System.currentTimeMillis() % 5
    val sequenceName = "session_id_" + tab
    db.run(sql"""select nextval($sequenceName)""".as[(Long)])
  }
}

class SessionRepositoryImpl extends SessionRepository with MySQLDBImpl
