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

  def selectSessionByToken(token: String): Future[Option[TmSessionRow]] = db.run {
    TmSession.filter(_.token === token).result.headOption
  }

  def updateSession(token: String, status: Byte): Unit = db.run {
    TmSession.filter(_.token === token).map(s => s.status).update(status)
  }

  var i = -1

  def getNextSessionId(): Future[Seq[(Long)]] = {
    i = i + 1
    val sequenceName = "session_id_" + (i % 5)
    db.run(sql"""select nextval($sequenceName)""".as[(Long)])
  }
}

class SessionRepositoryImpl extends SessionRepository with MySQLDBImpl
