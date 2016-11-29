package com.lawsofnature.sso.service

import java.sql.Timestamp
import javax.inject.Inject

import RpcMember.{BaseResponse, MemberResponse}
import RpcSSO.{LoginRequest, SessionResponse}
import com.lawsofnature.common.exception.ErrorCode
import com.lawsofnature.common.helper.{IPv4Helper, TokenHelper}
import com.lawsofnature.common.redis.RedisClientTemplate
import com.lawsofnature.member.client.MemberClientService
import com.lawsofnature.sso.domain.SessionCache
import com.lawsofnature.sso.repo.{SessionRepository, TmSessionRow}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by fangzhongwei on 2016/11/23.
  */
trait SessionService {
  def login(traceId: String, loginRequest: LoginRequest): SessionResponse

  def touch(traceId: String, token: String): SessionResponse

  implicit def sessionCache2Response(sessionCache: SessionCache): SessionResponse = {
    new SessionResponse(true, 0, sessionCache.token, sessionCache.salt, sessionCache.clientId.toInt, sessionCache.memberId, sessionCache.status, sessionCache.gmtCreate, sessionCache.lastAccessTime)
  }
}

class SessionServiceImpl @Inject()(sessionRepository: SessionRepository, redisClientTemplate: RedisClientTemplate, memberClientService: MemberClientService) extends SessionService {
  implicit val timeout = (90 seconds)
  val sessionExpireSeconds: Int = 8 * 60 * 60
  val repelledTokenExpireSeconds: Int = 60 * 60
  val TOKEN_SESSION_PRE = "sso:tk-s:"
  val TOKEN_SALT_PRE = "sso:tk-slt:"
  val MEMBER_ID_TOKEN_PRE = "sso:mi-tk:"
  val REPELLED_TOKEN_PRE = "sso:rptk:"

  override def login(traceId: String, request: LoginRequest): SessionResponse = {
    val memberResponse: MemberResponse = memberClientService.getMemberByIdentity(traceId, request.identity)
    memberResponse.success match {
      case false =>
        SessionResponse.makeErrorResponse(ErrorCode.EC_UC_MEMBER_INVALID_USERNAME_OR_PWD.getCode)
      case true =>
        memberResponse.status match {
          case 1 =>
            val checkPasswordResponse: BaseResponse = memberClientService.checkPassword(traceId, memberResponse.memberId, request.pwd)
            checkPasswordResponse.success match {
              case true =>
                val token: String = generateToken
                val tmSessionRow: TmSessionRow = TmSessionRow(token, TokenHelper.generate8HexToken, 0, request.clientId.toByte, memberResponse.memberId, request.identity, identityType(memberResponse, request.identity), IPv4Helper.ipToLong(request.ip), request.deviceType.toByte, request.deviceIdentity, request.lat, request.lng,
                  Some(request.country), Some(request.province), Some(request.city), Some(request.county), Some(request.address), new Timestamp(System.currentTimeMillis()))
                saveCache(token, new SessionCache(tmSessionRow.token, tmSessionRow.salt, tmSessionRow.clientId.toInt, tmSessionRow.memberId, tmSessionRow.status, tmSessionRow.gmtCreate.getTime, tmSessionRow.gmtCreate.getTime))
                sessionRepository.createSession(tmSessionRow)
                new SessionResponse(true, 0, tmSessionRow.token, tmSessionRow.salt, tmSessionRow.clientId.toInt, tmSessionRow.memberId, tmSessionRow.status, tmSessionRow.gmtCreate.getTime, tmSessionRow.gmtCreate.getTime)
              case false =>
                SessionResponse.makeErrorResponse(ErrorCode.EC_UC_MEMBER_INVALID_USERNAME_OR_PWD.getCode)
            }
          case _ =>
            SessionResponse.makeErrorResponse(ErrorCode.EC_UC_MEMBER_ACCOUNT_FREEZE.getCode)
        }
    }
  }

  def saveCache(token: String, sessionCache: SessionCache) = {
    val memberIdKey: String = generateMemberIdTokenCacheKey(sessionCache.memberId, sessionCache.clientId)
    redisClientTemplate.getString(memberIdKey) onSuccess {
      case Some(existingToken) => saveSession2Cache(token, sessionCache, Some(existingToken))
      case None => saveSession2Cache(token, sessionCache, None)
    }
  }

  def saveSession2Cache(token: String, sessionCache: SessionCache, existedToken: Option[String]) = {
    existedToken match {
      case Some(tk) => redisClientTemplate.setString(generateRepelledTokenCacheKey(tk), "1", repelledTokenExpireSeconds)
    }
    redisClientTemplate.setString(generateMemberIdTokenCacheKey(sessionCache.memberId, sessionCache.clientId), token, sessionExpireSeconds)
    redisClientTemplate.set(generateTokenSessionCacheKey(token), sessionCache, sessionExpireSeconds)
  }

  def generateTokenSessionCacheKey(token: String): String = {
    new StringBuilder(TOKEN_SESSION_PRE).append(token).toString()
  }

  def generateTokenSaltCacheKey(token: String): String = {
    new StringBuilder(TOKEN_SALT_PRE).append(token).toString()
  }

  def generateMemberIdTokenCacheKey(memberId: Long, clientId: Int): String = {
    new StringBuilder(MEMBER_ID_TOKEN_PRE).append(memberId.toString).append('-').append(clientId).toString()
  }

  def generateRepelledTokenCacheKey(token: String): String = {
    new StringBuilder(REPELLED_TOKEN_PRE).append(token).toString()
  }

  def identityType(memberResponse: MemberResponse, identity: String): Byte = {
    memberResponse.identityList.filter(_.identity == identity).head.pid.toByte
  }

  def generateToken: String = {
    val ids: Seq[Long] = Await.result(sessionRepository.getNextSessionId(), timeout)
    val sessionId: Long = ids(0)
    new StringBuilder(TokenHelper.generate8HexToken).append(sessionId.toString).toString()
  }

  override def touch(traceId: String, token: String): SessionResponse = {
    val sessionCacheKey: String = generateTokenSessionCacheKey(token)
    Await.result(redisClientTemplate.get[SessionCache](sessionCacheKey, classOf[SessionCache]), timeout) match {
      case Some(sessionCache) =>
        sessionCache.lastAccessTime = System.currentTimeMillis()
        redisClientTemplate.set(sessionCacheKey, sessionCacheKey, sessionExpireSeconds)
        sessionCache
      case None =>
        Await.result(redisClientTemplate.getString(generateRepelledTokenCacheKey(token)), timeout) match {
          case Some(v) => SessionResponse.makeErrorResponse(ErrorCode.EC_SSO_SESSION_REPELLED.getCode)
          case None => SessionResponse.makeErrorResponse(ErrorCode.EC_SSO_SESSION_EXPIRED.getCode)
        }
    }
  }
}