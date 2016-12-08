package com.lawsofnature.sso.repo

import com.lawsofnature.connection.MySQLDBImpl

/** Entity class storing rows of table TmSession
  *  @param token Database column token SqlType(VARCHAR), PrimaryKey, Length(32,true)
  *  @param salt Database column salt SqlType(VARCHAR), Length(8,true)
  *  @param status Database column status SqlType(TINYINT), Default(0)
  *  @param clientId Database column client_id SqlType(TINYINT)
  *  @param memberId Database column member_id SqlType(BIGINT)
  *  @param identity Database column identity SqlType(VARCHAR), Length(64,true)
  *  @param identityType Database column identity_type SqlType(TINYINT)
  *  @param ip Database column ip SqlType(BIGINT)
  *  @param deviceType Database column device_type SqlType(TINYINT)
  *  @param deviceIdenitty Database column device_idenitty SqlType(VARCHAR), Length(128,true)
  *  @param lat Database column lat SqlType(VARCHAR), Length(32,true)
  *  @param lng Database column lng SqlType(VARCHAR), Length(32,true)
  *  @param country Database column country SqlType(VARCHAR), Length(16,true), Default(Some())
  *  @param province Database column province SqlType(VARCHAR), Length(16,true), Default(Some())
  *  @param city Database column city SqlType(VARCHAR), Length(16,true), Default(Some())
  *  @param county Database column county SqlType(VARCHAR), Length(16,true), Default(Some())
  *  @param address Database column address SqlType(VARCHAR), Length(256,true), Default(Some())
  *  @param gmtCreate Database column gmt_create SqlType(TIMESTAMP)
  *  @param gmtUpdate Database column gmt_update SqlType(TIMESTAMP), Default(None) */
case class TmSessionRow(token: String, salt: String, status: Byte = 0, clientId: Byte, memberId: Long, identity: String, identityType: Byte, ip: Long, deviceType: Byte, deviceIdenitty: String, lat: String, lng: String, country: Option[String] = Some(""), province: Option[String] = Some(""), city: Option[String] = Some(""), county: Option[String] = Some(""), address: Option[String] = Some(""), gmtCreate: java.sql.Timestamp, gmtUpdate: Option[java.sql.Timestamp] = None)

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables extends MySQLDBImpl{
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = TmSession.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** GetResult implicit for fetching TmSessionRow objects using plain SQL queries */
  implicit def GetResultTmSessionRow(implicit e0: GR[String], e1: GR[Byte], e2: GR[Long], e3: GR[Option[String]], e4: GR[java.sql.Timestamp], e5: GR[Option[java.sql.Timestamp]]): GR[TmSessionRow] = GR{
    prs => import prs._
      TmSessionRow.tupled((<<[String], <<[String], <<[Byte], <<[Byte], <<[Long], <<[String], <<[Byte], <<[Long], <<[Byte], <<[String], <<[String], <<[String], <<?[String], <<?[String], <<?[String], <<?[String], <<?[String], <<[java.sql.Timestamp], <<?[java.sql.Timestamp]))
  }
  /** Table description of table tm_session. Objects of this class serve as prototypes for rows in queries. */
  class TmSession(_tableTag: Tag) extends profile.api.Table[TmSessionRow](_tableTag, "tm_session") {
    def * = (token, salt, status, clientId, memberId, identity, identityType, ip, deviceType, deviceIdenitty, lat, lng, country, province, city, county, address, gmtCreate, gmtUpdate) <> (TmSessionRow.tupled, TmSessionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(token), Rep.Some(salt), Rep.Some(status), Rep.Some(clientId), Rep.Some(memberId), Rep.Some(identity), Rep.Some(identityType), Rep.Some(ip), Rep.Some(deviceType), Rep.Some(deviceIdenitty), Rep.Some(lat), Rep.Some(lng), country, province, city, county, address, Rep.Some(gmtCreate), gmtUpdate).shaped.<>({r=>import r._; _1.map(_=> TmSessionRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get, _13, _14, _15, _16, _17, _18.get, _19)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column token SqlType(VARCHAR), PrimaryKey, Length(32,true) */
    val token: Rep[String] = column[String]("token", O.PrimaryKey, O.Length(32,varying=true))
    /** Database column salt SqlType(VARCHAR), Length(8,true) */
    val salt: Rep[String] = column[String]("salt", O.Length(8,varying=true))
    /** Database column status SqlType(TINYINT), Default(0) */
    val status: Rep[Byte] = column[Byte]("status", O.Default(0))
    /** Database column client_id SqlType(TINYINT) */
    val clientId: Rep[Byte] = column[Byte]("client_id")
    /** Database column member_id SqlType(BIGINT) */
    val memberId: Rep[Long] = column[Long]("member_id")
    /** Database column identity SqlType(VARCHAR), Length(64,true) */
    val identity: Rep[String] = column[String]("identity", O.Length(64,varying=true))
    /** Database column identity_type SqlType(TINYINT) */
    val identityType: Rep[Byte] = column[Byte]("identity_type")
    /** Database column ip SqlType(BIGINT) */
    val ip: Rep[Long] = column[Long]("ip")
    /** Database column device_type SqlType(TINYINT) */
    val deviceType: Rep[Byte] = column[Byte]("device_type")
    /** Database column device_idenitty SqlType(VARCHAR), Length(128,true) */
    val deviceIdenitty: Rep[String] = column[String]("device_idenitty", O.Length(128,varying=true))
    /** Database column lat SqlType(VARCHAR), Length(32,true) */
    val lat: Rep[String] = column[String]("lat", O.Length(32,varying=true))
    /** Database column lng SqlType(VARCHAR), Length(32,true) */
    val lng: Rep[String] = column[String]("lng", O.Length(32,varying=true))
    /** Database column country SqlType(VARCHAR), Length(16,true), Default(Some()) */
    val country: Rep[Option[String]] = column[Option[String]]("country", O.Length(16,varying=true), O.Default(Some("")))
    /** Database column province SqlType(VARCHAR), Length(16,true), Default(Some()) */
    val province: Rep[Option[String]] = column[Option[String]]("province", O.Length(16,varying=true), O.Default(Some("")))
    /** Database column city SqlType(VARCHAR), Length(16,true), Default(Some()) */
    val city: Rep[Option[String]] = column[Option[String]]("city", O.Length(16,varying=true), O.Default(Some("")))
    /** Database column county SqlType(VARCHAR), Length(16,true), Default(Some()) */
    val county: Rep[Option[String]] = column[Option[String]]("county", O.Length(16,varying=true), O.Default(Some("")))
    /** Database column address SqlType(VARCHAR), Length(256,true), Default(Some()) */
    val address: Rep[Option[String]] = column[Option[String]]("address", O.Length(256,varying=true), O.Default(Some("")))
    /** Database column gmt_create SqlType(TIMESTAMP) */
    val gmtCreate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("gmt_create")
    /** Database column gmt_update SqlType(TIMESTAMP), Default(None) */
    val gmtUpdate: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("gmt_update", O.Default(None))
  }
  /** Collection-like TableQuery object for table TmSession */
  lazy val TmSession = new TableQuery(tag => new TmSession(tag))
}
