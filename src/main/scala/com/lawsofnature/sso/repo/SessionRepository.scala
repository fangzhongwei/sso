package com.lawsofnature.sso.repo

import java.sql.Timestamp

import com.lawsofnature.connection.{DBComponent, MySQLDBImpl}
import com.lawsofnature.sso.domain.cache.session.SessionCache

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by fangzhongwei on 2016/11/23.
  */
trait SessionRepository extends Tables {
  this: DBComponent =>

  import profile.api._

  implicit def cacheToRaw(s: SessionCache): TmSessionRow = {
    TmSessionRow(s.token, s.clientId.toByte, s.ip, s.deviceType.toByte, s.fingerPrint, s.status.toByte, s.memberId, s.identity, s.identityTicket, new Timestamp(s.gmtCreate), new Timestamp(s.gmtCreate))
  }

  implicit def rawToCache(s: TmSessionRow): SessionCache = {
    SessionCache(s.token, s.clientId, s.ip, s.deviceType, s.fingerPrint, s.status, s.memberId, s.identity, s.identityTicket, s.gmtCreate.getTime)
  }

  def createSession(sessionCache: SessionCache): Future[Int] = db.run {
    TmSession += sessionCache
  }

  def selectSessionByToken(token: String): Option[SessionCache] = Await.result(db.run {
    TmSession.filter(_.token === token).result.headOption
  }, Duration.Inf) match {
    case Some(session) => Some(session)
    case None => None
  }

  def updateSession(token: String, status: Byte): Unit = db.run {
    TmSession.filter(_.token === token).map(s => s.status).update(status)
  }

  var i = -1

  def getNextSessionId(): Long = {
    i = i + 1
    val sequenceName = "session_id_" + (i % 5)
    Await.result(db.run(sql"""select nextval($sequenceName)""".as[(Long)]), Duration.Inf).head
  }
}

class SessionRepositoryImpl extends SessionRepository with MySQLDBImpl
