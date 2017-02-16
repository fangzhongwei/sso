package com.jxjxgo.sso.repo

import com.jxjxgo.mysql.connection.DBImpl

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables extends DBImpl {

  import profile.api._

  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** Entity class storing rows of table TmSession
    *
    * @param token          Database column token SqlType(varchar), PrimaryKey, Length(32,true)
    * @param clientId       Database column client_id SqlType(int2)
    * @param version        Database column version SqlType(varchar), Length(16,true)
    * @param ip             Database column ip SqlType(int8)
    * @param deviceType     Database column device_type SqlType(int4)
    * @param fingerPrint    Database column finger_print SqlType(varchar), Length(64,true)
    * @param status         Database column status SqlType(int2)
    * @param memberId       Database column member_id SqlType(int8)
    * @param identity       Database column identity SqlType(varchar), Length(11,true)
    * @param identityTicket Database column identity_ticket SqlType(varchar), Length(32,true)
    * @param gmtCreate      Database column gmt_create SqlType(timestamp)
    * @param gmtUpdate      Database column gmt_update SqlType(timestamp) */
  case class TmSessionRow(token: String, clientId: Short, version: String, ip: Long, deviceType: Int, fingerPrint: String, status: Short, memberId: Long, identity: String, identityTicket: String, gmtCreate: java.sql.Timestamp, gmtUpdate: java.sql.Timestamp)

  /** GetResult implicit for fetching TmSessionRow objects using plain SQL queries */
  implicit def GetResultTmSessionRow(implicit e0: GR[String], e1: GR[Short], e2: GR[Long], e3: GR[Int], e4: GR[java.sql.Timestamp]): GR[TmSessionRow] = GR {
    prs => import prs._
      TmSessionRow.tupled((<<[String], <<[Short], <<[String], <<[Long], <<[Int], <<[String], <<[Short], <<[Long], <<[String], <<[String], <<[java.sql.Timestamp], <<[java.sql.Timestamp]))
  }

  /** Table description of table tm_session. Objects of this class serve as prototypes for rows in queries. */
  class TmSession(_tableTag: Tag) extends profile.api.Table[TmSessionRow](_tableTag, "tm_session") {
    def * = (token, clientId, version, ip, deviceType, fingerPrint, status, memberId, identity, identityTicket, gmtCreate, gmtUpdate) <> (TmSessionRow.tupled, TmSessionRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(token), Rep.Some(clientId), Rep.Some(version), Rep.Some(ip), Rep.Some(deviceType), Rep.Some(fingerPrint), Rep.Some(status), Rep.Some(memberId), Rep.Some(identity), Rep.Some(identityTicket), Rep.Some(gmtCreate), Rep.Some(gmtUpdate)).shaped.<>({ r => import r._; _1.map(_ => TmSessionRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column token SqlType(varchar), PrimaryKey, Length(32,true) */
    val token: Rep[String] = column[String]("token", O.PrimaryKey, O.Length(32, varying = true))
    /** Database column client_id SqlType(int2) */
    val clientId: Rep[Short] = column[Short]("client_id")
    /** Database column version SqlType(varchar), Length(16,true) */
    val version: Rep[String] = column[String]("version", O.Length(16, varying = true))
    /** Database column ip SqlType(int8) */
    val ip: Rep[Long] = column[Long]("ip")
    /** Database column device_type SqlType(int4) */
    val deviceType: Rep[Int] = column[Int]("device_type")
    /** Database column finger_print SqlType(varchar), Length(64,true) */
    val fingerPrint: Rep[String] = column[String]("finger_print", O.Length(64, varying = true))
    /** Database column status SqlType(int2) */
    val status: Rep[Short] = column[Short]("status")
    /** Database column member_id SqlType(int8) */
    val memberId: Rep[Long] = column[Long]("member_id")
    /** Database column identity SqlType(varchar), Length(11,true) */
    val identity: Rep[String] = column[String]("identity", O.Length(11, varying = true))
    /** Database column identity_ticket SqlType(varchar), Length(32,true) */
    val identityTicket: Rep[String] = column[String]("identity_ticket", O.Length(32, varying = true))
    /** Database column gmt_create SqlType(timestamp) */
    val gmtCreate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("gmt_create")
    /** Database column gmt_update SqlType(timestamp) */
    val gmtUpdate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("gmt_update")
  }

  /** Collection-like TableQuery object for table TmSession */
  lazy val TmSession = new TableQuery(tag => new TmSession(tag))
}
