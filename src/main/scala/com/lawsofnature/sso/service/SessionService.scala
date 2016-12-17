package com.lawsofnature.sso.service

import java.sql.Timestamp
import javax.inject.Inject

import RpcMember.{BaseResponse, MemberResponse}
import RpcSSO.{LoginRequest, SSOBaseResponse, SessionResponse}
import com.lawsofnature.common.exception.ErrorCode
import com.lawsofnature.common.helper.{IPv4Helper, RegHelper, TokenHelper}
import com.lawsofnature.common.redis.RedisClientTemplate
import com.lawsofnature.member.client.MemberClientService
import com.lawsofnature.sso.domain.SessionCache
import com.lawsofnature.sso.repo.{SessionRepository, TmSessionRow}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}

/**
  * Created by fangzhongwei on 2016/11/23.
  */
trait SessionService {
  def logout(traceId: String, token: String): SSOBaseResponse

  def login(traceId: String, loginRequest: LoginRequest): SessionResponse

  def touch(traceId: String, token: String): SessionResponse

  implicit def sessionCache2Response(sessionCache: SessionCache): SessionResponse = {
    new SessionResponse(true, 0, sessionCache.token, sessionCache.salt, sessionCache.clientId.toInt, sessionCache.memberId, sessionCache.status, sessionCache.gmtCreate, sessionCache.lastAccessTime)
  }
}

class SessionServiceImpl @Inject()(sessionRepository: SessionRepository, redisClientTemplate: RedisClientTemplate, memberClientService: MemberClientService) extends SessionService {
  val logger: Logger = LoggerFactory.getLogger(getClass)
  implicit val timeout = (90 seconds)
  val sessionExpireSeconds: Int = 90 * 24 * 60 * 60
  val repelledTokenExpireSeconds: Int = 60 * 60
  val TOKEN_SESSION_PRE = "sso:tk-s:"
  val TOKEN_SALT_PRE = "sso:tk-slt:"
  val MEMBER_ID_TOKEN_PRE = "sso:mi-tk:"
  val REPELLED_TOKEN_PRE = "sso:rptk:"

  var ERROR_RESPONSE_MAP: scala.collection.mutable.Map[ErrorCode, SessionResponse] = scala.collection.mutable.Map[ErrorCode, SessionResponse]()

  /**
    * find by identity first, if not found, find by username
    *
    * @param traceId
    * @param request
    * @return
    */
  override def login(traceId: String, request: LoginRequest): SessionResponse = {
    var memberResponse: MemberResponse = memberClientService.getMemberByIdentity(traceId, request.identity)
    memberResponse.success match {
      case false => memberResponse.code match {
        //ErrorCode.EC_UC_MEMBER_NOT_EXISTS.getCode
        case 10001010 =>
          memberResponse = memberClientService.getMemberByUsername(traceId, request.identity)
          memberResponse.success match {
            case false =>
              memberResponse.code match {
                case 10001010 => errorResponse(ErrorCode.EC_UC_MEMBER_INVALID_USERNAME_OR_PWD)
                case _ => errorResponse(ErrorCode.get(memberResponse.code))
              }
            case true => checkAndResponse(traceId, request, memberResponse)
          }
        case _ => errorResponse(ErrorCode.get(memberResponse.code))
      }
      case true => checkAndResponse(traceId, request, memberResponse)
    }
  }

  def checkAndResponse(traceId: String, request: LoginRequest, memberResponse: MemberResponse): SessionResponse = {
    memberResponse.status match {
      case 1 =>
        val checkPasswordResponse: BaseResponse = memberClientService.checkPassword(traceId, memberResponse.memberId, request.pwd)
        checkPasswordResponse.success match {
          case true =>
            val token: String = generateToken
            val tmSessionRow: TmSessionRow = TmSessionRow(token, TokenHelper.generate8HexToken, 0, request.clientId.toByte, memberResponse.memberId, request.identity, identityType(request.identity), IPv4Helper.ipToLong(request.ip), request.deviceType.toByte, request.deviceIdentity, request.lat, request.lng,
              Some(request.country), Some(request.province), Some(request.city), Some(request.county), Some(request.address), new Timestamp(System.currentTimeMillis()))
            saveCache(token, new SessionCache(tmSessionRow.token, tmSessionRow.salt, tmSessionRow.clientId.toInt, tmSessionRow.memberId, tmSessionRow.status, tmSessionRow.gmtCreate.getTime, tmSessionRow.gmtCreate.getTime))
            sessionRepository.createSession(tmSessionRow)
            new SessionResponse(true, 0, tmSessionRow.token, tmSessionRow.salt, tmSessionRow.clientId.toInt, tmSessionRow.memberId, tmSessionRow.status, tmSessionRow.gmtCreate.getTime, tmSessionRow.gmtCreate.getTime)
          case false =>
            logger.info("invalid username or password.")
            errorResponse(ErrorCode.EC_UC_MEMBER_INVALID_USERNAME_OR_PWD)
        }
      case _ =>
        errorResponse(ErrorCode.EC_UC_MEMBER_ACCOUNT_FREEZE)
    }
  }

  def errorResponse(errorCode: ErrorCode): SessionResponse = {
    ERROR_RESPONSE_MAP.get(errorCode) match {
      case Some(sessionResponse) => sessionResponse
      case None =>
        val response: SessionResponse = new SessionResponse()
        response.success = false
        response.code = errorCode.getCode
        ERROR_RESPONSE_MAP += (errorCode -> response)
        response
    }
  }

  def saveCache(token: String, sessionCache: SessionCache) = {
    val memberIdKey: String = generateMemberIdTokenCacheKey(sessionCache.memberId, sessionCache.clientId)
    redisClientTemplate.getString(memberIdKey) match {
      case Some(existingToken) => saveSession2Cache(token, sessionCache, Some(existingToken))
      case None => saveSession2Cache(token, sessionCache, None)
    }
  }

  def saveSession2Cache(token: String, sessionCache: SessionCache, existedToken: Option[String]) = {
    existedToken match {
      case Some(tk) => redisClientTemplate.setString(generateRepelledTokenCacheKey(tk), "1", repelledTokenExpireSeconds)
      case None =>
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

  def identityType(identity: String): Byte = {
    if (RegHelper.isMobile(identity)) 1.toByte
    else if (RegHelper.isMobile(identity)) 2.toByte
    else 0.toByte
  }

  def generateToken: String = {
    val ids: Seq[Long] = Await.result(sessionRepository.getNextSessionId(), timeout)
    val sessionId: Long = ids(0)
    new StringBuilder(TokenHelper.generate8HexToken).append(sessionId.toString).toString()
  }

  def doLogout(traceId: String, token: String): Future[Unit] = {
    val promise: Promise[Unit] = Promise[Unit]()
    Future {
      val tokenSessionCacheKey: String = generateTokenSessionCacheKey(token)
      redisClientTemplate.get[SessionCache](tokenSessionCacheKey, classOf[SessionCache]) match {
        case Some(sessionCache) =>
          try {
            redisClientTemplate.delete(tokenSessionCacheKey)
            redisClientTemplate.delete(generateMemberIdTokenCacheKey(sessionCache.memberId, sessionCache.clientId))
            sessionRepository.updateSession(token, 99.toByte)
            promise.success()
          } catch {
            case ex: Exception => logger.error(traceId, ex)
          }
        case None => promise.success()
      }
    }
    promise.future
  }

  override def logout(traceId: String, token: String): SSOBaseResponse = {
    doLogout(traceId, token)
    new SSOBaseResponse(true, 0)
  }

  override def touch(traceId: String, token: String): SessionResponse = {
    val sessionCacheKey: String = generateTokenSessionCacheKey(token)
    redisClientTemplate.get[SessionCache](sessionCacheKey, classOf[SessionCache]) match {
      case Some(sessionCache) => sessionCache
      case None =>
        redisClientTemplate.getString(generateRepelledTokenCacheKey(token)) match {
          case Some(v) => errorResponse(ErrorCode.EC_SSO_SESSION_REPELLED)
          case None => errorResponse(ErrorCode.EC_SSO_SESSION_EXPIRED)
        }
    }
  }
}